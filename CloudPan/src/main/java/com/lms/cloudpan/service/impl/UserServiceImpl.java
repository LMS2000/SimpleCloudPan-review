package com.lms.cloudpan.service.impl;


import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.QuotaConstants;
import com.lms.cloudpan.constants.UserConstants;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.ResetPasswordVo;
import com.lms.cloudpan.entity.vo.UserVo;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.UserMapper;
import com.lms.cloudpan.service.IFolderService;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utis.MybatisUtils;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.contants.HttpCode;
import com.lms.page.CustomPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.lms.cloudpan.constants.FileConstants.STATIC_REQUEST_PREFIX;
import static com.lms.cloudpan.constants.UserConstants.DESIABLE;
import static com.lms.cloudpan.entity.factory.UserFactory.USER_CONVERTER;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private IRoleService roleService;

    @Resource
    private IFolderService folderService;
    private final PasswordEncoder passwordEncoder;

    @Resource
    private OssProperties ossProperties;


    @Resource
    private OssClient ossClient;

    @Override
    public Boolean saveUser(UserVo userVo) {
//        isUserExist(userVo.getUsername());
        User user = USER_CONVERTER.toUser(userVo);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnable(0);
        user.setUseQuota(QuotaConstants.EMTRY_QUOTA);
        user.setQuota(QuotaConstants.USER_QUOTA);
        return save(user);

    }

    @Override
    public Boolean resetPassword(ResetPasswordVo resetPasswordVo,Integer uid) {
        //如果新旧密码一致就直接返回
        if(resetPasswordVo.getOldPassword().equals(resetPasswordVo.getNewPassword())){
            return false;
        }
        String newPassoword = passwordEncoder.encode(resetPasswordVo.getNewPassword());
        return updateById(User.builder().userId(uid).password(newPassoword).build());
    }


    @Override
    public UserDto getUserById(Integer id) {
        return USER_CONVERTER.toUserDto(getById(id));
    }

    @Override
    public List<UserDto> listUser(CustomPage customPage) {
        List<User> result = CustomPage.getPageResult(customPage, new User(), this, null);
        return USER_CONVERTER.toListUserDto(result);
    }

    /**
     * 删除用户，并且删除关联的角色
     *
     * @param id
     */
    @Override
    @Transactional
    public Boolean delUserById(Integer id) {
        operationSuperException(id, "不能对管理员操作");
        removeById(id);
        return userRoleService.delByUserId(id);
    }

    @Override
    public Boolean enableUser(Integer id) {
        operationSuperException(id, "不能对管理员操作");
        return updateById(User.builder().userId(id).enable(0).build());
    }

    @Override
    public Boolean disableUser(Integer id) {
        operationSuperException(id, "不能对管理员操作");
        return updateById(User.builder().userId(id).enable(1).build());
    }

    /**
     * 赋予用户角色
     *
     * @param userAccount
     * @param roleName
     */
    @Override
    @Transactional
    public Boolean registerUser(String userAccount, String roleName) {
        User user = getOne(new QueryWrapper<User>().eq("username", userAccount));
        if (user == null || DESIABLE.equals(user.getEnable())) {
            throw new BusinessException(HttpCode.PARAMS_ERROR,"该账号不存在");
        }

        Role role = roleService.getOne(new QueryWrapper<Role>().eq("role_name", roleName));
        if (role == null || DESIABLE.equals(role.getEnabled())) {
            throw new BusinessException(HttpCode.PARAMS_ERROR,"该角色不可用");
        }

        return userRoleService.save(UserRole.builder().uid(user.getUserId()).rid(role.getRid()).build());

    }

    /**
     * 删除用户,使用户不可用，逻辑删除
     *
     * @param userAccount
     */
    @Override
    public Boolean deleteUser(String userAccount) {
        User one = getOne(new QueryWrapper<User>().eq("username", userAccount));
        if (one == null) {
            return false;
        }
//        removeById(one.getUid());
//        userRoleService.remove(new QueryWrapper<UserRole>().eq("uid", one.getUid()));
        return disableUser(one.getUserId());
    }

    @Override
    public User getUserByAccount(String account) {
        return getOne(new QueryWrapper<User>().eq("username", account));
    }

    @Override
    @Transactional
    public Boolean registerCommonUser(String username, String password) {
        isUserExist(username);
        Boolean isCreate = saveUser(UserVo.builder().username(username).password(password).build());
        Boolean isRegisterRole = registerUser(username, UserConstants.USER);
        //给用户分配空间
        User userByAccount = getUserByAccount(username);
        boolean isCreateRoot = folderService.save(Folder.builder().userId(userByAccount.getUserId())
                .folderName("/root").build());

        return isCreate&&isRegisterRole&&isCreateRoot;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Integer uid) {
         validFile(file);

        User user = this.getById(uid);
        String bucketName="bucket_user_"+uid;


        if(user.getAvatar().equals("#")){
            String[] split = user.getAvatar().split(bucketName);
             ossClient.deleteObject(bucketName,split[1]);
        }

        //上传文件

        String filePath;
        try {
            String randomPath =
                    com.lms.cloudpan.utis.FileUtil.generatorFileName(file.getOriginalFilename()==null?file.getName():file.getOriginalFilename());
             filePath="avatar/"+randomPath;
            ossClient.putObject(bucketName, filePath  ,file.getInputStream());

        } catch (IOException e) {
            throw new BusinessException(HttpCode.OPERATION_ERROR,"上传头像失败");
        }

        String fileUrl = com.lms.cloudpan.utis.FileUtil.getFileUrl(ossProperties.getEndpoint(), STATIC_REQUEST_PREFIX, bucketName, filePath);
        this.updateById(User.builder().userId(uid).avatar(fileUrl).build());

        return fileUrl;
    }

    //校验头像图片

    private void validFile(MultipartFile multipartFile) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024*10L;
            if (fileSize > ONE_M) {
                throw new BusinessException(HttpCode.PARAMS_ERROR, "文件大小不能超过 10M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(HttpCode.PARAMS_ERROR, "文件类型错误");
            }
    }


    private void operationSuperException(Integer id, String errMsg) {
        if (SecurityUtils.isAdmin(id)) {
            throw new BusinessException(HttpCode.PARAMS_ERROR,errMsg);
        }
    }

    private void isUserExist(String userAccount){
        User userByAccount = getUserByAccount(userAccount);
        if(userByAccount!=null){
            throw new BusinessException(HttpCode.PARAMS_ERROR,"用户已存在！");
        }
    }


}
