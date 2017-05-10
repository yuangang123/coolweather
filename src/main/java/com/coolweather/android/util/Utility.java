package com.coolweather.android.util;

import android.content.Context;
import android.text.TextUtils;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by 袁刚 on 2017/4/21.
 */

public class Utility {

    public  static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces= new JSONArray(response);
                for (int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response,int provinceid){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCitys=  new JSONArray(response);
                for (int i=0;i<allCitys.length();i++){
                    JSONObject cityObject =allCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceid);
                    city.save();
                }
                return  true;

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return  false;
    }

    public static  boolean handleCountyResponse(String response,int cityid){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray allCountys = new JSONArray(response);
                for (int i=0;i<allCountys.length();i++){
                    JSONObject countyObject =allCountys.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityid);
                    county.save();
                }
                return  true;
            }catch (Exception e){
                e.printStackTrace();
            }


        }
        return  false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     */

    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return  new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
