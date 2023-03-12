package com.zhao.myreader.webapi;

import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.source.HttpDataSource;
import com.zhao.myreader.util.HttpUtil;

import java.util.Map;

/**
 * Created by zhao on 2017/6/20.
 */

public class BaseApi {

    protected static void getCommonReturnHtmlStringApi(String url, Map<String, Object> params, String charsetName, final ResultCallback callback) {
    	HttpDataSource.httpGet_html(HttpUtil.makeURL(url, params), charsetName, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(o,code);
            }

            @Override
            public void onError(Exception e) {
                  callback.onError(e);
            }
        });
    }

    private static void error(Exception e, final ResultCallback callback){
      /*  if (e.toString().contains("SocketTimeoutException") || e.toString().contains("UnknownHostException")) {
            TextHelper.showText("缃戠粶杩炴帴瓒呮椂锛岃妫�鏌ョ綉缁�");
        }*/
        e.printStackTrace();
        callback.onError(e);
    }



}
