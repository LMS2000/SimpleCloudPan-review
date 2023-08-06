package com.lms.cloudpan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    @NotNull(message = "用户名不能为空")
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    @NotBlank(message = "密码不能为空")
    private String password;



//    @NotNull(message = "校验码不能为空")
//    @NotBlank(message = "校验码不能为空")
//    private String code;
    @NotNull(message = "邮箱验证码不能为空")
    @NotBlank(message = "邮箱验证码不能为空")
    private String emailCode;


    @Email(message = "邮箱格式不正确")
    private String email;

    private  Integer enable;

    private Long useQuota;
    private Long quota;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
