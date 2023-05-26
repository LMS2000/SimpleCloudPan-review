package com.lms.cloudpan.security.config;


import cn.hutool.extra.spring.SpringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 需要放行的swagger相关请求
     */
    private final List<String> SWAGGER_REQUEST_IGNORE = List.of("/css/**", "/fonts/**", "/images/**", "/js/**", "/webjars/**", "/doc.html#/**", "/swagger-resources", "/v3/**", "/doc.html", "/swagger-ui.html","/user/register","/static/**");

    /**
     * 放行静态资源
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers(SWAGGER_REQUEST_IGNORE.toArray(new String[]{}));
    }



    @Bean
    public ProviderManager providerManager(PasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return new ProviderManager(provider);
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UsernamePasswordLoginAuthFilter usernamePasswordLoginAuthFilter(AuthenticationManager authenticationManager){
        return new UsernamePasswordLoginAuthFilter(authenticationManager);
    }

    @Bean
    public DaoTokenAuthenticationFilter daoTokenAuthenticationFilter(UserDetailsService userDetailsService){
        return new DaoTokenAuthenticationFilter(userDetailsService);
    }

    //spring securiry 也需要设置跨域
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/pan/login", configuration); // 限定跨域资源的路径

        return source;
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //不需要管理员身份认证的请求--认证中心暴露给外界微服务调用的接口
        http.authorizeRequests().antMatchers("/authentication/**").permitAll()
                .anyRequest().authenticated();
//        //其他请求都是管理员访问
//        http.authorizeRequests().antMatchers(MASTER_LIST.toArray(new String[]{})).authenticated();
        //添加自定义的Token登录认证过滤器
        http.addFilterAfter(SpringUtil.getBean(UsernamePasswordLoginAuthFilter.class), LogoutFilter.class);
        //添加自定义的Token校验过滤器
        http.addFilterAfter(SpringUtil.getBean(DaoTokenAuthenticationFilter.class), LogoutFilter.class);
        http.cors(); //设置跨域
        disAbleDefaultConfiguration(http);
        disableSessionRespiratory(http);
    }


    private void disableSessionRespiratory(HttpSecurity http) throws Exception {
        //不用session去保存我的securitysession,不然第一次会话中用户带了token，而第二次没携带,仍然可以从上下文中获取到用户信息
        //下面配置本质是改变SecurityContextRepository实例化的子类,默认是HttpSessionSecurityContextRepository会负责保存上下文到session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    private void disAbleDefaultConfiguration(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.httpBasic().disable();
        http.formLogin().disable();
        http.removeConfigurer(LogoutConfigurer.class);
    }
}
