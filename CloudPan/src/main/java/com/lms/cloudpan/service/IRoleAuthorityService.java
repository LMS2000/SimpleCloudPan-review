package com.lms.cloudpan.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.RoleAuthority;
import com.lms.cloudpan.entity.dto.AuthorityDto;


import java.util.List;

public interface IRoleAuthorityService extends IService<RoleAuthority> {

    Boolean removeByRoleId(Integer id);

    Boolean removeByAuthorityId(Integer id);

    Boolean releaseAuthorityToRole(Integer roleId, List<Integer> authorityList);

    Boolean revokeAuthorityFromRole(Integer roleId, List<Integer> authorityList);

    List<AuthorityDto> getAuthorityOfRole(Integer roleId);
}
