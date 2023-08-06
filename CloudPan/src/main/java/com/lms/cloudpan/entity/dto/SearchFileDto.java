package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchFileDto extends CustomPage implements Serializable {

    private String fileName;

    /**
     * 文件类型
     */
    private Integer fileCategory;


}
