package com.zhao.myreader.webapi;

import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.util.crawler.BiQuGeReadUtil;

/**
 * Created by zhao on 2017/7/24.
 */

public class BookStoreApi extends BaseApi{


    /**
     * 鑾峰彇涔﹀煄灏忚鍒嗙被鍒楄〃
     * @param url
     * @param callback
     */
    public static void getBookTypeList(String url, final ResultCallback callback){

        getCommonReturnHtmlStringApi(url, null, "GBK", new ResultCallback() {
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

        getCommonReturnHtmlStringApi(url, null, "GBK", new ResultCallback() {
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

        getCommonReturnHtmlStringApi(book.getChapterUrl(), null, "GBK", new ResultCallback() {
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
