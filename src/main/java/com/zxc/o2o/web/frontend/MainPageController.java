package com.zxc.o2o.web.frontend;

import com.zxc.o2o.dao.HeadLineDao;
import com.zxc.o2o.entity.HeadLine;
import com.zxc.o2o.entity.ShopCategory;
import com.zxc.o2o.service.HeadLineService;
import com.zxc.o2o.service.ShopCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//响应前端主页的请求
@Controller
@RequestMapping("/frontend")
public class MainPageController {
    @Autowired
    private ShopCategoryService shopCategoryService;

    @Autowired
    private HeadLineService headLineService;

    //初始化前端展示系统的主页信息，包括获取一级店铺类别列表以及头条列表
    @RequestMapping(value = "/listmainpageinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> listMainPageInfo(){
        Map<String,Object> modelMap = new HashMap<>();
        List<ShopCategory> shopCategoryList = new ArrayList<>();
        try {
            //获取一级店铺列表
            shopCategoryList = shopCategoryService.getShopCategoryList(null);
            modelMap.put("shopCategoryList",shopCategoryList);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }
        List<HeadLine> headLineList = new ArrayList<>();
        try {
            //获取状态为1（可用）的头条类列表
            HeadLine headLineCondition = new HeadLine();
            headLineCondition.setEnableStatus(1);
            headLineList = headLineService.getHeadLineList(headLineCondition);
            modelMap.put("headLineList",headLineList);
        } catch (IOException e) {
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            return modelMap;
        }
        modelMap.put("success",true);
        return modelMap;
    }
}
