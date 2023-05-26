package com.lms.cloudpan.config;



import com.lms.cloudpan.fileHandler.CustomFileHandlerMapping;
import com.lms.cloudpan.fileHandler.CustomFileResourceHttpRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CustomMvcConfig {

    @Bean
    public CustomFileHandlerMapping customFileHandlerMapping(CustomFileResourceHttpRequestHandler customFileResourceHttpRequestHandler) {
        return new CustomFileHandlerMapping(customFileResourceHttpRequestHandler);
    }


    @Bean
    public CustomFileResourceHttpRequestHandler customFileResourceHttpRequestHandler(OssProperties ossProperties) {
        CustomFileResourceHttpRequestHandler customFileResourceHttpRequestHandler = new CustomFileResourceHttpRequestHandler();
       //设置的location会在访问静态资源的时候由CustomPathResourceResolver的getResource方法的localtion获取实际存储的rootpath
        //如D:/qhy-demo/file_home 这样我们就可以去映射我们的本地资源
        customFileResourceHttpRequestHandler.setLocationValues(List.of("file:" + ossProperties.getRootPath()));
        return customFileResourceHttpRequestHandler;
    }
}
