package com.zhao.myreader.webapi;

import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.util.HttpUtil;

import java.util.Map;

/**
 * Created by zhao on 2017/6/20.
 */

public class BaseApi {

    protected static void getCommonReturnHtmlStringApi(String url, Map<String, Object> params, String charsetName, final ResultCallback callback) {
    	HttpUtil.httpGet_Async(HttpUtil.makeURL(url, params), charsetName, new ResultCallback() {
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

}
