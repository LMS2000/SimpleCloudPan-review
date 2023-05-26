package com.lms.cloudpan.service;


import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.page.CustomPage;

import java.util.List;

public interface IRoleService extends IService<Role> {
    Boolean saveRole(RoleVo roleVo);

    RoleDto getRoleById(Integer id);

    List<RoleDto> listRole(CustomPage customPage);

    Boolean delRoleById(Integer id);

    Boolean enableRole(Integer id);

    Boolean disableRole(Integer id);
}
