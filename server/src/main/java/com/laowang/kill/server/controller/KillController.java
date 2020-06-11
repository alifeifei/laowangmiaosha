package com.laowang.kill.server.controller;

import com.laowang.kill.api.enums.StatusCode;
import com.laowang.kill.api.response.BaseResponse;
import com.laowang.kill.model.entity.KillSuccessUserInfo;
import com.laowang.kill.server.dto.KillDto;
import com.laowang.kill.server.service.KillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/kill")
public class KillController {

    @Autowired
    KillService killService;

    /**
     * 使用redis分布式锁和mysql锁来解决超卖和单个用户多次购买的问题
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = "/execute",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto dto, BindingResult result){
        //result.hasErrors()在这里的意思就是dto.getKillId() == null
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Boolean res=killService.killItemV3(dto.getKillId(),dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕!");
            }
        }catch (Exception e){
            response=new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }
        return response;
    }


    /**
     * 使用redis来解决超卖和单个用户多次购买的问题
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = "/executeByRedis",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeByRedis(@RequestBody @Validated KillDto dto, BindingResult result){
        //result.hasErrors()在这里的意思就是dto.getKillId() == null
        if (result.hasErrors() || dto.getKillId()<=0){
            return new BaseResponse(StatusCode.InvalidParams);
        }

        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Boolean res = killService.killItemRedis(dto.getKillId(), dto.getUserId());
            if (!res){
                return new BaseResponse(StatusCode.Fail.getCode(),"哈哈~商品已抢购完毕");
            }
        } catch (Exception e) {
            return new BaseResponse(StatusCode.Fail.getCode(),e.getMessage());
        }

        return response;
    }



    @RequestMapping("/execute/success")
    public String success(){

        return "executeSuccess";
    }

    @RequestMapping("/execute/fail")
    public String fail(){

        return "executeFail";
    }

    //${system.domain.url}/kill/record/detail/%s
    @RequestMapping("/record/detail/{orderNo}")
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap){
        if (orderNo == null || "".equals(orderNo)){
            return "redirect:/base/error";
        }
        KillSuccessUserInfo killSuccessUserInfo = null;
        try {
            killSuccessUserInfo = killService.killRecordDetail(orderNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (killSuccessUserInfo == null){
            return "redirect:/base/error";
        }
        modelMap.put("info", killSuccessUserInfo);
        return "killRecord";

    }



}
