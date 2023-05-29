package com.lms.cloudpan.entity.dao;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class File {



    @TableId(value = "file_id", type = IdType.AUTO)
    private  Integer fileId;

    private String fileName;

    private String fileUrl;

    private Long size;
    private Integer userId;
    private Integer deleteFlag;

    private String fileType;

    private String fingerPrint;

    private Integer folderId;
    private String shareLink;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
