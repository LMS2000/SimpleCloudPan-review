package com.lms.cloudpan.entity.factory;

import com.lms.cloudpan.entity.dao.Authority;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dto.AuthorityDto;
import com.lms.cloudpan.entity.dto.FolderDto;
import com.lms.cloudpan.entity.vo.AuthorityVo;
import com.lms.cloudpan.entity.vo.FolderVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class FolderFactory {

    public static final FolderConverter FOLDER_CONVERTER = Mappers.getMapper(FolderConverter.class);

    @Mapper
    public interface FolderConverter {
        @Mappings({
                @Mapping(target = "folderId", ignore = true),
        })
        Folder toFolder(FolderVo FolderVo);

        FolderDto toFolderDto(Folder folder);

        List<FolderDto> toListFolderDto(List<Folder> folderList);
    }
}
