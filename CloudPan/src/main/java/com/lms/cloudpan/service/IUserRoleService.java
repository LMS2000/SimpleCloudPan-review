package com.lms.cloudpan.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.AllocateUserDto;
import com.lms.cloudpan.entity.vo.*;

import java.util.List;

public interface IUserRoleService extends IService<UserRole> {

    Boolean delByUserId(Integer id);

    Boolean removeByRoleId(Integer id);

    List<RoleVo> getRoleOfUser(Integer userId);

    Boolean releaseRoleToUser(Integer userId, List<Integer> roleList);

    Boolean revokeRoleFromUser(Integer userId, List<Integer> roleList);


     UpdateUserVo getUpdateUserInfo(Integer uid);

    Boolean isManager(Integer uid);

    AddUserVo getInitInfo();

    Boolean setRolesForUser(Integer userId,List<Integer> rids);


    SetRoleVo getUserAndRoles(Integer uid);


    Boolean releaseRoleToUser(List<Integer> uid,Integer rid);

    Boolean grantRoleToUser(List<Integer> uid,Integer rid);

    /**
     * 获取授权用户分页
     * @param allocateUserDto
     * @return
     */
    Page<UserVo> getAllocateUsers(AllocateUserDto allocateUserDto);

    /**
     * 获取授权用户分页
     * @param allocateUserDto
     * @return
     */
    Page<UserVo> getUnAllocateUsers(AllocateUserDto allocateUserDto);

}
