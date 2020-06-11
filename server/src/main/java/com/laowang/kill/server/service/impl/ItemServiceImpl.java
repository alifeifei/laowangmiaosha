package com.laowang.kill.server.service.impl;

import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.model.mapper.ItemKillMapper;
import com.laowang.kill.server.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ItemServiceImpl implements ItemService {



    @Autowired
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<ItemKill> getKillItems() throws Exception {

        List<ItemKill> itemKills = redisTemplate.boundHashOps("seckill:itemKills").values();
        //List<ItemKill> itemKills = itemKillMapper.selectAll();
        return itemKills;
    }

    @Override
    public ItemKill getKillDetail(Integer id) throws Exception {
        ItemKill itemKill= (ItemKill)redisTemplate.boundHashOps("seckill:itemKills").get(id);
        //ItemKill itemKill = itemKillMapper.selectById(id);
        return itemKill;
    }
}
