package com.lms.cloudpan.fileHandler;

import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.List;
//自定义handler防止pathHalper对url二次的解码
public class CustomFileResourceHttpRequestHandler extends ResourceHttpRequestHandler {
    @Override
    public void afterPropertiesSet() throws Exception {
        setResourceResolvers(List.of(new CustomPathResourceResolver()));
        super.afterPropertiesSet();
        setUrlPathHelper(null);
    }
}
