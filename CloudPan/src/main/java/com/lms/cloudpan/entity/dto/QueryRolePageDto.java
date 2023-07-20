package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.Data;

import java.io.Serializable;
@Data
public class QueryRolePageDto extends CustomPage implements Serializable {

    private String roleName;

    private String description;


    private Integer enable;
}
