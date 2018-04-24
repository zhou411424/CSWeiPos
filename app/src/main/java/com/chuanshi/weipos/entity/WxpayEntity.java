package com.chuanshi.weipos.entity;

/**
 * Created by zhouliancheng on 2018/4/22.
 * 微信交易信息
 */

public class WxpayEntity {
    private String open_id;//用户在商户appid下的唯一标识
    private String transaction_id;//微信支付订单号
    private String mch_id;//微信商户号

    public String getOpen_id() {
        return open_id;
    }

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public String getMch_id() {
        return mch_id;
    }

    public void setMch_id(String mch_id) {
        this.mch_id = mch_id;
    }
}
