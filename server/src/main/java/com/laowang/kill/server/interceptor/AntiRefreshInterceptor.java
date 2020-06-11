package com.laowang.kill.server.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class AntiRefreshInterceptor implements HandlerInterceptor {
    @Autowired
    RedisTemplate<String,Object> redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String clientIP = httpServletRequest.getRemoteAddr();
        httpServletResponse.setContentType("text/html;charset=utf-8");
        if (redisTemplate.hasKey("blacklist")){
            if (redisTemplate.opsForSet().isMember("blacklist",clientIP)){
                httpServletResponse.getWriter().print("您已经被加入黑名单");
                return false;
            }
        }

        Integer num = (Integer)redisTemplate.opsForValue().get(clientIP);
        if (num == null){
            redisTemplate.opsForValue().set(clientIP,1L,60, TimeUnit.SECONDS);
        }else{
            if (num > 20 && num < 100){
                httpServletResponse.getWriter().print("请求过于频繁，请稍后再试");
                redisTemplate.opsForValue().increment(clientIP,1L);
                return false;
            }else if (num >= 100){
                httpServletResponse.getWriter().print("您已经被加入黑名单");
                redisTemplate.opsForSet().add("blacklist",clientIP);
                return false;
            }else{
                redisTemplate.opsForValue().increment(clientIP,1L);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
