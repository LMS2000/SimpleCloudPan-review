package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateRoleDto implements Serializable {
    private static final long serialVersionUID = 1L;


    @NotNull(message = "id不能为空")
    @Positive(message = "id不合法")
    private Integer rid;




    private String roleName;

    private String description;

    @RangeCheck(range = {0,1})
    private Integer enable;


    private List<Integer> authorities;


}
