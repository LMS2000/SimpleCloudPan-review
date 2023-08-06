package com.lms.cloudpan.service.impl;


import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.config.AppConfig;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.CommonConstants;
import com.lms.cloudpan.constants.QuotaConstants;
import com.lms.cloudpan.constants.UserConstants;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.*;

import com.lms.cloudpan.entity.vo.*;
import com.lms.cloudpan.exception.BusinessException;
import com.lms.cloudpan.mapper.UserMapper;
import com.lms.cloudpan.service.IRoleService;
import com.lms.cloudpan.service.IUserRoleService;
import com.lms.cloudpan.service.IUserService;
import com.lms.cloudpan.utils.MybatisUtils;
import com.lms.cloudpan.utils.SecurityUtils;
import com.lms.cloudpan.utils.StringTools;
import com.lms.contants.HttpCode;
import com.lms.page.CustomPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.lms.cloudpan.constants.FileConstants.STATIC_REQUEST_PREFIX;
import static com.lms.cloudpan.constants.QuotaConstants.USER_QUOTA;
import static com.lms.cloudpan.constants.UserConstants.*;
import static com.lms.cloudpan.entity.factory.RoleFactory.ROLE_CONVERTER;
import static com.lms.cloudpan.entity.factory.UserFactory.USER_CONVERTER;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Resource
    private IUserRoleService userRoleService;

    @Resource
    private IRoleService roleService;


    private final PasswordEncoder passwordEncoder;

    @Resource
    private OssProperties ossProperties;

    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private OssClient ossClient;

    @Resource
    private AppConfig appConfig;

    @Override
    public Boolean saveUser(UserDto userDto) {
//        isUserExist(userVo.getUsername());
        User user = USER_CONVERTER.toUser(userDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnable(0);
        user.setUseQuota(QuotaConstants.EMTRY_QUOTA);
        user.setQuota(USER_QUOTA);
        return save(user);

    }

    @Override
    public Boolean resetPassword(ResetPasswordDto resetPasswordDto, Integer uid) {

        String oldPassword = resetPasswordDto.getOldPassword();
        String newPassword = resetPasswordDto.getNewPassword();

        String encodeOldPassword = passwordEncoder.encode(oldPassword);
        //密码不对就报错
        BusinessException
                .throwIfNot(MybatisUtils.existCheck(this, Map.of("user_id", uid,
                        "password", encodeOldPassword)), HttpCode.PARAMS_ERROR);

        //如果新旧密码一致就直接返回
        if (oldPassword.equals(newPassword)) {
            return false;
        }
        String encodeNewPassword = passwordEncoder.encode(resetPasswordDto.getNewPassword());
        return updateById(User.builder().userId(uid).password(newPassword).build());
    }

    @Override
    public Boolean resetPassword(String password, Integer uid) {
        //判断用户是否存在
        BusinessException.throwIf(!MybatisUtils.existCheck(this, Map.of("user_id", uid)));
        String encode = passwordEncoder.encode(password);
        return this.updateById(User.builder().userId(uid).password(encode).build());
    }


    @Override
    public UserVo getUserById(Integer id) {
        return USER_CONVERTER.toUserVo(getById(id));
    }

    @Override
    public List<UserVo> listUser(CustomPage customPage) {
        List<User> result = CustomPage.getPageResult(customPage, new User(), this, null);
        return USER_CONVERTER.toListUserVo(result);
    }

    /**
     * 分页条件查询用户列表
     *
     * @param userPageDto
     * @return
     */
    @Override
    public Page<UserVo> pageUser(QueryUserPageDto userPageDto) {
        Integer enable = userPageDto.getEnable();
        String username = userPageDto.getUsername();
        Integer pageNum = userPageDto.getPageNum();
        Integer pageSize = userPageDto.getPageSize();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.like(StringUtils.isNotBlank(username), "username", userPageDto.getUsername())
                .eq(validEnable(enable), "enable", userPageDto.getEnable())
                .eq("delete_flag", NOT_DELETED);
        Page<User> page = this.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        List<User> records = page.getRecords();
//        List<UserVo> userVos = new ArrayList<>();
//        records.forEach(user->{
//            UserVo userVo=new UserVo();
//            BeanUtils.copyProperties(user,userVo);
//            userVo.setEnabled(user.getEnable());
//            userVos.add(userVo);
//        });
        List<UserVo> userVos = USER_CONVERTER.toListUserVo(records);
        Page<UserVo> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(userVos);
        return result;
    }

    public boolean validEnable(Integer enable) {
        return ObjectUtils.isNotEmpty(enable) && (ENABLE.equals(enable) || DISABLE.equals(enable));
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
        return updateById(User.builder().userId(id).enable(ENABLE).build());
    }

    @Override
    public Boolean disableUser(Integer id) {
        operationSuperException(id, "不能对管理员操作");
        return updateById(User.builder().userId(id).enable(DISABLE).build());
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
        if (user == null || DISABLE.equals(user.getEnable())) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "该账号不存在");
        }

        Role role = roleService.getOne(new QueryWrapper<Role>().eq("role_name", roleName));
        if (role == null || DISABLE.equals(role.getEnable())) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "该角色不可用");
        }

        return userRoleService.save(UserRole.builder().uid(user.getUserId()).rid(role.getRid()).build());

    }

    /**
     * 删除用户,使用户不可用，逻辑删除
     *
     * @param uids
     */
    @Override
    public Boolean deleteUser(List<Integer> uids) {
        //不可以删除超级管理员
        BusinessException.throwIf(uids.contains(1));
        //集合包括不存在的用户
        List<User> userIdList = this.list(new QueryWrapper<User>().in("user_id", uids));
        BusinessException.throwIf(userIdList.size() != uids.size());
        //还得删除用户角色表信息？
        return this.update(new UpdateWrapper<User>().set("delete_flag", DELETED).in("user_id", uids));
    }

    @Override
    public User getUserByAccount(String account) {
        return getOne(new QueryWrapper<User>().eq("username", account));
    }

    @Override
    @Transactional
    public Boolean registerCommonUser(String username, String password) {
        isUserExist(username);
        Boolean isCreate = saveUser(UserDto.builder().username(username).password(password).build());
        Boolean isRegisterRole = registerUser(username, UserConstants.USER);
        //给用户分配空间
        User userByAccount = getUserByAccount(username);
//        boolean isCreateRoot = folderService.save(Folder.builder().userId(userByAccount.getUserId())
//                .folderName("/root").build());

        return isCreate && isRegisterRole;
    }

    @Override
    public String uploadAvatar(MultipartFile file, Integer uid) {
        //校验文件
        validFile(file);

        User user = this.getById(uid);
        String bucketName = "bucket_user_" + uid;


        if (!user.getAvatar().equals("#")) {
            String[] split = user.getAvatar().split(bucketName);
            ossClient.deleteObject(bucketName, split[1]);
        }

        //上传文件

        String filePath;
        try {
            String randomPath =
                    com.lms.cloudpan.utils.FileUtil.generatorFileName(file.getOriginalFilename() == null ? file.getName() : file.getOriginalFilename());
            filePath = "avatar/" + randomPath;
            ossClient.putObject(bucketName, filePath, file.getInputStream());

        } catch (IOException e) {
            throw new BusinessException(HttpCode.OPERATION_ERROR, "上传头像失败");
        }

        String fileUrl = com.lms.cloudpan.utils.FileUtil.getFileUrl(ossProperties.getEndpoint(), STATIC_REQUEST_PREFIX, bucketName, filePath);
        this.updateById(User.builder().userId(uid).avatar(fileUrl).build());

        return fileUrl;
    }

    @Override
    public UpdateUserVo getUpdateUserInfo(Integer uid) {
        User userById = this.getOne(new QueryWrapper<User>().eq("user_id", uid).eq("delete_flag", NOT_DELETED));
        BusinessException.throwIf(userById == null, HttpCode.PARAMS_ERROR, "用户不存在或已被删除");
        UserVo userVo = USER_CONVERTER.toUserVo(userById);
        //获取用户的rids  和全部的roles
        List<Integer> rids = userRoleService.list(new QueryWrapper<UserRole>().eq("uid", uid)).stream().map(UserRole::getRid).collect(Collectors.toList());
        userVo.setRids(rids);
        List<Role> roles = roleService.list(new QueryWrapper<Role>());
        List<RoleVo> roleVos = ROLE_CONVERTER.toListRoleVo(roles);
        return UpdateUserVo.builder().userVo(userVo).roles(roleVos).maxQuota(USER_QUOTA).build();
    }


    /**
     * 添加用户并分配角色
     *
     * @param addUserDto
     * @return
     */
    @Override
    @Transactional
    public Integer addUser(AddUserDto addUserDto) {
        User user = new User();
        BeanUtils.copyProperties(addUserDto, user);
        //加密
        String password = user.getPassword();
        String encode = passwordEncoder.encode(password);
        user.setPassword(encode);
        this.save(user);
        List<Integer> rids = addUserDto.getRids();
        //没有角色直接返回
        if(rids==null||rids.size()<1){
            return user.getUserId();
        }
        //判断角色是否存在
        List<Role> ridList = roleService.list(new QueryWrapper<Role>().in("rid", rids).eq("enable",ENABLE));
        if (ridList.size() != rids.size()) {
            throw new BusinessException(HttpCode.PARAMS_ERROR);
        }
        List<UserRole> addRoleList = new ArrayList<>();
        ridList.forEach(role -> {
            addRoleList.add(UserRole.builder().uid(user.getUserId()).rid(role.getRid()).build());
        });
        userRoleService.saveBatch(addRoleList);
        return user.getUserId();
    }

    @Override
    @Transactional
    public Boolean updateUser(UpdateUserDto userDto) {
        Integer userId = userDto.getUserId();
        //不可以修改超级管理员
        BusinessException.throwIfOperationAdmin(userId);
        BusinessException.throwIfNot(MybatisUtils.existCheck(this,Map.of("user_id",userId,
                "delete_flag",NOT_DELETED)));
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        //判断使用配额是否高于修改后用户总容量
        User byId = this.getById(userId);
        BusinessException.throwIf(byId.getUseQuota() > userDto.getQuota());

        this.updateById(user);
        //为用户分配角色
        List<Integer> rids = userDto.getRids();

        if(rids==null||rids.size()<1){
            return true;
        }

        //判断角色是否合法
        BusinessException.throwIfNot(MybatisUtils.checkRids(rids));


        //先获取用户已有的角色
        List<Integer> userRidList = userRoleService.list(new QueryWrapper<UserRole>().eq("uid", userId)).stream().map(UserRole::getRid).collect(Collectors.toList());

        List<Integer> addList = rids.stream()
                .filter(roleId -> !userRidList.contains(roleId)).collect(Collectors.toList());

        List<Integer> deleteList = userRidList.stream()
                .filter(roleId -> !rids.contains(roleId)).collect(Collectors.toList());

        List<UserRole> addRoleList = new ArrayList<>();
        addList.forEach(rid -> {
            addRoleList.add(UserRole.builder().uid(userId).rid(rid).build());
        });

//        List<UserRole> deleteRoleList=new ArrayList<>();
//
//        deleteList.forEach(rid->{
//            deleteRoleList.add(UserRole.builder().uid(userId).rid(rid).build());
//        });

        if (addRoleList.size() > 0) {
            userRoleService.saveBatch(addRoleList);
        }

        if (deleteList.size() > 0) {
            userRoleService.remove(new QueryWrapper<UserRole>()
                    .eq("uid", userId).in("rid", deleteList));
        }
        return true;
    }

    @Override
    public String sendEmail(String email, Integer type) {
        //如果是注册，校验邮箱是否已存在
        if (Objects.equals(type, CommonConstants.ZERO)) {
           BusinessException.throwIf(MybatisUtils.existCheck(this,Map.of("email",email)));
        }
        //随机的邮箱验证码
        String code = StringTools.getRandomNumber(CommonConstants.LENGTH_5);
        sendEmailCode(email, code);
        return code;
    }
    private void sendEmailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //邮件发件人
            helper.setFrom(appConfig.getSendUserName());
            //邮件收件人 1或多个
            helper.setTo(toEmail);

            SysSettingsDto sysSettingsDto = new SysSettingsDto();

            //邮件主题
            helper.setSubject(sysSettingsDto.getRegisterEmailTitle());
            //邮件内容
            helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
            //邮件发送时间
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new BusinessException(HttpCode.OPERATION_ERROR,"邮件发送失败");
        }
    }


    //校验头像图片

    private void validFile(MultipartFile multipartFile) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024 * 10L;
        if (fileSize > ONE_M) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件大小不能超过 10M");
        }
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "文件类型错误");
        }
    }


    private void operationSuperException(Integer id, String errMsg) {
        if (SecurityUtils.isAdmin(id)) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, errMsg);
        }
    }

    private void isUserExist(String userAccount) {
        User userByAccount = getUserByAccount(userAccount);
        if (userByAccount != null) {
            throw new BusinessException(HttpCode.PARAMS_ERROR, "用户已存在！");
        }
    }


}
