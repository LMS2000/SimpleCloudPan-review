package com.lms.cloudpan.entity.factory;

import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dto.ShareDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

public class ShareFactory {
    public static final ShareConverter SHARE_CONVERTER= Mappers.getMapper(ShareConverter.class);
    @Mapper
    public interface ShareConverter {
        @Mappings({
                @Mapping(target = "id",ignore = true),
        }
        )
        Shares toShare(ShareDto shareDto);
    }
}
