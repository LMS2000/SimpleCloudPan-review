package com.lms.cloudpan.utils;



import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lms.cloudpan.service.IFileService;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j

public class FileUtil {

    /**
     * UUID替换原来的文件名
     */
    public static String generatorFileName(String fileName) {
        return UUID.randomUUID() + fileName.substring(fileName.lastIndexOf("."));
    }

    public static String getFileUrl(String... paths) {
        return String.join("/", paths);
    }

    public static void createDir(String rootPath, String bucketName) {
        try {
            Files.createDirectory(Path.of(rootPath, bucketName));
        } catch (IOException e) {
//            log.error("创建目录过程中出现错误: ", e);
        }
    }




    public static String pathMerge(String rootPath, String bucketName, String objectName) {
         if(objectName.startsWith("/")||objectName.startsWith("\\")){
             objectName=objectName.substring(1);
         }
    return String.join(File.separator,rootPath,bucketName,objectName);
    }
        public static void transFileTo(InputStream stream, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try(BufferedInputStream bufferedInputStream=new BufferedInputStream(stream)){
            PrintStream printStream=new PrintStream(file);
            byte[] buffer=new byte[1024];
            int len =-1;
            while ((len=(bufferedInputStream.read(buffer)))!=-1){
                printStream.write(buffer,0,len);
            }
//            log.info("写入文件成功，文件位置{}",filePath);
            printStream.flush();
        }

    }

    public static void createDir(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
    public static void deleteFile(String filePath){
        try{
            cn.hutool.core.io.FileUtil.del(filePath);
        }catch (RuntimeException e){
//            log.error("删除文件失败.文件名 {};异常信息：{}",filePath,e);
        }

    }



    public static void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }


    //获取子文件
    public static List<com.lms.cloudpan.entity.dao.File> getSubFile(String id,Integer status){
        IFileService fileService = SpringUtil.getBean(IFileService.class);
        List<com.lms.cloudpan.entity.dao.File> subIds=new ArrayList<>();
        searchSubFiles(subIds,id,fileService,status);
        return subIds;
    }

    private static void searchSubFiles(List<com.lms.cloudpan.entity.dao.File> subIds, String id, IFileService fileService,Integer status){
        List<com.lms.cloudpan.entity.dao.File> list = fileService.list(new QueryWrapper<com.lms.cloudpan.entity.dao.File>()
                .eq("pid", id).eq("delete_flag", status)
                .select("file_id","size"));
        if(list.size()<1)return;
        subIds.addAll(list);
        list.forEach(file->{
            searchSubFiles(subIds,file.getFileId(),fileService,status);
        });
    }
}
