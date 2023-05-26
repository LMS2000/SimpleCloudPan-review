package com.lms.cloudpan.entity.factory;
import com.lms.cloudpan.entity.dao.User;
import com.lms.cloudpan.entity.dto.UserDto;
import com.lms.cloudpan.entity.vo.UserVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;
public class UserFactory {
    public static final UserConverter USER_CONVERTER= Mappers.getMapper(UserConverter.class);
    @Mapper
   public interface UserConverter {
        @Mappings({
                @Mapping(target = "userId",ignore = true),
        }
        )
        User toUser(UserVo userVo);
        UserDto toUserDto(User user);
        List<UserDto> toListUserDto(List<User> user);
    }
}
