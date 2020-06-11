package com.laowang.kill.server.service;

import com.laowang.kill.model.entity.KillSuccessUserInfo;

public interface KillService {
    //Boolean killItem(Integer killId,Integer userId) throws Exception;

    KillSuccessUserInfo killRecordDetail(String orderNo) throws Exception;

    //Boolean killItemV2(Integer killId,Integer userId) throws Exception;

    Boolean killItemV3(Integer killId,Integer userId) throws Exception;

    Boolean killItemRedis(Integer killId,Integer userId) throws Exception;

    /*Boolean killItemV4(Integer killId,Integer userId) throws Exception;*/

}
