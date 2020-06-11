package com.laowang.kill.server.controller;

import com.laowang.kill.api.enums.StatusCode;
import com.laowang.kill.api.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/base")
public class BaseController {

    @RequestMapping("/welcome")
    public String welcome(String name, ModelMap modelMap){
        if (name == null || "".equals(name)){
            name = "加油";
        }
        modelMap.put("name",name);
        return "welcome";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String  data(String name){
        if (name == null || "".equals(name)){
            name = "加油";
        }
        return name;
    }

    @RequestMapping("/response")
    @ResponseBody
    public BaseResponse<String> response(String name){
        BaseResponse<String> response = new BaseResponse<>(StatusCode.Success);
        if (name == null || "".equals(name)){
            name = "加油";
        }
        response.setData(name);
        return response;
    }

    @RequestMapping("/error")
    public String error(){
        return "error";
    }
}
