package com.lms.cloudpan.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dto.PageFileDto;
import com.lms.cloudpan.entity.dto.RecyclePageDto;
import com.lms.cloudpan.entity.dto.SearchFileDto;
import com.lms.cloudpan.entity.dto.UploadFileDto;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.entity.vo.UploadStatusVo;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface IFileService extends IService<File> {

    //根据用户的id和获取指定路径下用户的所属文件
    Page<FileVo> getUserFileByPath(PageFileDto pageFileDto, Integer uid);

    UploadStatusVo insertFile(MultipartFile file, UploadFileDto uploadFileDto, Integer uid);

    boolean renameFile(String id, String newName, Integer uid);

//    List<FileVo> searchFile(String fileName, Integer uid);

    //删除文件，将所有的选中的文件及其子文件都标记删除
    boolean deleteFiles(List<String> ids, Integer uid);


    //标记回收
    boolean deleteRecycleFiles(List<String> ids,Integer uid);

    /**
     * 恢复文件
     * @param ids
     * @param uid
     * @return
     */

    boolean recoverFileBatch(List<String> ids,Integer uid);

    /**
     * 移动文件到文件夹下
     * @param ids
     * @param pid
     * @param uid
     * @return
     */
    Boolean moveFiles(List<String> ids, String pid, Integer uid);

    byte[] downloadFile(String url);

    Boolean checkUpload(String taskId);


    /**
     * 创建文件夹
     * @param uid
     * @param pid
     * @param folderName
     * @return
     */
    Boolean createFolder(Integer uid,String pid,String folderName);

    /**
     * 下载文件
     * @param request
     * @param response
     * @param code
     * @return
     */
    boolean download(HttpServletRequest request, HttpServletResponse response, String code);

     String createDownloadUrl(String fileId,Integer uid);

//     void transferFile(String fileId, User user);

    //获取上一级目录
    String getPreFiles( String fileId, Integer uid);



    List<FileVo> getFolderInfo(String folderId, String pid,Integer uid);

    Page<FileVo> pageRecycleFiles(RecyclePageDto recyclePageDto,Integer uid);

    Page<FileVo> searchFile(SearchFileDto searchFileDto,Integer uid);

    List<FileVo> getSaveFolder(String pid , Integer uid);
}
