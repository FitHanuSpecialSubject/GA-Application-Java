package org.fit.ssapp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * **LoggingAspect** - Aspect-Oriented Logging for Service Methods.
 * This class provides **logging functionality** using **Aspect-Oriented Programming (AOP)**
 * to track the execution of service methods in the application.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

  /**
   * Pointcut for game theory solver methods in the service layer.
   * Matches all methods in `org.fit.ssapp.service.*`.
   */
  @Pointcut("execution(* org.fit.ssapp.service.*.*(..))")
  public void gameTheorySolver() {
  }

  /**
   * Pointcut for stable matching solver methods in the service layer.
   * Matches all methods in `org.fit.ssapp.service.*`.
   */
  @Pointcut("execution(* org.fit.ssapp.service.*.*(..))")
  public void stableMatchingSolver() {
  }

  /**
   * Logs the execution time of **game theory solver methods**.
   * - Logs when the method **starts** execution.
   * - Calls the actual method using `proceed()`.
   * - Logs when the method **finishes** execution.
   *
   * @param joinPoint The method execution context.
   * @return The result of the original method execution.
   * @throws Throwable If an exception occurs during execution.
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
   * Logs the execution time of **stable matching solver methods**.
   * - Logs when the method **starts** execution.
   * - Calls the actual method using `proceed()`.
   * - Logs when the method **finishes** execution.
   *
   * @param joinPoint The method execution context.
   * @return The result of the original method execution.
   * @throws Throwable If an exception occurs during execution.
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
