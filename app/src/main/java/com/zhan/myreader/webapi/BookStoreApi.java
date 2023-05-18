package com.zhan.myreader.webapi;

import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.util.HttpUtil;
import com.zhan.myreader.util.crawler.BiQuGeReadUtil;

/**
 * Created by zhan on 2017/7/24.
 */

public class BookStoreApi{


    /**
     * 鑾峰彇涔﹀煄灏忚鍒嗙被鍒楄〃
     * @param url
     * @param callback
     */
    public static void getBookTypeList(String url, final ResultCallback callback){

        HttpUtil.httpGet_Async(url, null, "GBK", new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(BiQuGeReadUtil.getBookTypeList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }


    /**
     * 鑾峰彇鏌愪竴鍒嗙被灏忚鎺掕姒滃垪琛�
     * @param url
     * @param callback
     */
    public static void getBookRankList(String url, final ResultCallback callback){

        HttpUtil.httpGet_Async(url, null, "GBK", new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(BiQuGeReadUtil.getBookRankList((String) o),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);

            }
        });
    }


    /**
     * 鑾峰彇灏忚璇︾粏淇℃伅
     * @param book
     * @param callback
     */
    public static void getBookInfo(Book book, final ResultCallback callback){

        HttpUtil.httpGet_Async(book.getChapterUrl(), null, "GBK", new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                callback.onFinish(BiQuGeReadUtil.getBookInfo((String) o,book),0);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }



}