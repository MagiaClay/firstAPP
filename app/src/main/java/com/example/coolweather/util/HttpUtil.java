package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//将所有省市数据从服务器遍历得到
public class HttpUtil {
    public static void sendOKhttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client =new OkHttpClient();
        Request request =new Request.Builder().url(address).build();//发送亲求
        client.newCall(request).enqueue(callback);//注册一个回调来处理响应返回值市response，可打印
    }
}
