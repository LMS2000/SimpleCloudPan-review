package com.lms.cloudpan.utis;



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


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
}
