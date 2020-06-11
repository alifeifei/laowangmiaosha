package com.laowang.kill.model.entity;

import java.io.Serializable;
import java.util.Date;

public class KillSuccessUserInfo implements Serializable {

    private String code;

    private Integer itemId;

    private Integer killId;

    private String userId;

    private Byte status;

    private Date createTime;

    private Integer diffTime;

    private String userName;

    private String phone;

    private String email;

    private String itemName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getKillId() {
        return killId;
    }

    public void setKillId(Integer killId) {
        this.killId = killId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(Integer diffTime) {
        this.diffTime = diffTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return "KillSuccessUserInfo{" +
                "code='" + code + '\'' +
                ", itemId=" + itemId +
                ", killId=" + killId +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", diffTime=" + diffTime +
                ", userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
