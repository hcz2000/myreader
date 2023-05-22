package com.zhan.myreader.ui.home.bookcase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.core.content.ContextCompat;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhan.myreader.R;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.ui.home.MainActivity;
import com.zhan.myreader.ui.search.SearchBookActivity;
import com.zhan.myreader.util.VibratorUtil;
import com.zhan.myreader.webapi.BookApi;
import java.util.ArrayList;

/**
 * Created by zhan on 2017/7/25.
 */

public class BookcasePresenter extends BasePresenter {

    private final BookcaseFragment mBookcaseFragment;
    private final ArrayList<Book> mBooks = new ArrayList<>();//书目数组
    private BookcaseDragAdapter mBookcaseAdapter;
    private final BookService mBookService;
    private MainActivity mMainActivity;
//    private ChapterService mChapterService;

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
                VibratorUtil.Vibrate(mBookcaseFragment.getActivity(),200);
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
                mBookcaseFragment.getBookView().setTouchClashparent(((MainActivity) (mBookcaseFragment.getContext())).getVpContent());
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
//        mToolbar.setBackgroundResource(colorPrimary);
        mBookcaseFragment.getContentView().setPrimaryColorsId(colorPrimary, android.R.color.white);
        mBookcaseFragment.getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(mBookcaseFragment.getContext(), colorPrimaryDark));
    }

    @Override
    public void resume(){
        getData();
    }
}
