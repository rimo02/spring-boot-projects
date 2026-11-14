package com.example.banking.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceTimingAspect {
    private static final Logger log = LoggerFactory.getLogger(ServiceTimingAspect.class);

    @Around("execution(public * com.example.banking..service..*(..))")
    public Object time(ProceedingJoinPoint point) throws Throwable {
        long start = System.nanoTime();
        String outcome = "success";
        try {
            return point.proceed();
        } catch (Throwable e) {
            outcome = "failure";
            throw e;
        } finally {
            log.info("event=service_timing operation={} outcome={} elapsed_ms={}",
                    point.getSignature().toShortString(), outcome, (System.nanoTime() - start) / 1_000_000);
        }
    }
}
