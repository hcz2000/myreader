package com.zhan.myreader.entity;

import android.util.Log;

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

    public Book findBookByAuthorAndName(String name,String author){
        for(Book book : books){
            if(author.equals(book.getAuthor()) && name.equals(book.getName()))
                return book;
        }
        return null;
    }

    public Book findBookById(String bookId){
        for(Book book : books){
            if(bookId.equals(book.getId()))
                return book;
        }
        return null;
    }

    public void add(Book book){
        books.add(book);
        bookService.addBook(book);
    }

    public void remove(Book book) {
        if(books.remove(book)){
            bookService.deleteBook(book);
        }
    }

    public void update(Book book){
        if(book.getId()!=null && findBookById(book.getId()) != null){
            bookService.updateEntity(book);
        }
    }

    public void update(List<Book> list){
        List<Book> changedBooks=new ArrayList<>();
        for(Book book: list){
            if(book.getId()!=null && findBookById(book.getId())!=null){
                changedBooks.add(book);
            }
        }
        bookService.updateBooks(changedBooks);
    }

    public int getCount(){
        return books.size();
    }

    public Book getBook(int position){
        return books.get(position);
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
                    int oldUnReadNum=book.getUnReadNum();
                    int newUnReadNum = newTotal - book.getTotalChapterNum();
                    Log.d("BookcasePresenter","unReadNum: "+newUnReadNum+" --"+book.getName());
                    if (newUnReadNum < 0)
                        newUnReadNum=0;
                    if (newUnReadNum !=oldUnReadNum ) {
                        book.setUnReadNum(newUnReadNum);
                        bookService.updateEntity(book);
                        notifyDataChanged();
                    }
            });
        }
    }
    
    public void registerListener(DataChangedListener listener){
        listeners.add(listener);
    }

    public void notifyDataChanged(){
        for(DataChangedListener listener: listeners){
            listener.notifyDataChanged();
        }
    }

}
