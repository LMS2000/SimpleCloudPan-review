package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.cloudpan.aop.OpLog;
import com.lms.cloudpan.convert.StrToListFormatter;
import com.lms.cloudpan.entity.dto.AllocateUserDto;
import com.lms.cloudpan.entity.vo.*;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Validated
@RestController
@RequestMapping("/userRole")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "用户角色管理")
public class UserRoleController {
    private final IUserRoleService iUserRoleService;

    @InitBinder
    public void initBinder(DataBinder dataBinder){
        dataBinder.addCustomFormatter(new StrToListFormatter());
    }

    @ApiOperation("获取用户角色")
    @PostMapping("/{userId}")
    @PreAuthorize("@ss.hasPermi('pan:userRole:get')")
    public List<RoleVo> getRoleOfUser(@PathVariable("userId")@Positive(message = "id不合法")Integer userId){
        return iUserRoleService.getRoleOfUser(userId);
    }

//    @OpLog(desc ="角色%s赋予ID为%s的用户")
//    @ApiOperation("赋予用户角色")
//    @PostMapping("/releaseRoleToUser/{userId}")
//    @PreAuthorize("@ss.hasPermi('pan:userRole:releaseRoleToUser')")
//    public Boolean releaseRoleToUser(@RequestParam("roleList")@ApiParam("角色id列表,由'-'分割角色id,如: 1-2-3")List<Integer> authorityList, @PathVariable("userId")@Positive(message = "id不合法")Integer userId) {
//      return   iUserRoleService.releaseRoleToUser(userId,authorityList);
//    }
//
//    @OpLog(desc = "角色%s从ID为%s的用户处回收")
//    @ApiOperation("收回用户角色")
//    @GetMapping("/revokeRoleFromUser/{userId}")
//    @PreAuthorize("@ss.hasPermi('pan:userRole:revokeRoleFromUser')")
//    public Boolean revokeRoleFromUser(@RequestParam("roleList")@ApiParam("角色id列表,由'-'分割角色id,如: 1-2-3") List<Integer> authorityList, @PathVariable("userId")@Positive(message = "id不合法")Integer userId) {
//      return   iUserRoleService.revokeRoleFromUser(userId,authorityList);
//    }


    /**
     * 获取修改用户所需要的信息
     * @param userId
     * @return
     */
    @ApiOperation("获取修改用户所需要的信息")
    @GetMapping("/get/{userId}")
    public UpdateUserVo getUserById(@Positive(message = "id不合法") @PathVariable Integer userId) {

        return iUserRoleService.getUpdateUserInfo(userId);
    }

    /**
     * 是否是管理员
     * @return
     */
    @GetMapping("/isManager")
    public Boolean isManager() {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return iUserRoleService.isManager(userId);
    }


    /**
     * 获取新建用户所需要的数据
     * @return
     */
    @ApiOperation("获取新建用户所需要的数据")
    @GetMapping("/get/initInfo")
    @PreAuthorize("@ss.hasPermi('pan:role:getInitInfo')")
    public AddUserVo getInitInfo() {
        return iUserRoleService.getInitInfo();
    }

    /**
     * 为用户分配角色
     * @param userId
     * @param rids
     * @return
     */
    @ApiOperation("为用户分配角色")
    @PostMapping("/set/{userId}")
    public Boolean setRoles(@Positive(message = "用户id不合法") @PathVariable("userId") Integer userId, @RequestParam("rids") List<Integer> rids) {

        return iUserRoleService.setRolesForUser(userId, rids);
    }


    @ApiOperation("获取用户和其所有的角色")
    @GetMapping("/getUserAndRoles/{userId}")
    public SetRoleVo getUserAndRoles(@Positive(message = "id不合法") @PathVariable("userId") Integer userId){
        return iUserRoleService.getUserAndRoles(userId);
    }

    /**
     * 回收用户角色
     * @param userIds
     * @param rid
     * @return
     */
    @ApiOperation("回收用户角色")
    @PostMapping("/release/{rid}")
    public Boolean releaseRoleToUser(@NotNull(message = "用户列表不能为空") @RequestParam("userIds") List<Integer> userIds,
                                   @Positive(message = "id不合法")  @PathVariable Integer rid){
      return iUserRoleService.releaseRoleToUser(userIds,rid);
    }

    @ApiOperation("授予用户角色")
    @PostMapping("/grant/{rid}")
    public Boolean grantRoleToUser(@NotNull(message = "用户列表不能为空") @RequestParam("userIds") List<Integer> userIds,
                                   @Positive(message = "id不合法")  @PathVariable Integer rid){
        return iUserRoleService.grantRoleToUser(userIds,rid);
    }

    /**
     * 获取已经授权指定角色的用户列表
     * @param allocateUserDto
     * @return
     */
    @ApiOperation("获取已经授权指定角色的用户列表")
    @PostMapping("/page/allocate")
    public Page<UserVo> getAllocatedUser( @Validated @RequestBody AllocateUserDto allocateUserDto){
        return iUserRoleService.getAllocateUsers(allocateUserDto);
    }
    /**
     * 获取未授权指定角色的用户列表
     * @param allocateUserDto
     * @return
     */
    @ApiOperation("获取未授权指定角色的用户列表")
    @PostMapping("/page/unAllocate")
    public Page<UserVo> getUnAllocateUser(@Validated @RequestBody AllocateUserDto allocateUserDto){
       return iUserRoleService.getUnAllocateUsers(allocateUserDto);
    }




}
