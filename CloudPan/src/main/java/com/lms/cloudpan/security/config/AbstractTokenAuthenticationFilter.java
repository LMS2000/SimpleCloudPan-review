package com.lms.cloudpan.security.config;


import com.infrastructure.jwt.Jwt;
import com.infrastructure.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 拦截除了登录之外的需要权限的请求
 * OncePerRequestFilter是在一次外部请求中只过滤一次。对于服务器内部之间的forward等请求，不会再次执行过滤方法。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractTokenAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 无需token校验的请求集合
     */
    protected final Set<String> ignore_request_patterns =Set.of("/css/**", "/fonts/**", "/images/**", "/js/**", "/webjars/**", "/doc.html#/**", "/swagger-resources", "/v3/**", "/doc.html", "/swagger-ui.html","/authentication/**","/user/register","/static/**");
    /**
     * ant分隔路径匹配器
     */
    protected final AntPathMatcher antPathMatcher=new AntPathMatcher();
    /**
     * 请求路径处理器
     */
    protected final UrlPathHelper urlPathHelper=new UrlPathHelper();
    /**
     * jwt
     */
    @Resource
    protected Jwt jwt;
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
         if(!requiresAuthentication(httpServletRequest,httpServletResponse)){
             //不需要向认证中心发送请求获取当前资源关联的权限
             filterChain.doFilter(httpServletRequest,httpServletResponse);
             return;
         }

         if(!attemptAuthentication(httpServletRequest,httpServletResponse)){
             //认证失败不放行
               authenticationFailure(httpServletRequest,httpServletResponse,filterChain);
               SecurityContextHolder.clearContext();
               return;
         }
         beforeSuccessAuthInvoke(httpServletRequest,httpServletResponse);
         filterChain.doFilter(httpServletRequest,httpServletResponse);
         afterSuccessAuthInvoke(httpServletRequest,httpServletResponse);
         SecurityContextHolder.clearContext();
    }
    /**
     * 认证成功,当前请求还未被处理,进行前置处理
     * @param request
     * @param response
     */
    protected void beforeSuccessAuthInvoke(HttpServletRequest request, HttpServletResponse response){};

    /**
     * 认证成功,并且当前请求处理完后,进行后置处理
     * @param request
     * @param response
     */
    protected void afterSuccessAuthInvoke(HttpServletRequest request, HttpServletResponse response){};

    /**
     * 对当前用户状态进行检查,如果状态异常,返回false,否则返回true
     * @param userDetails
     * @return
     */
    protected abstract boolean userStatusCheck(UserDetails userDetails);

    /**
     * @param token 待验证令牌,并刷新令牌
     * @return 通过解析token来获取当前用户详细信息
     */
//    protected abstract UserDetails verifyToken(String token);

    /**
     * 认证失败,子类可以重写,决定是否采取何种措施,默认过滤器链继续往后执行,此时为匿名访问
     * @param request
     * @param response
     * @param chain
     */
    protected void authenticationFailure(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(request,response);
    }

    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String url = urlPathHelper.getPathWithinApplication(request);
        for (String ignoreUrl : ignore_request_patterns) {
            if(antPathMatcher.match(ignoreUrl,url)){
                return false;
            }
        }
        return true;
    }

    protected boolean attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        //从request中获取token字段然后去redis中查找，找不到返回null
        String token = request.getHeader(jwt.getTokenOfRequestHeaderName());

        LoginUser loginUser =(LoginUser)jwt.getLoginUser(token);
        if(loginUser==null || !userStatusCheck(loginUser)){
                return false;
        }
            authenticationSuccess(request,response,loginUser,token);
            return true;
    }

    /**
     * 认证成功处理
     * @param request
     * @param response
     * @param loginUser
     */
    protected void authenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser,String token) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=
                new UsernamePasswordAuthenticationToken(loginUser,null,loginUser.getAuthorities());
        //将认证信息放入security的上下文中
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        jwt.verifyToken(loginUser);
         //设置响应头,并且刷新令牌
        response.setHeader(jwt.getRefreshTokenOfResponseHeaderName(),token);
    }


}
