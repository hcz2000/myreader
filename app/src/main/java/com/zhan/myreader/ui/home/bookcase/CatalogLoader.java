package com.zhan.myreader.ui.home.bookcase;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.greendao.service.ChapterService;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.webapi.BookApi;

import java.util.List;

public class CatalogLoader extends AsyncTaskLoader<Book>{
    private BookService mBookService;
    private ChapterService mChapterService;
    private Book mBook;


    CatalogLoader(Context context, Book book) {
        super(context);
        mBook=book;
        mBookService = new BookService();
        mChapterService = new ChapterService();
    }

    @Override
    protected void onStartLoading(){
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Book loadInBackground() {
        Log.d("Catalogloader","catalog loading in background:"+mBook.getName());
        List<Chapter> chapters = BookApi.getBookChapters(mBook);
        int newTotal=chapters.get(chapters.size()-1).getNumber()+1;
        mBook.setTotalChapterNum(newTotal);
        mBook.setUnReadNum(0);
        mBookService.updateEntity(mBook);
        mChapterService.addChapters(chapters);
        Log.d("Catalogloader",mBook.getName()+" catalog Loaded");
        return mBook;
    }
    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void deliverResult(Book data) {
        if (isStarted()) {
            super.deliverResult(data);
        }
    }
}
