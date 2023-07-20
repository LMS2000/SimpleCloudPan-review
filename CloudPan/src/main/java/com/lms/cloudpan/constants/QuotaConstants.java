package com.lms.cloudpan.constants;

public interface QuotaConstants {

    /**
     * 空配额
     */
    Long EMTRY_QUOTA=0L;

    /**
     * 用户最大配额,默认10G
     */
    Long USER_QUOTA=10L*1024L*1024L*1024L;
}
