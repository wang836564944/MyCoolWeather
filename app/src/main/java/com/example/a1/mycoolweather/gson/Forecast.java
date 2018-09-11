package com.example.a1.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {

    @SerializedName("date")
    public String date;

    @SerializedName("cond")
    public Cond cond;

    @SerializedName("tmp")
    public Temperature temperature;

    public class Cond
    {
        @SerializedName("txt_d")
        public String info;
    }

    public class Temperature
    {
        @SerializedName("max")
        public String max;

        @SerializedName("min")
        public String min;
    }
}
