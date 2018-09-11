package com.example.a1.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("update")
    public Update update;

    public class Update
    {
        @SerializedName("loc")
        public String updateTime;
    }

}