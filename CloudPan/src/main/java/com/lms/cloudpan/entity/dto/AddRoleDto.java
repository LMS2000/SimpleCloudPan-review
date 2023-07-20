package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AddRoleDto  implements Serializable {
    private static final long serialVersionUID = 1L;


    @NotNull(message = "角色名不能为空")
    @NotBlank(message = "角色名不能为空")
    private String roleName;
    @NotNull(message = "角色描述不能为空")
    @NotBlank(message = "角色描述不能为空")
    private String description;

    @NotNull(message = "enable不能为空")
    @RangeCheck(range = {0,1})
    private Integer enable;
    @NotNull(message = "分配权限不能为空")
    private List<Integer> authorities;





}
