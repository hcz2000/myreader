package com.zhan.myreader.util;

import android.widget.Toast;

import com.zhan.myreader.base.application.MyApplication;


/**
 * Created by zhan on 2016/10/26.
 */

public class TextHelper {

    public static void showText(final String text){

        MyApplication.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getApplication(),text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showLongText(final String text){

        MyApplication.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyApplication.getApplication(),text, Toast.LENGTH_LONG).show();
            }
        });
    }
}