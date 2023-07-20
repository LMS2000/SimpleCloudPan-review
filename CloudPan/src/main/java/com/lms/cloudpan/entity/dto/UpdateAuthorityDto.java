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
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateAuthorityDto  implements Serializable {

    private static final long serialVersionUID = 1L;

    @Positive(message = "aid不合法")
    @NotNull(message = "aid不能为空")
    private Integer aid;

    /**
     * 父级id
     */
    private Integer parentId;


    private String icon;

    private String path;


    private String component;

    @RangeCheck(range = {0,1})
    private Integer visible;


    /**
     * 启用
     */
    @RangeCheck(range = {0,1})
    private Integer enable;



    private String authType;
    /**
     * 权限名
     */

    private String name;


    private String perms;
}
