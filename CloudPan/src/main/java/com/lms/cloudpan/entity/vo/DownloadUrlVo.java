package com.lms.cloudpan.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownloadUrlVo implements Serializable {


    private String fileId;
    private String filePath;
    private String fileName;
    private String downloadCode;


}
