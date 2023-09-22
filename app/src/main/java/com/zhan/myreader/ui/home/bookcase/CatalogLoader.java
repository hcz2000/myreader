package com.zhan.myreader.ui.home.bookcase;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.zhan.myreader.base.application.MyApplication;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.greendao.service.ChapterService;
import com.zhan.myreader.ui.home.reader.ProgressListener;
import com.zhan.myreader.webapi.BookApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CatalogLoader extends AsyncTaskLoader<List<Chapter>>{
    private ChapterService mChapterService;

    private Book mBook;
    private int downloadedCount;


    CatalogLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mChapterService = new ChapterService();
        downloadedCount=0;
    }

    @Override
    protected void onStartLoading(){
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Chapter> loadInBackground() {
        List<Chapter> chapters=mChapterService.findBookAllChapterByBookId(mBook.getId());

        try {
            latch.await(600, TimeUnit.SECONDS);
        }catch(Exception e){
            Log.d("BookLoader",e.getLocalizedMessage());
        };
        return chapters;
    }
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void deliverResult(List<Chapter> data) {
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

}
