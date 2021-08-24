package com.zxc.o2o.utils;

import com.google.code.kaptcha.Constants;
import javax.servlet.http.HttpServletRequest;

//验证码比对工具类
public class CodeUtil {
    public static boolean checkVerifyCode(HttpServletRequest request){
        //获取生成的验证码
        String verifyCodeExpected = (String) request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
        System.out.println(verifyCodeExpected+"========生成的=================");
        //获取用户提交的验证码
        String verifyCodeActual = HttpServletRequestUtil.getString(request,"verifyCodeActual");
        if (verifyCodeActual == null || !verifyCodeActual.equals(verifyCodeExpected)){
            return false;
        }
        return true;
    }
}
