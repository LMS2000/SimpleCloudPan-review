package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.infrastructure.jwt.Jwt;
import com.infrastructure.jwt.JwtUser;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.UpdateUserVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.contants.HttpCode;
import com.lms.result.EnableResponseAdvice;
import com.lms.result.ResultData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Optional;

import static com.lms.cloudpan.constants.UserConstants.ENABLE;

@RestController
@RequestMapping("/user")
@EnableResponseAdvice
@Api(tags = "用户管理")
public class UserController {

    @Resource
    private IUserService userService;


    @Resource
    private Jwt jwt;

    /**
     * 注册
     *
     * @param userDto
     * @return
     */
    @ApiOperation("注册")
    @PostMapping("/register")
    public Boolean registerUser(@RequestBody UserDto userDto) {
        return userService.registerCommonUser(userDto.getUsername(), userDto.getPassword());
    }


//    /**
//     * 登录
//     * @param userVo
//     * @param request
//     * @return
//     */
//    @PostMapping("/login")
//    public UserDto userLogin(@RequestBody UserVo userVo, HttpServletRequest request){
//        if (userVo == null) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR);
//        }
//        String username = userVo.getUsername();
//        String userPassword = userVo.getPassword();
//        if (StringUtils.isAnyBlank(username, userPassword)) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR);
//        }
//        User user = userService.userLogin(username, userPassword, request);
//        UserDto userDto=new UserDto();
//        BeanUtils.copyProperties(user,userDto);
//        return userDto;
//    }

//    /**
//     * 注销
//     * @param request
//     * @return
//     */
//   @PostMapping("/logout")
//   public Boolean logout(HttpServletRequest request){
//        if (request == null) {
//            throw new BusinessException(HttpCode.PARAMS_ERROR);
//        }
//        return userService.userLogout(request);
//    }


    /**
     * 获取当前用户
     *
     * @return
     */
    @ApiOperation("获取当前用户")
    @GetMapping(value = "/get/login")
    public UserVo getCurrentUser() {
        JwtUser user = SecurityUtils.getLoginUser().getUser();
//        UserVo userVo=new UserVo();
//        BeanUtils.copyProperties(user,userVo);
        return userService.getUserById(user.getUserId());
    }


    /**
     * 注销
     * @return
     */
    @ApiOperation("注销")
    @PostMapping("/logout")
    public Boolean logout() {
        String token = SecurityUtils.getLoginUser().getToken();
        jwt.delLoginUser(token);
        SecurityContextHolder.clearContext();
        return true;
    }

    /**
     * 添加用户
     *
     * @param userDto
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加用户")
    @PreAuthorize("@ss.hasAnyPermi('pan:user:add')")
    public Integer add(@Validated @RequestBody(required = true) AddUserDto userDto) {
        return userService.addUser(userDto);
    }

    /**
     * 删除用户
     * @param userIds
     * @return
     */
    @ApiOperation("删除用户")
    @PostMapping("/delete")
    public Boolean removeUser(@Positive @RequestParam("userIds") List<Integer> userIds){
        return  userService.deleteUser(userIds);
    }

//    @PostMapping("/add")
//    @PreAuthorize("@ss.hasAnyPermi('pan:user:update')")
//    public Boolean update(){
//
//    }

    /**
     * 删除用户,逻辑删除
     *
     * @return
     */
    @ApiOperation("删除用户，逻辑删除")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@ss.hasAnyPermi('pan:user:delete')")
    public Boolean delete(@PathVariable Integer id) {
        return userService.disableUser(id);
    }

    /**
     * 上传头像
     *
     * @param file
     * @return 返回头像图片地址0
     */

    @ApiOperation("上传头像")
    @PostMapping("/uploadAvatar")
    public String uploadAvatar(@RequestBody MultipartFile file) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return userService.uploadAvatar(file, userId);
    }

    /**
     * 修改当前用户的密码
     * @param resetPasswordDto
     * @return
     */
    @ApiOperation("修改当前用户的密码")
    @PostMapping("/resetPassword")
    public Boolean resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return userService.resetPassword(resetPasswordDto, userId);
    }

    /**
     * 分页条件获取用户列表
     * @param userPageDto
     * @return
     */
    @ApiOperation("分页条件获取用户列表")
    @PostMapping("/page")
    @PreAuthorize("@ss.hasPermi('pan:user:page')")
    public Page<UserVo> getUserPage(@RequestBody QueryUserPageDto userPageDto) {
        Page<UserVo> userVoPage = userService.pageUser(userPageDto);
        return userVoPage;
    }


    /**
     * 启用或者禁用用户
     * @param changeUserEnableDto
     * @return
     */
    @ApiOperation("启用或者禁用用户")
    @PostMapping("/change/enable")
    @PreAuthorize("@ss.hasPermi('pan:user:enable')")
    public Boolean changeUserEnable(@Validated @RequestBody ChangeUserEnableDto changeUserEnableDto) {
        Integer enable = changeUserEnableDto.getEnable();
        if (enable.equals(ENABLE)) {
            return userService.enableUser(changeUserEnableDto.getUserId());
        } else {
            return userService.disableUser(changeUserEnableDto.getUserId());
        }
    }

    /**
     * 修改用户
     * @param userDto
     * @return
     */
    @ApiOperation("修改用户")
    @PostMapping("/update")
    public Boolean updateUser(@Validated @RequestBody UpdateUserDto userDto){
           return userService.updateUser(userDto);
    }

    /**
     * 修改用户密码
     * @param password
     * @param userId
     * @return
     */
    @ApiOperation("修改用户密码")
    @PostMapping("/reset/{password}/{userId}")
    public Boolean resetPwd(@PathVariable("password") String password,
                            @PathVariable("userId") Integer userId){
     return   userService.resetPassword(password,userId);
    }




}
