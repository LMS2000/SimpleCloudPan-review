package com.lms.cloudpan.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lms.cloudpan.entity.dto.PageFileDto;
import com.lms.cloudpan.entity.dto.SearchFileDto;
import com.lms.cloudpan.entity.dto.UploadFileDto;
import com.lms.cloudpan.entity.vo.FileVo;

import com.lms.cloudpan.entity.vo.UploadStatusVo;
import com.lms.cloudpan.service.IFileService;

import com.lms.cloudpan.utils.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.FieldView;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
     * 分片上传文件
     * @param file
     * @param fileName
     * @param filePid
     * @param fileMd5
     * @param chunkIndex
     * @param chunks
     * @param fileId
     * @return
     */
    @PostMapping(value = "/upload")
    @ApiOperation("上传文件")
    public UploadStatusVo insertFile(@RequestBody @NotNull MultipartFile file,
                                     @NotNull @NotBlank @RequestParam("fileName") String fileName,
                                     @NotNull @NotBlank @RequestParam("filePid") String filePid,
                                     @NotNull @NotBlank  @RequestParam("fileMd5") String fileMd5,
                                     @NotNull @RequestParam("chunkIndex") Integer chunkIndex,
                                     @NotNull @RequestParam("chunks") Integer chunks,
                                     @RequestParam(value = "fileId",required = false)  String fileId) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();

        UploadFileDto uploadFileDto = UploadFileDto.builder().fileMd5(fileMd5).filePid(filePid).fileId(fileId).chunks(chunks)
                .chunkIndex(chunkIndex).fileName(fileName).build();
        return fileService.insertFile(file,uploadFileDto,userId);
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
     * @param pageFileDto 当前的路径
     * @return
     */
    @PostMapping("/getFiles")
    @ApiOperation("获取当前路径下的全部文件")
    public Page<FileVo> getFilesByPath(@Validated @RequestBody PageFileDto pageFileDto) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        //判断path是否以/或者\\开头
        return fileService.getUserFileByPath(pageFileDto, userId);
    }
    @GetMapping("/getPrev/{fileId}")
    @ApiOperation("获取上一级目录id")
    public String getPrevFiles(@PathVariable String fileId){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.getPreFiles(fileId,userId);
    }

    /**
     * 重命名文件（没有修改实际存储磁盘的文件名）
     *
     * @param id 文件id
     * @param fileName  新的文件名
     * @return
     */
    @PostMapping("/rename/{id}/{name}")
    @ApiOperation("重命名文件")
    public Boolean renameFile( @PathVariable("id") String id, @NotNull @PathVariable("name") String fileName) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.renameFile(id, fileName, userId);
    }

    /**
     * 根据文件名模糊查询
     *
     * @param searchFileDto
     * @return
     */
    @PostMapping("/search")
    public Page<FileVo> searchFileByName(@RequestBody SearchFileDto searchFileDto) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.searchFile(searchFileDto, userId);
    }


    /**
     * 将选中文件的以及子文件变成回收状态
     *
     * @param ids
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation("将选中文件的以及子文件变成回收状态")
    public Boolean deleteFiles( @RequestParam("ids") List<String> ids) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();
        return fileService.deleteFiles(ids, userId);
    }






    /**
     * 将多个文件移动到同一文件夹下
     *
     * @param fileIds
     * @param pid
     * @return
     */
    @PostMapping("/move")
    @ApiOperation("将多个文件移动到同一文件夹下")
    public Boolean moveFiles(@RequestParam("ids") List<String> fileIds, @RequestParam("pid") String pid) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.moveFiles(fileIds, pid, userId);
    }


    /**
     * 下载文件
     * @param code  为下载标识，一串随机字符串
     *       便于分割获取实际的地址
     * @return
     */
    @GetMapping("/download/{code}")
    @ApiOperation("下载文件")
    public Boolean downloadFile(HttpServletRequest request, HttpServletResponse response,@PathVariable("code") String code) {
       return fileService.download(request,response,code);
    }


    /**
     * 获取下载链接
     * @param fileId
     * @return
     */
    @GetMapping("/download/create/{fileId}")
    @ApiOperation("获取下载链接")
    public String getDownloadUrl(@PathVariable("fileId") String fileId){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.createDownloadUrl(fileId,userId);
    }

    /**
     * 创建文件夹
     * @param folderName
     * @param pid
     * @return
     */
    @PostMapping("/create/folder/{pid}/{folderName}")
    @ApiOperation("创建文件夹")
    public Boolean createFolder(@PathVariable("folderName") String folderName,
                                @PathVariable("pid") String pid){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.createFolder(userId,pid,folderName);
    }


    /**
     * 获取文件夹
     * @return
     */
    @GetMapping("/getFolderTree/{pid}/{fileId}")
    @ApiOperation("获取文件夹")
    public List<FileVo> getFolderInfo(@PathVariable("pid") String pid,@PathVariable("fileId") String fileId){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return fileService.getFolderInfo(pid,fileId,userId);
    }
}
