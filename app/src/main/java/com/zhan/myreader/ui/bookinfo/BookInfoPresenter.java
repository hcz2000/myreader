package com.zhan.myreader.ui.bookinfo;

import android.content.Intent;
import android.os.Handler;
import android.view.View;

import com.bumptech.glide.Glide;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.enums.BookSource;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.ui.reader.ReadActivity;
import com.zhan.myreader.util.StringHelper;
import com.zhan.myreader.util.TextHelper;
import com.zhan.myreader.webapi.BookStoreApi;


/**
 * Created by zhan on 2017/7/27.
 */

public class BookInfoPresenter extends  BasePresenter {

    private BookInfoActivity mBookInfoActivity;
    private Book mBook;
    private BookService mBookService;

    private Handler mHandle = new Handler(message -> {
        switch (message.what){
            case 1:
                init();
                break;
        }
        return false;
    });

    public BookInfoPresenter(BookInfoActivity bookInfoActivity){
        super(bookInfoActivity,bookInfoActivity.getLifecycle());
        mBookInfoActivity  = bookInfoActivity;
        mBookService = new BookService();
    }

     @Override
     public void create() {
            mBook = (Book) mBookInfoActivity.getIntent().getSerializableExtra(APPCONST.BOOK);
            if (StringHelper.isEmpty(mBook.getSource()) || BookSource.tianlai.toString().equals(mBook.getSource())){
                init();
            }else if(BookSource.biquge.toString().equals(mBook.getSource())){
                //getData();
            }
    }

    /*
    private void getData(){
        BookStoreApi.getBookInfo(mBook, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                mBook = (Book)o;
                mHandle.sendMessage(mHandle.obtainMessage(1));
            }

            @Override
            public void onError(Exception e) {
                TextHelper.showText(e.getMessage());

            }
        });
    }*/

    private void init(){
        mBookInfoActivity.getTvTitleText().setText(mBook.getName());
        mBookInfoActivity.getTvBookAuthor().setText(mBook.getAuthor());
        mBookInfoActivity.getTvBookDesc().setText(mBook.getDesc());
        mBookInfoActivity.getTvBookType().setText(mBook.getType());
        mBookInfoActivity.getTvBookName().setText(mBook.getName());
        if (isBookCollected()){
            mBookInfoActivity.getBtnAddBookcase().setText("不追了");
        }else {
            mBookInfoActivity.getBtnAddBookcase().setText("加入书架");
        }
        mBookInfoActivity.getLlTitleBack().setOnClickListener(view -> mBookInfoActivity.finish());
        mBookInfoActivity.getBtnAddBookcase().setOnClickListener(view -> {
            if (StringHelper.isEmpty(mBook.getId())){
                mBookService.addBook(mBook);
                TextHelper.showText("成功加入书架");
                mBookInfoActivity.getBtnAddBookcase().setText("不追了");
            }else {
                mBookService.deleteBookById(mBook.getId());
                TextHelper.showText("成功移除书籍");
                mBookInfoActivity.getBtnAddBookcase().setText("加入书架");
            }

        });
        mBookInfoActivity.getBtnReadBook().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mBookInfoActivity, ReadActivity.class);
                intent.putExtra(APPCONST.BOOK,mBook);
                mBookInfoActivity.startActivity(intent);
            }
        });
        Glide.with(mBookInfoActivity)
                .load(URLCONST.nameSpace_tianlai+mBook.getImgUrl())
                .into(mBookInfoActivity.getIvBookImg());
    }

    private boolean isBookCollected(){
        Book book = mBookService.findBookByAuthorAndName(mBook.getName(),mBook.getAuthor());
        if (book == null){
            return false;
        }else {
            mBook.setId(book.getId());
            return true;
        }
    }

}
