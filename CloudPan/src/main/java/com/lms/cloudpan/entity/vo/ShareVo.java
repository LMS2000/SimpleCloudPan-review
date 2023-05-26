package com.lms.cloudpan.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class ShareVo implements Serializable {
    private static final long serialVersionUID = 1L;


    private Integer sharedId;

    /**
     * 0为文件，1为文件夹
     */
    private Integer shareType;

    /**
     * 用户设置的分享码
     */
    private String shareSecretKey;

    /**
     * 下载次数
     */
    private Integer downloadCount;
//    private LocalDateTime shareTime;

    /**
     * 过期时间，单位为天数
     */
    private Integer expiration;


}
