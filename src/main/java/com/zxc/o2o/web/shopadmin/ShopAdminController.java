package com.zxc.o2o.web.shopadmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "shop",method = RequestMethod.GET)
public class ShopAdminController {


    @RequestMapping(value = "/shopedit")
    public String shopEdit(){
        return "shop/shopedit";
    }

    @RequestMapping(value = "/shopregistry")
    public String shopRegistry(){
        return "shop/shopregistry";
    }


    @RequestMapping(value = "/shopmanage")
    public String shopManage(){
        return "shop/shopmanage";
    }


    @RequestMapping(value = "/shoplist")
    public String shoplist(){
        return "shop/shoplist";
    }

    @RequestMapping(value = "/productcategorymanage")
    public String productCategoryManage(){
        //商品类型管理
        return "shop/productcategorymanage";
    }

    //商品信息编辑/添加
    @RequestMapping(value = "/productedit")
    public String productOperation(){
        //转发到商品添加/编辑页面
        return "shop/productedit";
    }

    //商品管理
    @RequestMapping(value = "/productmanage")
    public String productmanage(){
        return "shop/productmanage";
    }

}
