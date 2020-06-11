package com.laowang.kill.server.service;

import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.model.entity.KillSuccessUserInfo;
import com.laowang.kill.model.mapper.ItemKillMapper;
import com.laowang.kill.model.mapper.ItemKillSuccessMapper;
import com.laowang.kill.server.dto.KillDto;
import com.laowang.kill.server.dto.OrderDto;
import com.laowang.kill.server.utils.SnowFlake;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitSenderService {

    //private static SnowFlake snowFlake = new SnowFlake(2, 3);

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 把消息发送到下订单的消息队列
     * @param killId
     * @param userId
     */
    public void sendOrder(Integer killId, Integer userId){
        try{
            if (killId != null){
                //查到ItemKill
                //发送到队列去

                ItemKill itemKill = (ItemKill) redisTemplate.boundHashOps("seckill:itemKills").get(killId);
                //ItemKill itemKill = itemKillMapper.selectById(killId);
                if (itemKill != null ){
                    OrderDto orderDto = new OrderDto();
                    orderDto.setItemKill(itemKill);
                    orderDto.setUserId(userId);
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("order.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("order.routing.key"));
                    rabbitTemplate.convertAndSend(orderDto, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            //给消息设置一些配置信息
                            MessageProperties messageProperties=message.getMessageProperties();
                            //设置了之后接收者可以直接以OrderDto作为形参接收消息
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,OrderDto.class);
                            return message;
                        }
                    });
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendKillSuccessEmailMsg(String orderNo){
        try{
            if (orderNo != null || !"".equals(orderNo)){
                KillSuccessUserInfo killSuccessUserInfo =itemKillSuccessMapper.selectByCode(orderNo);
                if (killSuccessUserInfo != null ){
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("email.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("email.routing.key"));
                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            //给消息设置一些配置信息
                            MessageProperties messageProperties=message.getMessageProperties();
                            //消息持久化
                            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            //设置了之后接收者可以直接以KillSuccessUserInfo作为形参接收消息
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            return message;
                        }
                    });
                }
            }
        }catch (Exception e){

        }
    }

    //发送消息到死信队列
    public void sendKillSuccessOrderExpireMsg(String orderNo){

        try{
            if (orderNo != null || !"".equals(orderNo)){
                KillSuccessUserInfo killSuccessUserInfo =itemKillSuccessMapper.selectByCode(orderNo);
                if (killSuccessUserInfo != null ){
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    rabbitTemplate.setExchange(env.getProperty("basic.exchange"));
                    rabbitTemplate.setRoutingKey(env.getProperty("basic.routing.key"));
                    rabbitTemplate.convertAndSend(killSuccessUserInfo, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            //给消息设置一些配置信息
                            MessageProperties messageProperties=message.getMessageProperties();
                            messageProperties.setExpiration(env.getProperty("order.expire"));

                            //设置了之后接收者可以直接以KillSuccessUserInfo作为形参接收消息
                            messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            return message;
                        }
                    });
                }


            }


        }catch (Exception e){

        }
    }


}
