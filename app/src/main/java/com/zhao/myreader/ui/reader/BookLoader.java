package com.zhao.myreader.ui.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BookLoader extends AsyncTaskLoader<List<Chapter>>{
    private ChapterService mChapterService;
    private Book mBook;

    private List<ProgressListener> progressListeners;

    BookLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mChapterService = new ChapterService();
        progressListeners=new ArrayList<ProgressListener>();
    }

    @Override
    protected void onStartLoading(){
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Chapter> loadInBackground() {
        List<Chapter> chapters=mChapterService.findBookAllChapterByBookId(mBook.getId());
        int downloadedCount=0;
        ExecutorService executorService= Executors.newFixedThreadPool(5);
        for(Chapter chapter : chapters){
            Future<Integer> result=executorService.submit(()->{
                Integer res=loadChapter(chapter);
                mChapterService.updateChapter(chapter);
                return res;
            });
            try {
                downloadedCount=downloadedCount+result.get();
            } catch (Exception e) {  }
            if( downloadedCount%10 == 0 ){
                String progress=downloadedCount+"/"+chapters.size();
                for(ProgressListener listener:progressListeners)
                    listener.notify(progress);
            }
        }
        executorService.shutdown();
        /*
        for(Chapter chapter : chapters) {
            if(chapter.getContent()==null || chapter.getContent().equals("")) {
                loadChapter(chapter);
                System.out.println(chapter.getTitle());
                mChapterService.updateChapter(chapter);
            }

            if(chapter.getContent()!=null){
                downloadedCount++;
                if( downloadedCount%10 == 0 ){
                    String progress=downloadedCount+"/"+chapters.size();
                    for(ProgressListener listener:progressListeners)
                        listener.notify(progress);
                }
            }
        }*/
        String progress=downloadedCount+"/"+chapters.size();
        for(ProgressListener listener:progressListeners)
            listener.notify(progress);
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

    private Integer loadChapter(Chapter chapter){
        System.out.println("Loading: "+chapter.getTitle());
        StringBuffer content=new StringBuffer();
        String page1Url = chapter.getUrl();
        String page1Html=HttpUtil.httpGet_Sync(page1Url);
        content.append(TianLaiReadUtil.getContentFromHtml(page1Html));
        if(content.toString().equals("")) {
            chapter.setContent("");
            return new Integer(0);
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
        return new Integer(1);
    }

    public void registerProgressListener(ProgressListener listener){
        progressListeners.add(listener);
    }
}
