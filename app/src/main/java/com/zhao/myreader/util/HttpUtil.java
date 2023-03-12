package com.zhao.myreader.util;

import android.util.Base64;
import android.util.Log;

import com.zhao.myreader.application.MyApplication;
import com.zhao.myreader.callback.HttpCallback;
import com.zhao.myreader.common.APPCONST;
import com.zhao.myreader.common.URLCONST;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
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



    private static synchronized OkHttpClient getOkHttpClient(){
        if (mClient == null){

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30000, TimeUnit.SECONDS);
            builder.sslSocketFactory(createSSLSocketFactory(), (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) ->
                    true);


           mClient = builder.build();

        }
        return mClient;

    }


    /**
     * get请求
     * @param address
     * @param callback
     */
    public static void sendGetRequest(final String address, final HttpCallback callback) {
        new Thread(new Runnable() {
            HttpURLConnection connection = null;

            @Override
            public void run() {
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Content-type", "text/html");
                    connection.setRequestProperty("Accept-Charset", "utf-8");
                    connection.setRequestProperty("contentType", "utf-8");
                    connection.setConnectTimeout(60 * 1000);
                    connection.setReadTimeout(60 * 1000);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.e("Http", "网络错误异常！!!!");
                    }
                    InputStream in = connection.getInputStream();
                    Log.d("Http", "connection success");
                    if (callback != null) {
                        callback.onFinish(in);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Http", e.toString());
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

    public static void sendGetRequest_okHttp(final String address, final HttpCallback callback) {
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

    /**
     * post请求
     * @param address
     * @param output
     * @param callback
     */
    public static void sendPostRequest(final String address, final String output, final HttpCallback callback) {
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
     * 生成post输出参数串
     * @param params
     * @return
     */
    public static String makePostOutput(Map<String, Object> params) {
        StringBuilder output = new StringBuilder();
        Iterator<String> it = params.keySet().iterator();
        while (true) {
            String name = it.next();
            Log.d("http", name + "=" + params.get(name));
            output.append(name);
            output.append('=');
            try {
                if (URLCONST.isRSA) {
                    if (name.equals("token")) {
                        output.append(valueOf(params.get(name)));
                    } else {
                        output.append(StringHelper.encode(Base64.encodeToString(RSAUtilV2.encryptByPublicKey(valueOf(params.get(name)).getBytes(), APPCONST.publicKey), Base64.DEFAULT).replace("\n", "")));
                    }
                } else {
                    output.append(valueOf(params.get(name)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!it.hasNext()) {
                break;
            }
            output.append('&');
        }
        return output.toString();
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




}
