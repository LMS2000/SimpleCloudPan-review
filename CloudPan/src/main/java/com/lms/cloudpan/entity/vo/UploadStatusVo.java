package com.lms.cloudpan.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
@Data
@Builder
public class UploadStatusVo implements Serializable {

    private String fileId;

    private String status;






}
