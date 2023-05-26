package com.lms.cloudpan.entity.factory;

import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.ShareVo;
import com.lms.cloudpan.entity.vo.UserVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class ShareFactory {
    public static final ShareConverter SHARE_CONVERTER= Mappers.getMapper(ShareConverter.class);
    @Mapper
    public interface ShareConverter {
        @Mappings({
                @Mapping(target = "id",ignore = true),
        }
        )
        Shares toShare(ShareVo shareVo);
    }
}
