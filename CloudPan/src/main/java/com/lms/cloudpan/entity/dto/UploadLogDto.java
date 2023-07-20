package com.lms.cloudpan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UploadLogDto {

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
