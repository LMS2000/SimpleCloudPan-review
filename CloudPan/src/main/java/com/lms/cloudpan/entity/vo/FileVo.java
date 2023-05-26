package com.lms.cloudpan.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.infrastructure.validator.MultipartFileNotEmptyCheck;
import com.lms.cloudpan.constants.FileConstants;
import com.lms.redis.RedisCache;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

import static com.lms.cloudpan.constants.FileConstants.FILE_UPLOAD_TASK;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class FileVo {




    private String folderPath;



    private MultipartFile file;

    public static void changeUploadState(RedisCache redisCache, String taskId, Integer state) {
        redisCache.setCacheObject(FILE_UPLOAD_TASK + taskId, state, 30, TimeUnit.MINUTES);
    }

    public static Boolean getUploadState(RedisCache redisCache, String taskId) {
        Integer state = redisCache.getCacheObject(FILE_UPLOAD_TASK + taskId);
        return state.equals(FileConstants.FILE_UPLOAD_SUCCESS);
    }
}
