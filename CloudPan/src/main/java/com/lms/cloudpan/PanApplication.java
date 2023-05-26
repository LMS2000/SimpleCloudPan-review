package com.lms.cloudpan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 简易网盘项目
 * @Author LMS2000
 */
@SpringBootApplication
@EnableScheduling
public class PanApplication {
    public static void main(String[] args) {
        SpringApplication.run(PanApplication.class,args);
    }
}
