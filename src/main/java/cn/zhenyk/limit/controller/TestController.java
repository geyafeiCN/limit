package cn.zhenyk.limit.controller;

import cn.zhenyk.limit.common.annotation.RateLimiter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Yakai Zheng（zhengyk@cloud-young.com）
 * @date: Created on 2018/12/7
 * @description:
 * @version: 1.0
 */
@RestController
public class TestController {


    /**
     * @method: limit
     * @description: 10秒内访问次数超过3次则限流
     * @param
     */
    @RequestMapping("limit")
    @RateLimiter(period = 10,limitCount = 3)
    public String limit(){
        return "OK";
    }
}
