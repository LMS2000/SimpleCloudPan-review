package com.lms.cloudpan.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Shares implements Serializable {

    private static final long serialVersionUID = 1L;



    /**
     *  主键
     */
    @TableId(value = "share_id", type = IdType.ASSIGN_UUID)
    private String shareId;


    private String fileId;

    /**
     * 分享的类型  0 为文件 1为文件夹
     */
    private Integer shareType;


    /**
     * 分享时间有效期类型 0:1天 1:7天 2:30天 3:永久有效
     */
    private Integer validType;


    /**
     * 分享的用户id
     */
    private Integer  shareUser;


    /**
     * 分享码
     */
    private String shareKey;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 分享时间
     */
    private Date shareTime;

    /**
     * 过期时间
     */
    private Date expirationDate;



}
