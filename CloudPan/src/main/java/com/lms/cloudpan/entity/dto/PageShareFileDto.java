package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class PageShareFileDto extends CustomPage implements Serializable {



    @NotNull(message = "分享id不能为空")
    @NotBlank(message = "分享id不能为空")
    private String shareId;
    private String pid;

}
