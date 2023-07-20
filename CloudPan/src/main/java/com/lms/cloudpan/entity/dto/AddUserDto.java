package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddUserDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "账号不能为空")
    @NotBlank(message = "账号不能为空")
    private String username;
    @NotNull(message = "账号不能为空")
    @NotBlank(message = "账号不能为空")
    private String password;


    private List<Integer> rids;
   @Email(message = "邮箱格式错误")
    private String email;
    @Max(value = 10737418240L)
    private Long quota;
    @RangeCheck(range = {0,1})
    private Integer enable;

    @Length(max = 255)
    private String remark;

}
