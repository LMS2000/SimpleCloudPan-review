package com.lms.cloudpan.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.cloudpan.entity.dao.OperationLog;
import com.lms.cloudpan.entity.dto.OperationLogDto;
import com.lms.cloudpan.entity.vo.OperationLogVo;
import com.lms.cloudpan.mapper.OperationLogMapper;
import com.lms.cloudpan.service.IOperationLogService;
import com.lms.page.CustomPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lms.cloudpan.entity.factory.OperationLogFactory.OPERATIONLOG_CONVERTER;


@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {


    @Override
    public Boolean saveOperationLog(OperationLogVo operationlogVo) {
        OperationLog operationlog = OPERATIONLOG_CONVERTER.toOperationLog(operationlogVo);
       return save(operationlog);

    }
    @Override
    public OperationLogDto getOperationLogById(Integer id) {
        OperationLog operationlog = getById(id);
        return OPERATIONLOG_CONVERTER.toOperationLogDto(operationlog);
    }
    @Override
    public List<OperationLogDto> listOperationLog(CustomPage customPage) {
        List<OperationLog> operationlogList = CustomPage.getPageResult(customPage, new OperationLog(), this, null);
        return OPERATIONLOG_CONVERTER.toListOperationLogDto(operationlogList);
    }
    @Override
    public Boolean delOperationLogById(Integer id) {
        return   removeById(id);

    }
}
