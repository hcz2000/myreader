package com.zhao.myreader.util.reader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Chapter>>{

    private ChapterService mChapterService;

    private Book mBook;

    BookLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mChapterService = new ChapterService();
    }

    @Override
    protected void onStartLoading(){
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public List<Chapter> loadInBackground() {
        List<Chapter> chapters=mChapterService.findBookAllChapterByBookId(mBook.getId());
        for(Chapter chapter : chapters) {
            if(chapter.getContent()==null || chapter.getContent().equals("")) {
                loadChapter(chapter);
                System.out.println(chapter.getTitle());
                mChapterService.updateChapter(chapter);
            }
        }
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
        StringBuffer content=new StringBuffer();
        String page1Url = chapter.getUrl();
        String page1Html=HttpUtil.getRequest_Sync(page1Url);
        content.append(TianLaiReadUtil.getContentFromHtml(page1Html));
        if(content.toString().equals("")) {
            chapter.setContent("");
            return;
        }

        String page2Url=TianLaiReadUtil.getNextPageFromHtml(page1Html);
        if(page2Url!=null){
            String page2Html=HttpUtil.getRequest_Sync(page2Url);
            String page2=TianLaiReadUtil.getContentFromHtml(page2Html);
            String page3Url=TianLaiReadUtil.getNextPageFromHtml(page2Html);
            if(page2.length()>2) {
                content.append(page2.substring(2));
            }
            if(page3Url!=null){
                String page3Html=HttpUtil.getRequest_Sync(page3Url);
                String page3=TianLaiReadUtil.getContentFromHtml(page3Html);
                if(page3.length()>2)
                    content.append(page3.substring(2));
            }
        }
        chapter.setContent(content.toString());
    }

}
