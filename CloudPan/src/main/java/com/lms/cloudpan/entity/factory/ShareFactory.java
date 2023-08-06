package com.lms.cloudpan.entity.factory;

import com.lms.cloudpan.entity.dao.Shares;
import com.lms.cloudpan.entity.dto.ShareDto;
import com.lms.cloudpan.entity.vo.SharesVo;
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

        }
        )
        Shares toShare(ShareDto shareDto);

        SharesVo toSharesVo(Shares shares);

        List<SharesVo> toListSharesVo(List<Shares> shares);

    }
}
