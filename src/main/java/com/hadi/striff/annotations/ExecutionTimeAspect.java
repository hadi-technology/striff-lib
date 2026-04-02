package com.hadi.striff.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ExecutionTimeAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionTimeAspect.class);

    @Around("execution(*.new(..)) && @annotation(com.hadi.striff.annotations.LogExecutionTime)")
    public Object logConstructorExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        ConstructorSignature signature = (ConstructorSignature) joinPoint.getSignature();
        LOGGER.info("Execution time of {} constructor: {} ms.",
                signature.getDeclaringType().getSimpleName(), (endTime - startTime));
        return proceed;
    }

    @Around("@annotation(com.hadi.striff.annotations.LogExecutionTime)")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LOGGER.info("Execution time of {}.{} method: {} ms.",
                signature.getDeclaringType().getSimpleName(), signature.getName(), (endTime - startTime));
        return proceed;
    }
}
