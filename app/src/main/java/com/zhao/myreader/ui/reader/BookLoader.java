package com.zhao.myreader.ui.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.zhao.myreader.base.application.MyApplication;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;
import com.zhao.myreader.webapi.BookApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BookLoader extends AsyncTaskLoader<List<Chapter>>{
    private ChapterService mChapterService;
    private Book mBook;
    private int downloadedCount;

    private List<ProgressListener> progressListeners;

    BookLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mChapterService = new ChapterService();
        progressListeners=new ArrayList<ProgressListener>();
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
        CountDownLatch latch=new CountDownLatch(chapters.size());

        for(Chapter chapter : chapters){
            MyApplication.getApplication().newLoader(()->{
                if(chapter.getContent()==null||chapter.getContent().equals("")) {
                    loadChapter(chapter);
                    mChapterService.updateChapter(chapter);
                }
                if(chapter.getContent()!=null){
                    synchronized(this){
                        downloadedCount++;
                    }
                    String progress=downloadedCount+"/"+chapters.size();
                    for(ProgressListener listener:progressListeners)
                        listener.notify(progress);
                }
                latch.countDown();
            });
        }
        try {
            latch.await(600, TimeUnit.SECONDS);
        }catch(Exception e){
            Log.d("BookLoader",e.getLocalizedMessage());
        };
        String progress=downloadedCount+"/"+chapters.size();
        for(ProgressListener listener:progressListeners)
            listener.notify(progress);
        System.out.println("All download task completed");
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

    private void loadChapter(Chapter chapter){
        String content= BookApi.getChapterContent(chapter);
        if(content!=null){
            chapter.setContent(content);
            Log.d("BookLoader",chapter.getTitle()+" loaded");
            System.out.println(chapter.getTitle()+" loaded");
        }

    }

    public void registerProgressListener(ProgressListener listener){
        progressListeners.add(listener);
    }
}
