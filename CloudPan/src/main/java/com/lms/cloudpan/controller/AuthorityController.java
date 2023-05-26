package com.lms.cloudpan.controller;



import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.page.CustomPage;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@RestController
@RequestMapping("/authority")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "权限请求")
public class AuthorityController {
    private final IAuthorityService iAuthorityService;

    @ApiOperation("新增权限")
    @PostMapping("/save")
    @PreAuthorize("@ss.hasPermi('pan:authority:save')")
    public Boolean saveAuthority(@Valid AuthorityVo authorityVo) {
        return iAuthorityService.saveAuthority(authorityVo);
    }

    @ApiOperation("根据id查询权限")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:authority:get')")
    public AuthorityDto getAuthority(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
        return iAuthorityService.getAuthorityById(id);
    }

    @ApiOperation("分页批量查询权限")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('pan:authority:list')")
    public List<AuthorityDto> listAuthority(CustomPage customPage) {
        return iAuthorityService.listAuthority(customPage);
    }

    @ApiOperation("删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:authority:delete')")
    public Boolean delAuthority(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
       return iAuthorityService.delAuthorityById(id);
    }
}
