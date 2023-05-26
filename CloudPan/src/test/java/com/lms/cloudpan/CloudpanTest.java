package com.lms.cloudpan;


import com.lms.cloudpan.service.IUserService;
import com.lms.redis.RedisCache;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;


@SpringBootTest(classes = PanApplication.class)
public class CloudpanTest {




    @Test
    public void testForInsertUser(){
        AnnotationConfigApplicationContext annotationConfigApplicationContext=new AnnotationConfigApplicationContext(PanApplication.class);
        System.out.println(annotationConfigApplicationContext.getBean(RedisCache.class));

    }
}
