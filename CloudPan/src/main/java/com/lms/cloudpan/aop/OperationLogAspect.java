package com.lms.cloudpan.aop;



import com.lms.cloudpan.entity.vo.OperationLogVo;
import com.lms.cloudpan.service.IOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * 记录操作日志
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

   private final IOperationLogService operationLogService;


   @Around(value = "@annotation(opLog)")
   public Object aroundLog(ProceedingJoinPoint joinPoint, OpLog opLog) throws Throwable {
       Object[] methodArgs = joinPoint.getArgs();
       Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
       operationLog(methodArgs,method,opLog);
       return joinPoint.proceed();
   }
    /**
     * 日志记录失败就跳过记录--捕获异常,不进行抛出
     */
    private void operationLog(Object[] methodArgs, Method method, OpLog opLog) {
          try{
              Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
              UserDetails userDetails = (UserDetails) authentication.getPrincipal();
              String content= String.format(opLog.desc(),methodArgs);
              OperationLogVo operationLogVo =
                      OperationLogVo.builder().operationName(userDetails.getUsername()).operationContent(content).build();
             operationLogService.saveOperationLog(operationLogVo);

          }catch (Exception e){
              log.error("操作日志记录时出错",e);
              throw e;
          }
   }


}
