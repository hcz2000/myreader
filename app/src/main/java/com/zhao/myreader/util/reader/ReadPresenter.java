package com.zhao.myreader.util.reader;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;

import com.zhao.myreader.R;
import com.zhao.myreader.base.application.MyApplication;
import com.zhao.myreader.base.application.SysManager;
import com.zhao.myreader.base.BaseActivity;
import com.zhao.myreader.base.BasePresenter;
import com.zhao.myreader.callback.ResultCallback;
import com.zhao.myreader.common.APPCONST;
import com.zhao.myreader.creator.DialogCreator;
import com.zhao.myreader.entity.Setting;
import com.zhao.myreader.enums.BookSource;
import com.zhao.myreader.enums.Font;
import com.zhao.myreader.enums.Language;
import com.zhao.myreader.enums.ReadStyle;
import com.zhao.myreader.greendao.entity.Book;
import com.zhao.myreader.greendao.entity.Chapter;
import com.zhao.myreader.greendao.service.BookService;
import com.zhao.myreader.greendao.service.ChapterService;
import com.zhao.myreader.ui.font.FontsActivity;
import com.zhao.myreader.util.BrightUtil;
import com.zhao.myreader.util.DateHelper;
import com.zhao.myreader.util.StringHelper;
import com.zhao.myreader.util.TextHelper;
import com.zhao.myreader.webapi.CommonApi;

import java.util.ArrayList;
import java.util.Collections;


import static android.app.Activity.RESULT_OK;

/**
 * Created by zhao on 2017/7/27.
 */

public class ReadPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks{

    private ReadActivity mReadActivity;
    private Book mBook;
    private ArrayList<Chapter> mChapters = new ArrayList<>();
    private ArrayList<Chapter> mInvertedOrderChapters = new ArrayList<>();
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
    private float scrolledX;
    private float scrolledY;
    private long lastClickTime;//上次点击时间
    private long doubleClickInterval = 200;//双击确认时间
    private float settingOnClickValidFrom;
    private float settingOnClickValidTo;
    private Dialog mSettingDialog;//设置视图
    private Dialog mSettingDetailDialog;//详细设置视图
    private int curSortflag = 0; //0正序  1倒序
    private int cachedChapters = 0;//缓存章节数
    private LoaderManager loaderManager;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    init();
                    break;
                case 2://NOT USED?
                    /*hcz
                    mReadActivity.getPbLoading().setVisibility(View.GONE);
                    mReadActivity.getSrlContent().finishLoadMore();*/
                    break;
                case 3://NOT USED?
                    /*hcz
                    int position = msg.arg1;
                    mReadActivity.getRvContent().scrollToPosition(position);
                    if (position >= mChapters.size() - 1) {
                        turnToChapter(position);
                    }
                    mReadActivity.getPbLoading().setVisibility(View.GONE);*/
                    break;
                case 4:
                    int position = msg.arg1;
                    mReadActivity.getRvContent().scrollToPosition(position);
                    /*hcz
                    System.out.println("HistoryChapterNum/position:"+mBook.getHistoryChapterNum()+ "/"+position);
                    */
                    if (mBook.getHistoryChapterNum() < position) {
                        turnToChapter(position);
                    }
                    mReadActivity.getPbLoading().setVisibility(View.GONE);
                    break;
                case 5://NOT USED?
                    /*hcz
                    saveLastChapterReadPosition(msg.arg1);
                     */
                    break;
                case 6:
                    int lastPosition=mBook.getLastReadPosition();
                    mBook.setLastReadPosition(0);//下一步将触发OnScrollListener记录lastReadPosition
                    mReadActivity.getRvContent().scrollBy(0, lastPosition);
                    if (!StringHelper.isEmpty(mBook.getId())) {
                        mBookService.updateEntity(mBook);
                    }
                    break;
                case 7:
                    if (mContentLayoutManager != null) {
                        mReadActivity.getRvContent().scrollBy(0, 2);
                    }
                    break;
                case 8:
                    showSettingView();
                    break;
                case 9:
                    updateDownloadProgress((TextView)msg.obj);
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
        settingOnClickValidFrom = BaseActivity.height / 4;
        settingOnClickValidTo = BaseActivity.height / 4 * 3;
        mReadActivity.getSrlContent().setEnableLoadMore(false);
        mReadActivity.getSrlContent().setEnableRefresh(false);
        mReadActivity.getSrlContent().setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                settingChange = true;
                getData();
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
            if (StringHelper.isEmpty(mChapters.get(item).getContent())) {
                mReadActivity.getPbLoading().setVisibility(View.VISIBLE);
                CommonApi.getChapterContent(mChapters.get(item).getUrl(), new ResultCallback() {
                    @Override
                    public void onFinish(Object o, int code) {
                        mChapters.get(item).setContent((String) o);
                        mChapterService.saveOrUpdateChapter(mChapters.get(item));
                        mHandler.sendMessage(mHandler.obtainMessage(4, item, 0));
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });
            } else {
                mReadActivity.getRvContent().scrollToPosition(item);
                if (item > mBook.getHistoryChapterNum()) {
                    turnToChapter(item);
                }
            }
        });
        mReadActivity.getRvContent().addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, final int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //页面初始化的时候不要执行
                if (!isFirstInit) {
                    MyApplication.getApplication().newThread(new Runnable() {
                        @Override
                        public void run() {
                            saveLastChapterReadPosition(dy);
                        }
                    });
                } else {
                    isFirstInit = false;
                }
            }
        });

        mReadActivity.getTvChapterSort().setOnClickListener(view -> {
            if (curSortflag == 0) {//当前正序
                mReadActivity.getTvChapterSort().setText(mReadActivity.getString(R.string.positive_sort));
                curSortflag = 1;
                changeChapterSort();
            } else {//当前倒序
                mReadActivity.getTvChapterSort().setText(mReadActivity.getString(R.string.inverted_sort));
                curSortflag = 0;
                changeChapterSort();
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
        getData();
    }

    /**
     * 保存最后阅读章节的进度
     *
     * @param dy
     */
    private void saveLastChapterReadPosition(int dy) {
        if (mContentLayoutManager == null) return;
        if (mContentLayoutManager.findFirstVisibleItemPosition() != mContentLayoutManager.findLastVisibleItemPosition()
                || dy == 0) {
            mBook.setLastReadPosition(0);
        } else {
            mBook.setLastReadPosition(mBook.getLastReadPosition() + dy);
        }

        mBook.setHistoryChapterNum(mContentLayoutManager.findLastVisibleItemPosition());
        /* hcz
        System.out.println("Last Read Position:"+mBook.getLastReadPosition());
        System.out.println("HistoryChapterNum:"+mBook.getHistoryChapterNum());
         */

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
                return false;
            }
        });

        mReadContentAdapter.setClickItemListener(new ReadContentAdapter.OnClickItemListener() {
            @Override
            public void onClick(View view, final int positon) {
                if (pointY > settingOnClickValidFrom && pointY < settingOnClickValidTo) {
                    autoScrollMode = false;
                    long currentClickTime = DateHelper.getLongDate();
                    if (currentClickTime - lastClickTime < doubleClickInterval) {
                        autoScroll();
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(doubleClickInterval);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (!autoScrollMode) {
                                    mHandler.sendMessage(mHandler.obtainMessage(8));
                                }
                            }
                        }).start();
                    }
                    lastClickTime = currentClickTime;
                } else if (pointY > settingOnClickValidTo) {
                    mReadActivity.getRvContent().scrollBy(0, BaseActivity.height);
                } else if (pointY < settingOnClickValidFrom) {
                    mReadActivity.getRvContent().scrollBy(0, -BaseActivity.height);
                }
            }
        });
    }

    /**
     * 显示设置视图
     */
    private void showSettingView() {
        autoScrollMode = false;
        if (mSettingDialog != null) {
            mSettingDialog.show();
        } else {
            int progress = mContentLayoutManager.findLastVisibleItemPosition() * 100 / (mChapters.size() - 1);
            mSettingDialog = DialogCreator.createReadSetting(mReadActivity, mSetting.isDayStyle(), progress, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//返回
                            mReadActivity.finish();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//上一章
                            int curPosition = mContentLayoutManager.findLastVisibleItemPosition();
                            if (curPosition > 0) {
                                mReadActivity.getRvContent().scrollToPosition(curPosition - 1);
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//下一章
                            int curPosition = mContentLayoutManager.findLastVisibleItemPosition();
                            if (curPosition < mChapters.size() - 1) {
                                mReadActivity.getRvContent().scrollToPosition(curPosition + 1);
                                turnToChapter(curPosition + 1);
                            }
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {//目录
                            initCatalogView();
                            mReadActivity.getDlReadActivity().openDrawer(GravityCompat.START);
                            mSettingDialog.dismiss();
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
                            final int chapterNum = (mChapters.size() - 1) * i / 100;
                            getChapterContent(mChapters.get(chapterNum), new ResultCallback() {
                                @Override
                                public void onFinish(Object o, int code) {
                                    mChapters.get(chapterNum).setContent((String) o);
                                    mChapterService.saveOrUpdateChapter(mChapters.get(chapterNum));
                                    mHandler.sendMessage(mHandler.obtainMessage(4, chapterNum, 0));
                                }

                                @Override
                                public void onError(Exception e) {
                                    mHandler.sendMessage(mHandler.obtainMessage(1));
                                }
                            });
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
                                addBookToCaseAndDownload(tvDownloadProgress);
                            }else {
                                //getAllChapterData(tvDownloadProgress);
                                loadAllChapters(tvDownloadProgress);
                            }
                        }
                    });
        }

    }

    /**
     * 添加到书架并缓存整本
     * @param tvDownloadProgress
     */
    private void addBookToCaseAndDownload(final TextView tvDownloadProgress){
        DialogCreator.createCommonDialog(mReadActivity, mReadActivity.getString(R.string.tip), mReadActivity.getString(R.string.download_no_add_tips), true, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBookService.addBook(mBook);
                //getAllChapterData(tvDownloadProgress);
                loadAllChapters(tvDownloadProgress);
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
        mSettingDialog.dismiss();
        if (mSettingDetailDialog != null) {
            mSettingDetailDialog.show();
        } else {
            mSettingDetailDialog = DialogCreator.createReadDetailSetting(mReadActivity, mSetting,
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
                            init();
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
                            mSettingDetailDialog.dismiss();
                        }
                    });
        }
    }

    /**
     * 延迟跳转章节(防止跳到章节尾部)
     */
    private void turnToChapter(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                    mHandler.sendMessage(mHandler.obtainMessage(4, position, 0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * 延迟跳转章节位置
     */
    private void turnToLastPosition() {
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
                    initContentView();
                }
                break;
        }
    }

    /**
     * 初始化
     */
    private void init() {
        initContentView();
        initCatalogView();
    }

    /**
     * 初始化主内容视图
     */
    private void initContentView() {
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
            mReadContentAdapter.notifyDataSetChangedBySetting();
        }
        if (!settingChange) {
            mReadActivity.getRvContent().scrollToPosition(mBook.getHistoryChapterNum());
            turnToLastPosition();
        } else {
            settingChange = false;
        }
        mReadActivity.getPbLoading().setVisibility(View.GONE);
        mReadActivity.getSrlContent().finishLoadMore();
    }

    /**
     * 改变章节列表排序（正倒序）
     */
    private void changeChapterSort() {
        //设置布局管理器
        if (curSortflag == 0) {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mChapters);
        } else {
            mChapterTitleAdapter = new ChapterTitleAdapter(mReadActivity, R.layout.listview_chapter_title_item, mInvertedOrderChapters);
        }
        mReadActivity.getLvChapterList().setAdapter(mChapterTitleAdapter);
    }

    /**
     * 初始化章节目录视图
     */
    private void initCatalogView() {
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
     * 章节数据网络同步
     */
    private void getData() {

        CommonApi.getBookChapters(mBook, new ResultCallback() {
            @Override
            public void onFinish(Object o, int code) {
                final ArrayList<Chapter> chapters = (ArrayList<Chapter>) o;
                int newTotal=chapters.get(chapters.size()-1).getNumber()+1;
                mBook.setChapterTotalNum(newTotal);
                if (!StringHelper.isEmpty(mBook.getId())) {
                    mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
                    mBookService.updateEntity(mBook);
                }
                //System.out.println("ReadPresenter--getData: "+mChapters.size());
                updateAllOldChapterData(chapters);
                mInvertedOrderChapters.clear();
                mInvertedOrderChapters.addAll(mChapters);
                Collections.reverse(mInvertedOrderChapters);
                if (mChapters.size() == 0) {
                    TextHelper.showLongText("该书查询不到任何章节");
                    mReadActivity.getPbLoading().setVisibility(View.GONE);
                    settingChange = false;
                } else {
                    //if (mBook.getHisttoryChapterNum() < 0)
                    //    mBook.setHisttoryChapterNum(0);
                    //else if (mBook.getHisttoryChapterNum() >= mChapters.size())
                    if (mBook.getHistoryChapterNum() >= mChapters.size())
                        mBook.setHistoryChapterNum(mChapters.size() - 1);
                    getChapterContent(mChapters.get(mBook.getHistoryChapterNum()), new ResultCallback() {
                        @Override
                        public void onFinish(Object o, int code) {
                            mChapters.get(mBook.getHistoryChapterNum()).setContent((String) o);
                            mChapterService.saveOrUpdateChapter(mChapters.get(mBook.getHistoryChapterNum()));
                            mHandler.sendMessage(mHandler.obtainMessage(1));
                        }
                        @Override
                        public void onError(Exception e) {
                            mHandler.sendMessage(mHandler.obtainMessage(1));
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                mChapters = (ArrayList<Chapter>) mChapterService.findBookAllChapterByBookId(mBook.getId());
                mHandler.sendMessage(mHandler.obtainMessage(1));
            }
        });
    }

    /**
     * 更新所有章节
     *
     * @param newChapters
     */
    private void updateAllOldChapterData(ArrayList<Chapter> newChapters) {
        java.util.List<Chapter> chaptersToInsert=new ArrayList<Chapter>();
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
                newChapters.get(i).setId(StringHelper.getStringRandom(25));
                newChapters.get(i).setBookId(mBook.getId());
                chaptersToInsert.add(newChapters.get(i));
                System.out.println(""+newChapters.get(i).getNumber()+"-"+newChapters.get(i).getTitle());
            }
        }
        if(chaptersToInsert.size()>0) {
            mChapterService.addChapters(chaptersToInsert);
            mChapters.addAll(chaptersToInsert);
        }
        if(maxNewChapterNo>0 && maxNewChapterNo<mChapters.size()) {
            for (int j = maxNewChapterNo; j < mChapters.size(); j++) {
                mChapterService.deleteEntity(mChapters.get(j));
            }
            mChapters.subList(0,maxNewChapterNo);
        }
    }

    /**
     * 缓存所有章节
     */
    /*
    private void getAllChapterData(final TextView tvDownloadProgress) {
        cachedChapters = 0;
        MyApplication.getApplication().newThread(new Runnable() {
            @Override
            public void run() {
                for (final Chapter chapter : mChapters) {
                    if (StringHelper.isEmpty(chapter.getContent())) {
                        getChapterContent(chapter, new ResultCallback() {
                            @Override
                            public void onFinish(Object o, int code) {
                                chapter.setContent((String) o);
                                mChapterService.saveOrUpdateChapter(chapter);
                                cachedChapters++;
                                mHandler.sendMessage(mHandler.obtainMessage(9,tvDownloadProgress));
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                    }else {
                        cachedChapters++;
                        mHandler.sendMessage(mHandler.obtainMessage(9,tvDownloadProgress));
                    }
                }
                if (cachedChapters == mChapters.size()){
                    TextHelper.showText(mReadActivity.getString(R.string.download_already_all_tips));
                }
            }
        });
    }
*/

    private void loadAllChapters(final TextView tvDownloadProgress) {
        loaderManager.initLoader(mBook.getId().hashCode(), null, this);
    }


    private void updateDownloadProgress(TextView tvDownloadProgress){
        try {
            tvDownloadProgress.setText(cachedChapters * 100 / mChapters.size() + " %");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取章节内容
     *
     * @param chapter
     * @param resultCallback
     */
    private void getChapterContent(final Chapter chapter, ResultCallback resultCallback) {
        if (StringHelper.isEmpty(chapter.getBookId())) chapter.setId(mBook.getId());
        if (!StringHelper.isEmpty(chapter.getContent())) {
            if (resultCallback != null) {
                resultCallback.onFinish(chapter.getContent(), 0);
            }
        } else {
            if (resultCallback != null) {
                CommonApi.getChapterContent(chapter.getUrl(), resultCallback);
            } else {
                CommonApi.getChapterContent(chapter.getUrl(), new ResultCallback() {
                    @Override
                    public void onFinish(final Object o, int code) {
                        chapter.setContent((String) o);
                        mChapterService.saveOrUpdateChapter(chapter);
                    }

                    @Override
                    public void onError(Exception e) {

                    }

                });
            }
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
        init();
    }

    /**
     * 缩小字体
     */
    private void reduceTextSize() {
        if (mSetting.getReadWordSize() > 1) {
            mSetting.setReadWordSize(mSetting.getReadWordSize() - 1);
            SysManager.saveSetting(mSetting);
            settingChange = true;
            initContentView();
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
            initContentView();
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
        init();
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
        return new ChapterLoader(mReadActivity.getBaseContext(),mBook);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        Chapter chapter=(Chapter)data;
        mReadContentAdapter.notifyDataSetChanged();
        System.out.println("Loaded:"+chapter.getTitle());
        if(chapter!=null && chapter.getNumber()<mBook.getChapterTotalNum()) {
            cachedChapters++;
            loader.forceLoad();
            //mHandler.sendMessage(mHandler.obtainMessage(9,tvDownloadProgress));
        }else {
            TextHelper.showText(""+cachedChapters+"/"+mBook.getChapterTotalNum()+" cached");
            loaderManager.destroyLoader(loader.getId());
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        loaderManager.restartLoader(loader.getId(),null,this);;
    }

}
