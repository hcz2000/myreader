package com.zhan.myreader.ui.home.stock;


import static java.util.concurrent.TimeUnit.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.zhan.myreader.R;
import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.greendao.entity.Stock;
import com.zhan.myreader.greendao.service.StockService;
import com.zhan.myreader.ui.home.MainActivity;
import com.zhan.myreader.util.VibratorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by zhan on 2017/7/25.
 */

public class StockPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks {

    private final StockFragment mStockFragment;
    private final List<Stock> mStocks;
    private StockDragAdapter mStockAdapter;
    private final StockService mStockService;
    private MainActivity mMainActivity;
    private LoaderManager loaderManager;

    private final ScheduledExecutorService scheduledService= Executors.newScheduledThreadPool(1);


    StockPresenter(StockFragment stockFragment) {
        super(stockFragment.getContext(),stockFragment.getLifecycle());
        mStockFragment = stockFragment;
        mStockService = new StockService();
        mStocks = new ArrayList<>();
        mStocks.addAll(mStockService.findAllStocks());
    }

    @Override
    public void start() {
        System.out.println("start-StockPresenter");
        mMainActivity = ((MainActivity) (mStockFragment.getContext()));
        mStockFragment.getContentView().setEnableRefresh(false);
        mStockFragment.getContentView().setEnableHeaderTranslationContent(false);
        mStockFragment.getContentView().setEnableLoadMore(false);

        mStockFragment.getNoDataView().setOnClickListener((View view)->{
                Intent intent = new Intent(mStockFragment.getContext(), InputStockActivity.class);
                mStockFragment.startActivity(intent);
        });

        mStockFragment.getStockView().setOnItemLongClickListener((parent, view, position, id) -> {
            if (!mStockAdapter.inEditState()) {
                mMainActivity.getTvEditAdd().setOnClickListener( v -> {
                    Intent intent = new Intent(mStockFragment.getContext(), InputStockActivity.class);
                    mStockFragment.startActivity(intent);
                });
                mMainActivity.getTvEditFinish().setOnClickListener(v -> {
                    mMainActivity.getRlCommonTitle().setVisibility(View.VISIBLE);
                    mMainActivity.getRlEditTitile().setVisibility(View.GONE);
                    mStockFragment.getStockView().setDragModel(-1);
                    mStockAdapter.setEditState(false);
                    mStockAdapter.notifyDataSetChanged();
                });
                mStockFragment.getContentView().setEnableRefresh(false);
                mStockAdapter.setEditState(true);
                mStockFragment.getStockView().setDragModel(DragSortGridView.DRAG_BY_LONG_CLICK);
                mStockAdapter.notifyDataSetChanged();
                mMainActivity.getRlCommonTitle().setVisibility(View.GONE);
                mMainActivity.getRlEditTitile().setVisibility(View.VISIBLE);
                VibratorUtil.Vibrate(mStockFragment.getActivity(),200);
            }
            return true;
        });

        mStockAdapter = new StockDragAdapter(mStockFragment.getContext(), R.layout.gridview_stock_item, mStocks, false);
        mStockFragment.getStockView().setDragModel(-1);
        mStockFragment.getStockView().setTouchClashparent(((MainActivity) (mStockFragment.getContext())).getVpContent());
        mStockFragment.getStockView().setAdapter(mStockAdapter);

        loaderManager = mStockFragment.getLoaderManager();
        loaderManager.initLoader(0, null, this);
    }

    private void refreshData() {
        mStocks.clear();
        mStocks.addAll(mStockService.findAllStocks());
        if ( mStocks.size() == 0) {
            mStockFragment.getStockView().setVisibility(View.GONE);
            mStockFragment.getNoDataView().setVisibility(View.VISIBLE);
        } else {
            mStockFragment.getNoDataView().setVisibility(View.GONE);
            mStockFragment.getStockView().setVisibility(View.VISIBLE);
        }
        System.out.println("refreshData-StockPresenter");
        mStockAdapter.notifyDataSetChanged();
    }

    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
        mStockFragment.getContentView().setPrimaryColorsId(colorPrimary, android.R.color.white);
    }

    @Override
    public void resume(){
        refreshData();
    }


    public List<Stock> getStocks(){
        return mStocks;
    }


    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        return new StockLoader(mStockFragment.getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        System.out.println("onLoadFinished-StockPresenter");
        List<Stock> refreshedStocks=(List<Stock>)data;
        for(Stock refreshStock: refreshedStocks){
            //System.out.println(refreshStock.getName()+":"+refreshStock.getPrice());
            for(Stock stock: mStocks){
                if(refreshStock.getId().equals(stock.getId())){
                    stock.setLastPrice(stock.getPrice());
                    stock.setPrice(refreshStock.getPrice());
                    mStockService.updateStock(stock);
                }
            }
        }
        if(mStockAdapter != null) {
            mStockAdapter.notifyDataSetChanged();
        }
        scheduledService.schedule(()->loader.forceLoad(),30, SECONDS);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        loaderManager.restartLoader(0,null,this);
    }

}
