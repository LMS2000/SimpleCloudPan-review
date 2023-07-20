package com.lms.cloudpan.security.config;

import cn.hutool.core.bean.BeanUtil;
import com.infrastructure.jwt.JwtUser;
import com.infrastructure.jwt.LoginUser;
import com.lms.cloudpan.constants.UserConstants;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.contants.HttpCode;
import jdk.jfr.Enabled;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lms.cloudpan.constants.UserConstants.ENABLE;

/**
 * 用户验证处理
 *
 * @author ruoyi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService
{



    private final IUserService userService;

    private final IUserRoleService userRoleService;
    private final IRoleAuthorityService roleAuthorityService;
    @Override
    public UserDetails loadUserByUsername(String userAccount) throws UsernameNotFoundException
    {
        User user = userService.getUserByAccount(userAccount);
        if (user==null)
        {
            log.info("登录用户：{} 不存在.", userAccount);
            throw new BusinessException(HttpCode.PARAMS_ERROR,"登录用户：" + userAccount + " 不存在");
        }
        else if (UserConstants.DISABLE.equals(user.getEnable()))
        {
            log.info("登录用户：{} 已被停用.", userAccount);
            throw new BusinessException(HttpCode.PARAMS_ERROR,"对不起，您的账号：" + userAccount + " 已停用");
        }else if(UserConstants.DELETED.equals(user.getDeleteFlag())){
            log.info("登录用户：{} 已被删除.", userAccount);
            throw new BusinessException(HttpCode.PARAMS_ERROR,"对不起，您的账号：" + userAccount + " 已删除");

        }

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(User user)
    {
        JwtUser jwtUser=new JwtUser();
        BeanUtil.copyProperties(user,jwtUser);
        return new LoginUser(user.getUserId(), jwtUser,getUserPermission(user));
    }

    /**
     * 根据用户获取权限列表
     * @param user
     * @return
     */
    private Set<String> getUserPermission(User user){

        List<RoleVo> roleList = userRoleService.getRoleOfUser(user.getUserId());
        if(roleList==null)return null;
        Set<String> permissionSet =new HashSet<>();
        //超级管理员
        if(user.getUserId()==1){
            permissionSet.add("*:*:*");
            return permissionSet;
        }
        //根据uid获取所持有的全部角色然后再根据角色获取全部的权限，然后添加到authorities中
        roleList.stream().filter(roleDto -> Objects.equals(roleDto.getEnable(), ENABLE))
                .map(roleDto -> roleAuthorityService.getAuthorityOfRole(roleDto.getRid())
                        .stream().filter(authorityDto -> Objects.equals(authorityDto.getEnable(), ENABLE))
                        .map(AuthorityVo::getName)
                        .collect(Collectors.toList())
                ).forEach(permissionSet::addAll);
        return permissionSet;
    }
}
