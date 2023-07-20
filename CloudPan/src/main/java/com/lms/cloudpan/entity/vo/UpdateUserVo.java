package com.lms.cloudpan.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class UpdateUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserVo userVo;

    private List<RoleVo> roles;

    private Long maxQuota;

}
