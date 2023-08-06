package com.lms.cloudpan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UploadFileDto implements Serializable {


    /**
     * 文件名
     */
    @NotNull(message = "文件名不能为空")
    @NotBlank(message = "文件名不能为空")
    String fileName;

    /**
     * 路径id
     */
    @NotNull(message = "目录id不能为空")
    @NotBlank(message = "目录id不能为空")
    String filePid;
    /**
     * 文件md5值
     */
    @NotNull(message = "文件md5值不能为空")
    @NotBlank(message = "文件md5值不能为空")
    String fileMd5;
    /**
     * 当前分片
     */
    @NotNull
    Integer chunkIndex;
    /**
     * 总分片数
     */
    @NotNull
    Integer chunks;
    /**
     * 文件id
     */


    String fileId;


}
