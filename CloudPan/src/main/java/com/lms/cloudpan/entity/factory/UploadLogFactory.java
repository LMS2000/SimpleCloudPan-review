package com.lms.cloudpan.entity.factory;


import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.dto.UploadLogDto;
import com.lms.cloudpan.entity.vo.UploadLogVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class UploadLogFactory {
    public static final UploadLogConverter UPLOAD_LOG_CONVERTER= Mappers.getMapper(UploadLogConverter.class);
    @Mapper
   public interface  UploadLogConverter {
        @Mappings({
        @Mapping(target = "id",ignore = true),
    }
        )
        UploadLog toUploadLog(UploadLogVo uploadLogVo);
        UploadLogDto toUploadLogDto(UploadLog uploadLog);
       List<UploadLogDto> toListUploadLogDto(List<UploadLog> courseruploadlog);
    }
}
