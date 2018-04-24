package com.chuanshi.weipos.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhoulc on 18/3/24.
 */

public class CategoryInfo implements Serializable {
    private String category;
    private String amount;
    private String number;
    private List<GoodInfo> goodsList;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public List<GoodInfo> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<GoodInfo> goodsList) {
        this.goodsList = goodsList;
    }
}
