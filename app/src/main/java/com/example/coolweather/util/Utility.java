package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    //工具类，处理返回的JSON数据,自己写的接口
    public static  boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){//内容不为空？
            try {
                JSONArray allProvince=new JSONArray(response);
                for (int i=0;i<allProvince.length();i++){
                    JSONObject provinceObject=allProvince.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();//保存处理
                }
                return  true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    //解析返回市的数据
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCitys=new JSONArray(response);
                for (int i=0;i<allCitys.length();i++){
                    JSONObject cityObject=allCitys.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvincedId(provinceId);
                    city.save();//保存到数据库
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
    public static  boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for (int i=0;i<allCounties.length();i++){
                    JSONObject counyuObject=allCounties.getJSONObject(i);
                    County city=new County();
                    city.setCountyName(counyuObject.getString("name"));
                    city.setWeatherId(counyuObject.getString("weather_id"));
                    city.setCityId(cityId);
                    city.save();//保存到数据库
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //将返回的数据解析成Weather实体类
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");//和风天气主接口
            String weatherContent =jsonArray.getJSONObject(0).toString();//第一个基础数据，全部保存到weatherContent
            return new Gson().fromJson(weatherContent,Weather.class);//MARK：反射获取实例,在之前已经用注释规定了其模式，所以可以直接生成
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
