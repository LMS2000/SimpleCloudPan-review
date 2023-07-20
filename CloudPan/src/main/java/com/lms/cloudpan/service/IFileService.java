package com.lms.cloudpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dto.FileDto;
import com.lms.cloudpan.entity.vo.FileVo;


import java.util.List;

public interface IFileService extends IService<File> {

    //根据用户的id和获取指定路径下用户的所属文件
    List<FileVo> getUserFileByPath(String path, Integer uid);

    String insertFile(FileDto fileDto, Integer uid, String fingerPrint);

    boolean renameFile(Integer id, String newName, Integer uid);

    List<FileVo> searchFile(String fileName, Integer uid);

    boolean deleteFiles(List<Integer> ids, Integer uid);

    Boolean moveFiles(List<Integer> ids, String path, Integer uid);

    byte[] downloadFile(String url);

    Boolean checkUpload(String taskId);
}
