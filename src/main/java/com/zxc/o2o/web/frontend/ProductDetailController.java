package com.zxc.o2o.web.frontend;

import com.zxc.o2o.entity.Product;
import com.zxc.o2o.service.ProductService;
import com.zxc.o2o.utils.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/frontend")
public class ProductDetailController {

    @Autowired
    private ProductService productService;


    @RequestMapping(value = "/listproductdetailpageinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> listProductDetailPageInfo(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        long productId = HttpServletRequestUtil.getLong(request,"productId");
        if (productId > -1L){
            Product product = productService.getProductById(productId);
            modelMap.put("product",product);
            modelMap.put("success",true);
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","empty prodcutId");
        }
        return modelMap;
    }
}
