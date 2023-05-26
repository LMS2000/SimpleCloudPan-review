package com.lms.cloudpan.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.mapper.AuthorityMapper;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.cloudpan.service.IRoleAuthorityService;
import com.lms.page.CustomPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.lms.cloudpan.entity.factory.AuthorityFactory.AUTHORITY_CONVERTER;


@Service
public class AuthorityServiceImpl extends ServiceImpl<AuthorityMapper, Authority> implements IAuthorityService {

    @Resource
    private IRoleAuthorityService roleAuthorityService;


    @Override
    public Boolean saveAuthority(AuthorityVo authorityVo) {
        Authority authority = AUTHORITY_CONVERTER.toAuthority(authorityVo);
        return save(authority);
    }

    @Override
    public AuthorityDto getAuthorityById(Integer id) {
        Authority byId = getById(id);
        return AUTHORITY_CONVERTER.toAuthorityDto(byId);
    }

    @Override
    public List<AuthorityDto> listAuthority(CustomPage customPage) {
        List<Authority> result = CustomPage.getPageResult(customPage, new Authority(), this, null);
        return AUTHORITY_CONVERTER.toListAuthorityDto(result);
    }

    @Override
    @Transactional
    public Boolean delAuthorityById(Integer id) {
        removeById(id);
        Boolean isDelete = roleAuthorityService.removeByAuthorityId(id);
        return isDelete;
    }
}
