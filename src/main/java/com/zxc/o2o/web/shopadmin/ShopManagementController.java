package com.zxc.o2o.web.shopadmin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxc.o2o.dto.ImageHolder;
import com.zxc.o2o.dto.ShopExecution;
import com.zxc.o2o.entity.Area;
import com.zxc.o2o.entity.PersonInfo;
import com.zxc.o2o.entity.Shop;
import com.zxc.o2o.entity.ShopCategory;
import com.zxc.o2o.enums.ShopSateEnum;
import com.zxc.o2o.exceptions.ShopOperationException;
import com.zxc.o2o.service.AreaService;
import com.zxc.o2o.service.ShopCategoryService;
import com.zxc.o2o.service.ShopService;
import com.zxc.o2o.utils.CodeUtil;
import com.zxc.o2o.utils.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "shopadmin")
public class ShopManagementController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopCategoryService shopCategoryService;

    @Autowired
    private AreaService areaService;

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/getshopmanagerinfo",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object>  getShopManagerInfo(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<String,Object>();
        long shopId = HttpServletRequestUtil.getLong(request,"shopId");
        if (shopId <= 0){
            Object currentShopObj = request.getSession().getAttribute("currentShop");
            if (currentShopObj == null){
                modelMap.put("redirect",true);
                modelMap.put("url","/o2o/shopadmin/shoplist");
            }else {
                Shop currentShop = (Shop) currentShopObj;
                modelMap.put("redirect",false);
                modelMap.put("shopId",currentShop.getShopId());
            }
        }else {
            Shop currentShop = new Shop();
            currentShop.setShopId(shopId);
            request.getSession().setAttribute("currentShop",currentShop);
            modelMap.put("redirect",false);
        }
        return modelMap;
    }

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/getshoplist",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopList(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<String,Object>();
        //????????????
        PersonInfo user = new PersonInfo();
        /*  user.setUserId(8L);
        user.setName("test");
        request.getSession().getAttribute("user");*/
        user = (PersonInfo) request.getSession().getAttribute("user");
        System.out.println("user==========="+user);
        List<Shop> shopList = new ArrayList<Shop>();
        try {
            Shop shopCondition = new Shop();
            shopCondition.setOwner(user);
            ShopExecution se = shopService.getShopList(shopCondition,0,100);
            modelMap.put("shopList",se.getShopList());
            modelMap.put("user",user);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/getshopbyid",method = RequestMethod.GET)
    @ResponseBody
    private Map<String,Object> getShopById(HttpServletRequest request){
        Map<String ,Object> modelMap = new HashMap<String,Object>();
        Long shopId = HttpServletRequestUtil.getLong(request,"shopId");
        if (shopId > -1){
            try {
                Shop shop = shopService.getByShopId(shopId);
                List<Area> areaList = areaService.getAreaList();
                modelMap.put("shop",shop);
                modelMap.put("areaList",areaList);
                modelMap.put("success",true);
            }catch (Exception e){
                modelMap.put("success",false);
                modelMap.put("errMsg",e.toString());
            }
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","empty shopId");
        }
        return modelMap;
    }

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/getshopinitinfo",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getShopInitInfo(){
        Map<String,Object> modelMap = new HashMap<String,Object>();
        List<ShopCategory> shopCategoryList = new ArrayList<ShopCategory>();
        List<Area> areaList = new ArrayList<>();
        try {
            //??????parent_id???????????????????????????????????????redis??????????????????????????????Could not get a resource from the pool
            shopCategoryList = shopCategoryService.getShopCategoryList(new ShopCategory());
            //??????????????????
            areaList = areaService.getAreaList();
            modelMap.put("shopCategoryList",shopCategoryList);
            modelMap.put("areaList",areaList);
            modelMap.put("success",true);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
        }
        return modelMap;
    }

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/registershop",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> registerShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<String,Object>();
        if (!CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","???????????????????????????");
            return modelMap;
        }
        //1?????????????????????????????????????????????????????????????????????
        String shopStr = HttpServletRequestUtil.getString(request,"shopStr");
        ObjectMapper mapper = new ObjectMapper();
        Shop shop = null;
        try {
            shop = mapper.readValue(shopStr,Shop.class);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            System.out.println(modelMap.get("success"));
            return modelMap;
        }
        CommonsMultipartFile shopImg = null;  //?????????CommonsMultipartFile --> File
        CommonsMultipartResolver commonsMultipartResolver =
                new CommonsMultipartResolver(request.getSession().getServletContext());
        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            //shopImg = (File) multipartHttpServletRequest.getFile("shopImg");
            shopImg = (CommonsMultipartFile) multipartHttpServletRequest.getFile("shopImg");
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","????????????????????????");
            System.out.println(modelMap.get("success"));
            return modelMap;
        }
        //2???????????????
        if (shop != null && shopImg != null){
            PersonInfo owner = (PersonInfo) request.getSession().getAttribute("user");
            //session?????????
            //????????????owner
            shop.setOwner(owner);
            ShopExecution se = null;//??????????????????????????????????????????????????????????????????
            try {
                ImageHolder imageHolder = new ImageHolder(shopImg.getOriginalFilename(),shopImg.getInputStream());
                se = shopService.addShop(shop, imageHolder);

                if (se.getState() == ShopSateEnum.CHECK.getState()) {
                    modelMap.put("success", true);
                    //????????????????????????????????????
                    List<Shop> shopList = (List<Shop>)request.getSession().getAttribute("shopList");
                    if (shopList == null || shopList.size() == 0) {
                        shopList = new ArrayList<Shop>();
                    }
                        shopList.add(se.getShop());
                        request.getSession().setAttribute("shopList",shopList);

                    return modelMap;
                } else {
                    modelMap.put("success", false);
                    modelMap.put("errMsg", se.getStateInfo());
                    System.out.println(modelMap.get("success"));
                    return modelMap;
                }
            }catch (ShopOperationException e){
                modelMap.put("success", false);
                modelMap.put("errMsg", e.getMessage());
            }catch(IOException e){
                    modelMap.put("success", false);
                    modelMap.put("errMsg", e.getMessage());
             }
            return modelMap;

        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","?????????????????????");
            System.out.println(modelMap.get("success"));
            return modelMap;
        }
        //3???????????????
    }

    @SuppressWarnings(value = "unchecked")
    @RequestMapping(value = "/modifyshop",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> modifyShop(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<String,Object>();
        if (!CodeUtil.checkVerifyCode(request)){
            modelMap.put("success",false);
            modelMap.put("errMsg","???????????????????????????");
            return modelMap;
        }
        //1?????????????????????????????????????????????????????????????????????
        String shopStr = HttpServletRequestUtil.getString(request,"shopStr");
        ObjectMapper mapper = new ObjectMapper();
        Shop shop = null;
        //??????shopId
        try {
            shop = mapper.readValue(shopStr,Shop.class);
        }catch (Exception e){
            modelMap.put("success",false);
            modelMap.put("errMsg",e.getMessage());
            System.out.println(modelMap.get("success"));
            return modelMap;
        }
        CommonsMultipartFile shopImg = null;  //?????????CommonsMultipartFile --> File
        CommonsMultipartResolver commonsMultipartResolver =
                new CommonsMultipartResolver(request.getSession().getServletContext());
        if (commonsMultipartResolver.isMultipart(request)){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            //shopImg = (File) multipartHttpServletRequest.getFile("shopImg");
            shopImg = (CommonsMultipartFile) multipartHttpServletRequest.getFile("shopImg");
        }
        //?????????????????????
        /*else {
            modelMap.put("success",false);
            modelMap.put("errMsg","????????????????????????");
            System.out.println(modelMap.get("success"));
            return modelMap;
        }*/
        //2?????????????????????
        /*long shopId = HttpServletRequestUtil.getLong(request,"shopId");
        System.out.println("shopId="+shopId+"======================");
        shop.setShopId(shopId);*/

        if (shop != null && shop.getShopId() != null){
            ShopExecution se = null;//??????????????????????????????????????????????????????????????????
            try {
                if (shopImg == null){
                    se = shopService.modifyShop(shop,null);
                }else {
                    ImageHolder imageHolder = new ImageHolder(shopImg.getOriginalFilename(),shopImg.getInputStream());
                    se = shopService.modifyShop(shop,imageHolder);
                }
            }catch (ShopOperationException e){
                modelMap.put("success", false);
                modelMap.put("errMsg", e.getMessage());
            }catch(IOException e){
                modelMap.put("success", false);
                modelMap.put("errMsg", e.getMessage());
            }
            if (se.getState() == ShopSateEnum.SUCCESS.getState()){
                modelMap.put("success",true);
                System.out.println(modelMap.get("success"));
                return modelMap;
            }else {
                modelMap.put("success",false);
                modelMap.put("errMsg",se.getStateInfo());
                System.out.println(modelMap.get("success"));
                return modelMap;
            }
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","???????????????id");
            System.out.println(modelMap.get("success"));
            return modelMap;
        }
        //3???????????????
    }
}
