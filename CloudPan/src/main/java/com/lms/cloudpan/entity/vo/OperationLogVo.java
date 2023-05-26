package com.lms.cloudpan.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class OperationLogVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 操作者姓名
     */
    @ApiModelProperty("操作者姓名")
    private String operationName;
    /**
     * 操作内容
     */
    @ApiModelProperty("操作内容")
    private String operationContent;
}
