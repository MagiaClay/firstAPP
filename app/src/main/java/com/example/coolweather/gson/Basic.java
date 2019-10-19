package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {//更具接口的模式写处类的样式
    @SerializedName("city")
    public String ciytyname;
    //JSON中的一些字段可能不太适合直接作为java字段来命名，注释的方式来让JSON字段和Java字段之间建立映射
    @SerializedName("id")
    public String weatherId;
    public  Update update;
    public class  Update{
        @SerializedName("loc")
        public String updateTime;
    }
}
