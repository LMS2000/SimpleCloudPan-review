package com.lms.cloudpan.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.mapper.UserRoleMapper;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.utis.MybatisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lms.cloudpan.entity.factory.RoleFactory.ROLE_CONVERTER;


@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Resource
    private IRoleService roleService;


    /**
     * 根据uid删除
     * @param id
     */

    @Override
    public Boolean delByUserId(Integer id) {
      return   remove(new QueryWrapper<UserRole>().eq("uid",id));
    }

    @Override
    public Boolean removeByRoleId(Integer id) {
      return remove(new QueryWrapper<UserRole>().eq("rid",id));
    }

    /**
     * 根据uid获取角色列表
     * @param userId
     * @return
     */
    @Override
    public List<RoleDto> getRoleOfUser(Integer userId) {
        List<Integer> ridList = listByMap(Map.of("uid", userId)).stream().map(UserRole::getRid).collect(Collectors.toList());
        return ObjectUtils.isEmpty(ridList)?null:ROLE_CONVERTER.toListRoleDto(roleService.listByIds(ridList));
    }


    /**
     * 根据uid添加角色列表
     * @param userId
     * @param roleList
     */
    @Override
    @Transactional
    public Boolean releaseRoleToUser(Integer userId, List<Integer> roleList) {
        if(roleList==null||roleList.size()<1){
            return false;
        }
        for (Integer rid : roleList) {
            if(!MybatisUtils.existCheck(this,Map.of("uid",userId,"rid",rid))
            &&MybatisUtils.existCheck(roleService,Map.of("rid",rid))){
                save(UserRole.builder().rid(rid).uid(userId).build());
            }
        }
        return true;
    }

    /**
     * 根据uid删除角色列表
     * @param userId
     * @param roleList
     */
    @Override
    @Transactional
    public Boolean revokeRoleFromUser(Integer userId, List<Integer> roleList) {
        if(roleList==null||roleList.size()<1){
            return false;
        }
        for (Integer  rid : roleList) {
               remove(new QueryWrapper<UserRole>().eq("uid",userId).eq("rid",rid));
        }
        return true;
    }
}
