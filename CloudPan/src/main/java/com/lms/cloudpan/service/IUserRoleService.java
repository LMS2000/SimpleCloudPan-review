package com.lms.cloudpan.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.RoleDto;

import java.util.List;

public interface IUserRoleService extends IService<UserRole> {

    Boolean delByUserId(Integer id);

    Boolean removeByRoleId(Integer id);

    List<RoleDto> getRoleOfUser(Integer userId);

    Boolean releaseRoleToUser(Integer userId, List<Integer> roleList);

    Boolean revokeRoleFromUser(Integer userId, List<Integer> roleList);
}
