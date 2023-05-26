package com.lms.cloudpan.security.config;

import com.lms.cloudpan.utis.ResponseUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DaoTokenAuthenticationFilter extends AbstractTokenAuthenticationFilter{
 private final UserDetailsService userDetailsService;


    public DaoTokenAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void authenticationFailure(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        ResponseUtil.renderString(response,"认证失败,无权限");
    }

    @Override
    protected boolean userStatusCheck(UserDetails userDetails) {
        return userDetails.isEnabled();
    }

//    @Override
//    protected UserDetails verifyToken(String token) {
//        String username = jwt.verifyToken(token);
//        return userDetailsService.loadUserByUsername(username);
//    }
}
