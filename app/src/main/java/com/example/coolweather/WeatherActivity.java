package com.example.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.service.AutoUpateService;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
    private ImageView bingPicImg;//背景图片
    public SwipeRefreshLayout swipeRefreshLayout;//下拉刷新,后面刷新界面时需要调整
    private  String mWeatherId;//刷新的ID,在刷新时候统一的全局ID
    public DrawerLayout drawerLayout;//拖动页面
    private Button navButton;//导航的返回按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        //Mark设置图片覆盖
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//调用方法来改变显示，参数表示布局会显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//调用Bar设置为透明
        }
        //初始化各个控件
        drawerLayout=findViewById(R.id.drawer_layout);
        navButton=findViewById(R.id.nav_button);
        swipeRefreshLayout=findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置刷新按键的颜色
        bingPicImg=(ImageView)findViewById(R.id.bing_pic_img);
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

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);//本来点击后需要跳转到weather类中的，但只需要直接申请就行
            }
        });
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);//自动将当前类名最为文件名字
        String weatherString=preferences.getString("weather",null);//读取缓存区的接口数据
        if (weatherString!=null){
            //有缓存时直接解析天气

            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId=weather.basic.weatherId;//获得ID
            showWeatherInfo(weather);
        }else {
            //第一次访问没有进行缓存
            mWeatherId=getIntent().getStringExtra("weatherid");//关于主函数传递进来的城市接口对应的id
            weatherLayout.setVisibility(View.INVISIBLE);//布局不可见，使用这个方法可以解决网络申请带来的显示延迟问题
            requestWeather(mWeatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic=preferences.getString("bing_pic",null);//获得图像的
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingOic();//加载图片
        }

    }
    //根据天气的ID请求城市的信息
    public void requestWeather(final String weatherId){
        String weatherUrl ="http://guolin.tech/api/weather?cityid="+weatherId+"&key=b4b6904bd8a74390987cac9e94ee6182";//设置url
        HttpUtil.sendOKhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"从服务器获取信息失败！！",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final  String resoponseText=response.body().string();//获取完整信息
                final  Weather weather=Utility.handleWeatherResponse(resoponseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",resoponseText);//存入信息数据源
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);//取消刷新页面
                    }
                });
            }
        });
        loadBingOic();//加载图片
    }

    //输入的是一个weather的实体类数据，接口数据呈现
    private void  showWeatherInfo(Weather weather){
       String cityName=weather.basic.ciytyname;
       String updateTime=weather.basic.update.updateTime.split(" ")[1];//传入的信息格式
        String degree=weather.now.tmp+"C";
        String weatherInfo=weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();//清空当前的所有子布局
        for (Forecast forecast:weather.forecastList){//list基本信息是肯定有的，aqi和Suggestion不一定有
            //提取每个天气预报信息
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);//从中取出子布局的方法
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_text);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperatrue.max);
            minText.setText(forecast.temperatrue.min);
            forecastLayout.addView(view);//重新设置子布局//添加DD
        }
        if (weather.aqi!=null){//空气质量不一定有
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度"+weather.suggestion.comfort.info;
        String carWash="洗车指数"+weather.suggestion.carWash.info;
        String sport="运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashTextl.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);//显示布局
        Intent intent=new Intent(this, AutoUpateService.class);
        startService(intent);//启动服务，每次显示UI时候都intent
    }
    //加载必应图片
    public void loadBingOic(){
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                Toast.makeText(WeatherActivity.this,"今天没有图片哦！",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String bingPic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }
}
