package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dto.AddAuthorityDto;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.dto.QueryAuthDto;
import com.lms.cloudpan.entity.dto.UpdateAuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.page.CustomPage;

import java.util.List;

public interface IAuthorityService extends IService<Authority> {
    Boolean saveAuthority(AddAuthorityDto addAuthorityDto);

    Boolean updateAuthority(UpdateAuthorityDto  updateAuthorityDto);

    AuthorityVo getAuthorityById(Integer id);

    List<AuthorityVo> listAuthority(CustomPage customPage);

    Boolean delAuthorityById(Integer id);


    List<AuthorityVo> getAuthorityTree();

    List<AuthorityVo> getAuthorityList(QueryAuthDto queryAuthDto);

    List<AuthorityVo> getQueryTreeList(QueryAuthDto queryAuthDto);
}
