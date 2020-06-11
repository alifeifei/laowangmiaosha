package com.laowang.kill.model.mapper;

import com.laowang.kill.model.entity.ItemKill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.ognl.IteratorEnumeration;


import java.util.List;


public interface ItemKillMapper {

    List<ItemKill> selectAll();
    ItemKill selectById(@Param("id") Integer id);
    int updateKillItem(@Param("killId") Integer killId);
    int updateKillItemV2(@Param("killId") Integer killId);
    List<ItemKill> selectUnStart();
    void updateIsActive(ItemKill itemKill);
    List<ItemKill> selectEndItemKill();
    void updateById(@Param("id") Integer id);
    void update(ItemKill itemKill);

}