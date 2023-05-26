package com.infrastructure.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    /**
     * 秘钥
     */
    private String secret;
    /**
     * 令牌过期时间: 单位为分钟
     */
    private Integer expiration;
    /**
     * 令牌在请求头中的名字
     */
    private String tokenOfRequestHeaderName;
    /**
     * 刷新令牌在响应头中的名字
     */
    private String refreshTokenOfResponseHeaderName;
}
