package com.lms.cloudpan.constants;

import io.swagger.models.auth.In;

public interface ShareConstants {

    /**
     * 分享map
     */
    String SHARE_MAP="share_map:";


    /**
     * 分享资源当前下载次数
     */
    String SHARE_NUM_KEY="share_num_key";

    /**
     * 最大下载次数
     */
    String SHARE_MAX_NUM_KEY="share_max_num_key";

    /**
     * 分享码
     */
    String SHARE_SECRET_KEY="share_secret_key";

    /**
     *分享链接
     */
    String SHARE_LINK_HEADER="/pan/share/";

    Integer FILE_TYPE=0;
    Integer FOLDER_TYPE=1;



    String SHARED_FILEIDS= "share_file_id:";


    String SESSION_SHARE_KEY = "session_share_key_";





}
