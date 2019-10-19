package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//对返回的JSON数据进行创建
public class Weather {
    public String status;//状态,成功则返回ok，失败则返回失败原因
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
