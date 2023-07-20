package com.lms.cloudpan.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dao.RoleAuthority;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.AddUserVo;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.RoleMapper;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utis.MybatisUtils;
import com.lms.contants.HttpCode;
import com.lms.page.CustomPage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lms.cloudpan.constants.QuotaConstants.USER_QUOTA;
import static com.lms.cloudpan.constants.UserConstants.*;
import static com.lms.cloudpan.entity.factory.RoleFactory.ROLE_CONVERTER;
import static com.lms.cloudpan.entity.factory.UserFactory.USER_CONVERTER;


@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private IUserRoleService userRoleService;
    @Resource
    private IRoleAuthorityService roleAuthorityService;
    @Override
    public Boolean saveRole(RoleDto roleDto) {
        Role role = ROLE_CONVERTER.toRole(roleDto);
        return save(role);

    }

    @Override
    public RoleVo getRoleById(Integer id) {
        return ROLE_CONVERTER.toRoleVo(getById(id));
    }

    @Override
    public List<RoleVo> listRole(CustomPage customPage) {
        List<Role> result = CustomPage.getPageResult(customPage, new Role(), this, null);
        return ROLE_CONVERTER.toListRoleVo(result);
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
        return updateById(Role.builder().enable(ENABLE).rid(id).build());
    }

    @Override
    public Boolean disableRole(Integer id) {
        return updateById(Role.builder().enable(DISABLE).rid(id).build());
    }

    @Override
    public Page<RoleVo> pageRole(QueryRolePageDto rolePageDto) {

        Integer enable = rolePageDto.getEnable();
        String roleName = rolePageDto.getRoleName();
        String description = rolePageDto.getDescription();
        Integer pageSize = rolePageDto.getPageSize();
        Integer pageNum = rolePageDto.getPageNum();
        QueryWrapper<Role> roleQueryWrapper=new QueryWrapper<>();
        roleQueryWrapper.like(StringUtils.isNotBlank(roleName),"role_name",roleName)
                .eq(validEnable(enable),"enable",enable)
                .like(StringUtils.isNotBlank(description),"description",description);
        Page<Role> page = this.page(new Page<>(pageNum, pageSize), roleQueryWrapper);
        List<Role> records = page.getRecords();
//        List<UserVo> userVos = new ArrayList<>();
//        records.forEach(user->{
//            UserVo userVo=new UserVo();
//            BeanUtils.copyProperties(user,userVo);
//            userVo.setEnabled(user.getEnable());
//            userVos.add(userVo);
//        });
        List<RoleVo> roleVos = ROLE_CONVERTER.toListRoleVo(records);
        Page<RoleVo> result=new Page<>(pageNum,pageSize,page.getTotal());
        result.setRecords(roleVos);
        return result;
    }

    @Override
    @Transactional
    public Boolean updateRole(UpdateRoleDto updateRoleDto) {

        Integer rid = updateRoleDto.getRid();
        List<Integer> authorities = updateRoleDto.getAuthorities();
        //先校验rid
        BusinessException.throwIfNot(MybatisUtils.existCheck(this,Map.of("rid",rid)));





        Role role=new Role();

        BeanUtils.copyProperties(updateRoleDto,role);

        this.updateById(role);

        if(authorities==null||authorities.size()<1){
            return true;
        }

        //校验权限列表
        BusinessException.throwIfNot(MybatisUtils.checkAids(authorities));

        //先获取用户已有的角色
        List<Integer> userAidList = roleAuthorityService.list(new QueryWrapper<RoleAuthority>().eq("rid", rid)).stream().map(RoleAuthority::getAid).collect(Collectors.toList());
        //获取角色原来没有的权限
        List<Integer> addList=authorities.stream()
                .filter(aid->!userAidList.contains(aid)).collect(Collectors.toList());
        //获取角色现在去除的权限
        List<Integer> deleteList=userAidList.stream()
                .filter(aid->!authorities.contains(aid)).collect(Collectors.toList());

        List<RoleAuthority> addRoleAidsList=new ArrayList<>();
        addList.forEach(aid->{
            addRoleAidsList.add(RoleAuthority.builder().aid(aid).rid(rid).build());
        });
        ;

        if(addRoleAidsList.size()>0){
            roleAuthorityService.saveBatch(addRoleAidsList);
        }

        if(deleteList.size()>0){
            roleAuthorityService.remove(new QueryWrapper<RoleAuthority>()
                    .eq("rid",rid).in("aid",deleteList));
        }
        return true;
    }

    @Override
    public Boolean addRole(AddRoleDto addRoleDto) {

        String roleName = addRoleDto.getRoleName();
        String description = addRoleDto.getDescription();
        List<Integer> authorities = addRoleDto.getAuthorities();
        //校验是否存在重复的roleName和description
         BusinessException.throwIf(this.count(new QueryWrapper<Role>().eq("role_name",roleName).or()
                 .eq("description",description))>0,HttpCode.PARAMS_ERROR,"重复的角色名或者描述符");


         Role role=new Role();
         BeanUtils.copyProperties(addRoleDto,role);
         this.save(role);

         if(authorities==null||authorities.size()<1){
             return true;
         }
        Integer rid = role.getRid();

        //校验权限列表
         BusinessException.throwIfNot(MybatisUtils.checkAids(authorities));

         //给角色分配权限
        List<RoleAuthority> addList=new ArrayList<>();

        authorities.forEach(aid->{
            addList.add(RoleAuthority.builder().aid(aid).rid(rid).build());
        });

        if(addList.size()>0){
            roleAuthorityService.saveBatch(addList);
        }

         return true;
    }

    @Override
    public Boolean removeRoles(List<Integer> rids) {
        //校验rids的是否都存在

        BusinessException.throwIfNot(MybatisUtils.checkRids(rids), HttpCode.PARAMS_ERROR);
        //如果有用户在使用这些角色则无法删除
        BusinessException.throwIf(userRoleService.count(new QueryWrapper<UserRole>()
                .in("rid", rids))>0,HttpCode.PARAMS_ERROR,"某些角色被占用");

        //删除角色权限表信息？
        return this.remove(new QueryWrapper<Role>().in("rid",rids));
    }



    public boolean validEnable(Integer enable){
        return ObjectUtils.isNotEmpty(enable)&&(ENABLE.equals(enable)||DISABLE.equals(enable));
    }


}
