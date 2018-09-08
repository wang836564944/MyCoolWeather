package com.example.a1.mycoolweather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    private int id;//实体id
    private int provinceCode;//省代号
    private String provinceName;//省名称

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}
