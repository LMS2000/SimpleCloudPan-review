package com.infrastructure.jwt;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;
//登录信息记录类，后续的
@Data
public class AbstractLoginUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    protected Integer userId;



    /**
     * 用户唯一标识
     */
    protected String token;

    /**
     * 登录时间
     */
    protected Long loginTime;

    /**
     * 过期时间
     */
    protected Long expireTime;




    /**
     * 权限列表
     */
    protected Set<String> permissions;

//    /**
//     * 用户信息
//     */
//    protected JwtUser user;
//
//    public LoginUser(Integer uid, JwtUser user, Set<String> userPermission) {
//        this.userId=uid;
//        this.user=user;
//        this.permissions=userPermission;
//    }

    public AbstractLoginUser(){}



}

