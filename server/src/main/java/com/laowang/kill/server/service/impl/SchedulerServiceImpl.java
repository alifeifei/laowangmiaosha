package com.laowang.kill.server.service.impl;

import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.model.entity.ItemKillSuccess;
import com.laowang.kill.model.mapper.ItemKillMapper;
import com.laowang.kill.model.mapper.ItemKillSuccessMapper;
import com.laowang.kill.server.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class SchedulerServiceImpl implements SchedulerService {


    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private Environment env;
    /**
     * 定时获取status=0的订单并判断是否超过TTL，然后进行失效
     */
    /*@Override
    //@Scheduled(cron = "0/10 * * * * ?")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerExpireOrders() {
        try {
            List<ItemKillSuccess> itemKillSuccesses = itemKillSuccessMapper.selectExpireOrders();
            if (itemKillSuccesses != null && !itemKillSuccesses.isEmpty()){
                itemKillSuccesses.stream().forEach(new Consumer<ItemKillSuccess>() {
                    @Override
                    public void accept(ItemKillSuccess itemKillSuccess) {

                        if (itemKillSuccess.getDiffTime() > env.getProperty("scheduler.expire.orders.time",Integer.class)){
                            itemKillSuccessMapper.expireOrder(itemKillSuccess.getCode());
                        }
                    }
                });

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/

    @Override
    @Scheduled(cron = "0/5 * * * * ? ")
    public void startSecKill() {
        List<ItemKill> itemKills = itemKillMapper.selectUnStart();
        if (itemKills != null){
            for (ItemKill itemKill : itemKills) {
                redisTemplate.delete("seckill:killid:" + itemKill.getId());
                redisTemplate.delete("seckill:users:"+ itemKill.getId());
                redisTemplate.boundHashOps("seckill:itemKills").delete(itemKill.getId());
                int totalCount = itemKill.getTotal();
                for (int i = 0; i < totalCount; i++){
                    redisTemplate.opsForList().rightPush("seckill:killid:" + itemKill.getId(),itemKill.getItemId());
                }
                //将状态更新为1，表示启动秒杀
                itemKill.setIsActive(1);
                itemKillMapper.updateIsActive(itemKill);
                //把秒杀商品放入redis中
                redisTemplate.boundHashOps("seckill:itemKills").put(itemKill.getId(),itemKill);

                redisTemplate.boundHashOps("seckill:itemCount").increment(itemKill.getId(),totalCount);
            }
        }
    }

    @Override
    @Scheduled(cron = "0/5 * * * * ? ")
    public void endSecKill() {
        List<ItemKill> itemKills = itemKillMapper.selectEndItemKill();
        if (itemKills != null){
            for (ItemKill itemKill : itemKills) {
                itemKill.setIsActive(2);
                itemKillMapper.updateIsActive(itemKill);
                redisTemplate.delete("seckill:killid:" + itemKill.getId());
                ItemKill itemKillRedis = (ItemKill)redisTemplate.boundHashOps("seckill:itemKills").get(itemKill.getId());
                int total = itemKillRedis.getTotal();
                if (total > 0){
                    //将数据库的数据改成对应的值
                    itemKillMapper.update(itemKillRedis);
                }
                redisTemplate.boundHashOps("seckill:itemKills").delete(itemKill.getId());
                redisTemplate.boundHashOps("seckill:itemCount").delete(itemKill.getId());
            }
        }

    }


}
