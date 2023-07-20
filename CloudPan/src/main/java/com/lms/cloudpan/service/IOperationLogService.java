package com.lms.cloudpan.service;



import com.baomidou.mybatisplus.extension.service.IService;

import com.lms.cloudpan.entity.dao.OperationLog;
import com.lms.cloudpan.entity.dto.OperationLogDto;
import com.lms.cloudpan.entity.vo.OperationLogVo;
import com.lms.page.CustomPage;

import java.util.List;
public interface IOperationLogService extends IService<OperationLog> {
    Boolean saveOperationLog(OperationLogDto operationLogDto);
    OperationLogVo getOperationLogById(Integer id);
    List<OperationLogVo> listOperationLog(CustomPage customPage);
    Boolean delOperationLogById(Integer id);
}
