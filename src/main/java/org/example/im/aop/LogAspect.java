/* Copyright (C) 2025 */
package org.example.im.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class LogAspect {

  // 拦截鉴权操作相关 Controller 的 public 方法
  @Pointcut(
      "execution(public * org.example.im.auth.AuthController.*(..))")
  public void controllerMethods() {}

  @Around("controllerMethods()")
  public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request =
        ((ServletRequestAttributes)
                Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
            .getRequest();

    String url = request.getRequestURL().toString();
    String method = request.getMethod();
    String ip = request.getRemoteAddr();
    String classMethod = joinPoint.getSignature().toShortString();
    Object[] args = joinPoint.getArgs();

    log.info(
        "Request URL: {} HTTP Method: {} IP: {} Method: {} Args: {}",
        url,
        method,
        ip,
        classMethod,
        Arrays.toString(args));

    long start = System.currentTimeMillis();
    Object result;
    try {
      result = joinPoint.proceed(); // 执行目标方法
    } catch (Exception e) {
      log.error("Exception in {}: {}", classMethod, e.getMessage(), e);
      throw e;
    }
    long end = System.currentTimeMillis();

    log.info("Response from {}: {} Duration: {} ms", classMethod, result, (end - start));

    return result;
  }
}
