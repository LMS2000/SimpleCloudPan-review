package com.lms.cloudpan.task;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.service.IFileService;
import com.lms.cloudpan.service.IFolderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

//定期删除有删除标记的文件或者文件夹
@Component
@Slf4j
public class DeleteFilesTask {


    @Scheduled(cron = "0 0/30 * * * ?") // cron表达式:每三十分钟执行一次
    public void TaskForDeleteFiles(){
        //获取全部被标记的文件记录
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        OssProperties ossProperties = SpringUtil.getBean(OssProperties.class);
        IFolderService folderService = SpringUtil.getBean(IFolderService.class);

        //获取未删除的文件信息fingerPrint字段
        List<String> fingerPrints = fileService.list(new QueryWrapper<File>().eq("delete_flag", 0))
                .stream().map(File::getFingerPrint).collect(Collectors.toList());




        List<File> deleteFlag = fileService.list(new QueryWrapper<File>().eq("delete_flag", 1));
        //删除实际文件
        deleteFlag.forEach(file -> {


            //如果其他用户还在使用就不需要删除
            long count = fingerPrints.stream().filter(fingerPrint -> file.getFingerPrint().equals(fingerPrint))
                    .count();
            //只有没有其他人使用这个文件的时候才删除实际文件
            if(count<=0){
                //http://localhost:9998/pan/static/bucket_user_11/2023/05/22/f10a9aeb-b76f-4777-bc4c-1001e6e3b4c3.png
                String[] split=file.getFileUrl().split(FileConstants.STATIC_REQUEST_PREFIX);
                String realPath=ossProperties.getRootPath()+split[1];
                Path path = Paths.get(realPath);
                if(Files.exists(path)){
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            fileService.removeById(file.getFileId());
        });
        //删除文件夹记录
        List<Folder> deleteFolders = folderService.list(new QueryWrapper<Folder>().eq("delete_flag", 1));
        deleteFolders.forEach(folder -> {
            folderService.removeById(folder.getFolderId());
        });
    }
}
