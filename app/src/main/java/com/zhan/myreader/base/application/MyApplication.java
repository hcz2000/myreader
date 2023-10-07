package com.zhan.myreader.base.application;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.scwang.smartrefresh.header.WaveSwipeHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.zhan.myreader.R;
import com.zhan.myreader.util.HttpUtil;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Created by zhan on 2016/10/20.
 */

public class MyApplication extends Application {
    //private static int THREADS = Runtime.getRuntime().availableProcessors()/2+1;
    private static int THREADS = 2;
    private static int LOADER_THREADS = 1;
    private static Handler handler = new Handler();
    private static MyApplication application;
    private ExecutorService mFixedThreadPool;

    private ExecutorService mLoaderThreadPool;

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator((context, layout) -> {
            layout.setPrimaryColorsId(R.color.sys_book_type_bg, R.color.sys_refresh_main);//全局设置主题颜色
            return new WaveSwipeHeader(context);
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator((context, layout) -> {
            return new ClassicsFooter(context).setDrawableSize(20);
        });
    }



    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        HttpUtil.trustAllHosts();//信任所有证书
        mFixedThreadPool = Executors.newFixedThreadPool( THREADS);//初始化线程池
        mLoaderThreadPool = Executors.newFixedThreadPool(LOADER_THREADS);
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }


    public static Context getmContext() {
        return application;
    }
    public void newThread(Runnable runnable) {

        try {
            mFixedThreadPool.execute(runnable);
        } catch (Exception e) {
            e.printStackTrace();
            mFixedThreadPool = Executors.newFixedThreadPool(THREADS);//初始化线程池
            mFixedThreadPool.execute(runnable);
        }
    }

    public void newLoader(Runnable runnable) {

        try {
            mLoaderThreadPool.execute(runnable);
        } catch (Exception e) {
            e.printStackTrace();
            mLoaderThreadPool = Executors.newFixedThreadPool(LOADER_THREADS);//初始化线程池
            mLoaderThreadPool.execute(runnable);
        }
    }

    public void shutdownThreadPool(){
        mFixedThreadPool.shutdownNow();
        mLoaderThreadPool.shutdownNow();
    }


    /**
     * 主线程执行
     *
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public static MyApplication getApplication() {
        return application;
    }


}
