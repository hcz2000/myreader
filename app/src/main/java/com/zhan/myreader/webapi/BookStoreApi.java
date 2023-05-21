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
     * Get Type List
     * @param url page url
     * @param callback callback handler
     */
    public static void getBookTypeList(String url, final ResultCallback callback){

        HttpUtil.httpGet_Async(url, null, "UTF-8", new ResultCallback() {
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
     * Get ranked list of specific type
     * @param url page url
     * @param callback callback handler
     */
    public static void getBookRankList(String url, final ResultCallback callback){

        HttpUtil.httpGet_Async(url, null, "UTF-8", new ResultCallback() {
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
     * Get Book Info
     * @param book book
     * @param callback callback handler
     */
    public static void getBookInfo(Book book, final ResultCallback callback){

        HttpUtil.httpGet_Async(book.getChapterUrl(), null, "UTF-8", new ResultCallback() {
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
