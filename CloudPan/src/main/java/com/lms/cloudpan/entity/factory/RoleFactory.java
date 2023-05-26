package com.lms.cloudpan.entity.factory;



import com.lms.cloudpan.entity.dao.Role;
import com.lms.cloudpan.entity.dto.RoleDto;
import com.lms.cloudpan.entity.vo.RoleVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
public class RoleFactory {
    public static final RoleConverter ROLE_CONVERTER= Mappers.getMapper(RoleConverter.class);
    @Mapper
   public interface RoleConverter {
        @Mappings({
        @Mapping(target = "rid",ignore = true),
    }
        )
        Role toRole(RoleVo roleVo);
        RoleDto toRoleDto(Role role);
        List<RoleDto> toListRoleDto(List<Role> role);
    }
}
