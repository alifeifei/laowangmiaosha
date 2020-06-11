package com.laowang.kill.server.service;

import com.laowang.kill.model.entity.ItemKill;

import java.util.List;

public interface ItemService {
    List<ItemKill> getKillItems()throws Exception;

    ItemKill getKillDetail(Integer id) throws Exception;
}
