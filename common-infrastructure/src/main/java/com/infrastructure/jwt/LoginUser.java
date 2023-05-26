package com.infrastructure.jwt;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
@Data
public class LoginUser  implements UserDetails {

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
    private JwtUser user;


    public LoginUser(){}

    public LoginUser(Integer uid, JwtUser user, Set<String> userPermission){
        this.userId=uid;
        this.user=user;
        this.permissions=userPermission;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getEnable()==0;
    }
}
