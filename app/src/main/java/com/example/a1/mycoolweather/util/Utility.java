package com.example.a1.mycoolweather.util;

import android.text.TextUtils;

import com.example.a1.mycoolweather.db.City;
import com.example.a1.mycoolweather.db.County;
import com.example.a1.mycoolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONObject;

public class Utility {

    /**
     * 解析处理服务器返回的省级数据
     * @param response 返回的数据
     * @return true 解析成功
     */
    public static boolean handleProvinceResponse(String response)
    {
        if(!TextUtils.isEmpty(response))
        {
            try
            {
                JSONArray provinces = new JSONArray(response);
                for(int i = 0;i < provinces.length();i++)
                {
                    JSONObject jsonObject = provinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceCode(jsonObject.getInt("id"));
                    province.setProvinceName(jsonObject.getString("name"));
                    province.save();
                }
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 市
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCityHttpResponse(String response,int provinceId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try
            {
                JSONArray cities = new JSONArray(response);
                for(int i = 0;i < cities.length();i++)
                {
                    JSONObject jsonObject = cities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 县
     */
    public static boolean handleCountyHttpResponse(String response,int cityId)
    {
        if(!TextUtils.isEmpty(response))
        {
            try
            {
                JSONArray counties = new JSONArray(response);
                for(int i = 0;i < counties.length();i++)
                {
                    JSONObject jsonObject = counties.getJSONObject(i);
                    County county = new County();
                    county.setCountyCode(jsonObject.getInt("id"));
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
}
