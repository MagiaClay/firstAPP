package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String tmp;
    @SerializedName("cond")
    public More more;

    public class More{
        @SerializedName("txt")
        public String info;//天气信息
    }

}
