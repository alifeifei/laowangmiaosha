package com.laowang.kill.server.service.impl;

import com.laowang.kill.api.enums.SysConstant;
import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.model.entity.ItemKillSuccess;
import com.laowang.kill.model.entity.KillSuccessUserInfo;
import com.laowang.kill.model.mapper.ItemKillMapper;
import com.laowang.kill.model.mapper.ItemKillSuccessMapper;
import com.laowang.kill.server.service.KillService;
import com.laowang.kill.server.service.RabbitSenderService;
import com.laowang.kill.server.utils.RandomUtil;
import com.laowang.kill.server.utils.SnowFlake;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class KillServiceImpl implements KillService {

    private static SnowFlake snowFlake = new SnowFlake(2, 3);
    @Autowired
    ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    ItemKillMapper itemKillMapper;

    @Autowired
    RabbitSenderService rabbitSenderService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    /*@Autowired
    CuratorFramework curatorFramework;*/

    //private final String PATHPREFIX = "/kill/zkLock/";


    /**
     * 通过redis分布式锁进行优化
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        Boolean result=false;
        if (itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            ValueOperations stringStringValueOperations = stringRedisTemplate.opsForValue();
            final String key = new StringBuffer().append(killId).append(userId).append(":lock").toString();
            final String value = RandomUtil.generateOrderCode();
            Boolean aBoolean = stringStringValueOperations.setIfAbsent(key, value);
            if (aBoolean){
                stringRedisTemplate.expire(key,30, TimeUnit.SECONDS);
                try {
                    ItemKill itemKill=itemKillMapper.selectById(killId);
                    if (itemKill!=null){
                        //使用行锁解决超卖问题
                        int res=itemKillMapper.updateKillItemV2(killId);
                        if (res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result=true;
                        }
                    }
                }finally {
                    if (value.equals(stringStringValueOperations.get(key).toString())){
                        stringRedisTemplate.delete(key);
                    }
                }
            }
        }else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }

    /**
     * 通用的方法-记录用户秒杀成功后生成的订单-并进行异步邮件消息的通知
     * @param kill
     * @param userId
     * @throws Exception
     */
    private void commonRecordKillSuccessInfo(ItemKill kill, Integer userId) throws Exception{
        //记录抢购成功后生成的秒杀订单记录

        ItemKillSuccess entity=new ItemKillSuccess();
        String code = String.valueOf(snowFlake.nextId());
        //entity.setCode(RandomUtil.generateOrderCode());
        entity.setCode(code);
        entity.setItemId(kill.getItemId());
        entity.setKillId(kill.getId());
        entity.setUserId(userId.toString());
        entity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        entity.setCreateTime(new Date());
        if(itemKillSuccessMapper.countByKillUserId(kill.getId(),userId) <= 0){
            int res = itemKillSuccessMapper.insertSelective(entity);
            //异步发送mail
            if (res > 0){
                rabbitSenderService.sendKillSuccessEmailMsg(code);
                rabbitSenderService.sendKillSuccessOrderExpireMsg(code);
            }
        }

    }

    /**
     * 通过redis解决超卖和单一用户多次购买问题
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemRedis(Integer killId, Integer userId) throws Exception {
        Boolean res = false;
        ItemKill itemKill = (ItemKill) redisTemplate.boundHashOps("seckill:itemKills").get(killId);
        //ItemKill itemKill = itemKillMapper.selectById(killId);
        if (itemKill == null){
            //这里还有可能是商品被抢完了
            throw new Exception("秒杀活动不存在");
        }
        if (itemKill.getIsActive() == 0){
            throw new Exception("秒杀获得还没开始");
        }else if (itemKill.getIsActive() == 2){
            throw new Exception("秒杀活动已经结束");
        }
        Integer itemId = (Integer)redisTemplate.opsForList().leftPop("seckill:killid:" + itemKill.getId());
        if (itemId != null){
            if (!redisTemplate.opsForSet().isMember("seckill:users:"+ itemKill.getId(),userId)){
                //System.out.println("恭喜成功抢购商品");
                redisTemplate.opsForSet().add("seckill:users:"+ itemKill.getId(),userId);
                res = true;
                //向用于生成订单的队列和减库存队列发送消息
                rabbitSenderService.sendOrder(itemKill.getId(),userId);

            }else{
                redisTemplate.opsForList().rightPush("seckill:killid:" + killId,itemKill.getItemId());
                throw new Exception("您已经成功抢购该商品，请勿重新抢购");
            }

        }
        return res;
    }


    /*@Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        Boolean result=false;

        //判断当前用户是否已经抢购过当前商品
        //秒杀成功表(也就是订单表)记录了哪些用户成功秒杀了哪些商品，以及对应的秒杀id
        if (itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //查询待秒杀商品详情
            ItemKill itemKill=itemKillMapper.selectById(killId);

            //判断是否可以被秒杀canKill=1?
            if (itemKill!=null && 1==itemKill.getCanKill() ){
                //扣减库存-减一
                int res=itemKillMapper.updateKillItem(killId);

                //扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result=true;
                }
            }
        }else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }*/

    @Override
    public KillSuccessUserInfo killRecordDetail(String orderNo) throws Exception{
        return itemKillSuccessMapper.selectByCode(orderNo);

    }




    /*@Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        Boolean result=false;

        //判断当前用户是否已经抢购过当前商品
        //秒杀成功表(也就是订单表)记录了哪些用户成功秒杀了哪些商品，以及对应的秒杀id
        if (itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
            //查询待秒杀商品详情
            ItemKill itemKill=itemKillMapper.selectById(killId);

            //判断是否可以被秒杀canKill=1?
            if (itemKill!=null && 1==itemKill.getCanKill() ){
                //扣减库存-减一
                int res=itemKillMapper.updateKillItemV2(killId);

                //扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res>0){
                    commonRecordKillSuccessInfo(itemKill,userId);
                    result=true;
                }
            }
        }else{
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }
*/


























    /**
     * 利用zookeepr分布式锁实现优化
     *
     */
    /*@Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        InterProcessMutex mutex = new InterProcessMutex(curatorFramework,PATHPREFIX+killId+userId+":lock");
        try {
            if (mutex.acquire(10L,TimeUnit.SECONDS)){

                if (itemKillSuccessMapper.countByKillUserId(killId,userId) <= 0){
                    ItemKill itemKill=itemKillMapper.selectById(killId);
                    if (itemKill!=null && 1==itemKill.getCanKill() && itemKill.getTotal()>0){
                        int res=itemKillMapper.updateKillItemV2(killId);
                        if (res>0){
                            commonRecordKillSuccessInfo(itemKill,userId);
                            result=true;
                        }
                    }
                }else{
                    throw new Exception("zookeeper-您已经抢购过该商品了!");
                }
            }
        }catch (Exception e){
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
        }finally {
            if (mutex!=null){
                mutex.release();
            }
        }
        return result;
    }*/
}
