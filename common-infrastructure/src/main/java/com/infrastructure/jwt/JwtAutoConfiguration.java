package com.infrastructure.jwt;



import com.lms.redis.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
@ConditionalOnProperty(prefix = "jwt", name = "open", havingValue = "true")
public class JwtAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public Jwt jwt(JwtProperties jwtProperties){
       return new JwtImpl(jwtProperties);
    }


}
