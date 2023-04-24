package com.zhao.myreader.ui.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookLoader extends AsyncTaskLoader<List<Chapter>>{
    private ChapterService mChapterService;
    private Book mBook;
    private int downloadedCount;

    private ExecutorService executorService;

    private boolean loadingStopped;

    private List<ProgressListener> progressListeners;

    BookLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mChapterService = new ChapterService();
        progressListeners=new ArrayList<ProgressListener>();
        executorService= Executors.newFixedThreadPool(5);
        loadingStopped =false;
        downloadedCount=0;
    }

    @Override
    protected void onStartLoading(){
        loadingStopped =false;
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Chapter> loadInBackground() {
        List<Chapter> chapters=mChapterService.findBookAllChapterByBookId(mBook.getId());

        for(Chapter chapter : chapters){
            if(loadingStopped) {
                break;
            }
            executorService.execute(()->{
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
            });
        }
        //executorService.shutdown();
        System.out.println("All jobs submitted");
        String progress=downloadedCount+"/"+chapters.size();
        for(ProgressListener listener:progressListeners)
            listener.notify(progress);
        return chapters;
    }
    @Override
    protected void onStopLoading() {
        System.out.println("Stop loading");
        loadingStopped =true;
        executorService.shutdown();
        cancelLoad();
    }

    @Override
    public void deliverResult(List<Chapter> data) {
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    private void loadChapter(Chapter chapter){
        StringBuffer content=new StringBuffer();
        String page1Url = chapter.getUrl();
        String page1Html=HttpUtil.httpGet_Sync(page1Url);
        content.append(TianLaiReadUtil.getContentFromHtml(page1Html));
        if(content.toString().equals("")) {
            chapter.setContent("");
        }

        String page2Url=TianLaiReadUtil.getNextPageFromHtml(page1Html);
        if(page2Url!=null){
            String page2Html=HttpUtil.httpGet_Sync(page2Url);
            String page2=TianLaiReadUtil.getContentFromHtml(page2Html);
            String page3Url=TianLaiReadUtil.getNextPageFromHtml(page2Html);
            if(page2.length()>2) {
                content.append(page2.substring(2));
            }
            if(page3Url!=null){
                String page3Html=HttpUtil.httpGet_Sync(page3Url);
                String page3=TianLaiReadUtil.getContentFromHtml(page3Html);
                if(page3.length()>2)
                    content.append(page3.substring(2));
            }
        }
        chapter.setContent(content.toString());
        Log.d("BookLoader",chapter.getTitle()+" loaded");
        System.out.println(chapter.getTitle()+" loaded");
    }

    public void registerProgressListener(ProgressListener listener){
        progressListeners.add(listener);
    }
}
