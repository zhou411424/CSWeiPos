package com.chuanshi.weipos.entity;

/**
 * Created by zhouliancheng on 2018/4/22.
 * 银行卡交易信息
 */

public class BankEntity {
    private String voucher_no;//凭证号
    private String bank_no;//银行卡号
    private String ref_no;//参考号

    public String getVoucher_no() {
        return voucher_no;
    }

    public void setVoucher_no(String voucher_no) {
        this.voucher_no = voucher_no;
    }

    public String getBank_no() {
        return bank_no;
    }

    public void setBank_no(String bank_no) {
        this.bank_no = bank_no;
    }

    public String getRef_no() {
        return ref_no;
    }

    public void setRef_no(String ref_no) {
        this.ref_no = ref_no;
    }
}
