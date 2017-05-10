package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 袁刚 on 2017/4/22.
 */

public class ChooseAreaFragment extends Fragment {
    //用于判断是在哪一个层次
    public static final  int LEVEL_PROINCE=0;
    public static final  int LEVEL_CITY=1;
    public static final  int LRVEL_COUNTY=2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList = new ArrayList<>();
    private List<City> cityList = new ArrayList<>();
    private List<County>countyList = new ArrayList<>();
    private  Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView =(ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try{
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (currentLevel==LEVEL_PROINCE){
                        selectedProvince = provinceList.get(i);
                        queryCitys();
                    }
                    else if (currentLevel==LEVEL_CITY){
                        selectedCity = cityList.get(i);
                        queryCounty();
                    }else if (currentLevel==LRVEL_COUNTY){
                        String weatherId =countyList.get(i).getWeatherId();
                        if (getActivity() instanceof MainActivity){

                            Intent intent =new Intent(getActivity(),WeatherActivity.class);
                            intent.putExtra("weather_id",weatherId);
                            startActivity(intent);
                            getActivity().finish();
                        }else if(getActivity() instanceof WeatherActivity){
                            WeatherActivity weatherActivity = (WeatherActivity)getActivity();
                            weatherActivity.drawerLayout.closeDrawers();
                            weatherActivity.swipeRefreshLayout.setRefreshing(true);
                            weatherActivity.requestWeather(weatherId);
                        }
                    }
                }
            });

            backButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    if (currentLevel==LRVEL_COUNTY){
                        queryCitys();

                    }
                    else if (currentLevel==LEVEL_CITY){
                        queryProvinces();
                    }
                }
            });
            queryProvinces();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROINCE;
        }else {
            String address = "http://guolin.tech/api/china";
        queryFromServer(address,"province");
    }
    }

    private void queryCitys(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList
                 ) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else {
            String address = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
            queryFromServer(address,"city");
        }
    }

    private void queryCounty(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList =DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList
                 ) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LRVEL_COUNTY;
        }else {
            String address ="http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
            queryFromServer(address,"county");
        }

    }

    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                      closeProgressDialog();
                      Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                  }
              });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    boolean result = false;
                    if ("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }
                            else if ("city".equals(type)){
                                queryCitys();
                            }
                            else if ("county".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }

            }
        });
    }

    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
