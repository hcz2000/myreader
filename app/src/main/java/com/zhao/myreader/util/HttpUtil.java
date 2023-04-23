package com.zhao.myreader.util;

import android.util.Base64;
import android.util.Log;

import com.zhao.myreader.base.application.MyApplication;
import com.zhao.myreader.callback.HttpCallback;
import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.common.APPCONST;
import com.zhao.myreader.common.URLCONST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.String.valueOf;

/**
 * Created by zhao on 2016/4/16.
 */

public class HttpUtil {

    private static String sessionid;
    //最好只使用一个共享的OkHttpClient 实例，将所有的网络请求都通过这个实例处理。
    //因为每个OkHttpClient 实例都有自己的连接池和线程池，重用这个实例能降低延时，减少内存消耗，而重复创建新实例则会浪费资源。
    private static OkHttpClient mClient;
    static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }
    };



    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            ssfFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }


    /**
     * 生成URL
     * @param p_url
     * @param params
     * @return
     */
    public static String makeURL(String p_url, Map<String, Object> params) {
        if (params == null) return p_url;
        StringBuilder url = new StringBuilder(p_url);
        Log.d("http", p_url);
        if (url.indexOf("?") < 0)
            url.append('?');
        for (String name : params.keySet()) {
            Log.d("http", name + "=" + params.get(name));
            url.append('&');
            url.append(name);
            url.append('=');
            try {
                if (URLCONST.isRSA) {
                    if (name.equals("token")) {
                        url.append(valueOf(params.get(name)));
                    } else {
                        url.append(StringHelper.encode(Base64.encodeToString(RSAUtilV2.encryptByPublicKey(valueOf(params.get(name)).getBytes(), APPCONST.publicKey), Base64.DEFAULT).replace("\n", "")));
                    }
                } else {
                    url.append(valueOf(params.get(name)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return url.toString().replace("?&", "?");
    }


    /**
     * Trust every server - dont check for any certificate
     */
    public static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static synchronized OkHttpClient getOkHttpClient(){
        if (mClient == null){
         OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30000, TimeUnit.SECONDS);
            builder.sslSocketFactory(createSSLSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            mClient = builder.build();
        }
        return mClient;

    }


    public static void httpGet_Async(final String address, final HttpCallback callback) {
       MyApplication.getApplication().newThread(() -> {
           try{
             OkHttpClient client = getOkHttpClient();
               Request request = new Request.Builder()
                       .url(address)
                       .build();
               Response response = client.newCall(request).execute();
               callback.onFinish(response.body().byteStream());
           }catch(Exception e){
               e.printStackTrace();
               callback.onError(e);
           }
       });
    }

    public static String httpGet_Sync(final String address) {

        try{
                OkHttpClient client = getOkHttpClient();
                Request request = new Request.Builder()
                        .url(address)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
        }catch(Exception e){
                e.printStackTrace();
        }
        return null;
    }


    /**
     * post请求
     * @param address
     * @param output
     * @param callback
     */
    public static void httpPost_Async(final String address, final String output, final HttpCallback callback) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (output != null) {
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.writeBytes(output);
                    }
                    InputStream in = connection.getInputStream();
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        callback.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }



    /**
     * http请求 (get)
     * @param url
     * @param callback
     */
    public static void httpGet_Async(String url, final String charsetName, final ResultCallback callback){
        Log.d("HttpGet URl", url);
        httpGet_Async(url, new HttpCallback() {
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
    public static void httpPost_Async(String url, String output, final ResultCallback callback) {
        Log.d("HttpPost:", url + "&" + output);
        httpPost_Async(url, output, new HttpCallback() {
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
