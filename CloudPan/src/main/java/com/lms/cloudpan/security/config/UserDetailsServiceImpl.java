package com.lms.cloudpan.security.config;

import cn.hutool.core.bean.BeanUtil;
import com.infrastructure.jwt.JwtUser;
import com.infrastructure.jwt.LoginUser;
import com.lms.cloudpan.constants.UserConstants;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.contants.HttpCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        else if (UserConstants.DESIABLE.equals(user.getEnable()))
        {
            log.info("登录用户：{} 已被停用.", userAccount);
            throw new BusinessException(HttpCode.PARAMS_ERROR,"对不起，您的账号：" + userAccount + " 已停用");
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

        List<RoleDto> roleList = userRoleService.getRoleOfUser(user.getUserId());
        if(roleList==null)return null;
        Set<String> permissionSet =new HashSet<>();
        //根据uid获取所持有的全部角色然后再根据角色获取全部的权限，然后添加到authorities中
        roleList.stream().filter(roleDto -> roleDto.getEnabled()==0)
                .map(roleDto -> roleAuthorityService.getAuthorityOfRole(roleDto.getRid())
                        .stream().filter(authorityDto -> authorityDto.getEnabled()==0)
                        .map(AuthorityDto::getName)
                        .collect(Collectors.toList())
                ).forEach(permissionSet::addAll);
        return permissionSet;
    }
}
