package com.zhan.myreader.ui.home.bookstore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;


import androidx.recyclerview.widget.LinearLayoutManager;


import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.entity.bookstore.BookCatalog;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.ui.bookinfo.BookInfoActivity;
import com.zhan.myreader.util.TextHelper;
import com.zhan.myreader.webapi.BookStoreApi;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhan on 2017/7/25.
 */

public class BookStorePresenter extends BasePresenter {

    final private BookStoreFragment mBookStoreFragment;
    private LinearLayoutManager mLinearLayoutManager;
    private List<BookCatalog> mBookCatalogs;
    private List<Book> bookList;
    private BookCatalog selectedCatalog;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    setupCatalogView();
                    break;
                case 2:
                    setupBookListView();
                    break;
           }
        }
    };

     BookStorePresenter(BookStoreFragment bookStoreFragment){
         super(bookStoreFragment.getContext(),bookStoreFragment.getLifecycle());
         mBookStoreFragment = bookStoreFragment;
     }


    public void init() {
         //无需加载更多
         mBookStoreFragment.getSrlBookList().setEnableLoadMore(false);
         //小说列表下拉刷新事件
         mBookStoreFragment.getSrlBookList().setOnRefreshListener(refreshLayout->getBookList());
         getBookCatalogs();
    }


    /**
     * 获取页面数据
     */
    private void getBookCatalogs(){
        mBookStoreFragment.getBinding().pbLoading.setVisibility(View.VISIBLE);
         BookStoreApi.getBookTypeList(URLCONST.nameSpace_tianlai+"/sort.html", new ResultCallback() {
             @Override
             public void onFinish(Object o, int code) {
                 mBookCatalogs = (List<BookCatalog>)o;
                 mHandler.sendMessage(mHandler.obtainMessage(1));
                 selectedCatalog=mBookCatalogs.get(0);
                 getBookList();
             }

             @Override
             public void onError(Exception e) {
                 TextHelper.showText(e.getMessage());
             }
         });
    }

    /**
     * 获取小数列表数据
     */
    private void getBookList(){

        BookStoreApi.getBookRankList(selectedCatalog.getUrl(), new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                bookList= (ArrayList<Book>)o;
                mHandler.sendMessage(mHandler.obtainMessage(2));
            }

            @Override
            public void onError(Exception e) {
                TextHelper.showText(e.getMessage());
            }
        });
    }


    /**
     * 初始化类别列表
     */
    private void setupCatalogView(){
        mBookStoreFragment.getBinding().pbLoading.setVisibility(View.GONE);
        //设置布局管理器
        mLinearLayoutManager = new LinearLayoutManager(mBookStoreFragment.getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBookStoreFragment.getRvTypeList().setLayoutManager(mLinearLayoutManager);
        BookStoreCatalogAdapter mBookStoreBookTypeAdapter = new BookStoreCatalogAdapter(mBookStoreFragment.getActivity(), mBookCatalogs);
        mBookStoreFragment.getRvTypeList().setAdapter(mBookStoreBookTypeAdapter);

        //点击事件
        mBookStoreBookTypeAdapter.setOnItemClickListener((pos, view) -> {
            mBookStoreFragment.getBinding().pbLoading.setVisibility(View.VISIBLE);
            selectedCatalog=mBookCatalogs.get(pos);
            getBookList();
        });
   }



    /**
     * 初始化小说列表
     */
    private void setupBookListView(){
        mBookStoreFragment.getBinding().pbLoading.setVisibility(View.GONE);
        //设置布局管理器
        mLinearLayoutManager = new LinearLayoutManager(mBookStoreFragment.getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBookStoreFragment.getRvBookList().setLayoutManager(mLinearLayoutManager);
        BookStoreBookAdapter mBookStoreBookAdapter = new BookStoreBookAdapter(mBookStoreFragment.getActivity(),bookList);
        mBookStoreFragment.getRvBookList().setAdapter(mBookStoreBookAdapter);

        //点击事件
        mBookStoreBookAdapter.setOnItemClickListener((pos, view) -> {
            Intent intent = new Intent(mBookStoreFragment.getActivity(), BookInfoActivity.class);
            intent.putExtra(APPCONST.BOOK, bookList.get(pos));
            //mBookStoreFragment.getActivity().startActivity(intent);
            mBookStoreFragment.startActivity(intent);
        });

        //刷新动作完成
        mBookStoreFragment.getSrlBookList().finishRefresh();
    }


}
