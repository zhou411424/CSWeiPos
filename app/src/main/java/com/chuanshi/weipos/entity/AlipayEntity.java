package com.chuanshi.weipos.entity;

/**
 * Created by zhouliancheng on 2018/4/22.
 * 支付宝交易信息
 */

public class AlipayEntity {
    private String seller_id;//卖家-收款支付宝账号对应的支付宝唯一用户号
    private String buyer_logon_id;//买家-支付宝登陆账号
    private String trade_no;//支付宝交易号

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public String getBuyer_logon_id() {
        return buyer_logon_id;
    }

    public void setBuyer_logon_id(String buyer_logon_id) {
        this.buyer_logon_id = buyer_logon_id;
    }

    public String getTrade_no() {
        return trade_no;
    }

    public void setTrade_no(String trade_no) {
        this.trade_no = trade_no;
    }
}
