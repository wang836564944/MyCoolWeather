package com.example.a1.mycoolweather.ui.Activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.a1.mycoolweather.R;

public class MainActivity extends AppCompatActivity {


    private Fragment chooseAreaFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initView();
    }
    public void initView()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.choose_area_fragment_in_mainActivity,chooseAreaFragment);
        transaction.commit();
    }
}
