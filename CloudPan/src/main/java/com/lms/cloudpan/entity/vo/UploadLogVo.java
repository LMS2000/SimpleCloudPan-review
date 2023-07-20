package com.lms.cloudpan.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UploadLogVo implements Serializable {
    private static final long serialVersionUID = 1L;


    private  Integer id;

    private String bucketName;

    private String fileName;

    private String fileMd5;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
