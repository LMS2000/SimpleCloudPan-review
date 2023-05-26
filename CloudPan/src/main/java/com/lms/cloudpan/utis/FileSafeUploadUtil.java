package com.lms.cloudpan.utis;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.extra.spring.SpringUtil;
import com.lms.cloudpan.client.OssClient;
import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.vo.UploadLogVo;
import com.lms.cloudpan.service.IUploadLogService;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

//用于文件回滚
public class FileSafeUploadUtil {

    /**
     *
     * @param file 上传的文件
     * @param bucket 上传到的文件桶
     * @param dir 实际存储的路径
     * @return
     */
//    public static UploadLogVo uploadFile(MultipartFile file, List<Integer> uploadFileLogRecordList, String bucket, String dir) {
//        return doUpload(file, uploadFileLogRecordList, bucket, dir);
//    }


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

    public static void deleteFile(UploadLog uploadLog) {
        IUploadLogService uploadLogService = SpringUtil.getBean(IUploadLogService.class);
        OssClient ossClient = SpringUtil.getBean(OssClient.class);
          //删文件
          ossClient.deleteObject(uploadLog.getBucketName(),uploadLog.getFileName());

        //删除文件日志记录
        uploadLogService.removeById(uploadLog.getId());
    }

    public static void deleteLogRecord(List<Integer> uploadFileLogRecordList) {
//        ICourserUploadLogService iCourserUploadLogService = SpringUtil.getBean(ICourserUploadLogService.class);
//        iCourserUploadLogService.removeBatchByIds(uploadFileLogRecordList);
    }

//    public static UploadLogVo uploadZipFile(MultipartFile file, List<Integer> uploadFileLogRecordList, String bucket, String dir) {
//        return doUpload(file, uploadFileLogRecordList, bucket, dir, true);
//    }
}
