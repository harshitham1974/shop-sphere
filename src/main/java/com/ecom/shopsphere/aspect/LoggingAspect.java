package com.ecom.shopsphere.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* com.ecom.shopsphere.service.impl.*.*(..))")
    public void beforeMethod(JoinPoint joinPoint) {

        log.info("========== BEFORE ==========");

        log.info("Method Name : {}", joinPoint.getSignature().getName());

        log.info("Arguments : {}", joinPoint.getArgs());

        log.info("============================");
    }

    @AfterReturning(
            pointcut = "execution(* com.ecom.shopsphere.service.impl.*.*(..))",
            returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {

        log.info("======= AFTER RETURNING =======");

        log.info("Method Name : {}", joinPoint.getSignature().getName());

        log.info("Returned Object : {}", result);

        log.info("===============================");
    }

    @AfterThrowing(
            pointcut = "execution(* com.ecom.shopsphere.service.impl.*.*(..))",
            throwing = "exception")
    public void afterThrowing(
            JoinPoint joinPoint,
            Exception exception) {

        log.error("======= EXCEPTION =======");

        log.error("Method Name : {}", joinPoint.getSignature().getName());

        log.error("Exception : {}", exception.getClass().getSimpleName());

        log.error("Message : {}", exception.getMessage());

        log.error("=========================");
    }

    @Around("execution(* com.ecom.shopsphere.service.impl.*.*(..))")
    public Object measureExecutionTime(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();

        log.info("======= EXECUTION TIME =======");

        log.info("Method : {}", joinPoint.getSignature().getName());

        log.info("Execution Time : {} ms",
                endTime - startTime);

        log.info("==============================");

        return result;
    }
}