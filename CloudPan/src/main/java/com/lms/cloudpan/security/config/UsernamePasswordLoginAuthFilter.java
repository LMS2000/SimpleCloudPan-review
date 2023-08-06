package com.lms.cloudpan.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.cloudpan.constants.UserConstants;
import com.lms.cloudpan.entity.dto.UserLoginInfoDto;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.contants.HttpCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class UsernamePasswordLoginAuthFilter extends TokenLoginAuthFilter{

    private final String USERNAME_PARAM="username";
    private final String PASSWORD_PARAM="password";



    //获取全局的authenticationManager
    public UsernamePasswordLoginAuthFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }


    @Override
    protected Authentication getAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) {

        String username ;
        String password ;
        // 读取请求体的内容
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper objectMapper = new ObjectMapper();
            UserLoginInfoDto userLoginInfoDto=objectMapper.readValue(requestBody, UserLoginInfoDto.class);
            username=userLoginInfoDto.getUsername();
            password=userLoginInfoDto.getPassword();
            String code = userLoginInfoDto.getCode();
            String realCode = (String)request.getSession().getAttribute(UserConstants.CHECK_CODE_KEY);
            if(StringUtils.isBlank(code)||StringUtils.isBlank(realCode)|| !realCode.equals(realCode)){
               throw new BusinessException(HttpCode.PARAMS_ERROR,"验证码不正确");
            }
            BusinessException.throwIfNot(realCode.equals(code),HttpCode.PARAMS_ERROR,"验证码不正确");
            request.getSession().removeAttribute(UserConstants.CHECK_CODE_KEY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        username = (username != null) ? username : "";
        username = username.trim();
        password = (password != null) ? password : "";
        return new UsernamePasswordAuthenticationToken(username, password);
    }
}
