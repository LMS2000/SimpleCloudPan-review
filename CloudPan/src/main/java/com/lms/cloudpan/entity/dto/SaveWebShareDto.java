package com.lms.cloudpan.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class SaveWebShareDto implements Serializable {


    /**
     * 分享id
     */
    String shareId;

    /**
     * 需要保存分享的文件id列表
     */
    @NotNull
    List<String> shareFileIds;

    /**
     * 保存到的路径id
     */
    String myFolderId;
}
