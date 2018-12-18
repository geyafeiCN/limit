package cn.zhenyk.limit.common.aop;

import cn.zhenyk.limit.common.annotation.RateLimiter;
import cn.zhenyk.limit.common.enums.LimitType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author: Yakai Zheng（zhengyk@cloud-young.com）
 * @date: Created on 2018/12/7
 * @description:
 * @version: 1.0
 */
@Slf4j
@Aspect
@Component
public class LimitAspect {

    private static String redisLimitLua;

    /**
     * 使用静态
     */
    @Value("${redis-limit-lua}")
    public void setRedisLimitLua(String limitLua){
        redisLimitLua = limitLua;
    }

    @Autowired
    private RedisTemplate<String,Serializable> redisTemplate;


    @Around("within(cn.zhenyk.limit.controller.*) && @annotation(limit)")
    public Object interceptor(ProceedingJoinPoint pjp, RateLimiter limit) throws Throwable{
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        LimitType limitType = limit.limitType();
        String key;
        int limitCount = limit.limitCount();
        int period = limit.period();
        switch (limitType){
            case IP:
                key = getIpAddress();
                break;
            case METHOD:
                key = method.getName();
                break;
            default:
                key = getIpAddress();
        }
        List<String> keys = Collections.singletonList(key);
        try {
            RedisScript<Number> redisScript = new DefaultRedisScript<>(redisLimitLua, Number.class);
            Number c = redisTemplate.execute(redisScript, keys, period, limitCount);
            if(c.intValue() == 1){
                Object proceed = pjp.proceed();
                log.info("未被限流，正常执行:{}", proceed);
                return proceed;
            }else {
                log.info("被限流");
                return "被限流";
            }
        }catch (Exception e){
            log.error("执行发生异常：",e);
            return "系统错误";
        }

    }

    private String getIpAddress() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
