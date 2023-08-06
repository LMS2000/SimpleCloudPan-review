package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.*;
import com.lms.cloudpan.entity.vo.AddUserVo;
import com.lms.cloudpan.entity.vo.PageVo;
import com.lms.cloudpan.entity.vo.UpdateUserVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.page.CustomPage;
import io.swagger.models.auth.In;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService extends IService<User> {
    Boolean saveUser(UserDto userDto);

    Boolean resetPassword(ResetPasswordDto resetPasswordDto, Integer uid);

    Boolean resetPassword(String password,Integer uid);
    UserVo getUserById(Integer id);

    List<UserVo> listUser(CustomPage customPage);

    Page<UserVo> pageUser(QueryUserPageDto userPageDto);
    Boolean delUserById(Integer id);

    Boolean enableUser(Integer id);

    Boolean disableUser(Integer id);

    Boolean registerUser(String username, String role);

    Boolean deleteUser(List<Integer> uids);



    User getUserByAccount(String userAccount);

    Boolean registerCommonUser(String userAccount,String password);


    String uploadAvatar(MultipartFile file,Integer uid);



    UpdateUserVo getUpdateUserInfo(Integer uid);



    Integer  addUser(AddUserDto addUserDto);


    Boolean updateUser(UpdateUserDto userDto);


    String sendEmail(String email ,Integer type);
}
