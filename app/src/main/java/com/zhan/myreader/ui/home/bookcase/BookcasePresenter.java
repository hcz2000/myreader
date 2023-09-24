package com.zhan.myreader.ui.home.bookcase;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhan.myreader.R;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.ui.home.MainActivity;
import com.zhan.myreader.ui.home.reader.BookLoader;
import com.zhan.myreader.ui.search.SearchBookActivity;
import com.zhan.myreader.util.TextHelper;
import com.zhan.myreader.util.VibratorUtil;
import com.zhan.myreader.webapi.BookApi;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhan on 2017/7/25.
 */

public class BookcasePresenter extends BasePresenter implements LoaderManager.LoaderCallbacks{

    private final BookcaseFragment mBookcaseFragment;
    private final ArrayList<Book> mBooks = new ArrayList<>();//书目数组
    private BookcaseDragAdapter mBookcaseAdapter;
    private final BookService mBookService;
    private MainActivity mMainActivity;
    private LoaderManager loaderManager;


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mBookcaseAdapter.notifyDataSetChanged();
                    mBookcaseFragment.getContentView().finishRefresh();
                    break;
                case 2:
                    mBookcaseFragment.getContentView().finishRefresh();
                    break;
            }
        }
    };

    BookcasePresenter(BookcaseFragment bookcaseFragment) {
        super(bookcaseFragment.getContext(),bookcaseFragment.getLifecycle());
        mBookcaseFragment = bookcaseFragment;
        mBookService = new BookService();
    }

    @Override
    public void start() {
        mMainActivity = ((MainActivity) (mBookcaseFragment.getContext()));
        loaderManager=mMainActivity.getLoaderManager();
        mBookcaseFragment.getContentView().setEnableRefresh(false);
        mBookcaseFragment.getContentView().setEnableHeaderTranslationContent(false);
        mBookcaseFragment.getContentView().setEnableLoadMore(false);
        mBookcaseFragment.getContentView().setOnRefreshListener((refreshLayout)->initNoReadNum());

        mBookcaseFragment.getNoDataView().setOnClickListener(view->{
            Intent intent = new Intent(mBookcaseFragment.getContext(), SearchBookActivity.class);
            mBookcaseFragment.startActivity(intent);
        });

        mBookcaseFragment.getBookView().setOnItemLongClickListener((parent, view, position, id) -> {
            if (!mBookcaseAdapter.inEditState()) {
                mMainActivity.getTvEditAdd().setOnClickListener(v -> {
                    Intent intent = new Intent(mBookcaseFragment.getContext(), SearchBookActivity.class);
                    mBookcaseFragment.startActivity(intent);
                });

                mMainActivity.getTvEditFinish().setOnClickListener(v -> {
                    mMainActivity.getRlCommonTitle().setVisibility(View.VISIBLE);
                    mMainActivity.getRlEditTitile().setVisibility(View.GONE);
                    mBookcaseFragment.getBookView().setDragModel(-1);
                    mBookcaseAdapter.setEditState(false);
                    mBookcaseAdapter.notifyDataSetChanged();
                });
                mBookcaseFragment.getContentView().setEnableRefresh(false);
                mBookcaseAdapter.setEditState(true);
                mBookcaseFragment.getBookView().setDragModel(DragSortGridView.DRAG_BY_LONG_CLICK);
                mBookcaseAdapter.notifyDataSetChanged();
                mMainActivity.getRlCommonTitle().setVisibility(View.GONE);
                mMainActivity.getRlEditTitile().setVisibility(View.VISIBLE);
                VibratorUtil.Vibrate(Objects.requireNonNull(mBookcaseFragment.getActivity()),200);
            }
            return true;
        });
        /*
        mBookcaseFragment.getContentView().setOnRefreshListener(refreshLayout -> {
            System.out.println("Refresh");
        });*/
    }

    private void init() {
        initBook();
        if (mBooks == null || mBooks.size() == 0) {
            mBookcaseFragment.getBookView().setVisibility(View.GONE);
            mBookcaseFragment.getNoDataView().setVisibility(View.VISIBLE);
        } else {
            if(mBookcaseAdapter == null) {
                mBookcaseAdapter = new BookcaseDragAdapter(mBookcaseFragment.getContext(), R.layout.gridview_book_item, mBooks, false);
                mBookcaseFragment.getBookView().setDragModel(-1);
                mBookcaseFragment.getBookView().setTouchClashparent(((MainActivity) (Objects.requireNonNull(mBookcaseFragment.getContext()))).getVpContent());
                mBookcaseFragment.getBookView().setAdapter(mBookcaseAdapter);
            }else {
                mBookcaseAdapter.notifyDataSetChanged();
            }
            mBookcaseFragment.getNoDataView().setVisibility(View.GONE);
            mBookcaseFragment.getBookView().setVisibility(View.VISIBLE);
        }
    }

    public void getData() {
        init();
        initNoReadNum();
    }

    private void initBook() {
        mBooks.clear();
        mBooks.addAll(mBookService.getAllBooks());
        for (int i = 0; i < mBooks.size(); i++) {
            if (mBooks.get(i).getSortCode() != i + 1) {
                mBooks.get(i).setSortCode(i + 1);
                mBookService.updateEntity(mBooks.get(i));
            }
        }
    }

    private void initNoReadNum() {
        for (final Book book : mBooks) {
            if(book.getChapterTotalNum()==0) {
                Bundle args=new Bundle();
                args.putString("BookId",book.getId());
                int loaderid=Integer.parseInt(book.getId());
                if(loaderManager.getLoader(loaderid)==null)
                    loaderManager.initLoader(loaderid, args, this);
            }
            BookApi.getNewChapterCount(book, new ResultCallback() {
                @Override
                public void onFinish(Object obj, int code) {
                    int newTotal=(int)obj;
                    int noReadNum = newTotal - book.getChapterTotalNum();
                    if (noReadNum > 0) {
                        book.setNoReadNum(noReadNum);
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    } else {
                        book.setNoReadNum(0);
                        mHandler.sendMessage(mHandler.obtainMessage(2));
                    }
                    mBookService.updateEntity(book);
                }

                @Override
                public void onError(Exception e) {
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }
            });
        }
    }

    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
        mBookcaseFragment.getContentView().setPrimaryColorsId(colorPrimary, android.R.color.white);
        Objects.requireNonNull(mBookcaseFragment.getActivity()).getWindow()
            .setStatusBarColor(ContextCompat.getColor(Objects.requireNonNull(mBookcaseFragment.getContext()), colorPrimaryDark));
    }

    @Override
    public void resume(){
        getData();
    }

    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        String bookid=args.getString("BookId");
        for(Book book : mBooks){
            if(book.getId().equals(bookid))
                return new CatalogLoader(mMainActivity.getBaseContext(),book);
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Log.d("ReadPresenter","Download completed");
        TextHelper.showText(((Book)data).getName() +" catalog downloaded");
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.d("ReadPresenter","Loader reseting");
        //loaderManager.restartLoader(loader.getId(),null,this);;
    }
}
