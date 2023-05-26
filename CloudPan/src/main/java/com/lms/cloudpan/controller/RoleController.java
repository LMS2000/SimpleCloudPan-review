package com.lms.cloudpan.controller;



import com.lms.cloudpan.aop.OpLog;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.service.IRoleService;
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
@RequestMapping("/role")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "角色请求")
public class RoleController {
    private final IRoleService iRoleService;

    @OpLog(desc = "新增角色,角色信息为: %s")
    @ApiOperation("新增角色")
    @PostMapping("/save")
    @PreAuthorize("@ss.hasAnyPermi('pan:role:save')")
    public Boolean saveRole(@Valid RoleVo roleVo) {
        return iRoleService.saveRole(roleVo);
    }

    @ApiOperation("根据id查询角色")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyPermi('pan:role:get')")
    public RoleDto getRole(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
        return iRoleService.getRoleById(id);
    }

    @OpLog(desc = "启用角色,角色ID为: %s")
    @ApiOperation("启用角色")
    @PostMapping("/enable/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:enable')")
    public Boolean enableRole(@PathVariable("id") Integer id){
       return iRoleService.enableRole(id);
    }

    @OpLog(desc = "禁用角色,角色ID为: %s")
    @ApiOperation("禁用角色")
    @PostMapping("/disable/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:disable')")
    public Boolean disableRole(@PathVariable("id") Integer id){
       return iRoleService.disableRole(id);
    }


    @ApiOperation("分页批量查询角色")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('pan:role:list')")
    public List<RoleDto> listRole(CustomPage customPage) {
        return iRoleService.listRole(customPage);
    }

    @OpLog(desc = "删除角色,角色ID为: %s")
    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:delete')")
    public Boolean delRole(@PathVariable("id")@Positive(message = "id不合法") Integer id) {
       return iRoleService.delRoleById(id);
    }
}
