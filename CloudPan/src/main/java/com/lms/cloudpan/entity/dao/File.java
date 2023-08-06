package com.lms.cloudpan.entity.dao;

import com.baomidou.mybatisplus.annotation.FieldFill;
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


    /*
     文件主键id
     */
    @TableId(value = "file_id")
    private  String fileId;

    /*
    文件名
     */

    private String fileName;

    /*
     文件路径
     */

    private String filePath;

    /*
    文件大小
     */
    private Long size;

    /*
    所属用户id
     */

    private Integer userId;

    /*
    删除标记  0 为正常 1 为删除 2 为回收站
     */

    private Integer deleteFlag;

    /*
    文件类型
     */

    private Integer fileCategory;

    /**
     * 1:视频 2:音频  3:图片 4:pdf 5:doc 6:excel 7:txt 8:code 9:zip 10:其他
     */
    private Integer fileType;

    /*
        文件夹类型  0为文件  1为文件夹
     */
    private Integer folderType;

    /*
      文件的md5值
     */
    private String fileMd5;

    /*
     父级id
     */
    private String pid;
    /*
    文件合并状态 0为转码中，1为转码失败，2为正常使用
     */

    private Integer fileStatus;






    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
