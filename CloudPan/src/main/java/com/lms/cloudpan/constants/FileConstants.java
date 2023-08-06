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
    //文件fingerprint指纹，目前是指前端传入的文件md5字符串
    String FINGER_PRINT="finger_print:";


    //文件的三个状态，0为在使用，1为删除，2为回收站
    Integer USING =0;
    Integer DELETED=1;
    Integer RECYCLE=2;


    Integer FILE_TYPE=0;
    Integer FOLDER_TYPE=1;

    String FILE_FOLDER_PATH="/file/";
    String FILE_FOLDER_TEMP = "/temp/";

    String DOWNLOAD_URL_HEADER="download:";
    String REDIS_KEY_USER_FILE_TEMP_SIZE = "easypan:user:file:temp:";

}
