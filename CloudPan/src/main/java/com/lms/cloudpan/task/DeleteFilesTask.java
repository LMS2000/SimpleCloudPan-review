package com.lms.cloudpan.task;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.service.IFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

//定期删除有删除标记的文件或者文件夹
@Component
@Slf4j
public class DeleteFilesTask {




    @Scheduled(cron = "0 0/30 * * * ?") // cron表达式:每三十分钟执行一次
    public void TaskForDeleteFiles(){
        //查找有删除标记的文件，物理删除
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        OssProperties ossProperties = SpringUtil.getBean(OssProperties.class);
        List<File> deleteList = fileService.list(new QueryWrapper<File>().eq("delete_flag", FileConstants.DELETED)
                .eq("folder_type",FileConstants.FILE_TYPE));
        deleteList.forEach(file -> {
            String realPath=ossProperties.getRootPath()+file.getFilePath();
            try {
                Files.delete(Path.of(realPath));
            } catch (IOException e) {
                log.error("删除文件失败，文件路径为{}",realPath);
            }
        });

    }
}
