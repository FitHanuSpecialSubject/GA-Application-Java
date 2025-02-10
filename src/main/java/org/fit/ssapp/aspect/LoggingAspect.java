package org.fit.ssapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * LoggingAspect.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  /**
   * gameTheorySolver.
   */
  @Pointcut("execution(* org.fit.ssapp.service.*.*(..))")
  public void gameTheorySolver() {
  }

  /**
   * stableMatchingSolver.
   */
  @Pointcut("execution(* org.fit.ssapp.service.*.*(..))")
  public void stableMatchingSolver() {
  }

  /**
   * logAroundGameTheorySolver.
   *
   * @param joinPoint is.
   *
   * @return Object
   *
   * @throws Throwable error
   *
   */
  @Around("gameTheorySolver()")
  public Object logAroundGameTheorySolver(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint
            .getSignature()
            .getDeclaringTypeName();
    String methodName = joinPoint
            .getSignature()
            .getName();

    log.info("{}.{}() is started", className, methodName);
    Object result = joinPoint.proceed();
    log.info("{}.{}() is finished", className, methodName);
    return result;
  }


  /**
   * logAroundStableMatchingSolver.
   *
   * @param joinPoint ?
   *
   * @return Object
   *
   * @throws Throwable error
   *
   */
  @Around("stableMatchingSolver()")
  public Object logAroundStableMatchingSolver(ProceedingJoinPoint joinPoint) throws Throwable {
    String className = joinPoint
            .getSignature()
            .getDeclaringTypeName();
    String methodName = joinPoint
            .getSignature()
            .getName();

    log.info("{}.{}() is started", className, methodName);
    Object result = joinPoint.proceed();
    log.info("{}.{}() is finished", className, methodName);
    return result;
  }

}
