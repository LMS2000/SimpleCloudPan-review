package com.lms.cloudpan.utis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.extra.spring.SpringUtil;
import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.vo.UploadLogVo;
import com.lms.cloudpan.service.IUploadLogService;

import com.lms.redis.RedisCache;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//用于文件回滚
public class FileSafeUploadUtil {


    //上传文件
    public static UploadLog doUpload(InputStream inputStream, String bucket, String dir) throws IOException {
        IUploadLogService uploadLogService = SpringUtil.getBean(IUploadLogService.class);
        UploadLog uploadLog = UploadLog.builder().bucketName(bucket).fileName(dir).build();
        //记录日志
        uploadLogService.save(uploadLog);
        //上传文件
        OssClient ossClient = SpringUtil.getBean(OssClient.class);

        ossClient.putObject(bucket, dir, inputStream);
        return uploadLog;
    }

    //回滚文件
    public static void deleteFile(UploadLog uploadLog) {
        IUploadLogService uploadLogService = SpringUtil.getBean(IUploadLogService.class);
        OssClient ossClient = SpringUtil.getBean(OssClient.class);
        //删文件
        ossClient.deleteObject(uploadLog.getBucketName(), uploadLog.getFileName());

        //删除文件日志记录
        uploadLogService.removeById(uploadLog.getId());
    }

    //验证MD5
    public static String checkMd5String(String md5String) {
        String key = FileConstants.FINGER_PRINT + md5String;

        RedisCache redisCache = SpringUtil.getBean(RedisCache.class);

        String md5FileUrl = redisCache.getCacheObject(key);
        //确定这个文件没有存储在系统中
        return md5FileUrl;
    }


    //设置MD5字段缓存，value为文件的url下载地址
    public static  void setMd5String(String md5String,String url){
        String key = FileConstants.FINGER_PRINT + md5String;
        RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
        redisCache.setCacheObject(key,url);
    }


}
