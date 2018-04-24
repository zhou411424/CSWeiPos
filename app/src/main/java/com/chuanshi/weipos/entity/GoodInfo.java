package com.chuanshi.weipos.entity;

import java.io.Serializable;

/**
 * Created by zhoulc on 17/12/18.
 */

public class GoodInfo implements Serializable {
    private String name;
    private String num;
    private String amount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
