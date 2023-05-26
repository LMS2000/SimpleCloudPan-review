package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.OperationLog;
import com.lms.cloudpan.entity.dto.OperationLogDto;
import com.lms.cloudpan.entity.vo.OperationLogVo;
import com.lms.page.CustomPage;

import java.util.List;
public interface IOperationLogService extends IService<OperationLog> {
    Boolean saveOperationLog(OperationLogVo operationlogVo);
    OperationLogDto getOperationLogById(Integer id);
    List<OperationLogDto> listOperationLog(CustomPage customPage);
    Boolean delOperationLogById(Integer id);
}
