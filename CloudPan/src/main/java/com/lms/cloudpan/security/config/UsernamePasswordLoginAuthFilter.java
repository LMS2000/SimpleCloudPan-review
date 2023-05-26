package com.lms.cloudpan.security.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UsernamePasswordLoginAuthFilter extends TokenLoginAuthFilter{

    private final String USERNAME_PARAM="username";
    private final String PASSWORD_PARAM="password";

    //获取全局的authenticationManager
    public UsernamePasswordLoginAuthFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }


    @Override
    protected Authentication getAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        String username = request.getParameter(USERNAME_PARAM);
        String password = request.getParameter(PASSWORD_PARAM);
        username = (username != null) ? username : "";
        username = username.trim();
        password = (password != null) ? password : "";
        return new UsernamePasswordAuthenticationToken(username, password);
    }
}
