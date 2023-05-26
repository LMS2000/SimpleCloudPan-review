package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.page.CustomPage;

import java.util.List;

public interface IAuthorityService extends IService<Authority> {
    Boolean saveAuthority(AuthorityVo authorityVo);

    AuthorityDto getAuthorityById(Integer id);

    List<AuthorityDto> listAuthority(CustomPage customPage);

    Boolean delAuthorityById(Integer id);
}
