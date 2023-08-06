package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class ShareDto implements Serializable {
    private static final long serialVersionUID = 1L;


    @NotNull
    @NotBlank
    private String fileId;

    /**
     * 0为文件，1为文件夹
     */
    @NotNull
    @RangeCheck(range = {0,1})
    private Integer shareType;



    /**
     * 用户设置的分享码, 5位的分享码
     */
    private String shareKey;

    /**
     * 下载次数
     */
    private Integer downloadCount;
//    private LocalDateTime shareTime;

    /**
     * 分享时间有效期类型 0:1天 1:7天 2:30天 3:永久有效
     */
    @NotNull
    @RangeCheck(range = {0,1,2,3})
    private Integer validType;




}
