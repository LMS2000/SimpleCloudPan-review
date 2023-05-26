package com.lms.cloudpan.fileHandler;



import com.lms.cloudpan.constants.FileConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//自定义的mapping参考自SimpleUrlMapping源码
public class CustomFileHandlerMapping extends AbstractUrlHandlerMapping implements ApplicationContextAware {

    private final Map<String, Object> urlMap = new LinkedHashMap<>();
    private final CustomFileResourceHttpRequestHandler fileResourceHttpRequestHandler;

    public CustomFileHandlerMapping(CustomFileResourceHttpRequestHandler fileResourceHttpRequestHandler) {
        this.fileResourceHttpRequestHandler = fileResourceHttpRequestHandler;
        //设置映射表所有的/static/**的请求都是由fileResourceHttpRequestHandler去处理
        this.urlMap.put(FileConstants.STATIC_REQUEST_PREFIX_PATTERN, fileResourceHttpRequestHandler);
    }

    @Override
    public void initApplicationContext() throws BeansException {
        super.initApplicationContext();
        registerHandlers(this.urlMap);
    }

    //registerHandlers是SimpleUrlMapping的源码
    private void registerHandlers(Map<String, Object> urlMap) {

        if (urlMap.isEmpty()) {
            logger.trace("No patters in" + formatMappingName());

        } else {
            urlMap.forEach((url, handler) -> {
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                //如果是String类型就是beanName方式注册
                if(handler instanceof String ){
                     handler=((String)handler).trim();
                }
                //注册处理器
                registerHandler(url,handler);
            });
            if (logger.isDebugEnabled()) {
                List<String> patterns = new ArrayList<>();
                if (getRootHandler() != null) {
                    patterns.add("/");
                }
                if (getDefaultHandler() != null) {
                    patterns.add("/**");
                }
                patterns.addAll(getHandlerMap().keySet());
                logger.debug("Patterns " + patterns + " in " + formatMappingName());
            }
        }

    }

    @Override
    public int getOrder() {
        return 1;
    }
}
