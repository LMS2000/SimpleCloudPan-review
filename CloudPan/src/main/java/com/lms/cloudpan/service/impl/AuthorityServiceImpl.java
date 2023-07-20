package com.lms.cloudpan.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.AuthorityMapper;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.cloudpan.utis.MybatisUtils;
import com.lms.contants.HttpCode;
import com.lms.page.CustomPage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AUTH;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lms.cloudpan.constants.UserConstants.*;
import static com.lms.cloudpan.entity.factory.AuthorityFactory.AUTHORITY_CONVERTER;


@Service
public class AuthorityServiceImpl extends ServiceImpl<AuthorityMapper, Authority> implements IAuthorityService {

    @Resource
    private IRoleAuthorityService roleAuthorityService;


    @Override
    public Boolean saveAuthority(AddAuthorityDto addAuthorityDto) {
        String name = addAuthorityDto.getName();
        String perms = addAuthorityDto.getPerms();
        Integer parentId = addAuthorityDto.getParentId();
        String component = addAuthorityDto.getComponent();
        String path = addAuthorityDto.getPath();
        String authType = addAuthorityDto.getAuthType();
        //判断权限标识和权限名是否重复
        long count = this.count(new QueryWrapper<Authority>().eq("name", name));
        BusinessException.throwIf(count>0,HttpCode.PARAMS_ERROR,"已存在相同的权限名");
        //校验父id是否存在
        if(parentId!=0){
            BusinessException.throwIfNot(MybatisUtils.existCheck(this,Map.of("aid",parentId)),
                    HttpCode.PARAMS_ERROR,"父id不存在");
        }

        //判断菜单类型和接口类型的权限的  C F

        // 如果是菜单则需要判断path和component是否填写，如果是C 则判断perms是否填写
        if(authType.equals("C")){
            BusinessException.throwIfNot(StringUtils.isNotBlank(path)&&
                    StringUtils.isNotBlank(component));
        }else{
            BusinessException.throwIfNot(StringUtils.isNotBlank(perms));
        }

         Authority authority=new Authority();
        BeanUtils.copyProperties(addAuthorityDto,authority);
        return this.save(authority);

    }

    @Override
    public Boolean updateAuthority(UpdateAuthorityDto updateAuthorityDto) {
        Integer aid = updateAuthorityDto.getAid();
        Integer parentId = updateAuthorityDto.getParentId();
        String name = updateAuthorityDto.getName();
        //校验aid是否存在
         BusinessException.throwIfNot(MybatisUtils.existCheck(this,Map.of("aid",aid)));
        //如果有parentId 校验
        if(parentId!=null&&parentId!=0){
            BusinessException.throwIfNot(MybatisUtils.existCheck(this,Map.of("aid",parentId)),
                    HttpCode.PARAMS_ERROR,"父id不存在");
        }
        //判断权限名是否重复
        if(StringUtils.isNotBlank(name)){
            BusinessException.throwIf(this.count(new QueryWrapper<Authority>().eq("name",name)
                    .ne("aid",aid))>0);
        }
          Authority authority=new Authority();
        BeanUtils.copyProperties(updateAuthorityDto,authority);
        return this.updateById(authority);
    }

    @Override
    public AuthorityVo getAuthorityById(Integer id) {
        Authority byId = getById(id);
        return AUTHORITY_CONVERTER.toAuthorityVo(byId);
    }

    @Override
    public List<AuthorityVo> listAuthority(CustomPage customPage) {
        List<Authority> result = this.list();
        return AUTHORITY_CONVERTER.toListAuthorityVo(result);
    }

    @Override
    public Boolean delAuthorityById(Integer id) {
//        removeById(id);
          //判断是否有子目录
        BusinessException.throwIf(MybatisUtils.existCheck(this, Map.of("parent_id",id)),
                HttpCode.PARAMS_ERROR,"存在子权限");
        //校验是否被角色占用
        BusinessException.throwIf(MybatisUtils.existCheck(roleAuthorityService,Map.of("aid",id)),
                HttpCode.PARAMS_ERROR,"该权限正在被角色占用");
       return this.update(new UpdateWrapper<Authority>().set("delete_flag",DELETED).eq("aid",id));
//
//        Boolean isDelete = roleAuthorityService.removeByAuthorityId(id);
    }

    /**
     * 递归获取权限树
     * @return
     */
    @Override
    public List<AuthorityVo> getAuthorityTree() {

        //获取全部的集合
        List<Authority> list = this.list(new QueryWrapper<Authority>().eq("delete_flag", NOT_DELETED).eq("enable", ENABLE));
        List<AuthorityVo> authorityVos = AUTHORITY_CONVERTER.toListAuthorityVo(list);

        //获取层次的权限集合
        List<AuthorityVo> resultList= authorityVos.stream()
                .filter(authorityVo -> authorityVo.getParentId()==0)
                .map(authorityVo -> {authorityVo.setChildren(getChildrenList(authorityVo,authorityVos));
                    return authorityVo;
                })
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    public List<AuthorityVo> getAuthorityList(QueryAuthDto queryAuthDto) {

        String name = queryAuthDto!=null? queryAuthDto.getName():null;
        Integer visible =queryAuthDto!=null? queryAuthDto.getVisible():null;
        List<Authority> list = this.list(new QueryWrapper<Authority>()
                .like(StringUtils.isNotBlank(name), "name", name)
                .eq(visible != null, "visible", visible).eq("delete_flag",NOT_DELETED));

        List<AuthorityVo> authorityVos = AUTHORITY_CONVERTER.toListAuthorityVo(list);
        return authorityVos;
    }

    @Override
    public List<AuthorityVo> getQueryTreeList(QueryAuthDto queryAuthDto) {
        String name = queryAuthDto!=null? queryAuthDto.getName():null;
        Integer visible =queryAuthDto!=null? queryAuthDto.getVisible():null;
        List<Authority> list = this.list(new QueryWrapper<Authority>()
                .like(StringUtils.isNotBlank(name), "name", name)
                .eq(isVisible(visible), "visible", visible).eq("delete_flag",NOT_DELETED));

        List<AuthorityVo> authorityVos = AUTHORITY_CONVERTER.toListAuthorityVo(list);
        List<AuthorityVo> resultList= authorityVos.stream()
                .filter(authorityVo -> authorityVo.getParentId()==0)
                .map(authorityVo -> {authorityVo.setChildren(getChildrenList(authorityVo,authorityVos));
                    return authorityVo;
                })
                .collect(Collectors.toList());

        return resultList;
    }

    private List<AuthorityVo> getChildrenList(AuthorityVo cur,List<AuthorityVo> allList){
        List<AuthorityVo> res=allList.stream()
                .filter(authorityVo -> authorityVo.getParentId().equals(cur.getAid()))
                .map(authorityVo -> {
                    authorityVo.setChildren(getChildrenList(authorityVo,allList));
                    return authorityVo;
                }).collect(Collectors.toList());
        return res;
    }

    private boolean isVisible(Integer visible){
        return ObjectUtils.isNotEmpty(visible)&&(visible.equals(0)||visible.equals(1));
    }

}
