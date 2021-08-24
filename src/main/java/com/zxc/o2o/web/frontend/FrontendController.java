package com.zxc.o2o.web.frontend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/frontend")
public class FrontendController {

    @RequestMapping(value = "/index",method = RequestMethod.GET)
    private String index(){
        return "frontend/index";
    }

    //店铺类别列表路口
    @RequestMapping(value = "/shoplist",method = RequestMethod.GET)
    private String showShopList(){
        return "frontend/shoplist";
    }

    //店铺详情页路由
    @RequestMapping(value = "/shopdetail",method = RequestMethod.GET)
    private String showShopDetail(){
        return "frontend/shopdetail";
    }


    //商品详情页路由
    @RequestMapping(value = "/productdetail",method = RequestMethod.GET)
    private String showProductDetail(){
        return "frontend/productdetail";
    }

}
