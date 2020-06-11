package com.laowang.kill.server.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitmqConfig {



    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private Environment env;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    @Bean
    public RabbitTemplate rabbitTemplate(){
        //生成者到交换机的发送确认
        connectionFactory.setPublisherConfirms(true);
        //交换机到队列的失败回调
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        /**
         * （开启失败回调）
         * 为true时，消息通过交换器无法匹配到队列会返回给生产者
         * 为false时，直接丢弃
         */
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                //System.out.println("消息发送成功");
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //System.out.println("消息丢失");
            }
        });
        return rabbitTemplate;
    }

    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        //设置消息预取的数量
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        return factory;
    }

    /**
     * 用于异步生成订单，也是限流的
     * @return
     */
    @Bean(name = "processOrderListenerContainer")
    public SimpleRabbitListenerContainerFactory limiterlistenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        //设置消息预取的数量
        factory.setPrefetchCount(500);
        //设置消息手动确认
        //factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    //创建用于异步生成订单的队列
    @Bean
    public Queue orderQueue(){
        return new Queue(env.getProperty("order.queue"),true);
    }

    //创建用于异步生成订单的交换机
    @Bean
    public TopicExchange orderExchange(){
        return new TopicExchange(env.getProperty("order.exchange"),true,false);
    }

    //绑定用于订单的队列和交换机
    @Bean
    public Binding orderBinding(){
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(env.getProperty("order.routing.key"));
    }



    @Bean
    public Queue emailQueue(){

        return new Queue(env.getProperty("email.queue"),true);
    }

    @Bean
    public TopicExchange emailExchange(){
        return new TopicExchange(env.getProperty("email.exchange"),true,false);

    }

    @Bean
    public Binding emailBinding(){
        return BindingBuilder.bind(emailQueue()).to(emailExchange()).with(env.getProperty("email.routing.key"));
    }

    /**
     * 构建秒杀成功之后-订单超时未支付的死信队列消息模型
     */

    @Bean
    public Queue deadQueue(){
        Map<String,Object> map = new HashMap<>();
        map.put("x-dead-letter-exchange", env.getProperty("dead.exchange"));
        map.put("x-dead-letter-routing-key",env.getProperty("dead.routing.key"));
        return new Queue(env.getProperty("dead.queue"),
                true,false,false,map);
    }
    //基本交换机
    @Bean
    public TopicExchange basicExchange(){
        return new TopicExchange(env.getProperty("basic.exchange"),true,false);
    }

    //绑定基本交换机与死信队列
    @Bean
    public Binding deadBinding(){
        return BindingBuilder.bind(deadQueue()).to(basicExchange()).with(env.getProperty("basic.routing.key"));
    }

    //创建死信交换机
    @Bean
    public TopicExchange deadExchange(){
        return new TopicExchange(env.getProperty("dead.exchange"),true,false);
    }

    //创建消息失效后要去的队列
    @Bean
    public Queue expireQueue(){
        return new Queue(env.getProperty("expire.queue"),true);
    }

    //绑定死信交换机和失效队列
    @Bean
    public Binding deadProdBinding(){
        return BindingBuilder.bind(expireQueue()).to(deadExchange()).with(env.getProperty("dead.routing.key"));
    }



    /*//用户减库存的队列
    @Bean
    public Queue delStockQueue(){
        return new Queue(env.getProperty("stock.queue"),true);
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(env.getProperty("order.stock.mail.exchange"),true,false);
    }*/


    /*//绑定用于减库存的队列和交换机
    @Bean
    public Binding stockBinding(){
        return BindingBuilder.bind(delStockQueue()).to(fanoutExchange());
    }

    //绑定用于订单的队列和交换机
    @Bean
    public Binding orderBinding(){
        return BindingBuilder.bind(orderQueue()).to(fanoutExchange());
    }

    //绑定用于发送邮件的队列和交换机
    @Bean
    public Binding emailBinding(){
        return BindingBuilder.bind(successEmailQueue()).to(fanoutExchange());
    }*/



}
