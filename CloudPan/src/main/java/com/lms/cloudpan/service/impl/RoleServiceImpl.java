package com.lms.cloudpan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.mapper.RoleMapper;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.page.CustomPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.lms.cloudpan.entity.factory.RoleFactory.ROLE_CONVERTER;


@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private IUserRoleService userRoleService;
    @Resource
    private IRoleAuthorityService roleAuthorityService;
    @Override
    public Boolean saveRole(RoleVo roleVo) {
        Role role = ROLE_CONVERTER.toRole(roleVo);
        return save(role);

    }

    @Override
    public RoleDto getRoleById(Integer id) {
        return ROLE_CONVERTER.toRoleDto(getById(id));
    }

    @Override
    public List<RoleDto> listRole(CustomPage customPage) {
        List<Role> result = CustomPage.getPageResult(customPage, new Role(), this, null);
        return ROLE_CONVERTER.toListRoleDto(result);
    }

    @Override
    @Transactional
    public Boolean delRoleById(Integer id) {
        removeById(id);
        userRoleService.removeByRoleId(id);
        roleAuthorityService.removeByRoleId(id);
        return true;
    }

    @Override
    public Boolean enableRole(Integer id) {
     return    updateById(Role.builder().enabled(1).rid(id).build());
    }

    @Override
    public Boolean disableRole(Integer id) {
        return   updateById(Role.builder().enabled(0).rid(id).build());
    }
}
