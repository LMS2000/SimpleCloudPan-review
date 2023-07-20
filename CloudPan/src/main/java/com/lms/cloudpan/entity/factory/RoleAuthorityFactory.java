package com.lms.cloudpan.entity.factory;



import com.lms.cloudpan.entity.dao.RoleAuthority;
import com.lms.cloudpan.entity.dto.RoleAuthorityDto;
import com.lms.cloudpan.entity.vo.RoleAuthorityVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class RoleAuthorityFactory {
    public static final RoleAuthorityConverter ROLEAUTHORITY_CONVERTER = Mappers.getMapper(RoleAuthorityConverter.class);

    @Mapper
    public interface RoleAuthorityConverter {
        @Mappings({
                @Mapping(target = "id", ignore = true),
        })
        RoleAuthority toRoleAuthority(RoleAuthorityDto roleAuthorityDto);

        RoleAuthorityVo toRoleAuthorityVo(RoleAuthority roleauthority);

        List<RoleAuthorityVo> toListRoleAuthorityVo(List<RoleAuthority> roleauthority);
    }
}
