package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import com.lms.page.CustomPage;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
@Data
public class PageFileDto extends CustomPage implements Serializable {

    //父级组件id
    @NotNull(message = "目录id不能为空")
    @NotBlank(message = "目录id不能为空")
    private String pid;





}
