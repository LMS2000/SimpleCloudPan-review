package com.lms.cloudpan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author LMS
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserAuthorityDto {
    /**
     * 当前用户如何被禁用了或者认证中心不存在,就是不合法的
     */
     private Boolean legal;
    /**
     * 用户所具备的权限列表
     */
     private List<String> authorities;
}
