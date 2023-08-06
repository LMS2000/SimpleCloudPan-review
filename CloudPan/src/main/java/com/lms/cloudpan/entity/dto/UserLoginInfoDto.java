package com.lms.cloudpan.entity.dto;

import lombok.Data;

import java.io.Serializable;
@Data
public class UserLoginInfoDto implements Serializable {

    private String username;
    private String password;
    private String code;
}
