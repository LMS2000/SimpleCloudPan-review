package com.lms.cloudpan.controller;


import com.infrastructure.jwt.JwtUser;

import com.infrastructure.validator.MultipartFileNotEmptyCheck;
import com.infrastructure.validator.NotEmptyCheck;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.FileDto;
import com.lms.cloudpan.entity.vo.FileVo;

import com.lms.cloudpan.service.IFileService;

import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
@EnableResponseAdvice
@Validated
public class FileController {


    @Resource
    private IFileService fileService;




    /**
     * 上传文件到指定的路径下
     *
     * @param file 上传的文件
     * @param path 上传的路径
     * @return
     */
    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("上传文件")
    public String insertFile(@RequestBody @NotNull MultipartFile file, @NotNull @ApiParam("指定上传路径")  @RequestParam("path")  String path,
                        @NotNull   @RequestParam("fingerPrint")  String fingerPrint) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        FileVo fileVo = new FileVo();
        fileVo.setFile(file);
        fileVo.setFolderPath(path);
        return fileService.insertFile(fileVo, userId,fingerPrint);
    }

    /**
     * 查看文件是否上传成功
     * @param taskId  异步任务id
     * @return
     */
    @GetMapping("/check/{taskId}")
    public Boolean checkFileUpload(@PathVariable String taskId){
        return fileService.checkUpload(taskId);
    }

    /**
     * 获取当前用户的指定路径下的全部文件
     *
     * @param path 当前的路径
     * @return
     */
    @PostMapping("/getFiles")
    @ApiOperation("获取当前路径下的全部文件")
    public List<FileDto> getFilesByPath(@NotNull @RequestParam("path") String path) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        //判断path是否以/或者\\开头
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return fileService.getUserFileByPath(path, userId);
    }

    /**
     * 重命名文件（没有修改实际存储磁盘的文件名）
     *
     * @param id 文件id
     * @param fileName  新的文件名
     * @return
     */
    @PostMapping("/rename/{id}/{name}")
    public Boolean renameFile(@Positive(message = "id不合法") @PathVariable("id") Integer id, @NotNull @PathVariable("name") String fileName) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.renameFile(id, fileName, userId);
    }

    /**
     * 根据文件名模糊查询
     *
     * @param fileName
     * @return
     */
    @GetMapping("/search/{fileName}")
    public List<FileDto> searchFileByName(@NotNull @PathVariable("fileName") String fileName) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.searchFile(fileName, userId);
    }


    /**
     * 删除多个文件
     *
     * @param ids
     * @return
     */
    @PostMapping("/delete")
    public Boolean deleteFiles( @RequestParam("ids") List<Integer> ids) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return fileService.deleteFiles(ids, userId);
    }


    //多选文件移动

    /**
     * 将多个文件移动到同一文件夹下
     *
     * @param fileIds
     * @param path
     * @return
     */
    @PostMapping("/move")
    public Boolean moveFiles(@RequestParam("ids") List<Integer> fileIds, @RequestParam("path") String path) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.moveFiles(fileIds, path, userId);
    }


    /**
     * 下载文件
     * @param url  文件路径 如 http://localhodst:9998/static/bucket_user_11/demo.txt
     *       便于分割获取实际的地址
     * @return
     */
    @PostMapping("/download")
    public byte[] downloadFile(@NotNull @RequestParam("url") String url) {
       return fileService.downloadFile(url);

    }
}
