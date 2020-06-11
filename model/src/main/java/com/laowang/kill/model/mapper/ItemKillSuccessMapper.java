package com.laowang.kill.model.mapper;


import com.laowang.kill.model.entity.ItemKillSuccess;
import com.laowang.kill.model.entity.KillSuccessUserInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemKillSuccessMapper {


    int countByKillUserId(@Param("killId") Integer killId, @Param("userId") Integer userId);
    //添加秒杀成功订单
    int insertSelective(ItemKillSuccess record);

    KillSuccessUserInfo selectByCode(@Param("code") String code);

    ItemKillSuccess selectByPrimaryKey(@Param("code") String code);

    int expireOrder(@Param("code") String code);

    List<ItemKillSuccess> selectExpireOrders();


}