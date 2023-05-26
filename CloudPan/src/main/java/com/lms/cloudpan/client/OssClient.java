package com.lms.cloudpan.client;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.lms.cloudpan.config.ContentNegotiateManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Oss 基础操作
 * 想要更复杂操作可以直接获取AmazonS3，通过AmazonS3 来进行复杂的操作
 * https://docs.aws.amazon.com/zh_cn/sdk-for-java/v1/developer-guide/examples-s3-buckets.html
 * @author zdh
 */
public interface OssClient{
    /**
     * 创建bucket
     * @param bucketName
     */
    void createBucket(String bucketName);

    /**
     * 创建存储桶,同时指定存储桶读写权限
     */
    void createBucket(String bucketName, CannedAccessControlList bucketAccess);

    void deleteBucket(String bucketName);

    /**
     * 获取url
     * @param bucketName
     * @param objectName
     * @return
     */
    String getObjectURL(String bucketName, String objectName);


    /**
     * 获取存储对象信息
     * @param bucketName
     * @param objectName
     * @return
     */
    S3Object getObjectInfo(String bucketName, String objectName);


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
    PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size, String contextType) throws IOException;

    default PutObjectResult putObject(String bucketName, String objectName, InputStream stream) throws IOException{
        return putObject(bucketName,objectName,stream, stream.available(), ContentNegotiateManager.getMimeType(objectName));
    }

    void deleteObject(String bucketName, String objectName);

    AmazonS3 getS3Client();
}

