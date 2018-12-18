package cn.zhenyk.limit.common.annotation;

/**
 * @author: Yakai Zheng（zhengyk@cloud-young.com）
 * @date: Created on 2018/12/7
 * @description:
 * @version: 1.0
 */

import cn.zhenyk.limit.common.enums.LimitType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RateLimiter {


    /**
     * 时间段
     */
    int period();

    /**
     * 限制的次数
     */
    int limitCount();

    /**
     * 默认对 IP 限流
     * @return
     */
    LimitType limitType() default LimitType.IP;
}
