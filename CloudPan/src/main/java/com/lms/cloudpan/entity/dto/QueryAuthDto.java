package com.lms.cloudpan.entity.dto;

import com.infrastructure.validator.RangeCheck;
import io.swagger.models.auth.In;
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
public class QueryAuthDto implements Serializable {
    private static final long serialVersionUID = 1L;


    private String name;


    private Integer visible;

}
