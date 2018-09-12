package com.example.a1.mycoolweather.ui.Activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.a1.mycoolweather.R;
import com.example.a1.mycoolweather.gson.Forecast;
import com.example.a1.mycoolweather.gson.Weather;
import com.example.a1.mycoolweather.util.HttpUtil;
import com.example.a1.mycoolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActiviy extends AppCompatActivity {

    private ImageView mBingPic;
    private ScrollView mWeatherLayout;//天气布局
    private TextView mTitleText;//城市名
    private TextView mTitleUpdateTime;//时间
    private TextView mDegreeText;//当前气温
    private TextView mWeatherInfoText;//天气概况
    private LinearLayout forecastLayout;//天气信息布局
    private TextView mAQIText;//AQI指数
    private TextView PM25Text;//PM2.5指数
    private TextView mComfortText;//舒适度
    private TextView mCarWashText;//洗车指数
    private TextView mSportText;//运动建议
    public SwipeRefreshLayout swipeRefreshLayout;//刷新界面
    public DrawerLayout mDrawerLayout;//滑动界面
    private Button exchangeCityBt;//切换城市界面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activiy);
        //隐藏状态栏
        if(Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            //活动布局显示在状态栏之上
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明的
        }

        initView();
    }

    private void initView()
    {
        mWeatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        mTitleText = (TextView)findViewById(R.id.title_city_weather);
        mTitleUpdateTime = (TextView)findViewById(R.id.title_update_time_weather);
        mDegreeText = (TextView)findViewById(R.id.degree_text_now);
        mWeatherInfoText = (TextView)findViewById(R.id.text_info_text_now);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        mAQIText = (TextView)findViewById(R.id.aqi_text);
        PM25Text = (TextView)findViewById(R.id.pm25_text);
        mComfortText = (TextView)findViewById(R.id.comfort_text);
        mCarWashText = (TextView)findViewById(R.id.car_wash_text);
        mSportText = (TextView)findViewById(R.id.sport_text);
        mBingPic = (ImageView)findViewById(R.id.bing_pic_img);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swip_refresh_layout);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        exchangeCityBt = (Button)findViewById(R.id.exchange_city_bt);

        final String weatherId;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherMessage = preferences.getString("weather",null);
        String bingPictureUrl = preferences.getString("bing_picture",null);
        if(bingPictureUrl != null)
        {
            Glide.with(this).load(bingPictureUrl).into(mBingPic);
        }
        else
        {
            loadBingPicture();
        }
        if(weatherMessage == null)
        {
            //从网络获取
            weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
        else
        {
            //从本地读取
            Weather weatherInfo = Utility.handWeatherResponse(weatherMessage);
            weatherId = weatherInfo.basic.weatherId;
            showWeatherInfo(weatherInfo);
        }

        //打开隐藏菜单
        exchangeCityBt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                requestWeather(weatherId);
            }
        });


    }

    /**
     * 从网络获取天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId)
    {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=20b43daed5fa4deea0a3bdaca7815b8b";
        Log.d("WeatherActivity","url: " + weatherUrl);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActiviy.this,"天气请求失败QAQ",Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String reponseText = response.body().string();
                final Weather weather = Utility.handWeatherResponse(reponseText);
                Log.d("WeatherActivity","response: " +reponseText);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if(weather != null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActiviy.this).edit();
                            editor.putString("weather",reponseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActiviy.this,"获取天气失败QAQ",Toast.LENGTH_SHORT).show();
                            Log.d("WeatherActivity","Response fail");
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPicture();
    }

    /**
     * 处理并显示天气信息
     * @param weather
     */
    public void showWeatherInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String cityUpdateTime = weather.basic.update.updateTime;
        String degree = weather.now.temperature + "°C";
        String confortInfo = weather.now.cond.info;
        mTitleText.setText(cityName);
        mTitleUpdateTime.setText(cityUpdateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(confortInfo);

        forecastLayout.removeAllViews();
        for(Forecast forecast : weather.forecastListe)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            Log.d("WeatherActivity","Time :" + dateText.getText().toString());
            infoText.setText(forecast.cond.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if(weather.aqi != null)
        {
            PM25Text.setText(weather.aqi.aqiCity.aqi);
            mAQIText.setText(weather.aqi.aqiCity.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
    }


    /**
     * 加载每日一图背景图
     */
    public void loadBingPicture()
    {
        String bingUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActiviy.this,"每日一图背景加载失败QAQ",Toast.LENGTH_SHORT).show();
                //e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPictureUrl = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActiviy.this).edit();
                editor.putString("bing_picture",bingPictureUrl);
                editor.apply();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Glide.with(WeatherActiviy.this).load(bingPictureUrl).into(mBingPic);
                    }
                });
            }
        });
    }
}
