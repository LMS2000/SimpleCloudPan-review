package com.lms.cloudpan.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SharesVo implements Serializable {
    /**
     *  主键
     */
    private String shareId;

    /**
     * 分享的类型  0 为文件 1为文件夹
     */
    private Integer shareType;


    private String fileId;

    private FileVo fileVo;


    /**
     * 分享的用户id
     */
    private Integer  shareUser;


    private UserVo sharer;


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
