package com.lms.cloudpan.entity.factory;

import com.lms.cloudpan.entity.dao.File;
import com.lms.cloudpan.entity.dao.Folder;
import com.lms.cloudpan.entity.dto.FolderDto;
import com.lms.cloudpan.entity.vo.FileVo;
import com.lms.cloudpan.entity.vo.FolderVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

public class FileFactory {

    public static final FileConverter FILE_CONVERTER = Mappers.getMapper(FileConverter.class);

    @Mapper
    public interface FileConverter {
        @Mappings({

        })
        FileVo toFileVo(File file);

        List<FileVo> toListFileVo(List<File> fileList);
    }
}
