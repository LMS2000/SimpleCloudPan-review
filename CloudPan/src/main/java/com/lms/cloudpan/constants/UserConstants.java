package com.lms.cloudpan.constants;

import io.swagger.models.auth.In;

/**
 * 用户相关常量信息
 * @author LMS2000
 * @create 2023/6/20 11:26
 */
public interface UserConstants {
      Integer SUPER_MANAGER_SIGN=1;
      //角色标识
      String  ADMIN="admin";
      String  USER="user";
      //超级管理员id，只有一个超级管理员，默认全部权限，不可被操作修改
      Integer ADMIN_UID=1;
      //禁用标记
      Integer DISABLE=1;
      Integer ENABLE=0;

      //删除标记
      Integer DELETED=1;
      Integer NOT_DELETED=0;
      //用户初始密码
      String  INITPASSWORD="12345678";

       String CHECK_CODE_KEY = "check_code_key";
       String CHECK_CODE_KEY_EMAIL = "check_code_key_email";

       // 邮箱验证码前缀

      String EMAIIL_HEADER="check_email_code:";

}
