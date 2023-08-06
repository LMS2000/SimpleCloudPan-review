package com.lms.cloudpan.security.config;

import com.lms.cloudpan.utils.ResponseUtil;
import com.lms.contants.HttpCode;
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

    //认证未通过时的返回
    @Override
    protected void authenticationFailure(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        ResponseUtil.renderString(response, HttpCode.NO_AUTH_ERROR,false);
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
