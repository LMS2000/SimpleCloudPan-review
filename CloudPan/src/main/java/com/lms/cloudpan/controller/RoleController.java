package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.cloudpan.aop.OpLog;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.AddUserVo;
import com.lms.cloudpan.entity.vo.RoleVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.page.CustomPage;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import io.swagger.v3.oas.models.media.EmailSchema;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Update;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

import static com.lms.cloudpan.constants.UserConstants.ENABLE;

@Validated
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "角色管理")
public class RoleController {
    private final IRoleService iRoleService;

    @OpLog(desc = "新增角色,角色信息为: %s")
    @ApiOperation("新增角色")
    @PostMapping("/save")
    @PreAuthorize("@ss.hasAnyPermi('pan:role:save')")
    public Boolean saveRole(@Valid RoleDto roleDto) {
        return iRoleService.saveRole(roleDto);
    }


    @ApiOperation("根据角色id查询角色")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasAnyPermi('pan:role:get')")
    public RoleVo getRole(@PathVariable("id") @Positive(message = "id不合法") Integer id) {
        return iRoleService.getRoleById(id);
    }

    @OpLog(desc = "启用角色,角色ID为: %s")
    @ApiOperation("启用角色")
    @PostMapping("/enable/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:enable')")
    public Boolean enableRole(@PathVariable("id") Integer id) {
        return iRoleService.enableRole(id);
    }

    @OpLog(desc = "禁用角色,角色ID为: %s")
    @ApiOperation("禁用角色")
    @PostMapping("/disable/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:disable')")
    public Boolean disableRole(@PathVariable("id") Integer id) {
        return iRoleService.disableRole(id);
    }


    @ApiOperation("分页批量查询角色")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('pan:role:list')")
    public List<RoleVo> listRole(CustomPage customPage) {
        return iRoleService.listRole(customPage);
    }

    @OpLog(desc = "删除角色,角色ID为: %s")
    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:role:delete')")
    public Boolean delRole(@PathVariable("id") @Positive(message = "id不合法") Integer id) {
        return iRoleService.delRoleById(id);
    }


    /**
     * 分页条件获取角色列表
     * @param rolePageDto
     * @return
     */
    @ApiOperation("分页条件获取角色列表")
    @PostMapping("/page")
    @PreAuthorize("@ss.hasPermi('pan:role:page')")
    public Page<RoleVo> getUserPage(@RequestBody QueryRolePageDto rolePageDto) {
        Page<RoleVo> userVoPage = iRoleService.pageRole(rolePageDto);
        return userVoPage;
    }

    /**
     * 启用或者禁用角色
     * @param changeUserEnableDto
     * @return
     */
    @ApiOperation("启用或者禁用角色")
    @PostMapping("/change/enable")
    public Boolean changeUserEnable(@Validated @RequestBody ChangeRoleEnableDto changeUserEnableDto) {
        Integer enable = changeUserEnableDto.getEnable();
        if (enable.equals(ENABLE)) {
            return iRoleService.enableRole(changeUserEnableDto.getRid());
        } else {
            return iRoleService.disableRole(changeUserEnableDto.getRid());
        }
    }

    /**
     * 添加角色
     * @param addRoleDto
     * @return
     */
    @ApiOperation("添加角色")
    @PostMapping("/add")
    @PreAuthorize("@ss.hasPermi('pan:role:add')")
    public Boolean addRole(@Validated @RequestBody AddRoleDto addRoleDto){
        return iRoleService.addRole(addRoleDto);
    }

    /**
     * 修改角色
     * @param updateRoleDto
     * @return
     */
    @ApiOperation("修改角色")
    @PostMapping("/update")
    public Boolean updateRole(@Validated @RequestBody UpdateRoleDto updateRoleDto){
        return iRoleService.updateRole(updateRoleDto);
    }

    /**
     * 批量删除
     * @param rids
     * @return
     */
    @ApiOperation("批量删除")
    @PostMapping("/delete")
    @PreAuthorize("@ss.hasPermi('pan:role:delete')")
    public Boolean removeRoleBatch(@Validated  @RequestParam("rids") List<Integer> rids){
      return  iRoleService.removeRoles(rids);
    }






}
