package com.lms.cloudpan.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.lms.cloudpan.config.OssProperties;
import com.lms.cloudpan.utis.FileUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
//本地文件存储服务

@Component
public class LocalClient implements OssClient{

    @Resource
    private  OssProperties ossProperties;
    @Override
    public void createBucket(String bucketName) {
        FileUtil.createDir(ossProperties.getRootPath(),bucketName);
    }

    /**
     * 上传文件
     * @param bucketName
     * @param objectName
     * @param stream
     * @param size
     * @param contextType
     * @return
     * @throws IOException
     */
    @Override
    public PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size, String contextType) throws IOException {
          //创建服务端存储的相对路径
          String fileName = FileUtil.pathMerge(ossProperties.getRootPath(),bucketName,objectName);
//          log.info("写入文件位置为{}",fileName);
          //存储文件（写入）
          FileUtil.transFileTo(stream,fileName);
          return null;
    }

    /**
     * 删除文件
     * @param bucketName
     * @param objectName
     */
    @Override
    public void deleteObject(String bucketName, String objectName) {
        String fileName= FileUtil.pathMerge(ossProperties.getRootPath(),bucketName,objectName);
        FileUtil.deleteFile(fileName);
    }

    @Override
    public void createBucket(String bucketName, CannedAccessControlList bucketAccess) {

    }

    @Override
    public void deleteBucket(String bucketName) {

    }

    @Override
    public String getObjectURL(String bucketName, String objectName) {
        return null;
    }

    @Override
    public S3Object getObjectInfo(String bucketName, String objectName) {
        return null;
    }

    @Override
    public AmazonS3 getS3Client() {
        return null;
    }
}
