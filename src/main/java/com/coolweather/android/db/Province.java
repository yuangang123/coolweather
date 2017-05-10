package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 袁刚 on 2017/4/21.
 */

public class Province extends DataSupport {//每个Litepal中的类都需要继承dataSupport类
    private  int id;//每个实体类都应该具有的
    private String provinceName;//记录省的名字
    private int provinceCode;//记录省的代号

    public int getId() {
        return id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
