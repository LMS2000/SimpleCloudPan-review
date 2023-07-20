package com.lms.cloudpan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lms.cloudpan.entity.dao.UploadLog;
import com.lms.cloudpan.entity.dto.UploadLogDto;
import com.lms.cloudpan.entity.vo.UploadLogVo;


import java.util.List;

public interface IUploadLogService extends IService<UploadLog> {
    Integer saveCourserUploadLog(UploadLogDto uploadLogDto);
    UploadLogVo getCourserUploadLogById(Integer id);
//    List<CourserUploadLogDto> listCourserUploadLog(CustomPage customPage);
    void delCourserUploadLogById(Integer id);

    void deleteUploadedFile(List<Integer> uploadFileLogRecordList);
}
