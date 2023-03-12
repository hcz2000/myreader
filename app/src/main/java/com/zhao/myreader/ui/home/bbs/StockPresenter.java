package com.zhao.myreader.ui.home.bbs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.zhao.myreader.R;
import com.zhao.myreader.base.BasePresenter;
import com.zhao.myreader.custom.DragSortGridView;
import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.greendao.service.StockService;
import com.zhao.myreader.ui.home.MainActivity;
import com.zhao.myreader.ui.stock.InputStockActivity;
import com.zhao.myreader.util.VibratorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhao on 2017/7/25.
 */

public class StockPresenter extends BasePresenter implements LoaderManager.LoaderCallbacks {

    private StockFragment mStockFragment;
    private ArrayList<Stock> mStocks = new ArrayList<>();
    private StockDragAdapter mStockAdapter;
    private StockService mStockService;
    private MainActivity mMainActivity;
    private LoaderManager loaderManager;


    StockPresenter(StockFragment stockFragment) {
        super(stockFragment.getContext(),stockFragment.getLifecycle());
        mStockFragment = stockFragment;
        mStockService = new StockService();
    }

    @Override
    public void start() {
        mMainActivity = ((MainActivity) (mStockFragment.getContext()));
        mStockFragment.getContentView().setEnableRefresh(false);
        mStockFragment.getContentView().setEnableHeaderTranslationContent(false);
        mStockFragment.getContentView().setEnableLoadMore(false);

        mStockFragment.getNoDataView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mStockFragment.getContext(), InputStockActivity.class);
                mStockFragment.startActivity(intent);
            }
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

        loaderManager = mStockFragment.getLoaderManager();
        //System.out.println("StockPresenter Started");
    }

    private void init() {
        mStocks.clear();
        mStocks.addAll(mStockService.findAllStock());
        if (mStocks == null || mStocks.size() == 0) {
            mStockFragment.getStockView().setVisibility(View.GONE);
            mStockFragment.getNoDataView().setVisibility(View.VISIBLE);
        } else {
            if(mStockAdapter == null) {
                mStockAdapter = new StockDragAdapter(mStockFragment.getContext(), R.layout.gridview_stock_item, mStocks, false);
                mStockFragment.getStockView().setDragModel(-1);
                mStockFragment.getStockView().setTouchClashparent(((MainActivity) (mStockFragment.getContext())).getVpContent());
                mStockFragment.getStockView().setAdapter(mStockAdapter);
            }else {
                mStockAdapter.notifyDataSetChanged();
            }
            mStockFragment.getNoDataView().setVisibility(View.GONE);
            mStockFragment.getStockView().setVisibility(View.VISIBLE);
            loaderManager.initLoader(0, null, this);
        }
    }


    private void setThemeColor(int colorPrimary, int colorPrimaryDark) {
        mStockFragment.getContentView().setPrimaryColorsId(colorPrimary, android.R.color.white);
        if (Build.VERSION.SDK_INT >= 21) {
            mStockFragment.getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(mStockFragment.getContext(), colorPrimaryDark));
        }
    }

    @Override
    public void resume(){
        init();
    }

    public ArrayList<Stock> getStocks(){
        return mStocks;
    }


    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        return new StockLoader(mStockFragment.getContext(),mStocks);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        List<Stock> stocks=(List<Stock>)data;
        for(Stock stock: stocks){
            System.out.println(stock.getName()+":"+stock.getPrice());
        }
        if(mStockAdapter != null) {
            mStockAdapter.notifyDataSetChanged();
        }
        //loaderManager.initLoader(0,null,this);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        loaderManager.restartLoader(0,null,this);;
    }
}
