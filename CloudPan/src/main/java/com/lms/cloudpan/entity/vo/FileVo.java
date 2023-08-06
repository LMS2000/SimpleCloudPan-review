package com.lms.cloudpan.entity.vo;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class FileVo implements Serializable {

    private static final long serialVersionUID = 1L;


    /*
     文件主键id
     */
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
    private Integer fileType;

    /*
        文件夹类型  0为文件  1为文件夹
     */
    private Integer folderType;

    /*
      文件的md5值
     */
    private String fileMd5;

    private Integer fileCategory;

    /*
     父级id
     */
    private String pid;



    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

}
