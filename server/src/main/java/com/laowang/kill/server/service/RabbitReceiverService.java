package com.laowang.kill.server.service;

import com.laowang.kill.api.enums.SysConstant;
import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.model.entity.ItemKillSuccess;
import com.laowang.kill.model.entity.KillSuccessUserInfo;
import com.laowang.kill.model.entity.User;
import com.laowang.kill.model.mapper.ItemKillMapper;
import com.laowang.kill.model.mapper.ItemKillSuccessMapper;
import com.laowang.kill.model.mapper.UserMapper;
import com.laowang.kill.server.dto.MailDto;
import com.laowang.kill.server.dto.OrderDto;
import com.laowang.kill.server.utils.SnowFlake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RabbitReceiverService {

    private static SnowFlake snowFlake = new SnowFlake(2, 3);

    @Autowired
    private MailService mailService;

    @Autowired
    private Environment env;

    @Autowired
    ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    ItemKillMapper itemKillMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitSenderService rabbitSenderService;

    /**
     * 秒杀异步邮件通知-接收消息
     */
    @RabbitListener(queues = {"${email.queue}"},containerFactory = "singleListenerContainer")
    public void consumeEmailMsg(KillSuccessUserInfo info){
            if (info != null){
                /*MailDto mailDto = new MailDto(env.getProperty("mail.kill.item.success.subject"),
                    "接收到测试的消息",new String[]{killSuccessUserInfo.getEmail()});
            mailService.sendSimpleEmail(mailDto);*/
                System.out.println("准备发送email了");
                String content=String.format(env.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());
                MailDto dto=new MailDto(env.getProperty("mail.kill.item.success.subject"),content,new String[]{info.getEmail()});
                mailService.sendHTMLMail(dto);
            }
    }


    /**
     * 用户秒杀成功后超时未支付-监听者
     * @param killSuccessUserInfo
     */
    @RabbitListener(queues = {"${expire.queue}"},containerFactory = "singleListenerContainer")
    public void consumeExpireOrder(KillSuccessUserInfo killSuccessUserInfo){
        try {

            if (killSuccessUserInfo != null){
                /**
                 * 不能用killSuccessUserInfo.getStatus() == 0来判断，因为这是在死信队列待了很久后的消息
                 * 里面的status和数据库的status可能会不一样，得去数据库查
                 */
                ItemKillSuccess itemKillSuccess = itemKillSuccessMapper.selectByPrimaryKey(killSuccessUserInfo.getCode());
                if (itemKillSuccess != null && itemKillSuccess.getStatus().intValue() == 0){
                    itemKillSuccessMapper.expireOrder(itemKillSuccess.getCode());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 下单的消费者
     * @param orderDto
     */
    @RabbitListener(queues = {"${order.queue}"}, containerFactory = "processOrderListenerContainer")
    public void consumeOrder(OrderDto orderDto){
        //System.out.println("准备开始生成订单");
        if (orderDto != null){

            ItemKill kill = orderDto.getItemKill();
            Integer userId = orderDto.getUserId();
            //生成订单
            ItemKillSuccess entity=new ItemKillSuccess();
            String code = String.valueOf(snowFlake.nextId());
            entity.setCode(code);
            entity.setItemId(kill.getItemId());
            entity.setKillId(kill.getId());
            entity.setUserId(userId.toString());
            entity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
            entity.setCreateTime(new Date());
            itemKillSuccessMapper.insertSelective(entity);
            //减库存
            //System.out.println("开始减库存");
            Long itemCount = redisTemplate.boundHashOps("seckill:itemCount").increment(kill.getId(), -1);
            kill.setTotal(itemCount.intValue());
            if (itemCount <= 0){
                itemKillMapper.updateById(kill.getId());
            }
            redisTemplate.boundHashOps("seckill:itemKills").put(kill.getId(),kill);

            //itemKillMapper.updateKillItemV2(kill.getId());
            //发邮件
            //System.out.println("开始发邮件");
            User user = userMapper.selectById(userId);
            String content = String.format(env.getProperty("mail.kill.item.success.content"),kill.getItemName(),code);
            MailDto mailDto = new MailDto(env.getProperty("mail.kill.item.success.subject"),
                    content,new String[]{user.getEmail()});
            mailService.sendHTMLMail(mailDto);
            //发送到死信队列中
            //System.out.println("开始发送到死信队列中");
            rabbitSenderService.sendKillSuccessOrderExpireMsg(code);
        }
    }

    /*@RabbitListener(queues = {"${stock.queue}"}, containerFactory = "processOrderListenerContainer")
    public void consumeDelStockQueue(OrderDto orderDto){
        System.out.println("准备开始减库存");
        if (orderDto != null){
            ItemKill kill = orderDto.getItemKill();
            itemKillMapper.updateKillItemV2(kill.getId());
        }
    }*/
}
