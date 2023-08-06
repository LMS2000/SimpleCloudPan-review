package com.lms.cloudpan.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserService;

import java.util.List;
import java.util.Map;

import static com.lms.cloudpan.constants.UserConstants.ENABLE;
import static com.lms.cloudpan.constants.UserConstants.NOT_DELETED;

public class MybatisUtils {

    //查找符合条件的记录是否存在
    public static <T> boolean existCheck(IService<T> service, Map<String,Object> map){
        QueryWrapper<T> wrapper=new QueryWrapper<>();
        map.forEach(wrapper::eq);
        return service.count(wrapper)>0;
    }

    //判断角色列表是否包含的是已存在的角色

    public static boolean checkRids( List<Integer> rids){
        if(rids==null||rids.size()<1)return true;
        IRoleService roleService = SpringUtil.getBean(IRoleService.class);
        long ridCount = roleService.count(new QueryWrapper<Role>().in("rid", rids).eq("enable",ENABLE));
        return ridCount==rids.size();
    }
    //判断权限列表是否包含的是已存在的权限
    public static boolean checkAids(List<Integer> aids){
        if(aids==null||aids.size()<1)return true;
        IAuthorityService authorityService = SpringUtil.getBean(IAuthorityService.class);
        long aidCount = authorityService.count(new QueryWrapper<Authority>().in("aid", aids).eq("enable",ENABLE));
        return aidCount==aids.size();
    }
    //判断用户列表是否包含的是已存在的用户
    public static boolean checkUids(List<Integer> uids){
        if(uids==null||uids.size()<1)return true;
        IUserService userService = SpringUtil.getBean(IUserService.class);
        long uidCount = userService.count(new QueryWrapper<User>().in("user_id", uids).eq("delete_flag",NOT_DELETED));
        return uidCount==uids.size();
    }

    public static boolean checkFids(List<Integer> fids){
        if(fids==null||fids.size()<1)return true;
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        long fidCount = fileService.count(new QueryWrapper<File>().in("user_id", fids).eq("delete_flag", NOT_DELETED));
        return fidCount==fids.size();
    }
}
