package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageSharesDto extends CustomPage implements Serializable {

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 分享文件类型 0文件 1 文件夹
     */
    private Integer shareType;
}
