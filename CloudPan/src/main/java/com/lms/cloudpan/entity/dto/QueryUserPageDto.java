package com.lms.cloudpan.entity.dto;

import com.lms.page.CustomPage;
import lombok.Data;

@Data
public class QueryUserPageDto extends CustomPage {

    private String username;

    private Integer enable;
}
