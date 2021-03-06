package com.zxc.o2o.web.shopadmin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zxc.o2o.dto.ImageHolder;
import com.zxc.o2o.dto.ProductCategoryExecution;
import com.zxc.o2o.dto.ProductExecution;
import com.zxc.o2o.dto.ShopExecution;
import com.zxc.o2o.entity.*;
import com.zxc.o2o.enums.ProductCategoryStateEnum;
import com.zxc.o2o.enums.ProductStateEnum;
import com.zxc.o2o.exceptions.ProductOperationException;
import com.zxc.o2o.service.ProductCategoryService;
import com.zxc.o2o.service.ProductService;
import com.zxc.o2o.utils.CodeUtil;
import com.zxc.o2o.utils.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/shopadmin")
public class ProductManagementController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    //支持上传商品详情图的最大数值
    private static final int IMAGEMAXCOUNT = 6;

    //商品编辑
    @RequestMapping(value = "/modifyproduct",method = RequestMethod.POST)
    @ResponseBody
    private Map<String,Object> modifyProduct(HttpServletRequest request){
        Map<String,Object> modelMap = new HashMap<>();
        //是商品编辑的时候调用还是上下架操作的时候调用
        //若为商品编辑的时候进行验证码判断，后者就可以跳过验证码验证
        boolean statusChange = HttpServletRequestUtil.getBoolean(request,"statusChange");
        //验证码判断
        if (!statusChange && !CodeUtil.checkVerifyCode(request)){
           modelMap.put("success",false);
           modelMap.put("errMsg","输入了错误的验证码");
           return modelMap;
        }
        //接收前端参数的变量的初始化，包括商品，缩略图，商品详情图列表实体类
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = null;
        ImageHolder thumbnail = null;
        List<ImageHolder> productImgList = new ArrayList<>();
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        //若请求中存在文件流，则取出相关的文件（包括缩略图和详情图）
        try {
            if (multipartResolver.isMultipart(request)){
               thumbnail = handleImage(request,thumbnail,productImgList);
            }
        } catch (IOException e) {
            modelMap.put("success",false);
            modelMap.put("errMsg","上传图片不能为空");
            return modelMap;
        }
        try {
            String productStr = HttpServletRequestUtil.getString(request,"productStr");
            //尝试获取前端传过来的表单string流并将其转换成Product实体类
            product = objectMapper.readValue(productStr,Product.class);
        } catch (Exception e) {
           modelMap.put("success",false);
           modelMap.put("errMsg",e.toString());
           return modelMap;
        }

        //非空判断
        if (product != null){
            try {
                //从Session中获取当前店铺的Id并赋值给product，减少对前端数据的依赖
                Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
                Shop shop = new Shop();
                shop.setShopId(currentShop.getShopId());
                product.setShop(shop);
                //开始进行商品信息的变更操作
                ProductExecution productExecution = productService.modifyProduct(product,thumbnail,productImgList);
                if (productExecution.getState() == ProductStateEnum.SUCCESS.getState()){
                    modelMap.put("success",true);
                }else {
                    modelMap.put("success",false);
                    modelMap.put("errMsg",productExecution.getStateInfo());
                }
            }catch (RuntimeException e){
                modelMap.put("success",false);
                modelMap.put("errMsg",e.toString());
                return modelMap;
            }
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","请输入商品信息");
        }
        return modelMap;
    }

    //获取当前商品编辑的信息
    @RequestMapping(value = "/getproductbyid")
    @ResponseBody
    private Map<String,Object> getProductById(@RequestParam Long productId){
        Map<String,Object> modelMap = new HashMap<>();
        //非空判断
        if (productId > -1){
            //获取商品信息
            Product product = productService.getProductById(productId);
            //获取该店铺下的商品类别列表
            List<ProductCategory> productCategoryList =
                    productCategoryService.getProductCategoryList(product.getShop().getShopId());
            modelMap.put("product",product);
            modelMap.put("productCategoryList",productCategoryList);
            modelMap.put("success",true);
        }else {
            modelMap.put("success",false);
            modelMap.put("errMsg","empty productId");
        }
        return modelMap;
    }


    //商品添加
    @RequestMapping(value = "/addproduct", method = RequestMethod.POST)
    @ResponseBody
    private Map<String, Object> addProduct(HttpServletRequest request) {
        Map<String, Object> modelMap = new HashMap<>();
        //验证码校验
        if (CodeUtil.checkVerifyCode(request)) {
            modelMap.put("success", false);
            modelMap.put("errMsg", "输入了错误的验证码");
        }

        //接收前端参数的变量的初始化，包括商品，缩略图，详情图列表实体类         !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        ObjectMapper objectMapper = new ObjectMapper();
        Product product = null;
        String productStr = HttpServletRequestUtil.getString(request, "productStr");
        MultipartHttpServletRequest multipartHttpServletRequest = null;
        ImageHolder thumbnail = null;
        List<ImageHolder> productImgList = new ArrayList<>();
        //获取文件流
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
        try {
            //若请求中存在文件流，则取出相关的文件（包括缩略图和详情图）
            if (multipartResolver.isMultipart(request)) {
                thumbnail = handleImage(request,thumbnail,productImgList);
            } else {
                modelMap.put("success", false);
                modelMap.put("errMsg", "上传图片不能为null");
                return modelMap;
            }
        } catch (Exception e) {
            modelMap.put("success", false);
            modelMap.put("errMsg", e.toString());
            return modelMap;
        }
        try {
            //尝试获取前端传过来的表单String流并将其转换成Product实体类
            product = objectMapper.readValue(productStr, Product.class);
        } catch (Exception e) {
            modelMap.put("success", false);
            modelMap.put("errMsg", e.toString());
            return modelMap;
        }
        //若Product信息，缩略图以及详情图列表为非空，则开始进行商品添加操作
        if (product != null && thumbnail != null && productImgList.size() > 0) {
            try {
                //从session中获取当前店铺的Id并赋值给product，减少对前端数据的依赖
                Shop currentShop = (Shop) request.getSession().getAttribute("currentShop");
              /*  Shop shop = new Shop();
                shop.setShopId(currentShop.getShopId());*/
                product.setShop(currentShop);
                //执行添加操作
                ProductExecution pe = productService.addProduct(product, thumbnail, productImgList);
                if (pe.getState() == ProductStateEnum.SUCCESS.getState()) {
                    modelMap.put("success", true);
                } else {
                    modelMap.put("success", false);
                    modelMap.put("errMsg", pe.getStateInfo());
                }
            } catch (ProductOperationException e) {
                modelMap.put("success", false);
                modelMap.put("errMsg", e.toString());
                return modelMap;
            }
        } else {
            modelMap.put("success", false);
            modelMap.put("errMsg", "请输入商品信息");
        }
        return modelMap;
    }

    @RequestMapping(value = "/getproductlistbyshop", method = RequestMethod.GET)
    @ResponseBody
    private Map<String, Object> listProductsByShop(HttpServletRequest request) {
        Map<String, Object> modelMap = new HashMap<String, Object>();

        //获取前台传过来的页码  暂时使用地址栏传值分页范围
        int pageIndex = HttpServletRequestUtil.getInt(request, "pageIndex");
        //获取前台传过来的每页要求返回的商品数上限
        int pageSize = HttpServletRequestUtil.getInt(request, "pageSize");
        //从当前session中获取店铺信息，主要是获取shopId
        Shop currentShop = (Shop) request.getSession().getAttribute(
                "currentShop");
        //空值判断
        if ((pageIndex > -1) && (pageSize > -1) && (currentShop != null)
                && (currentShop.getShopId() != null)) {
            //获取传入需要检索的条件，包括是否需要从某个商品类别以及模糊查找商品名去筛选某个店铺下的商品列表
            //筛选条件可以进行排列组合
            long productCategoryId = HttpServletRequestUtil.getLong(request,
                    "productCategoryId");
            String productName = HttpServletRequestUtil.getString(request,
                    "productName");
            //!!!!!!!!!!!!
            Product productCondition = compactProductConditionSearch(
                    currentShop.getShopId(), productCategoryId, productName);
            //传入查询条件以及分页信息进行查询，返回响应商品列表以及总数
            ProductExecution pe = productService.getProductList(
                    productCondition, pageIndex, pageSize);
            modelMap.put("productList", pe.getProductList());
            modelMap.put("count", pe.getCount());
            modelMap.put("success", true);
        } else {
            modelMap.put("success", false);
            modelMap.put("errMsg", "empty pageSize or pageIndex or shopId");
        }
        return modelMap;
    }

    private ImageHolder handleImage(HttpServletRequest request, ImageHolder thumbnail, List<ImageHolder> productImgList) throws IOException {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        //取出缩略图并构建的ImageHolder对象
        CommonsMultipartFile thumbnailFile = (CommonsMultipartFile) multipartRequest.getFile("thumbnail");
        if (thumbnailFile != null){
            thumbnail = new ImageHolder(thumbnailFile.getOriginalFilename(),thumbnailFile.getInputStream());
        }
        //取出详情图列表并构建List<ImageHolder>列表对象，最多支持6张图片上传
        for (int i = 0;i < IMAGEMAXCOUNT;i++){
            CommonsMultipartFile productImgFile =
                    (CommonsMultipartFile) multipartRequest.getFile("productImg"+i);
            if (productImgFile != null){
                //若取出的第i个详情图片的文件流不为空，则将其加入详情图列表
                ImageHolder productImg = new ImageHolder(thumbnailFile.getOriginalFilename(),
                        thumbnailFile.getInputStream());
                productImgList.add(productImg);
            }else {
                //若取出的第i个详情图片流为空，那么就终止循环
                break;
            }
        }
        return thumbnail;
    }

    /*
    * 封装产品查询条件到Product实例中
    *
    * shopId(mandatory)
    * productCategoryId(optional)
    * productName(optional)
    * */
    private Product compactProductConditionSearch(Long shopId, long productCategoryId, String productName) {
        Product productCondition = new Product();
        Shop shop = new Shop();
        shop.setShopId(shopId);
        productCondition.setShop(shop);
        //若指定类别要求则添加进去
        if (productCategoryId != -1L){
            ProductCategory productCategory = new ProductCategory();
            productCategory.setProductCategoryId(productCategoryId);
            productCondition.setProductCategory(productCategory);
        }

        //若有商品名模糊查询的要求则添加进去
        if (productName != null){
            productCondition.setProductName(productName);
        }
        return productCondition;
    }
}
