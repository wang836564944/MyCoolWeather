package com.example.a1.mycoolweather.ui.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.a1.mycoolweather.R;
import com.example.a1.mycoolweather.db.City;
import com.example.a1.mycoolweather.db.County;
import com.example.a1.mycoolweather.db.Province;
import com.example.a1.mycoolweather.ui.Activity.MainActivity;
import com.example.a1.mycoolweather.ui.Activity.WeatherActiviy;
import com.example.a1.mycoolweather.util.HttpUtil;
import com.example.a1.mycoolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static int LEVEL_COUNTY = 0;
    public static int LEVEL_CITY = 1;
    public static int LEVEL_PROVINCE = 2;

    private ProgressDialog dialog;
    private TextView tvTitleText;//标题
    private Button btBack;//返回按钮
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表
    private Province provinced;//选中的省
    private City citied;//选中的市
    private County countied;//选中的县

    private int currentLevel;//选中等级

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.choose_area_fragment,container,false);
        tvTitleText = (TextView)view.findViewById(R.id.tv_title_text);
        btBack = (Button)view.findViewById(R.id.bt_back);
        listView = (ListView)view.findViewById(R.id.choose_list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE)
                {
                    provinced = provinceList.get(position);
                    queryCity();
                }
                else if(currentLevel == LEVEL_CITY)
                {
                    citied = cityList.get(position);
                    queryCounty();
                }
                else if(currentLevel == LEVEL_COUNTY)
                {
                    String weatherId = countyList.get(position).getWeatherId();
                    Log.d("ChooseAreaFragment","Weather id : " + weatherId);
                    if(getActivity() instanceof MainActivity)
                    {
                        Intent intent = new Intent(getActivity(), WeatherActiviy.class);
                        Log.d("ChooseAreaFragment","weather id :" + weatherId);
                        intent.putExtra("weather_id",weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if(getActivity() instanceof WeatherActiviy)
                    {
                        WeatherActiviy weatherActiviy = (WeatherActiviy) getActivity();
                        weatherActiviy.mDrawerLayout.closeDrawers();
                        weatherActiviy.swipeRefreshLayout.setRefreshing(true);
                        weatherActiviy.requestWeather(weatherId);
                    }
                }
            }
        });
        btBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(currentLevel == LEVEL_COUNTY)
                {
                    queryCity();
                }
                else if(currentLevel == LEVEL_CITY)
                {
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有在服务器查询
     */
    private void queryProvince()
    {
        tvTitleText.setText("中国");
        btBack.setVisibility(View.GONE);//隐藏Button
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0)
        {
            //本地数据库查询
            dataList.clear();
            for(Province province : provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else
        {
            //服务器查询
            String address = "http://guolin.tech/api/china";
            queryFromService(address,"province");
        }
    }

    /**
     * 市级数据
     */
    public void queryCity()
    {
        tvTitleText.setText(provinced.getProvinceName());
        btBack.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(provinced.getId()))
                   .find(City.class);
        if(cityList.size() >0)
        {
            dataList.clear();
            for(City city : cityList)
            {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else
        {
            String address = "http://guolin.tech/api/china/" + provinced.getProvinceCode();
            queryFromService(address,"city");
        }
    }

    /**
     * 县级数据
     */
    private void queryCounty()
    {
        tvTitleText.setText(citied.getCityName());
        btBack.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",String.valueOf(citied.getId()))
                    .find(County.class);
        if(countyList.size() > 0)
        {
            dataList.clear();
            for(County county : countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else
        {
            String address = "http://guolin.tech/api/china/" + provinced.getProvinceCode() + "/" + citied.getCityCode();
            queryFromService(address,"county");
        }
    }

    /**
     * 根据传入的地址从服务器查询省、市、县数据
     * @param address 地址
     * @param type
     */
    private void queryFromService(String address,final String type)
    {
        showDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //如果接收失败则通过RunOnUiThread返回主线程
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        closeDialog();
                        Toast.makeText(getContext(),"加载失败...",Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //接收到返回的信息
                String responseText = response.body().string();
                Log.d("ChooseAreaFragment","response :" + responseText);
                boolean result = false;

                //判断接收到的信息是否处理成功
                if("province".equals(type))
                {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type))
                {
                    result = Utility.handleCityHttpResponse(responseText,provinced.getId());
                }
                else if("county".equals(type))
                {
                    result = Utility.handleCountyHttpResponse(responseText,citied.getId());
                }
                if(result)
                {
                    //如果处理成功则根据type显示对应列表
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            {
                                closeDialog();
                                if("province".equals(type))
                                {
                                    queryProvince();
                                }
                                else if("city".equals(type))
                                {
                                    queryCity();
                                }
                                else if("county".equals(type))
                                {
                                    queryCounty();

                                }
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示对话框
     */
    public void showDialog()
    {
        if(dialog == null)
        {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("正在加载中，请稍等一会QAQ");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    public void closeDialog()
    {
        if(dialog != null)
        {
            dialog.dismiss();
        }
    }
}
