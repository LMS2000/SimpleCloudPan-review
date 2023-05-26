package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.ResetPasswordVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.page.CustomPage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService extends IService<User> {
    Boolean saveUser(UserVo userVo);

    Boolean resetPassword(ResetPasswordVo resetPasswordVo,Integer uid);
    UserDto getUserById(Integer id);

    List<UserDto> listUser(CustomPage customPage);

    Boolean delUserById(Integer id);

    Boolean enableUser(Integer id);

    Boolean disableUser(Integer id);

    Boolean registerUser(String username, String role);

    Boolean deleteUser(String username);



    User getUserByAccount(String userAccount);

    Boolean registerCommonUser(String userAccount,String password);


    String uploadAvatar(MultipartFile file,Integer uid);

}
