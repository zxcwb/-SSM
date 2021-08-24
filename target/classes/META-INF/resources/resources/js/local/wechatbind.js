$(function () {
    //绑定账号的controller url
    var bindUrl = "/o2o/local/bindlocalauth";
    //从地址栏的URL里获取userType
    //从userType=1则为前端展示系统，其他为店家管理系统
    var userType = getQueryString('usertType');
    $('#submit').click(function () {
        //获取输入的账号
        var username = $('#username').val();
        //获取输入的密码
        var password = $('#password').val();
        //获取输入的验证码
        var verifyCodeActual = $('#j_captcha').val();
        var needVerify = false;
        if (!verifyCodeActual){
            $.toast("请输入验证码!");
            return;
        }
        //访问后台，绑定账号
        $.ajax({
            url:bindUrl,
            async:false,
            cache:false,
            type:"post",
            dataType:'json',
            data:{
                username:username,
                password:password,
                verifyCodeActual:verifyCodeActual
            },
            success:function (data) {
                if (data.success){
                    $.toast("绑定成功!");
                    if (userType == 1){
                        //若用户在前端展示系统页面自动退回到前端展示系统首页
                        window.location.href='/o2o/frontend/index?userType='+userType;
                    }else {
                        //若用户是在店家管理系统页面则自动退回到店铺列表页中
                        window.location.href='/o2o/shop/shoplist?userType='+userType;
                    }
                }else {
                    $.toast("提交失败！"+data.errMsg);
                    $('#captcha_img').click();
                }
            }
        });
    });
});