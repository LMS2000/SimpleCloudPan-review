package com.lms.cloudpan.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dao.UserRole;

import com.lms.cloudpan.entity.dto.AllocateUserDto;
import com.lms.cloudpan.entity.vo.*;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.UserRoleMapper;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utils.MybatisUtils;
import com.lms.contants.HttpCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lms.cloudpan.constants.QuotaConstants.USER_QUOTA;
import static com.lms.cloudpan.constants.UserConstants.*;
import static com.lms.cloudpan.constants.UserConstants.INITPASSWORD;
import static com.lms.cloudpan.entity.factory.RoleFactory.ROLE_CONVERTER;
import static com.lms.cloudpan.entity.factory.UserFactory.USER_CONVERTER;


@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements IUserRoleService {

    @Resource
    private IRoleService roleService;


    @Resource
    private IUserService userService;

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
    public List<RoleVo> getRoleOfUser(Integer userId) {
        List<Integer> ridList = listByMap(Map.of("uid", userId)).stream().map(UserRole::getRid).collect(Collectors.toList());
        return ObjectUtils.isEmpty(ridList)?null:ROLE_CONVERTER.toListRoleVo(roleService.listByIds(ridList));
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

    @Override
    public UpdateUserVo getUpdateUserInfo(Integer uid) {
        UserVo userById = userService.getUserById(uid);
        //获取用户的rids  和全部的roles
        List<Integer> rids = this.list(new QueryWrapper<UserRole>().eq("uid",uid)).stream().map(UserRole::getRid).collect(Collectors.toList());
        userById.setRids(rids);
        List<Role> roles = roleService.list(null);
        List<RoleVo> roleVos = ROLE_CONVERTER.toListRoleVo(roles);
        return UpdateUserVo.builder().userVo(userById).roles(roleVos).maxQuota(USER_QUOTA).build();
    }

    @Override
    public Boolean isManager(Integer uid) {

        List<Integer> rids = this.list(new QueryWrapper<UserRole>().eq("uid", uid)).stream().map(UserRole::getRid).collect(Collectors.toList());

        List<String> roleNames = roleService.list(new QueryWrapper<Role>().in("rid", rids).eq("enable", ENABLE)).stream().map(Role::getRoleName).collect(Collectors.toList());

        return roleNames.contains(ADMIN)&&MybatisUtils.existCheck(userService, Map.of("user_id",uid,"delete_flag",NOT_DELETED));
    }

    @Override
    public AddUserVo getInitInfo() {
        List<Role> list = roleService.list(null);
        return AddUserVo.builder().roles(ROLE_CONVERTER.toListRoleVo(list))
                .initPassword(INITPASSWORD).maxQuota(USER_QUOTA).build();
    }

    @Override
    public Boolean setRolesForUser(Integer userId, List<Integer> rids) {

        //校验用户是否存在
        BusinessException.throwIfNot(MybatisUtils.existCheck(userService,Map.of("user_id",userId)));

        //校验rids是否合法
        BusinessException.throwIfNot(MybatisUtils.checkRids(rids));


        //先获取用户已有的角色
        List<Integer> userRidList = this.list(new QueryWrapper<UserRole>().eq("uid", userId)).stream().map(UserRole::getRid).collect(Collectors.toList());

        List<Integer> addList=rids.stream()
                .filter(roleId->!userRidList.contains(roleId)).collect(Collectors.toList());

        List<Integer> deleteList=userRidList.stream()
                .filter(roleId->!rids.contains(roleId)).collect(Collectors.toList());

        List<UserRole> addRoleList=new ArrayList<>();
        addList.forEach(rid->{
            addRoleList.add(UserRole.builder().uid(userId).rid(rid).build());
        });

//        List<UserRole> deleteRoleList=new ArrayList<>();
//
//        deleteList.forEach(rid->{
//            deleteRoleList.add(UserRole.builder().uid(userId).rid(rid).build());
//        });

        if(addRoleList.size()>0){
            this.saveBatch(addRoleList);
        }

        if(deleteList.size()>0){
            this.remove(new QueryWrapper<UserRole>()
                    .eq("uid",userId).in("rid",deleteList));
        }
        return  true;
    }

    @Override
    public SetRoleVo getUserAndRoles(Integer uid) {

        UserVo userVo = userService.getUserById(uid);
        BusinessException.throwIf(userVo==null);
        List<Integer> userRids = this.list(new QueryWrapper<UserRole>().eq("uid", uid)).stream().map(UserRole::getRid).collect(Collectors.toList());
        userVo.setRids(userRids);
        List<Integer> rids = this.list(null).stream().map(UserRole::getRid).collect(Collectors.toList());

        List<Role> roles = roleService.list(new QueryWrapper<Role>().in("rid", rids));

        List<RoleVo> roleVos = ROLE_CONVERTER.toListRoleVo(roles);
        return SetRoleVo.builder().user(userVo).roles(roleVos).build();

    }

    @Override
    public Boolean releaseRoleToUser(List<Integer> uids, Integer rid) {
        BusinessException.throwIf(uids.contains(ADMIN_UID),HttpCode.PARAMS_ERROR,"不可操作超级管理员");
        if(uids==null||uids.size()<1){
            return false;
        }
        return this.remove(new QueryWrapper<UserRole>().in("uid",uids).eq("rid",rid));
    }

    @Override
    public Boolean grantRoleToUser(List<Integer> uids, Integer rid) {

        //判断uids是否都存在
        BusinessException.throwIfNot(MybatisUtils.checkUids(uids),
                HttpCode.PARAMS_ERROR,"一些用户不存在");

        BusinessException.throwIf(uids.contains(ADMIN_UID),HttpCode.PARAMS_ERROR,"不可操作超级管理员");
        //判断rid是否存在
        BusinessException.throwIfNot(MybatisUtils.existCheck(roleService,Map.of("rid",rid)));


        for (Integer uid : uids) {
            if(!MybatisUtils.existCheck(this,Map.of("uid",uid,"rid",rid))){
                save(UserRole.builder().rid(rid).uid(uid).build());
            }
        }
        return true;
    }

    @Override
    public Page<UserVo> getAllocateUsers(AllocateUserDto allocateUserDto) {
        Integer rid = allocateUserDto.getRid();
        String username = allocateUserDto.getUsername();
        Integer pageNum = allocateUserDto.getPageNum();
        Integer pageSize = allocateUserDto.getPageSize();
        //校验角色id
        BusinessException.throwIfNot(MybatisUtils.existCheck(roleService,
                Map.of("rid",rid)),HttpCode.PARAMS_ERROR,"角色不存在");
        //获取uids
        List<Integer> uids = this.list(new QueryWrapper<UserRole>().eq("rid", rid)).stream().map(UserRole::getUid).collect(Collectors.toList());
        List<User> userList=null;
        Page<UserVo> userVoPage =new Page<>(pageNum,pageSize);
        if(CollectionUtil.isNotEmpty(uids)){
            userList = userService.list(new QueryWrapper<User>().in(CollectionUtil.isNotEmpty(uids),"user_id", uids)
                    .like(StringUtils.isNotBlank(username), "username", username));
            userVoPage.setRecords(USER_CONVERTER.toListUserVo(userList));
            userVoPage.setTotal(userList.size());
        }

        return userVoPage;
    }

    @Override
    public Page<UserVo> getUnAllocateUsers(AllocateUserDto allocateUserDto) {
        Integer rid = allocateUserDto.getRid();
        String username = allocateUserDto.getUsername();
        Integer pageNum = allocateUserDto.getPageNum();
        Integer pageSize = allocateUserDto.getPageSize();
        //校验角色id
        BusinessException.throwIfNot(MybatisUtils.existCheck(roleService,
                Map.of("rid",rid)),HttpCode.PARAMS_ERROR,"角色不存在");
        //获取uids
        List<Integer> uids = this.list(new QueryWrapper<UserRole>().eq("rid", rid)).stream().map(UserRole::getUid).collect(Collectors.toList());

        List<User> userList = userService.list(new QueryWrapper<User>().notIn(CollectionUtil.isNotEmpty(uids),"user_id", uids)
                .like(StringUtils.isNotBlank(username), "username", username));

        Page<UserVo> userVoPage =new Page<>(pageNum,pageSize);
        userVoPage.setRecords(USER_CONVERTER.toListUserVo(userList));
        userVoPage.setTotal(userList.size());
        return userVoPage;
    }

}
