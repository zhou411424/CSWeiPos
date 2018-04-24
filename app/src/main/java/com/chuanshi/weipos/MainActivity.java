package com.chuanshi.weipos;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chuanshi.weipos.entity.CategoryInfo;
import com.chuanshi.weipos.entity.GoodInfo;
import com.chuanshi.weipos.entity.PayType;
import com.chuanshi.weipos.utils.Constants;
import com.chuanshi.weipos.utils.NetworkUtil;
import com.chuanshi.weipos.webview.CustomWebChromeClient;
import com.chuanshi.weipos.webview.CustomWebViewClient;
import com.chuanshi.weipos.widget.CustomWebView;
import com.wangpos.by.cashier3.CashierHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.weipass.pos.sdk.IPrint;
import cn.weipass.pos.sdk.LatticePrinter;
import cn.weipass.pos.sdk.Weipos;
import cn.weipass.pos.sdk.impl.WeiposImpl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private CustomWebView mWebView;
    private LinearLayout mNetworkErrorLayout;
    private ImageView mWelcomeIv;
    private boolean mFirstRun = true;
    private LatticePrinter mLatticePrinter;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mWebView = (CustomWebView) findViewById(R.id.webview);
        //此句加上也可以，防止输入法调不出来
//        mWebView.requestFocusFromTouch();

        mWebView.setWebChromeClient(new CustomWebChromeClient(this));
        mWebView.setWebViewClient(new CustomWebViewClient());
        //为H5调用android提供接口
        mWebView.addJavascriptInterface(new ChuanShiJavascriptInterface(), "chuanshi");

        mNetworkErrorLayout = (LinearLayout) findViewById(R.id.layout_network_error);
        Button reloadBtn = (Button) findViewById(R.id.btn_reload);
        reloadBtn.setOnClickListener(this);

        mWelcomeIv = (ImageView) findViewById(R.id.iv_welcome_img);
        if (mFirstRun) {
            mFirstRun = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWelcomeIv.setVisibility(View.GONE);
                }
            }, 2000);
        }
        Button mPrintBtn = (Button) findViewById(R.id.btn_print);
        Button mPrintBillBtn = (Button) findViewById(R.id.btn_print_bill);
        mPrintBtn.setVisibility(View.VISIBLE);
        mPrintBillBtn.setVisibility(View.VISIBLE);
        mPrintBtn.setOnClickListener(this);
        mPrintBillBtn.setOnClickListener(this);

        initPrint();

        loadData();
    }

    /**
     * H5调用Android接口类
     */
    private class ChuanShiJavascriptInterface {
        /**
         * 消费
         * @param out_trade_no 商户订单号
         * @param body 商品描述
         * @param goods_detail 商品详情
         * @param pay_type 支付方式
         * @param total_fee 订单金额（单位：分）
         * @param unfavor_fee 不优惠金额（注意：该字段暂时保留，不起作用）
         * @param attach 附加数据
         * @param notify_url 	通知url
         * @param time_start 交易起始时间
         * @param time_expire 交易结束时间
         * @param print_note 打印备注
         * @param operator 操作人员
         * @param activity_path 接收支付结果的Activity 类名
         */
        @JavascriptInterface
        public void onWeiPosConsume(String out_trade_no, String body, String goods_detail,
                              String pay_type, String total_fee, String unfavor_fee, String attach,
                              String notify_url, String time_start, String time_expire, String print_note,
                              String operator, String activity_path) {
            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", Constants.APPID);
            paramMap.put("out_trade_no", out_trade_no);
            paramMap.put("body", body);
            paramMap.put("goods_detail", goods_detail);
            paramMap.put("pay_type", pay_type);
            paramMap.put("total_fee", total_fee);
            if (!TextUtils.isEmpty(unfavor_fee)) {
                paramMap.put("unfavor_fee", unfavor_fee);
            }
            paramMap.put("attach", attach);
            paramMap.put("notify_url", notify_url);
            paramMap.put("time_start", time_start);
            paramMap.put("time_expire", time_expire);
            paramMap.put("print_note", print_note);
            paramMap.put("operator", operator);
            paramMap.put("activity_path", activity_path);

            CashierHelper.consume(MainActivity.this, paramMap, new CashierHelper.PayCallBack() {
                @Override
                public void success(String data) {
                    Log.d(TAG, "onConsume==>success...data="+data);
                    if (TextUtils.isEmpty(data)) {
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(data);
                        JSONObject jsonData = object.optJSONObject("data");
                        String out_trade_no = jsonData.optString("out_trade_no");
                        String trade_status = jsonData.getString("trade_status");
                        String cashier_trade_no = jsonData.getString("cashier_trade_no");
                        long discount_platform = jsonData.getLong("discount_platform");
                        long discount_channel = jsonData.getLong("discount_channel");
                        int pay_type = jsonData.getInt("pay_type");
                        String attach = jsonData.getString("attach");
                        String en = jsonData.getString("en");
                        String operator = jsonData.getString("operator");
                        if (jsonData.has("bank")) {
                            JSONObject bankJsonObj = jsonData.getJSONObject("bank");
                            if (bankJsonObj != null) {
                                String voucher_no = bankJsonObj.getString("voucher_no");
                                String bank_no = bankJsonObj.getString("bank_no");
                                String ref_no = bankJsonObj.getString("ref_no");
                            }
                        }
                        if (jsonData.has("wxpay")) {
                            JSONObject wxpayJsonObj = jsonData.getJSONObject("wxpay");
                            if (wxpayJsonObj != null) {
                                String open_id = wxpayJsonObj.getString("open_id");
                                String transaction_id = wxpayJsonObj.getString("transaction_id");
                                String mch_id = wxpayJsonObj.getString("mch_id");
                            }
                        }
                        if (jsonData.has("alipay")) {
                            JSONObject alipayJsonObj = jsonData.getJSONObject("alipay");
                            if (alipayJsonObj != null) {
                                String seller_id = alipayJsonObj.getString("seller_id");
                                String buyer_logon_id = alipayJsonObj.getString("buyer_logon_id");
                                String trade_no = alipayJsonObj.getString("trade_no");
                            }
                        }
                        String es_url = jsonData.getString("es_url");
                        long total_fee = jsonData.getLong("total_fee");//订单金额
                        Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();

                        //成功回调
                        MainActivity.this.onWeiPosConsumeCallback(total_fee, out_trade_no, pay_type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(String errMessage) {
                    Log.d(TAG, "onConsume==>failed...errMessage="+errMessage);
                    Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * 查询
         * @param out_trade_no 商户订单号
         */
        @JavascriptInterface
        public void onWeiPosQuery(String out_trade_no) {
            HashMap<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", Constants.APPID);
            paramMap.put("out_trade_no", out_trade_no);
            CashierHelper.query(MainActivity.this, paramMap, new CashierHelper.PayCallBack() {
                @Override
                public void success(String data) {
                    Log.d(TAG, "onQuery==>success...data="+data);
                    if (TextUtils.isEmpty(data)) {
                        return;
                    }
                    try {
                        JSONObject object = new JSONObject(data);
                        JSONObject jsonData = object.optJSONObject("data");
                        String out_trade_no = jsonData.optString("out_trade_no");
                        String goods_detail = "";
                        if (jsonData.has("goods_detail")) {
                            JSONObject goodsDetailJsonObj = jsonData.getJSONObject("goods_detail");
                            if (goodsDetailJsonObj != null) {
                                goods_detail = goodsDetailJsonObj.toString();
                                String goods_id = goodsDetailJsonObj.getString("goods_id");
                                String wang_goods_id = goodsDetailJsonObj.getString("wang_goods_id");
                                String goods_name = goodsDetailJsonObj.getString("goods_name");
                                int price = goodsDetailJsonObj.getInt("price");
                                String goods_category = goodsDetailJsonObj.getString("goods_category");
                                String body = goodsDetailJsonObj.getString("body");
                                String show_url = goodsDetailJsonObj.getString("show_url");
                            }
                        }
                        int pay_type = jsonData.getInt("pay_type");
                        String total_fee = jsonData.optString("total_fee");
                        String time_start = jsonData.optString("time_start");
                        String time_expire = jsonData.optString("time_expire");
                        Toast.makeText(MainActivity.this, "查询成功", Toast.LENGTH_SHORT).show();

                        MainActivity.this.onWeiPosQueryCallback(out_trade_no, pay_type, time_start, time_expire, goods_detail);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void failed(String errMessage) {
                    Log.d(TAG, "onQuery==>failed...errMessage="+errMessage);
                    Toast.makeText(MainActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * 打印结账单
         */
        @JavascriptInterface
        public void onWeiPosPrintBill(String jsonStr) {
            printWeiPosBill(jsonStr);
        }

        /**
         * 打印结账单
         */
        @JavascriptInterface
        public void onWeiPosPrintPerform(String jsonStr) {
            printWeiPosPerform(jsonStr);
        }
    }

    //----------------------------接口 callback start---------------------------

    /**
     * 消费callback
     * @param total_fee 订单金额，单位(分)
     * @param out_trade_no 商户订单号
     * @param pay_type 支付方式
     */
    public void onWeiPosConsumeCallback(long total_fee, String out_trade_no, int pay_type) {
        Log.d(TAG, "onWeiPosConsumeCallback==>");
        if (mWebView != null) {
            mWebView.loadUrl("javascript:onWeiPosConsumeCallback('" + total_fee+ "', '" + out_trade_no+ "'," +
                    " '" + pay_type+ "');");
        }
    }

    /**
     * 查询callback
     * @param out_trade_no 商户订单号
     * @param pay_type 支付方式
     * @param time_start 交易起始时间
     * @param time_expire 交易结束时间
     * @param goods_detail 商品详情
     */
    public void onWeiPosQueryCallback(String out_trade_no, int pay_type, String time_start, String time_expire, String goods_detail) {
        Log.d(TAG, "onWeiPosQueryCallback==>");
        if (mWebView != null) {
            mWebView.loadUrl("javascript:onWeiPosQueryCallback('" + out_trade_no+ "', '" + pay_type+ "'," +
                    " '" + time_start+ "', '" + time_expire+ "', '" + goods_detail+ "');");
        }
    }

    //----------------------------接口 callback end---------------------------

    public static String getPrintErrorInfo(int what, String info) {
        String message = "";
        switch (what) {
            case IPrint.EVENT_CONNECT_FAILD:
                message = "连接打印机失败";
                break;
            case IPrint.EVENT_CONNECTED:
                // Log.e("subscribe_msg", "连接打印机成功");
                break;
            case IPrint.EVENT_PAPER_JAM:
                message = "打印机卡纸";
                break;
            case IPrint.EVENT_UNKNOW:
                message = "打印机未知错误";
                break;
            case IPrint.EVENT_STATE_OK:
                //打印机状态正常
                break;
            case IPrint.EVENT_OK://
                // 回调函数中不能做UI操作，所以可以使用runOnUiThread函数来包装一下代码块
                // 打印完成结束
                break;
            case IPrint.EVENT_NO_PAPER:
                message = "打印机缺纸";
                break;
            case IPrint.EVENT_HIGH_TEMP:
                message = "打印机高温";
                break;
            case IPrint.EVENT_PRINT_FAILD:
                message = "打印失败";
                break;
        }
        return message;
    }

    private void initPrint() {
        WeiposImpl.as().init(MainActivity.this, new Weipos.OnInitListener() {

            @Override
            public void onInitOk() {
                Log.d(TAG, "initPrint==>onInitOk...");
                mLatticePrinter = WeiposImpl.as().openLatticePrinter();
            }
            @Override
            public void onError(String message) {
                Log.d(TAG, "initPrint==>onError...message="+message);
            }

            @Override
            public void onDestroy() {
                Log.d(TAG, "initPrint==>onDestroy...");
            }
        });
    }

    private void loadData() {
        //网络检查
        if (!NetworkUtil.isNetworkAvailable(this)) {
            mWebView.setVisibility(View.GONE);
            mNetworkErrorLayout.setVisibility(View.VISIBLE);
        } else {
            mWebView.setVisibility(View.VISIBLE);
            mNetworkErrorLayout.setVisibility(View.GONE);
            String url = "http://www.csshidai.com";
//            String url = "http://www.chuanshitech.com";
            mWebView.loadUrl(url);
        }
    }

    /**
     * 打印预结单
     */
    private void printWeiPosPerform(String jsonStr) {
        String json = "{\n" +
                "    \"title\": \"杭州君悦大酒店(预结单)\",\n" +
                "    \"table\": \"001\",\n" +
                "    \"orderNumber\": \"2017113423232323\",\n" +
                "    \"time\": \"2017-11-30 12:24:31\",\n" +
                "    \"categoryList\":[\n"+
                "        {\n" +
                "            \"category\": \"水饺\",\n" +
                "            \"amount\": \"22\",\n" +
                "            \"number\": \"1\",\n" +
                "            \"goodsList\": [\n" +
                "               {\n" +
                "                   \"name\": \"大葱大肉    \",\n" +
                "                   \"num\": \"15.0*1.0\",\n" +
                "                   \"amount\": \"22\"\n" +
                "               },\n" +
                "               {\n" +
                "                   \"name\": \"鸭子毛调蒜汁    \",\n" +
                "                   \"num\": \"3.0*1.0\",\n" +
                "                   \"amount\": \"22\"\n" +
                "               }\n" +
                "           ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"category\": \"热菜\",\n" +
                "            \"amount\": \"222\",\n" +
                "            \"number\": \"1\",\n" +
                "            \"goodsList\": [\n" +
                "               {\n" +
                "                   \"name\": \"大葱大肉2    \",\n" +
                "                   \"num\": \"15.0*1.0\",\n" +
                "                   \"amount\": \"222\"\n" +
                "               },\n" +
                "               {\n" +
                "                   \"name\": \"鸭子毛调蒜汁2    \",\n" +
                "                   \"num\": \"3.0*1.0\",\n" +
                "                   \"amount\": \"223\"\n" +
                "               }\n" +
                "           ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"heji\": \"56\",\n" +
                "    \"sfje\": \"100\",\n" +
                "    \"couponAmount\": \"0.0\"\n" +
                "}";
        Log.d(TAG, "printShengPayPerform==>");
        String title = "", table = "", orderNumber = "",
                time = "", heji = "", couponAmount = "",
                sfje = "";
        List<CategoryInfo> categories = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject != null) {
                    title = jsonObject.getString("title");
                    table = jsonObject.getString("table");
                    orderNumber = jsonObject.getString("orderNumber");
                    time = jsonObject.getString("time");
                    heji = jsonObject.getString("heji");
                    sfje = jsonObject.getString("sfje");//实付金额
                    couponAmount = jsonObject.getString("couponAmount");
                    JSONArray categoryListJsonArray = jsonObject.getJSONArray("categoryList");
                    if(categoryListJsonArray != null){
                        for (int i = 0; i < categoryListJsonArray.length(); i++) {
                            CategoryInfo categoryInfo = new CategoryInfo();
                            List<GoodInfo> goodInfos = new ArrayList<>();
                            JSONObject categoryJsonObject = categoryListJsonArray.getJSONObject(i);
                            String category = categoryJsonObject.getString("category");
                            String amountTotal = categoryJsonObject.getString("amount");
                            String number = categoryJsonObject.getString("number");
                            JSONArray goodsListJsonArray = categoryJsonObject.getJSONArray("goodsList");
                            if (goodsListJsonArray != null) {
                                for (int j = 0; j < goodsListJsonArray.length(); j++) {
                                    JSONObject goodInfoJsonObject = goodsListJsonArray.getJSONObject(j);
                                    if (goodInfoJsonObject != null) {
                                        GoodInfo goodInfo = new GoodInfo();
                                        goodInfo.setName(goodInfoJsonObject.getString("name"));
                                        goodInfo.setNum(goodInfoJsonObject.getString("num"));
                                        goodInfo.setAmount(goodInfoJsonObject.getString("amount"));
                                        goodInfos.add(goodInfo);
                                    }
                                }
                            }
                            categoryInfo.setAmount(amountTotal);
                            categoryInfo.setCategory(category);
                            categoryInfo.setNumber(number);
                            categoryInfo.setGoodsList(goodInfos);
                            categories.add(categoryInfo);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mLatticePrinter == null) {
            Log.e(TAG, "打印机未初始化");
            return;
        }

        //打印文本
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(table)) {
            sb.append("桌台："+table+"\n");
        }
        if (!TextUtils.isEmpty(orderNumber)) {
            sb.append("单号："+orderNumber+"\n");
        }
        if (!TextUtils.isEmpty(time)) {
            sb.append("时间："+time+"\n");
        }
        sb.append("********************************\n");
        sb.append("名称            价*量        金额\n");
        for (int i = 0; i < categories.size(); i++) {
            CategoryInfo categoryInfo = categories.get(i);
            String category = categoryInfo.getCategory();
            String amount = categoryInfo.getAmount();
            String number = categoryInfo.getNumber();
            List<GoodInfo> goodInfos = categoryInfo.getGoodsList();
            if (goodInfos != null && !goodInfos.isEmpty()) {
                for (int j = 0;j < goodInfos.size(); j++) {
                    GoodInfo goodInfo = goodInfos.get(j);
                    if (goodInfo != null) {
                        Log.d(TAG, "name="+goodInfo.getName()+", num="+goodInfo.getNum()+", amount="+goodInfo.getAmount());
                        sb.append(goodInfo.getName() + goodInfo.getNum() + goodInfo.getAmount()+"\n");
                    }
                }
            }
            sb.append("      "+category+":"+number+"       "+amount+"\n");
        }

        sb.append("********************************\n");
        if (!TextUtils.isEmpty(heji)) {
            String hejiStr = "合计："+heji+"\n";
            sb.append(hejiStr);
        }

        sb.append("********************************\n");
        if (!TextUtils.isEmpty(sfje)) {
            sb.append("实付金额："+sfje+"元    优惠："+couponAmount+"元\n");
        }
        String text = sb.toString();
        mLatticePrinter.printText(text, LatticePrinter.FontFamily.SONG,
                LatticePrinter.FontSize.LARGE, LatticePrinter.FontStyle.BOLD);


        mLatticePrinter.submitPrint();//提交打印事件之后，才会开始打印
        //打印监听，返回打印信息
        mLatticePrinter.setOnEventListener(new IPrint.OnEventListener() {
            @Override
            public void onEvent(int what, String info) {
                String message = getPrintErrorInfo(what, info);
                Log.d(TAG, "onPrint==>onEvent...message="+message);
            }
        });
    }

    /**
     * 打印结账单
     */
    private void printWeiPosBill(String jsonStr) {
        String json = "{\n" +
                "    \"title\": \"杭州君悦大酒店(结账单)\",\n" +
                "    \"table\": \"001\",\n" +
                "    \"orderNumber\": \"2017113423232323\",\n" +
                "    \"time\": \"2017-11-30 12:24:31\",\n" +
                "    \"categoryList\":[\n"+
                "        {\n" +
                "            \"category\": \"水饺\",\n" +
                "            \"amount\": \"22\",\n" +
                "            \"number\": \"1\",\n" +
                "            \"goodsList\": [\n" +
                "               {\n" +
                "                   \"name\": \"大葱大肉    \",\n" +
                "                   \"num\": \"15.0*1.0\",\n" +
                "                   \"amount\": \"22\"\n" +
                "               },\n" +
                "               {\n" +
                "                   \"name\": \"鸭子毛调蒜汁    \",\n" +
                "                   \"num\": \"3.0*1.0\",\n" +
                "                   \"amount\": \"22\"\n" +
                "               }\n" +
                "           ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"category\": \"热菜\",\n" +
                "            \"amount\": \"222\",\n" +
                "            \"number\": \"1\",\n" +
                "            \"goodsList\": [\n" +
                "               {\n" +
                "                   \"name\": \"大葱大肉2    \",\n" +
                "                   \"num\": \"15.0*1.0\",\n" +
                "                   \"amount\": \"222\"\n" +
                "               },\n" +
                "               {\n" +
                "                   \"name\": \"鸭子毛调蒜汁2    \",\n" +
                "                   \"num\": \"3.0*1.0\",\n" +
                "                   \"amount\": \"223\"\n" +
                "               }\n" +
                "           ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"payTypeList\": [\n" +
                "        {\n" +
                "            \"payType\": \"支付宝      \",\n" +
                "            \"amount\": \"22\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"payType\": \"微信      \",\n" +
                "            \"amount\": \"22\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"payType\": \"现金      \",\n" +
                "            \"amount\": \"22\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"heji\": \"56\",\n" +
                "    \"sdje\": \"100\",\n" +
                "    \"zl\": \"44\",\n" +
                "    \"actualPayAmount\": \"12.0\",\n" +
                "    \"couponAmount\": \"0.0\",\n" +
                "    \"memberName\": \"张飞\",\n" +
                "    \"discount\": \"8.5\",\n" +
                "    \"memberNo\": \"0001\",\n" +
                "    \"remainAmount\": \"2000\",\n" +
                "    \"welcome\": \"谢谢惠顾,欢迎下次光临!\"\n" +
                "}";
        String title = "", table = "", orderNumber = "",
                time = "", heji = "", actualPayAmount = "", couponAmount = "",
                memberNo = "", remainAmount = "", welcome = "",
                sdje = "", zl = "", memberName = "", discount = "";
        List<CategoryInfo> categories = new ArrayList<>();

        List<PayType> payTypes = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject != null) {
                    title = jsonObject.getString("title");
                    table = jsonObject.getString("table");
                    orderNumber = jsonObject.getString("orderNumber");
                    time = jsonObject.getString("time");
                    heji = jsonObject.getString("heji");
                    sdje = jsonObject.getString("sdje");//收到金额
                    zl = jsonObject.getString("zl");//找零
                    actualPayAmount = jsonObject.getString("actualPayAmount");
                    couponAmount = jsonObject.getString("couponAmount");
                    JSONArray categoryListJsonArray = jsonObject.getJSONArray("categoryList");
                    if(categoryListJsonArray != null){
                        for (int i = 0; i < categoryListJsonArray.length(); i++) {
                            CategoryInfo categoryInfo = new CategoryInfo();
                            List<GoodInfo> goodInfos = new ArrayList<>();
                            JSONObject categoryJsonObject = categoryListJsonArray.getJSONObject(i);
                            String category = categoryJsonObject.getString("category");
                            String amountTotal = categoryJsonObject.getString("amount");
                            String number = categoryJsonObject.getString("number");
                            JSONArray goodsListJsonArray = categoryJsonObject.getJSONArray("goodsList");
                            if (goodsListJsonArray != null) {
                                for (int j = 0; j < goodsListJsonArray.length(); j++) {
                                    JSONObject goodInfoJsonObject = goodsListJsonArray.getJSONObject(j);
                                    if (goodInfoJsonObject != null) {
                                        GoodInfo goodInfo = new GoodInfo();
                                        goodInfo.setName(goodInfoJsonObject.getString("name"));
                                        goodInfo.setNum(goodInfoJsonObject.getString("num"));
                                        goodInfo.setAmount(goodInfoJsonObject.getString("amount"));
                                        goodInfos.add(goodInfo);
                                    }
                                }
                            }
                            categoryInfo.setAmount(amountTotal);
                            categoryInfo.setCategory(category);
                            categoryInfo.setNumber(number);
                            categoryInfo.setGoodsList(goodInfos);
                            categories.add(categoryInfo);
                        }
                    }


                    JSONArray payTypeListJsonArray = jsonObject.getJSONArray("payTypeList");
                    if (payTypeListJsonArray != null) {
                        for (int i = 0; i < payTypeListJsonArray.length(); i++) {
                            JSONObject payTypeJsonObject = payTypeListJsonArray.getJSONObject(i);
                            if (payTypeJsonObject != null) {
                                PayType payType = new PayType();
                                payType.setPayType(payTypeJsonObject.getString("payType"));
                                payType.setAmount(payTypeJsonObject.getString("amount"));
                                payTypes.add(payType);
                            }
                        }
                    }
                    memberNo = jsonObject.getString("memberNo");
                    remainAmount = jsonObject.getString("remainAmount");
                    memberName = jsonObject.getString("memberName");
                    discount = jsonObject.getString("discount");
                    welcome = jsonObject.getString("welcome");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (mLatticePrinter == null) {
            Log.e(TAG, "打印机未初始化");
            return;
        }

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title+"\n");
        }

        if (!TextUtils.isEmpty(table)) {
            sb.append("桌台："+table+"\n");
        }
        if (!TextUtils.isEmpty(orderNumber)) {
            sb.append("单号："+orderNumber+"\n");
        }
        if (!TextUtils.isEmpty(time)) {
            sb.append("时间："+time+"\n");
        }
        sb.append("********************************\n");
        sb.append("名称            价*量        金额\n");
        for (int i = 0; i < categories.size(); i++) {
            CategoryInfo categoryInfo = categories.get(i);
            String category = categoryInfo.getCategory();
            String amount = categoryInfo.getAmount();
            String number = categoryInfo.getNumber();
            List<GoodInfo> goodInfos = categoryInfo.getGoodsList();
            if (goodInfos != null && !goodInfos.isEmpty()) {
                for (int j = 0;j < goodInfos.size(); j++) {
                    GoodInfo goodInfo = goodInfos.get(j);
                    if (goodInfo != null) {
                        Log.d(TAG, "name="+goodInfo.getName()+", num="+goodInfo.getNum()+", amount="+goodInfo.getAmount());
                        sb.append(goodInfo.getName() + goodInfo.getNum() + goodInfo.getAmount()+"\n");
                    }
                }
            }
            sb.append("      "+category+":"+number+"       "+amount+"\n");
        }

        sb.append("********************************\n");

        if (!TextUtils.isEmpty(heji)) {
            String hejiStr = "合计："+heji+"\n";
            sb.append(hejiStr);
        }

        sb.append("支付方式                    金额\n");
        if (payTypes != null && !payTypes.isEmpty()) {
            for (int i = 0;i < payTypes.size(); i++) {
                PayType payType = payTypes.get(i);
                if (payType != null) {
                    sb.append(payType.getPayType() + " " + payType.getAmount()+"\n");
                }
            }
        }
        sb.append("********************************\n");
        if (!TextUtils.isEmpty(sdje)) {
            sb.append("收到金额："+sdje+"元    找零："+zl+"元\n");
        }
        if (!TextUtils.isEmpty(actualPayAmount)) {
            sb.append("实付金额："+actualPayAmount+"元    优惠："+couponAmount+"元\n");
        }
        if (!TextUtils.isEmpty(remainAmount)) {
            sb.append("姓名："+memberName+"\n");
            sb.append("卡号："+memberNo+"\n");
            sb.append("余额："+remainAmount+"\n");
        }
        if (!TextUtils.isEmpty(discount)) {
            sb.append("姓名："+memberName+"\n");
            sb.append("卡号："+memberNo+"\n");
            sb.append("折扣："+discount+"\n");
        }
        if (!TextUtils.isEmpty(welcome)) {
            sb.append(welcome+"\n");
        }
        String text = sb.toString();
        mLatticePrinter.printText(text, LatticePrinter.FontFamily.SONG,
                LatticePrinter.FontSize.LARGE, LatticePrinter.FontStyle.BOLD);


        mLatticePrinter.submitPrint();//提交打印事件之后，才会开始打印
        //打印监听，返回打印信息
        mLatticePrinter.setOnEventListener(new IPrint.OnEventListener() {
            @Override
            public void onEvent(int what, String info) {
                String message = getPrintErrorInfo(what, info);
                Log.d(TAG, "onPrint==>onEvent...message="+message);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload:
                loadData();
                break;
            case R.id.btn_print:
                printWeiPosPerform("");
                break;
            case R.id.btn_print_bill:
                printWeiPosBill("");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult==>data == null ? " + (data == null)
                + ", requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (data == null) {
            Log.d(TAG, "onActivityResult==>data == null");
            return;
        }
//        Bundle bundle = data.getExtras();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
