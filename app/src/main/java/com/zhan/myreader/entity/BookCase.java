package com.zhan.myreader.entity;

import android.util.Log;

import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.webapi.BookApi;

import java.util.ArrayList;
import java.util.List;

public class BookCase {
    static private BookCase bookcase;
    private List<Book> books;
    private BookService bookService;
    private List<DataChangedListener> listeners;
    private BookCase(){
        bookService=new BookService();
        books=new ArrayList<>();
        books.addAll(bookService.getAllBooks());
        listeners=new ArrayList<>();
    }

    public List<Book> getBooks(){
        return books;
    }

    public void add(Book book){
        books.add(book);
        bookService.addBook(book);
    }

    public void remove(Book book) {
        books.remove(book);
        bookService.deleteBook(book);
    }

    public void sync(Book book){
        bookService.updateEntity(book);
    }

    static public BookCase getInstance(){
        if(bookcase == null)
            bookcase=new BookCase();
        return bookcase;
    }

    public void refresh(){
        for (final Book book : books) {
            BookApi.getNewChapterCount(book, (result, code) -> {
                    int newTotal=(int)result;
                    int unReadNum = newTotal - book.getTotalChapterNum();
                    Log.d("BookcasePresenter","unReadNum: "+unReadNum+" --"+book.getName());
                    if (unReadNum > 0) {
                        book.setUnReadNum(unReadNum);
                        bookService.updateEntity(book);
                    } else {
                        book.setUnReadNum(0);
                        bookService.updateEntity(book);
                    }
                    notifyDataChanged();
            });
        }
    }
    
    public void registerListener(DataChangedListener listener){
        listeners.add(listener);
    }

    private void notifyDataChanged(){
        for(DataChangedListener listener: listeners){
            listener.notify();
        }
    }

}
