package com.laowang.kill.server.controller;

import com.laowang.kill.model.entity.ItemKill;
import com.laowang.kill.server.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ItemController {

    private static final String PREIX = "/item";

    @Autowired
    ItemService itemService;
    /**
     * 获取待秒杀商品列表
     * @return
     */
    @RequestMapping("/")
    public String list(ModelMap modelMap){
        try {
            List<ItemKill> killItems = itemService.getKillItems();
            modelMap.put("list",killItems);

        } catch (Exception e) {
            return "redirect:/base/error";
        }
        return "list";
    }
    @RequestMapping(PREIX + "/detail/{id}")
    public String detail(@PathVariable Integer id, ModelMap modelMap){
        if (id == null || id <= 0 ){
            return "redirect:/base/error";
        }

        try{
            ItemKill killDetail = itemService.getKillDetail(id);
            modelMap.put("detail",killDetail);
        }catch (Exception e){

            return "redirect:/base/error";
        }
        return "info";
    }
}
