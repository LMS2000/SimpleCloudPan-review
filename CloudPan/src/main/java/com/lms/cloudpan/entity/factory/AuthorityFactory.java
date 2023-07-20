package com.lms.cloudpan.entity.factory;



import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class AuthorityFactory {
    public static final AuthorityConverter AUTHORITY_CONVERTER = Mappers.getMapper(AuthorityConverter.class);

    @Mapper
    public interface AuthorityConverter {
        @Mappings({
                @Mapping(target = "aid", ignore = true),
        })
        Authority toAuthority(AuthorityDto authorityDto);

        AuthorityVo toAuthorityVo(Authority authority);

        List<AuthorityVo> toListAuthorityVo(List<Authority> authority);
    }
}
