package com.lms.cloudpan.entity.factory;



import com.lms.cloudpan.entity.dao.UserRole;
import com.lms.cloudpan.entity.dto.UserRoleDto;
import com.lms.cloudpan.entity.vo.UserRoleVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
public class UserRoleFactory {
    public static final UserRoleConverter USERROLE_CONVERTER= Mappers.getMapper(UserRoleConverter.class);
    @Mapper
   public interface UserRoleConverter {
        @Mappings({
        @Mapping(target = "id",ignore = true),
    }
        )
        UserRole toUserRole(UserRoleDto userRoleDto);
        UserRoleVo toUserRoleVo(UserRole userrole);
        List<UserRoleVo> toListUserRoleVo(List<UserRole> userrole);
    }
}
