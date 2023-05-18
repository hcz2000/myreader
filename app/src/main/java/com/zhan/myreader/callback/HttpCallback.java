package com.zhan.myreader.callback;

import android.graphics.Bitmap;

import java.io.InputStream;

/**
 * Created by zhan on 2016/4/16.
 */
public interface HttpCallback {
    void onFinish(InputStream in);
    void onError(Exception e);
}
