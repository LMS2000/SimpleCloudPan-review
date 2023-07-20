package com.lms.cloudpan.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.RoleAuthority;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.entity.vo.GetRoleAuthorityVo;
import com.lms.cloudpan.mapper.RoleAuthorityMapper;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.utis.MybatisUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lms.cloudpan.entity.factory.AuthorityFactory.AUTHORITY_CONVERTER;


@Service
public class RoleAuthorityServiceImpl extends ServiceImpl<RoleAuthorityMapper, RoleAuthority> implements IRoleAuthorityService {

    @Resource
    private IAuthorityService authorityService;


    @Resource
    private IRoleService roleService;

    @Override
    public Boolean removeByRoleId(Integer id) {
     return    remove(new QueryWrapper<RoleAuthority>().eq("rid", id));
    }

    @Override
    public Boolean removeByAuthorityId(Integer id) {
      return   remove(new QueryWrapper<RoleAuthority>().eq("aid", id));
    }


    /**
     * 为指定的角色添加权限
     * @param roleId
     * @param authorityList
     */
    @Override
    @Transactional
    public Boolean releaseAuthorityToRole(Integer roleId, List<Integer> authorityList) {
        if (authorityList == null || authorityList.size() < 1) {
            return false;
        }
        for (Integer aid : authorityList) {
            if (!MybatisUtils.existCheck(this, Map.of("rid", roleId, "aid", aid))
                    && MybatisUtils.existCheck(authorityService, Map.of("id", aid))) {
                 save(RoleAuthority.builder().aid(aid).rid(roleId).build());
            }
        }
        return true;

    }


    /**
     * 根据角色和权限删除记录
     * @param roleId
     * @param authorityList
     */
    @Override
    @Transactional
    public Boolean revokeAuthorityFromRole(Integer roleId, List<Integer> authorityList) {
        if(authorityList==null||authorityList.size()<1){
            return false;
        }
        for (Integer id : authorityList) {
            remove(new QueryWrapper<RoleAuthority>().eq("aid",id).eq("rid",roleId));
        }
        return true;
    }

    /**
     * 根据角色查找权限列表
     * @param roleId
     * @return
     */
    @Override
    public List<AuthorityVo> getAuthorityOfRole(Integer roleId) {
        List<Integer> aidList =
                listByMap(Map.of("rid", roleId)).stream().map(RoleAuthority::getAid).collect(Collectors.toList());
        return ObjectUtils.isEmpty(aidList)?null:AUTHORITY_CONVERTER.toListAuthorityVo(authorityService.listByIds(aidList));

    }

    @Override
    public GetRoleAuthorityVo getAuthorityOfRoleTree(Integer rid) {
        //角色的权限ids
        List<Integer> aids = this.list(new QueryWrapper<RoleAuthority>().eq("rid", rid)).stream().map(RoleAuthority::getAid).collect(Collectors.toList());

        List<AuthorityVo> authorityTree = authorityService.getAuthorityTree();

        return GetRoleAuthorityVo.builder().authorityVos(authorityTree).checks(aids).build();
    }
}
