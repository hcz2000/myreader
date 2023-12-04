package com.zhan.myreader.ui.home.bookcase;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.zhan.myreader.R;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.entity.BookCase;
import com.zhan.myreader.entity.DataChangedListener;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.ui.home.MainActivity;
import com.zhan.myreader.ui.search.SearchBookActivity;
import com.zhan.myreader.util.TextHelper;
import com.zhan.myreader.util.VibratorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by zhan on 2017/7/25.
 */

public class BookcasePresenter extends BasePresenter implements LoaderManager.LoaderCallbacks, DataChangedListener {

    private final BookcaseFragment mBookcaseFragment;
    private final List<Book> mBooks ;//书目数组
    private BookcaseDragAdapter mBookcaseAdapter;
    private final BookCase mBookCase;
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
            }
        }
    };

    BookcasePresenter(BookcaseFragment bookcaseFragment) {
        super(bookcaseFragment.getContext(),bookcaseFragment.getLifecycle());
        mBookcaseFragment = bookcaseFragment;
        mBookCase = BookCase.getInstance();
        mBookCase.registerListener(this);
        mBooks=mBookCase.getBooks();
    }

    @Override
    public void start() {
        mMainActivity = ((MainActivity) (mBookcaseFragment.getContext()));
        loaderManager=mMainActivity.getLoaderManager();
        mBookcaseFragment.getContentView().setEnableRefresh(false);
        mBookcaseFragment.getContentView().setEnableHeaderTranslationContent(false);
        mBookcaseFragment.getContentView().setEnableLoadMore(false);
        mBookcaseFragment.getContentView().setOnRefreshListener((refreshLayout)-> {
            mBookCase.refresh();});
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
                });
                mBookcaseFragment.getContentView().setEnableRefresh(false);
                mBookcaseAdapter.setEditState(true);
                mBookcaseFragment.getBookView().setDragModel(DragSortGridView.DRAG_BY_LONG_CLICK);
                mMainActivity.getRlCommonTitle().setVisibility(View.GONE);
                mMainActivity.getRlEditTitile().setVisibility(View.VISIBLE);
                VibratorUtil.Vibrate(Objects.requireNonNull(mBookcaseFragment.getActivity()),200);
            }
            return true;
        });

        mBookcaseAdapter = new BookcaseDragAdapter(mBookcaseFragment.getContext(), R.layout.gridview_book_item, mBookCase, false);
        mBookcaseFragment.getBookView().setDragModel(-1);
        mBookcaseFragment.getBookView().setTouchClashparent(((MainActivity) (Objects.requireNonNull(mBookcaseFragment.getContext()))).getVpContent());
        mBookcaseFragment.getBookView().setAdapter(mBookcaseAdapter);
        /*
        mBookcaseFragment.getContentView().setOnRefreshListener(refreshLayout -> {
            System.out.println("Refresh");
        });*/
    }

    private void initView() {
        for (int i = 0; i < mBooks.size(); i++) {
            Book book=mBooks.get(i);
            Log.d("BookcasePresenter","totalChapterNum("+book.getName()+"):"+book.getTotalChapterNum());
            if(book.getTotalChapterNum()==0) {
                Bundle args=new Bundle();
                args.putString("BookId",book.getId());
                int loaderid=book.getId().hashCode();
                if(loaderManager.getLoader(loaderid)==null)
                    loaderManager.initLoader(loaderid, args, this);
            }
        }
        if (mBooks == null || mBooks.size() == 0) {
            mBookcaseFragment.getBookView().setVisibility(View.GONE);
            mBookcaseFragment.getNoDataView().setVisibility(View.VISIBLE);
        } else {
            mBookcaseFragment.getNoDataView().setVisibility(View.GONE);
            mBookcaseFragment.getBookView().setVisibility(View.VISIBLE);
        }
    }

    public void refreshData() {
        initView();
        mBookCase.refresh();
    }

    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
        mBookcaseFragment.getContentView().setPrimaryColorsId(colorPrimary, android.R.color.white);
        Objects.requireNonNull(mBookcaseFragment.getActivity()).getWindow()
            .setStatusBarColor(ContextCompat.getColor(Objects.requireNonNull(mBookcaseFragment.getContext()), colorPrimaryDark));
    }

    @Override
    public void resume(){
        Log.d("BookcasePresenter","resume");
        refreshData();
    }

    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        String bookid=args.getString("BookId");
        for(Book book : mBooks){
            if(book.getId().equals(bookid)){
                Log.d("BookcasePresenter","create CatalogLoader:"+book.getName());
                return new CatalogLoader(mMainActivity.getBaseContext(),book);
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Book book= (Book)data;
        Log.d("BookcasePresenter",book.getName() +" catalog downloaded");
        TextHelper.showText(book.getName() +" catalog downloaded");
        loaderManager.destroyLoader(loader.getId());
        mBookcaseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.d("BookcasePresenter","Loader reseting");
        //loaderManager.restartLoader(loader.getId(),null,this);;
    }

    @Override
    public void notifyDataChanged() {
        mHandler.sendMessage(mHandler.obtainMessage(1));
    }
}
