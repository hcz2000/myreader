package com.zhan.myreader.ui.home.reader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.app.LoaderManager;
import android.content.Loader;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.zhan.myreader.R;
import com.zhan.myreader.base.application.MyApplication;
import com.zhan.myreader.base.application.SysManager;
import com.zhan.myreader.base.BaseActivity;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.creator.DialogCreator;
import com.zhan.myreader.ui.font.FontsActivity;
import com.zhan.myreader.entity.Setting;
import com.zhan.myreader.enums.BookSource;
import com.zhan.myreader.enums.Font;
import com.zhan.myreader.enums.Language;
import com.zhan.myreader.enums.ReadStyle;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.greendao.entity.Chapter;
import com.zhan.myreader.greendao.service.BookService;
import com.zhan.myreader.greendao.service.ChapterService;
import com.zhan.myreader.util.BrightUtil;
import com.zhan.myreader.util.DateHelper;
import com.zhan.myreader.util.StringHelper;
import com.zhan.myreader.util.TextHelper;
import com.zhan.myreader.webapi.BookApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import static android.app.Activity.RESULT_OK;

/**
 * Created by zhan on 2017/7/27.
 */

public class ReadPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks, ProgressListener {

    private ReadActivity mReadActivity;
    private Book mBook;
    private List<Chapter> mChapters;
    private List<Chapter> mInvertedOrderChapters;
    private ChapterService mChapterService;
    private BookService mBookService;
    private ReadContentAdapter mReadContentAdapter;
    private ChapterTitleAdapter mChapterTitleAdapter;
    private Setting mSetting;
    private LinearLayoutManager mContentLayoutManager;
    private boolean isFirstInit = true;
    private boolean settingChange;//是否是设置改变
    private boolean autoScrollMode = false;//是否开启自动滑动
    private float pointX;
    private float pointY;
    private long lastClickTime;//上次点击时间
    private long doubleClickInterval = 200;//双击确认时间
    private float settingOnClickValidFrom;
    private float settingOnClickValidTo;
    private Dialog mOperationDialog;//设置视图
    private Dialog mSettingDialog;//详细设置视图
    private int curSortflag = 0; //0正序  1倒序
    private LoaderManager loaderManager;
    private BookLoader bookLoader;
    private TextView downloadProgressView;
    private String downloadProgress;
    private boolean downloadInProgress=false;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    setupViews();
                    break;
                case 2://NOT USED
                    break;
                case 3://NOT USED
                    break;
                case 4://NOT USED
                    break;
                case 5://NOT USED
                    break;
                case 6://linger move to offset of special chapter
                    int lastPosition=mBook.getLastReadPosition();
                    mBook.setLastReadPosition(0);//下一步将触发OnScrollListener记录lastReadPosition
                    Log.d("ReadPresenter","lastPosition:"+lastPosition);
                    mReadActivity.getRvContent().scrollBy(0, lastPosition);
                    if (!StringHelper.isEmpty(mBook.getId())) {
                        mBookService.updateEntity(mBook);
                    }
                    break;
                case 7://Auto Scroll
                    if (mContentLayoutManager != null) {
                        mReadActivity.getRvContent().scrollBy(0, 2);
                    }
                    break;
                case 8:
                    showBottomMenu();
                    break;
                case 9:
                    refreshDownloadProgress();
                    break;
            }
        }
    };


    public ReadPresenter(ReadActivity readActivity) {
        super(readActivity,readActivity.getLifecycle());
        mReadActivity = readActivity;
        mBookService = new BookService();
        mChapterService = new ChapterService();
        mSetting = SysManager.getSetting();
        mChapters=new ArrayList<>();
        mInvertedOrderChapters=new ArrayList<>();
    }


    @Override
    public void create() {
        if (mSetting.isDayStyle()) {
            mReadActivity.getDlReadActivity().setBackgroundResource(mSetting.getReadBgColor());
        } else {
            mReadActivity.getDlReadActivity().setBackgroundResource(R.color.sys_night_bg);
        }
        if (!mSetting.isBrightFollowSystem()) {
            BrightUtil.setBrightness(mReadActivity, mSetting.getBrightProgress());
        }
        mBook = (Book) mReadActivity.getIntent().getSerializableExtra(APPCONST.BOOK);
        if (StringHelper.isEmpty(mBook.getSource())){
            mBook.setSource(BookSource.tianlai.toString());
            mBookService.updateEntity(mBook);
        }
        //HCZ 20230715
        if (!StringHelper.isEmpty(mBook.getId())){
            mChapters.addAll(mChapterService.findBookAllChapterByBookId(mBook.getId()));
            mInvertedOrderChapters.addAll(mChapters);
            Collections.reverse(mInvertedOrderChapters);
        }else{
            BookApi.getPreviewChapters(mBook,new ResultCallback() {
                @Override
                public void onFinish(Object o, int code) {
                    final List<Chapter> chapters = (List<Chapter>) o;
                    int newTotal=chapters.get(chapters.size()-1).getNumber()+1;
                    mBook.setTotalChapterNum(newTotal);
                    mChapters.addAll(chapters);
                    if (mChapters.size() == 0) {
                        TextHelper.showLongText("该书查询不到任何章节");
                        mReadActivity.getPbLoading().setVisibility(View.GONE);
                        settingChange = false;
                    } else {
                        mInvertedOrderChapters.addAll(mChapters);
                        Collections.reverse(mInvertedOrderChapters);
                        mHandler.sendMessage(mHandler.obtainMessage(1));
                    }
                }

                @Override
                public void onError(Exception e) {
                    TextHelper.showLongText("获取章节目录出错");
                }
            });

        }
        settingOnClickValidFrom = BaseActivity.width / 4;
        settingOnClickValidTo = BaseActivity.width / 4 * 3;
        mReadActivity.getSrlContent().setEnableLoadMore(true);
        mReadActivity.getSrlContent().setEnableRefresh(false);
        mReadActivity.getSrlContent().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                mReadActivity.getSrlContent().setEnableLoadMore(false);
                Log.d("ReadPresenter","Loading More...");
                Toast.makeText(mReadActivity,"刷新章节列表",Toast.LENGTH_SHORT);
                settingChange = true;
                refreshData();
                Log.d("ReadPresenter","Loaded！");
            }
        });
        mReadActivity.getPbLoading().setVisibility(View.VISIBLE);
        mReadActivity.getLvChapterList().setOnItemClickListener((adapterView, view, i, l) -> {
            //关闭侧滑菜单
            mReadActivity.getDlReadActivity().closeDrawer(GravityCompat.START);
            final int item;
            if (curSortflag == 0) {
                item = i;
            } else {
                item = mChapters.size() - 1 - i;
            }
            ((LinearLayoutManager)mReadActivity.getRvContent().getLayoutManager()).scrollToPositionWithOffset(item,0);
            mBook.setLastReadPosition(0);
        });
        mReadActivity.getRvContent().addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //页面初始化的时候不要执行
                if (!isFirstInit) {
                    //MyApplication.getApplication().newThread(()->saveLastPosition(dy));
                    saveLastPosition(dy);
                } else {
                    isFirstInit = false;
                }
            }
        });

        mReadActivity.getTvChapterSort().setOnClickListener(view -> {
            if (curSortflag == 0) {//当前正序
                mReadActivity.getTvChapterSort().setText(mReadActivity.getString(R.string.positive_sort));
                curSortflag = 1;
                changeCatalogSortMode();
            } else {//当前倒序
                mReadActivity.getTvChapterSort().setText(mReadActivity.getString(R.string.inverted_sort));
                curSortflag = 0;
                changeCatalogSortMode();
            }
        });

        //关闭手势滑动
        mReadActivity.getDlReadActivity().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mReadActivity.getDlReadActivity().addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                //打开手势滑动
                mReadActivity.getDlReadActivity().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                //关闭手势滑动
                mReadActivity.getDlReadActivity().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        loaderManager =mReadActivity.getLoaderManager();
        //HCZ 20230715
        if(mChapters.isEmpty()) {
            Log.d("ReadPresenter",mBook.getName()+mBook.getId());
            refreshData();
        }
        setupViews();
    }

    /**
     * 保存最后阅读章节的进度
     *
     * @param dy
     */
    private void saveLastPosition(int dy) {
        if (mContentLayoutManager == null) return;
        mBook.setLastReadPosition(mBook.getLastReadPosition() + dy);
        Log.d("ReadPresenter","lastReadPos:"+mBook.getLastReadPosition()+" dy:"+dy+" firstVisiblePos:"+mContentLayoutManager.findFirstVisibleItemPosition()+" lastVisiblePos:"+mContentLayoutManager.findLastVisibleItemPosition());
        mBook.setHistoryChapterNum(mContentLayoutManager.findFirstVisibleItemPosition());
        if (!StringHelper.isEmpty(mBook.getId())) {
            mBookService.updateEntity(mBook);
        }
    }

    /**
     * 初始化阅读界面点击事件
     */
    private void initReadViewOnClick() {
        mReadContentAdapter.setTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                pointY = event.getRawY();
                pointX = event.getRawX();
                return false;
            }
        });

        mReadContentAdapter.setClickItemListener(new ReadContentAdapter.OnClickItemListener() {
            @Override
            public void onClick(View view, final int positon) {
                if (pointX > settingOnClickValidFrom && pointX < settingOnClickValidTo) {
                    autoScrollMode = false;
                    long currentClickTime = DateHelper.getLongDate();
                    if (currentClickTime - lastClickTime < doubleClickInterval) {
                        autoScroll();
                    } else {
                        new Thread(() -> {
                            try {
                                Thread.sleep(doubleClickInterval);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (!autoScrollMode) {
                                mHandler.sendMessage(mHandler.obtainMessage(8));
                            }
                        }).start();
                    }
                    lastClickTime = currentClickTime;
                } else if (pointX > settingOnClickValidTo) {
                    mReadActivity.getRvContent().scrollBy(0, BaseActivity.height);
                } else if (pointX < settingOnClickValidFrom) {
                    mReadActivity.getRvContent().scrollBy(0, -BaseActivity.height);
                }
            }
        });
    }

    /**
     * 显示设置视图
     */
    private void showBottomMenu() {
        autoScrollMode = false;
        if (mOperationDialog != null) {
            mOperationDialog.show();
        } else {
            int progress = mContentLayoutManager.findLastVisibleItemPosition() * 100 / (mChapters.size() - 1);
            mOperationDialog = DialogCreator.createReadSetting(mReadActivity, mSetting.isDayStyle(), progress, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//返回
                            mReadActivity.finish();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//上一章
                            //int curPosition = mContentLayoutManager.findLastVisibleItemPosition();
                            int curPosition = mContentLayoutManager.findFirstVisibleItemPosition();
                            if (curPosition > 0) {
                                mReadActivity.getRvContent().scrollToPosition(curPosition - 1);
                                mBook.setLastReadPosition(0);
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//下一章
                            int curPosition = mContentLayoutManager.findLastVisibleItemPosition();
                            if (curPosition < mChapters.size() - 1) {
                                ((LinearLayoutManager)mReadActivity.getRvContent().getLayoutManager()).scrollToPositionWithOffset(curPosition + 1,0);
                                mBook.setLastReadPosition(0);
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//目录
                            setupCatalogView();
                            mReadActivity.getDlReadActivity().openDrawer(GravityCompat.START);
                            mOperationDialog.dismiss();
                        }
                    }, new DialogCreator.OnClickNightAndDayListener() {
                        @Override
                        public void onClick(Dialog dialog, View view, boolean isDayStyle) {//日夜切换
                            changeNightAndDaySetting(isDayStyle);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//设置
                            showSettingDetailView();
                        }
                    }, new SeekBar.OnSeekBarChangeListener() {//阅读进度
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                            mReadActivity.getPbLoading().setVisibility(View.VISIBLE);
                            final int newChapterNum = (mChapters.size() - 1) * i / 100;
                            ((LinearLayoutManager)mReadActivity.getRvContent().getLayoutManager()).scrollToPositionWithOffset(newChapterNum,0);
                            mBook.setLastReadPosition(0);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    }
                    , null, new DialogCreator.OnClickDownloadAllChapterListener() {//缓存整本
                        @Override
                        public void onClick(Dialog dialog, View view, TextView tvDownloadProgress) {
                            if (StringHelper.isEmpty(mBook.getId())){
                                addToBookcaseAndDownload(tvDownloadProgress);
                            }else {
                                downloadBook(tvDownloadProgress);
                            }
                        }
                    });
        }
    }

    /**
     * 添加到书架并缓存整本
     * @param tvDownloadProgress
     */
    private void addToBookcaseAndDownload(final TextView tvDownloadProgress){
        DialogCreator.createCommonDialog(mReadActivity, mReadActivity.getString(R.string.tip), mReadActivity.getString(R.string.download_no_add_tips), true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBookService.addBook(mBook);
                downloadBook(tvDownloadProgress);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 显示详细设置视图
     */
    private void showSettingDetailView() {
        mOperationDialog.dismiss();
        if (mSettingDialog != null) {
            mSettingDialog.show();
        } else {
            mSettingDialog = DialogCreator.createReadDetailSetting(mReadActivity, mSetting,
                    new DialogCreator.OnReadStyleChangeListener() {
                        @Override
                        public void onChange(ReadStyle readStyle) {
                            changeStyle(readStyle);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reduceTextSize();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            increaseTextSize();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mSetting.getLanguage() == Language.simplified) {
                                mSetting.setLanguage(Language.traditional);
                            } else {
                                mSetting.setLanguage(Language.simplified);
                            }
                            SysManager.saveSetting(mSetting);
                            settingChange = true;
                            setupViews();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mReadActivity, FontsActivity.class);
                            mReadActivity.startActivityForResult(intent, APPCONST.REQUEST_FONT);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            autoScroll();
                            mSettingDialog.dismiss();
                        }
                    });
        }
    }

    /**
     * 延迟跳转至章节内指定偏移量
     */
    private void lingerToLastPosition() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    mHandler.sendMessage(mHandler.obtainMessage(6));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 字体结果回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case APPCONST.REQUEST_FONT:
                if (resultCode == RESULT_OK) {
                    mSetting.setFont((Font) data.getSerializableExtra(APPCONST.FONT));
                    settingChange = true;
                    setupContentView();
                }
                break;
        }
    }

    /**
     * 初始化
     */
    private void setupViews() {
        setupContentView();
        setupCatalogView();
    }

    /**
     * 初始化主内容视图
     */
    private void setupContentView() {
        if (mSetting.isDayStyle()) {
            mReadActivity.getDlReadActivity().setBackgroundResource(mSetting.getReadBgColor());
        } else {
            mReadActivity.getDlReadActivity().setBackgroundResource(R.color.sys_night_bg);
        }
        if (mReadContentAdapter == null) {
            //设置布局管理器
            mContentLayoutManager = new LinearLayoutManager(mReadActivity);
            mContentLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mReadActivity.getRvContent().setLayoutManager(mContentLayoutManager);
            mReadContentAdapter = new ReadContentAdapter(mReadActivity, R.layout.listview_chapter_content_item, mChapters, mBook);
            initReadViewOnClick();
            mReadActivity.getRvContent().setAdapter(mReadContentAdapter);
        } else {
            initReadViewOnClick();
            mReadContentAdapter.notifyDataSetChangedBySetting();
        }
        if (!settingChange) {
            ((LinearLayoutManager)mReadActivity.getRvContent().getLayoutManager()).scrollToPositionWithOffset(mBook.getHistoryChapterNum(),0);
            lingerToLastPosition();
        } else {
            settingChange = false;
        }
        mReadActivity.getPbLoading().setVisibility(View.GONE);
        mReadActivity.getSrlContent().finishLoadMore();
    }

    /**
     * 初始化章节目录视图
     */
    private void setupCatalogView() {
        if (mSetting.isDayStyle()) {
            mReadActivity.getTvCatalog().setTextColor(mReadActivity.getResources().getColor(mSetting.getReadWordColor()));
            mReadActivity.getTvChapterSort().setTextColor(mReadActivity.getResources().getColor(mSetting.getReadWordColor()));
        } else {
            mReadActivity.getTvCatalog().setTextColor(mReadActivity.getResources().getColor(R.color.sys_night_word));
            mReadActivity.getTvChapterSort().setTextColor(mReadActivity.getResources().getColor(R.color.sys_night_word));
        }
        if (mSetting.isDayStyle()) {
            mReadActivity.getLlCatalogView().setBackgroundResource(mSetting.getReadBgColor());
        } else {
            mReadActivity.getLlCatalogView().setBackgroundResource(R.color.sys_night_bg);
        }
        int selectedPostion, curChapterPosition;

        //设置布局管理器
        if (curSortflag == 0) {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mChapters);
            curChapterPosition = mContentLayoutManager.findLastVisibleItemPosition();
            selectedPostion = curChapterPosition - 5;
            if (selectedPostion < 0) selectedPostion = 0;
            if (mChapters.size() - 1 - curChapterPosition < 5) selectedPostion = mChapters.size();
            mChapterTitleAdapter.setCurChapterPosition(curChapterPosition);
        } else {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mInvertedOrderChapters);
            curChapterPosition = mChapters.size() - 1 - mContentLayoutManager.findLastVisibleItemPosition();
            selectedPostion = curChapterPosition - 5;
            if (selectedPostion < 0) selectedPostion = 0;
            if (mChapters.size() - 1 - curChapterPosition < 5) selectedPostion = mChapters.size();
            mChapterTitleAdapter.setCurChapterPosition(curChapterPosition);
        }
        mReadActivity.getLvChapterList().setAdapter(mChapterTitleAdapter);
        mReadActivity.getLvChapterList().setSelection(selectedPostion);
    }


    /**
     * 改变章节列表排序（正倒序）
     */
    private void changeCatalogSortMode() {
        //设置布局管理器
        if (curSortflag == 0) {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mChapters);
        } else {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mInvertedOrderChapters);
        }
        mReadActivity.getLvChapterList().setAdapter(mChapterTitleAdapter);
    }


    /**
     * 章节数据网络同步
     */
    private void refreshData() {
        //HCZ 20230715, 临时浏览书籍不支持动态刷新
        if(StringHelper.isEmpty(mBook.getId()))
            return;
        BookApi.getBookChapters(mBook, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                final List<Chapter> chapters = (List<Chapter>) o;
                int oldTotal=mBook.getTotalChapterNum();
                int newTotal=chapters.get(chapters.size()-1).getNumber()+1;

                if (oldTotal!=newTotal) {
                    mBook.setTotalChapterNum(newTotal);
                    mBookService.updateEntity(mBook);
                }
                updateCatalog(chapters);
                mInvertedOrderChapters.clear();
                mInvertedOrderChapters.addAll(mChapters);
                Collections.reverse(mInvertedOrderChapters);

                if (mChapters.size() == 0) {
                    TextHelper.showLongText("该书查询不到任何章节");
                    mReadActivity.getPbLoading().setVisibility(View.GONE);
                    settingChange = false;
                } else {
                    if (mBook.getHistoryChapterNum() >= mChapters.size()) {
                        mBook.setHistoryChapterNum(mChapters.size() - 1);
                        mBookService.updateEntity(mBook);
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(1));
                }
            }

            @Override
            public void onError(Exception e) {
                mChapters = (List<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
     }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */

    private void updateCatalog(List<Chapter> newChapters) {
        List<Chapter> chaptersToInsert=new ArrayList<Chapter>();
        int maxNewChapterNo=0;
        for (int i = 0;i < newChapters.size(); i++) {
            Chapter newChapter = newChapters.get(i);
            int newChapterNo=newChapter.getNumber();
            maxNewChapterNo=newChapterNo;
            if(newChapterNo<mChapters.size()){
                Chapter oldChapter = mChapters.get(newChapterNo);
                if (!oldChapter.getTitle().equals(newChapter.getTitle())) {
                    oldChapter.setTitle(newChapter.getTitle());
                    oldChapter.setUrl(newChapter.getUrl());
                    oldChapter.setContent(null);
                    mChapterService.updateEntity(oldChapter);
                }
            }else{
                //newChapters.get(i).setId(StringHelper.getStringRandom(25));
                //newChapters.get(i).setBookId(mBook.getId());
                chaptersToInsert.add(newChapters.get(i));
                Log.d("ReadPresenter","New chapter found:"+newChapters.get(i).getNumber()+"-"+newChapters.get(i).getTitle());
            }
        }
        if(chaptersToInsert.size()>0) {
            mChapterService.addChapters(chaptersToInsert);
            mChapters.addAll(chaptersToInsert);
        }
        if(maxNewChapterNo>0 && maxNewChapterNo+1<mChapters.size()) {
            for (int j = maxNewChapterNo+1; j < mChapters.size(); j++) {
                mChapterService.deleteEntity(mChapters.get(j));
            }
            mChapters.subList(0,maxNewChapterNo+1);
        }
    }

    private void downloadBook(final TextView tvDownloadProgress) {
        downloadProgressView=tvDownloadProgress;
        synchronized(this){
            if(bookLoader==null){
                downloadInProgress=true;
                bookLoader=(BookLoader)loaderManager.initLoader(mBook.getId().hashCode(), null, this);
                bookLoader.registerProgressListener(this);
            }else {
                if(downloadInProgress){
                    TextHelper.showText("BookLoader is now processing");
                }else{
                    downloadInProgress=true;
                    loaderManager.restartLoader(bookLoader.getId(), null, this);
                }
            }
        }
    }

    private void refreshDownloadProgress(){
        try {
            if(downloadProgressView!=null)
                downloadProgressView.setText(this.downloadProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 白天夜间改变
     *
     * @param isCurDayStyle
     */
    private void changeNightAndDaySetting(boolean isCurDayStyle) {
        mSetting.setDayStyle(!isCurDayStyle);
        SysManager.saveSetting(mSetting);
        settingChange = true;
        setupViews();
    }

    /**
     * 缩小字体
     */
    private void reduceTextSize() {
        if (mSetting.getReadWordSize() > 1) {
            mSetting.setReadWordSize(mSetting.getReadWordSize() - 1);
            SysManager.saveSetting(mSetting);
            settingChange = true;
            setupContentView();
        }
    }

    /**
     * 增大字体
     */
    private void increaseTextSize() {
        if (mSetting.getReadWordSize() < 40) {
            mSetting.setReadWordSize(mSetting.getReadWordSize() + 1);
            SysManager.saveSetting(mSetting);
            settingChange = true;
            setupContentView();
        }
    }

    /**
     * 改变阅读风格
     *
     * @param readStyle
     */
    private void changeStyle(ReadStyle readStyle) {
        settingChange = true;
        if (!mSetting.isDayStyle()) mSetting.setDayStyle(true);
        mSetting.setReadStyle(readStyle);
        switch (readStyle) {
            case common:
                mSetting.setReadBgColor(R.color.sys_common_bg);
                mSetting.setReadWordColor(R.color.sys_common_word);
                break;
            case leather:
                mSetting.setReadBgColor(R.mipmap.theme_leather_bg);
                mSetting.setReadWordColor(R.color.sys_leather_word);
                break;
            case protectedEye:
                mSetting.setReadBgColor(R.color.sys_protect_eye_bg);
                mSetting.setReadWordColor(R.color.sys_protect_eye_word);
                break;
            case breen:
                mSetting.setReadBgColor(R.color.sys_breen_bg);
                mSetting.setReadWordColor(R.color.sys_breen_word);
                break;
            case blueDeep:
                mSetting.setReadBgColor(R.color.sys_blue_deep_bg);
                mSetting.setReadWordColor(R.color.sys_blue_deep_word);
                break;
        }
        SysManager.saveSetting(mSetting);
        setupViews();
    }

    /**
     * 自动滚动
     */
    private void autoScroll() {
        autoScrollMode = true;
        new Thread(() -> {
            while (autoScrollMode) {
                try {
                    Thread.sleep(mSetting.getAutoScrollSpeed() + 1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(7));
            }
        }).start();
    }

    @Override
    public void destroy(){
        MyApplication.getApplication().shutdownThreadPool();
    }

    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        return new BookLoader(mReadActivity.getBaseContext(),mBook);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Log.d("ReadPresenter","Download completed");
        mChapters=(List<Chapter>)data;
        mReadContentAdapter.notifyDataSetChanged();
        this.downloadProgressView.setText("重载");
        TextHelper.showText(this.downloadProgress+" cached");
        loaderManager.destroyLoader(loader.getId());
        downloadInProgress=false;
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        Log.d("ReadPresenter","Loader reseting");
        //loaderManager.restartLoader(loader.getId(),null,this);;
    }

    public void notify(String progress){
        this.downloadProgress=progress;
        mHandler.sendMessage(mHandler.obtainMessage(9));
    }

}
