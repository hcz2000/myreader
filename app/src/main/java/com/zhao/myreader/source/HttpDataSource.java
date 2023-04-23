package com.zhao.myreader.source;

import android.util.Log;

import com.zhao.myreader.callback.HttpCallback;
import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.util.HttpUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by zhao on 2016/4/16.
 */

public class HttpDataSource {

    /**
     * http请求 (get) ps:获取html
     * @param url
     * @param callback
     */
    public static void httpGet_html(String url, final String charsetName, final ResultCallback callback){
        Log.d("HttpGet URl", url);
        HttpUtil.getRequest_Async(url, new HttpCallback() {
            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response.toString());
                       callback.onFinish(response.toString(),0);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }

        });
    }




    /**
     * http请求 (post)
     * @param url
     * @param output
     * @param callback
     */
    public static void httpPost(String url, String output, final ResultCallback callback) {
        Log.d("HttpPost:", url + "&" + output);
        HttpUtil.postRequest(url, output, new HttpCallback() {
            @Override
            public void onFinish(InputStream in) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null) {
                        response.append(line);
                        line = reader.readLine();
                    }
                    if (callback != null) {
                        Log.d("Http", "read finish：" + response);
                        callback.onFinish(response.toString(),0);
                    }
                } catch (Exception e) {
                    callback.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

}
