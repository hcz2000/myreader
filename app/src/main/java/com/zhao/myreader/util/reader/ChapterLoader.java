package com.zhao.myreader.util.reader;

import android.content.Context;
import android.content.AsyncTaskLoader;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.util.HttpUtil;
import com.zhao.myreader.util.crawler.TianLaiReadUtil;
import java.util.List;

public class ChapterLoader extends AsyncTaskLoader<Chapter>{

    private int mCurrentChapter;
    final private ChapterService mChapterService;
    final private List<Chapter> mChapters ;

    ChapterLoader(Context context, Book book) {
        super(context);
        mChapterService = new ChapterService();
        mChapters=mChapterService.findBookAllChapterByBookId(book.getId());
        mCurrentChapter=0;
    }

    @Override
    protected void onStartLoading(){
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Chapter loadInBackground() {
        if(mCurrentChapter<mChapters.size()) {
            Chapter chapter = mChapters.get(mCurrentChapter);
            if(chapter.getContent()==null || chapter.getContent().equals(""))
                loadChapter(chapter);
            mCurrentChapter++;
            return chapter;
        }else
            return null;
    }
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void deliverResult(Chapter data) {
        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    public void loadChapter(Chapter chapter){
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
