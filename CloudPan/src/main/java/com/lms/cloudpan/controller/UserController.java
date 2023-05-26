package com.lms.cloudpan.controller;


import com.infrastructure.jwt.Jwt;
import com.infrastructure.jwt.JwtUser;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.ResetPasswordVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.contants.HttpCode;
import com.lms.redis.RedisCache;
import com.lms.result.EnableResponseAdvice;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
@EnableResponseAdvice
public class UserController {

    @Resource
    private IUserService userService;


    @Resource
    private Jwt jwt;

    /**
     * 注册
     * @param userVo
     * @return
     */

    @PostMapping( "/register")
    public Boolean registerUser(@RequestBody UserVo userVo){
        return userService.registerCommonUser(userVo.getUsername(),userVo.getPassword());
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
     * @return
     */
    @GetMapping(value = "/get/login")
    public UserDto getCurrentUser(){
        JwtUser user = SecurityUtils.getLoginUser().getUser();
        UserDto userDto=new UserDto();
        BeanUtils.copyProperties(user,userDto);
        return userDto;
    }


    //注销
    @PostMapping("/logout")
    public Boolean logout(){
        String token = SecurityUtils.getLoginUser().getToken();
        jwt.delLoginUser(token);
        SecurityContextHolder.clearContext();
        return true;
    }

    /**
     * 添加用户
     * @param userVo
     * @return
     */
    @PostMapping("/add")
    @PreAuthorize("@ss.hasAnyPermi('pan:user:add')")
    public Integer add(@RequestBody(required = true)UserVo userVo){
        if (userVo == null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(HttpCode.OPERATION_ERROR);
        }
        return user.getUserId();
    }

    /**
     * 删除用户,逻辑删除
     * @return
     */
   @DeleteMapping("/delete/{id}")
   @PreAuthorize("@ss.hasAnyPermi('pan:user:delete')")
   public Boolean delete(@PathVariable Integer id){
        return userService.disableUser(id);
   }

    /**
     * 上传头像
     * @param file
     * @return 返回头像图片地址0
     */
   @PostMapping("/uploadAvatar")
   public String uploadAvatar(@RequestBody MultipartFile file){
       Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
       return userService.uploadAvatar(file,userId);
   }



   @PostMapping("/resetPassword")
   public Boolean resetPassword(@RequestBody ResetPasswordVo resetPasswordVo){
       Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
       return userService.resetPassword(resetPasswordVo,userId);
   }


//    /**
//     * 更新用户
//     *
//     * @param userUpdateRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/update")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public ResultData updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
//                                            HttpServletRequest request) {
//        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User user = new User();
//        BeanUtils.copyProperties(userUpdateRequest, user);
//        boolean result = userService.updateById(user);
//        return ResultUtils.success(result);
//    }
//    /**
//     * 获取用户列表
//     *
//     * @param userQueryRequest
//     * @param request
//     * @return
//     */
//    @GetMapping("/list")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
//        User userQuery = new User();
//        if (userQueryRequest != null) {
//            BeanUtils.copyProperties(userQueryRequest, userQuery);
//        }
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
//        List<User> userList = userService.list(queryWrapper);
//        List<UserVO> userVOList = userList.stream().map(user -> {
//            UserVO userVO = new UserVO();
//            BeanUtils.copyProperties(user, userVO);
//            return userVO;
//        }).collect(Collectors.toList());
//        return ResultUtils.success(userVOList);
//    }
//    /**
//     * 根据 id 获取用户
//     *
//     * @param id
//     * @param request
//     * @return
//     */
//    @GetMapping("/get")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
//        if (id <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User user = userService.getById(id);
//        UserVO userVO = new UserVO();
//        BeanUtils.copyProperties(user, userVO);
//        return ResultUtils.success(userVO);
//    }
//
//
//
//
//
//
//
//    /**
//     * 获取用户分页列表
//     * @param userQueryRequest
//     * @param request
//     * @return
//     */
//
//    @GetMapping("/list/page")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
//    public BaseResponse<Page<UserVO>> page(UserQueryRequest userQueryRequest,HttpServletRequest request){
//        long current = 1;
//        long size = 10;
//        User userQuery = new User();
//        if (userQueryRequest != null) {
//            BeanUtils.copyProperties(userQueryRequest, userQuery);
//            current = userQueryRequest.getCurrent();
//            size = userQueryRequest.getPageSize();
//        }
//        QueryWrapper<User> wrapper=new QueryWrapper<>(userQuery);
//
//        Page<User> userPage=userService.page(new Page<>(current,size),wrapper);
//        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
//        List<UserVO> userVOList=userPage.getRecords().stream().map(user->{
//            UserVO userVO=new UserVO();
//            BeanUtils.copyProperties(user,userVO);
//            return userVO;
//        }).collect(Collectors.toList());
//        userVOPage.setRecords(userVOList);
//        return ResultUtils.success(userVOPage);
//    }

}
