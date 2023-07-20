package com.lms.cloudpan.controller;


import com.lms.cloudpan.entity.dto.FolderDto;
import com.lms.cloudpan.entity.vo.FolderVo;
import com.lms.cloudpan.service.IFolderService;
import com.lms.cloudpan.utis.SecurityUtils;
import com.lms.result.EnableResponseAdvice;
import com.lms.result.ResultData;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/folder")
@EnableResponseAdvice
@Api(tags = "文件夹管理")
public class FolderController {

    @Resource
    private IFolderService folderService;


    /**
     * 获取用户当前路径下的文件夹
      * @param path  当前的路径
     * @return
     */
    @PostMapping("/getUserDir")
    public List<FolderVo> getCurrentDir(@RequestParam("path") String path) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return folderService.getUserFolder(path, userId);
    }

    /**
     * 获取目录树
     * @return
     */
    @GetMapping("/getTierDir")
    public List<FolderVo> getTierFolders(){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return folderService.getFolderList(userId);
    }



    /**
     * 新建文件夹
     * @param path  文件夹名
     * @param parentPath 父级文件夹名
     * @return
     */
    @PostMapping("/createPath")
    public Boolean insertFolder(@NotNull@RequestParam("path") String path, @NotNull @RequestParam("parentPath") String parentPath) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return folderService.insertFolder(path,parentPath, userId);
    }


    /**
     * 重命名文件夹
     *
     * @param folderDto
     * @return
     */
    @PostMapping("/rename")
    public Boolean renameFolder(@RequestBody FolderDto folderDto) {
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        return folderService.renameFolder(folderDto.getFolderId(), folderDto.getNewPath(), userId);
    }


    /**
     * 删除文件夹
     * @param folderId
     * @return
     */
    //删除文件夹
    @PostMapping("/delete/{id}")
    public Boolean deleteFolder(@Positive(message = "id不合法")@PathVariable("id") Integer folderId) {
        Integer userId = SecurityUtils.getLoginUser().getUser().getUserId();

        return folderService.deleteFolder(folderId, userId);
    }


    /**
     * 下载文件夹，采用下载链接的方式，下载文件夹的压缩包
     * @param path
     * @return
     */
    @PostMapping("/download")
    public ResultData downloadFolder(@NotNull @RequestParam("path") String path){
        Integer userId = SecurityUtils.getLoginUser().getUserId();
        ResultData success = ResultData.success();
        success.put("data",folderService.downloadFolder(path,userId));
        return success;
    }

}
