package com.lms.cloudpan.controller;



import com.lms.cloudpan.entity.vo.OperationLogVo;
import com.lms.cloudpan.service.IOperationLogService;
import com.lms.page.CustomPage;
import com.lms.result.EnableResponseAdvice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/operationLog")
@RequiredArgsConstructor
@EnableResponseAdvice
@Api(tags = "操作日志请求")
public class OperationLogController {
    private final IOperationLogService iOperationLogService;

    @ApiOperation("根据id查询操作日志")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:operationLog:get')")
    public OperationLogVo getOperationLog(@PathVariable("id") Integer id) {
        return iOperationLogService.getOperationLogById(id);
    }

    @ApiOperation("分页批量查询操作日志")
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermi('pan:operationLog:list')")
    public List<OperationLogVo> listOperationLog(CustomPage customPage) {
        return iOperationLogService.listOperationLog(customPage);
    }

    @ApiOperation("删除操作日志")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermi('pan:operationLog:delete')")
    public Boolean delOperationLog(@PathVariable("id") Integer id) {
      return   iOperationLogService.delOperationLogById(id);
    }
}
