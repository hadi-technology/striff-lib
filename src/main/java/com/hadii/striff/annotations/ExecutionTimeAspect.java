package com.hadii.striff.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Aspect
public class ExecutionTimeAspect {

    private static final Logger LOGGER = LogManager.getLogger(ExecutionTimeAspect.class);

    @Around("execution(*.new(..)) && @annotation(com.hadii.striff.annotations.LogExecutionTime)")
    public Object logConstructorExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        ConstructorSignature signature = (ConstructorSignature) joinPoint.getSignature();
        LOGGER.info("Execution time of " + signature.getDeclaringType().getSimpleName() + " constructor: "
                + (endTime - startTime) + " ms.");
        return proceed;
    }

    @Around("@annotation(com.hadii.striff.annotations.LogExecutionTime)")
    public Object logMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LOGGER.info("Execution time of " + signature.getDeclaringType().getSimpleName() + "." + signature.getName()
                + " method: " + (endTime - startTime) + " ms.");
        return proceed;
    }
}
