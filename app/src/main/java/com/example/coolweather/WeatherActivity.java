package com.example.coolweather;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;//三个模块的卷轴布局、
    private TextView titleCity;//城市的名字
    private TextView titleUpdateTime;//更新时间
    private TextView degreeText;//温度信息
    private TextView weatherInfoText;//天气的具体信息
    private LinearLayout forecastLayout;//预报模块
    private TextView aqiText;//空气质量模块信息
    private TextView pm25Text;
    private TextView comfortText;//舒适建议
    private TextView carWashTextl;//洗车建议
    private TextView sportText;//运动建议

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //初始化各个控件
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        forecastLayout=findViewById(R.id.forecast_layout);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashTextl=findViewById(R.id.car_wash_text);
        sportText=findViewById(R.id.sport_text);
        //初始化完成

        SharedPreferences preferences=

    }
}
