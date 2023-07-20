package com.lms.cloudpan.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

//返回用户和角色
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Slf4j
public class SetRoleVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private UserVo user;

    private List<RoleVo> roles;
}
