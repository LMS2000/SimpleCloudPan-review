package com.lms.cloudpan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer userId;

    private String username;
    private String password;


    private String email;
    private  Integer enable;

    private Long useQuota;
    private Long quota;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
