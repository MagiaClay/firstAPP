package com.example.coolweather.View;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.MainActivity;
import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;//进度条对话框
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_CONTY=2;
    private ProgressDialog progressDialog;//j
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;//适配器
    private List<String> dataList=new ArrayList<>();//展示的存储名字的容器

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;//选中的省份
    private  City selectedCity;//选中的城市
    private  int currentLevel;//当前选中的级别

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);//绑定布局
        titleText=(TextView)view.findViewById(R.id.title_text);
        backButton=(Button)view.findViewById(R.id.back_button);
        listView=(ListView)view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);//标准的List
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();//进行
                }else if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if (currentLevel==LEVEL_CONTY){//如果点击具体的名称数
                    String weatherId=countyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(), WeatherActivity.class);
                    Log.d("MainActivity",weatherId);
                    intent.putExtra("weatherid",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_CONTY){
                    queryCities();//
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    //优先从数据库搜索，若没有则从服务器上寻找
    private void queryProvinces(){
        titleText.setText("天朝");
        backButton.setVisibility(View.GONE);
        provinceList= LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList){
                dataList.add(province.getProvinceName());//把名字放入数组
            }
            adapter.notifyDataSetChanged();//观察改变
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");//根据传入地址和类型从服务器上查询对应的数据
        }
    }

    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList=LitePal.where("provincedid = ?",String.valueOf(selectedProvince.getId())).find(City.class);//反射查询返回实例
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//开始的索引值，从头开始
            currentLevel=LEVEL_CITY;//当前操作步骤
        }else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;//省份地址
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=LitePal.where("cityId = ?",String.valueOf(selectedCity.getId())).find(County.class);//从数据库获得数据
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CONTY;
        }else {
            int proviceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+proviceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    //根据传入地址和类型从服务器上查询对应的数据
    private void queryFromServer(String address,final String type){
        showProgressDialog();//展开进度dialog
        HttpUtil.sendOKhttpRequest(address, new Callback() {//以OKhttp发送请求
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String reposeText=response.body().string();//获取返回的信息
                boolean result=false;
                if ("province".equals(type)){
                    result= Utility.handleProvinceResponse(reposeText);//保存所有对应类型的数据,顺带保存了对应的城市Id
                }else if ("city".equals(type)){
                    result= Utility.handleCityResponse(reposeText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result= Utility.handleCountyResponse(reposeText,selectedCity.getId());
                }
                //如果有返回结果
                if (result){
                    getActivity().runOnUiThread(new Runnable() {//跟新UI界面
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }

        });
    }

    //显示进度对话框
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());//获得当前活动的进度条
            progressDialog.setMessage("正在加载中");
            progressDialog.setCancelable(false);//无法通过回退退出
        }
        progressDialog.show();//展示
    }
    //关闭对话框
    private void  closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();//取消对话框
        }
    }
}
