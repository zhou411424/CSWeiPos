package com.chuanshi.weipos.entity;

import java.io.Serializable;

/**
 * Created by zhoulc on 17/12/20.
 */

public class PayType implements Serializable {
    private String payType;
    private String amount;

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
