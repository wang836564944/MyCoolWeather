package com.example.a1.mycoolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.a1.mycoolweather.gson.Weather;
import com.example.a1.mycoolweather.util.HttpUtil;
import com.example.a1.mycoolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyAutoUpdateService extends Service {
    public MyAutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        updateBingPic();
        updateWeather();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int min = 30 * 60 * 1000;//一小时
        long triggerAtTime = SystemClock.elapsedRealtime() + min;
        Intent i = new Intent(this,MyAutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherMsg = preferences.getString("weather",null);
        if(weatherMsg != null)
        {
            //有缓存，优先于缓存
            Weather weather = Utility.handWeatherResponse(weatherMsg);
            final String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=20b43daed5fa4deea0a3bdaca7815b8b";
            Log.d("MyAutoUpdateService","url :" + weatherUrl);
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(MyAutoUpdateService.this,"网络响应失败啦QAQ",Toast.LENGTH_SHORT).show();
                    Log.d("MyAutoUpdateService","响应失败");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String weatherResponse = response.body().string();
                    Weather weather1 = Utility.handWeatherResponse(weatherResponse);
                    if(weatherResponse != null && "ok".equals(weather1.status))
                    {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyAutoUpdateService.this).edit();
                        editor.putString("weather",weatherResponse);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 更新每日一图
     */
    public void updateBingPic()
    {
        String bingUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(bingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(MyAutoUpdateService.this,"加载每日一图失败啦QAQ",Toast.LENGTH_SHORT).show();
                Log.d("MyAutoUpdateService","每日一图加载失败");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingResponse = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyAutoUpdateService.this).edit();
                editor.putString("bing_pic",bingResponse);
                editor.apply();
            }
        });
    }
}
