package com.lms.cloudpan.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.Role;

import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.AddUserVo;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.page.CustomPage;
import io.swagger.models.auth.In;

import java.util.List;

public interface IRoleService extends IService<Role> {
    Boolean saveRole(RoleDto roleDto);

    RoleVo getRoleById(Integer id);

    List<RoleVo> listRole(CustomPage customPage);

    Boolean delRoleById(Integer id);

    Boolean enableRole(Integer id);

    Boolean disableRole(Integer id);


    Page<RoleVo> pageRole(QueryRolePageDto rolePageDto);

    /**
     * 修改角色
     * @param updateRoleDto
     * @return
     */
    Boolean updateRole(UpdateRoleDto updateRoleDto);

    /**
     * 添加角色
     * @param addRoleDto
     * @return
     */
    Boolean addRole(AddRoleDto addRoleDto);

    /**
     * 批量删除角色
     * @param rids
     * @return
     */
    Boolean removeRoles(List<Integer> rids);



}
