package com.zhao.myreader.ui.home.stock;

import static android.content.Context.INPUT_METHOD_SERVICE;

import static com.zhao.myreader.base.application.MyApplication.runOnUiThread;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.base.BasePresenter;
import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.greendao.service.StockService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.StringHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by zhan on 2017/7/26.
 */

public class InputStockPrensenter extends BasePresenter {

    private InputStockActivity mInputStockActivity;
    private String stockCode;//搜索关键字
    private StockService mStockService;

    InputStockPrensenter(InputStockActivity inputStockActivity) {
        super(inputStockActivity,inputStockActivity.getLifecycle());
        mInputStockActivity = inputStockActivity;
        mStockService = new StockService();
    }

    @Override
    public void create() {

        mInputStockActivity.getTvTitleText().setText("搜索");
        mInputStockActivity.getTvSuccess().setVisibility(View.GONE);
        mInputStockActivity.getLlTitleBack().setOnClickListener(view -> mInputStockActivity.finish());
        mInputStockActivity.getEtStockCode().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
           @Override
            public void afterTextChanged(final Editable editable) {
                stockCode = editable.toString();
                if (StringHelper.isEmpty(stockCode)) {
                    insert();
                }
            }
        });

        mInputStockActivity.getEtStockCode().setOnKeyListener((v, keyCode, event) -> {
            //是否是回车键
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                //隐藏键盘
                ((InputMethodManager) mInputStockActivity.getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mInputStockActivity.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                //搜索
                insert();
            }
            return false;
        });

        mInputStockActivity.getTvSuccess().setOnClickListener(view -> {
            mInputStockActivity.getEtStockCode().setText("");
            mInputStockActivity.getEtStockQuantity().setText("");
            mInputStockActivity.getEtStockCost().setText("");
            mInputStockActivity.getEtStockUpperThreshold().setText("0.00");
            mInputStockActivity.getEtStockLowerThreshold().setText("0.00");
            mInputStockActivity.getLlStock().setVisibility(View.VISIBLE);
            mInputStockActivity.getTvSuccess().setVisibility(View.GONE);
            mInputStockActivity.getTvInsert().setVisibility(View.VISIBLE);
        });
        mInputStockActivity.getTvInsert().setOnClickListener(view -> insert());
        mInputStockActivity.getTvSuccess().setVisibility(View.GONE);
    }


   /**
     * 获取搜索数据
     */
   private void verifyAndInsert() {
        String url;
        if(stockCode.startsWith("6")){
            url="https://xueqiu.com/s/SH"+stockCode;
        }else{
            url="https://xueqiu.com/s/SZ"+stockCode;
        }
        HttpUtil.httpGet_Async(url, "utf-8",new ResultCallback(){
            @Override
            public void onFinish(Object content, int code) {
                Document doc = Jsoup.parse((String)content);
                Elements stockNames = doc.getElementsByClass("stock-name");
                if(!stockNames.isEmpty()) {
                    String quantityStr=mInputStockActivity.getEtStockQuantity().getText().toString();
                    int quantity=Integer.parseInt(quantityStr);
                    String costStr=mInputStockActivity.getEtStockCost().getText().toString();
                    double cost=Double.parseDouble(costStr);
                    String upperThresholdStr=mInputStockActivity.getEtStockUpperThreshold().getText().toString();
                    double upperThreshold=Double.parseDouble(upperThresholdStr);
                    String lowerThresholdStr=mInputStockActivity.getEtStockUpperThreshold().getText().toString();
                    double lowerThreshold=Double.parseDouble(lowerThresholdStr);
                    Element stockName=stockNames.first();
                    String name=stockName.text();
                    Stock stock=new Stock();
                    stock.setId(stockCode);
                    stock.setName(name.split("\\(")[0]);
                    stock.setQuantity(quantity);
                    stock.setCost(cost);
                    stock.setLastPrice(0.00);
                    stock.setPrice(0.00);
                    mStockService.addOrUpdateStock(stock);
                    runOnUiThread(()-> {
                          Toast.makeText(mInputStockActivity.getApplicationContext(), name+" 成功添加",Toast.LENGTH_LONG).show();
                          mInputStockActivity.getPbLoading().setVisibility(View.GONE);
                          mInputStockActivity.getLlStock().setVisibility(View.GONE);
                          mInputStockActivity.getTvSuccess().setVisibility(View.VISIBLE);
                          mInputStockActivity.getTvInsert().setVisibility(View.GONE);
                    });
                }else {
                    runOnUiThread(()-> {
                          Toast.makeText(mInputStockActivity.getApplicationContext(), "股票代码不存在", Toast.LENGTH_LONG).show();
                          mInputStockActivity.getPbLoading().setVisibility(View.GONE);
                    });
                }
       }

            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mInputStockActivity.getApplicationContext(), "股票代码不存在", Toast.LENGTH_LONG).show();
                        mInputStockActivity.getPbLoading().setVisibility(View.GONE);
                    }
                });
            }

        });


    }

    /**
     * 搜索
     */
   private void insert() {
        mInputStockActivity.getPbLoading().setVisibility(View.VISIBLE);
        if (StringHelper.isEmpty(stockCode)) {
            mInputStockActivity.getPbLoading().setVisibility(View.GONE);
        } else {
            verifyAndInsert();
        }
   }

   boolean onBackPressed() {
        if (StringHelper.isEmpty(stockCode)) {
            return false;
        } else {
            mInputStockActivity.getEtStockCode().setText("");
            return true;
        }
   }
}

