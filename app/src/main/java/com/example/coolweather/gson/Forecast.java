package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

//是以一个数组，写法不同,只写一个即可
public class Forecast {
    public String date;//那一天
    @SerializedName("tmp")
    public Temperatrue temperatrue;
    @SerializedName("cond")
    public More more;
    public class Temperatrue{
        public String max;
        public String min;
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
