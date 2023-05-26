package com.lms.cloudpan.fileHandler;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.io.IOException;
//重写
public class CustomPathResourceResolver extends PathResourceResolver {
    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
        Resource resource=new FileSystemResource(location.getFile().getAbsoluteFile()+ File.separator+resourcePath);
        if(resource.isReadable()){
            return resource;
        }
        return null;
    }
}
