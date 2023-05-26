package com.lms.cloudpan.constants;

/**
 * @author LMS2000
 * @create 2023/4/22 21:22
 */
public interface FileConstants {

    /**
     * 静态资源的请求前缀
     */
    String STATIC_REQUEST_PREFIX_PATTERN="/static/**";
    String STATIC_REQUEST_PREFIX="static";
    /**
     * 异步任务缓存头
     */
    String FILE_UPLOAD_TASK="file_upload_task:";

    //文件上传失败
    Integer FILE_UPLOAD_FAILURE=1;
    //文件上传成功
    Integer FILE_UPLOAD_SUCCESS=2;
    //文件上传中
    Integer FILE_UPLOAD_RUNNING=0;


}
