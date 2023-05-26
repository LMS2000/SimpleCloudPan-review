package com.infrastructure.jwt;


/**
 * @author lms2000
 * @create 2022/9/23 20:48
 */
public interface Jwt {
    /**
     * 创建token令牌
     * @param loginUser
     * @return
     */
     String createToken(LoginUser loginUser);

    /**
     * 刷新token
     * @param loginUser
     */
     void refreshToken(LoginUser loginUser);

     String getTokenOfRequestHeaderName();

     String getRefreshTokenOfResponseHeaderName();
    LoginUser getLoginUser(String token);
     void verifyToken(LoginUser loginUser);
     boolean verifyToken(String token);
     String getToken(String token);

    void delLoginUser(String token);
}
