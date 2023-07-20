package com.lms.cloudpan.controller;



import com.lms.cloudpan.entity.dto.AddAuthorityDto;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.dto.QueryAuthDto;
import com.lms.cloudpan.entity.dto.UpdateAuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.service.IAuthorityService;
import com.lms.page.CustomPage;
import com.lms.result.EnableResponseAdvice;
import com.lms.result.ResultData;
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


    @PostMapping("/save")
    @PreAuthorize("@ss.hasPermi('pan:authority:save')")
    public Boolean addAuthority(@Validated @RequestBody AddAuthorityDto addAuthorityDto){
          return  iAuthorityService.saveAuthority(addAuthorityDto);
    }

    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermi('pan:authority:update')")
    public Boolean updatgeAuthority(@Validated @RequestBody UpdateAuthorityDto updateAuthorityDto){
          return iAuthorityService.updateAuthority(updateAuthorityDto);
    }

    @ApiOperation("根据id查询权限")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:authority:get')")
    public AuthorityVo getAuthority(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
        return iAuthorityService.getAuthorityById(id);
    }

    /**
     * 获取权限列表树
     * @return
     */
    @ApiOperation("查询权限树")
    @GetMapping("/treeList")
    @PreAuthorize("@ss.hasPermi('pan:authority:get')")
    public List<AuthorityVo> getAuthorityTree(){
        return iAuthorityService.getAuthorityTree();
    }

    @PostMapping("/queryTree")
    @PreAuthorize("@ss.hasPermi('pan:authority:get')")
    public List<AuthorityVo> getQueryAuthorityTree( @RequestBody QueryAuthDto authDto){
        return iAuthorityService.getQueryTreeList(authDto);
    }

    /**
     * 获取权限列表
     * @param queryAuthDto
     * @return
     */
    @ApiOperation("查询权限列表")
    @PostMapping("/list")
    @PreAuthorize("@ss.hasPermi('pan:authority:get')")
    public List<AuthorityVo> getAuthorityList(@Validated @RequestBody(required = false) QueryAuthDto queryAuthDto){
        return iAuthorityService.getAuthorityList(queryAuthDto);
    }




//    @ApiOperation("分页批量查询权限")
//    @GetMapping("/list")
//    @PreAuthorize("@ss.hasPermi('pan:authority:list')")
//    public List<AuthorityVo> listAuthority(CustomPage customPage) {
//        return iAuthorityService.listAuthority(customPage);
//    }

    @ApiOperation("删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:authority:delete')")
    public Boolean delAuthority(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
       return iAuthorityService.delAuthorityById(id);
    }
}
