package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class SendEmailDto implements Serializable {

    @Email(message = "邮箱格式不正确")
    @NotNull(message = "邮箱不能为空")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @RangeCheck(range = {0,1})
    @NotNull(message = "邮件类型不能为空")
    private Integer type;
    //图片校验码
    @NotNull(message = "图片校验码不能为空")
    @NotBlank(message = "图片校验码不能为空")
    private String code;
}
