package com.lms.cloudpan.entity.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DownloadFileDto {

    @NotBlank(message = "下载路径不能为空")
    @NotNull(message = "下载路径不能为空")
    private String url;
    @NotBlank(message = "下载路径不能为空")
    @NotNull(message = "下载路径不能为空")
    private String name;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
