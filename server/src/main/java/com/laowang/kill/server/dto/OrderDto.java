package com.laowang.kill.server.dto;

import com.laowang.kill.model.entity.ItemKill;

import java.io.Serializable;

public class OrderDto implements Serializable {
    private ItemKill itemKill;
    private Integer userId;


    public ItemKill getItemKill() {
        return itemKill;
    }

    public void setItemKill(ItemKill itemKill) {
        this.itemKill = itemKill;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "itemKill=" + itemKill +
                ", userId=" + userId +
                '}';
    }
}
