package com.lms.cloudpan.entity.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class FolderVo {
//    private String path;
//    private String  parentPath;
//

    @Positive(message = "id不合法")
    private Integer folderId;
    @NotNull(message = "重命名文件夹不能为空")
    @NotBlank(message = "重命名文件夹不能为空")
    private String newPath;


}
