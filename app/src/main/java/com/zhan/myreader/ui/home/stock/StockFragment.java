package com.zhan.myreader.ui.home.stock;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhan.myreader.custom.DragSortListView;
import com.zhan.myreader.databinding.FragmentStockBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockFragment extends Fragment {

    private final StockPresenter mStockPresenter;
    private FragmentStockBinding binding;

    public StockFragment() {
        System.out.println("Constructor-StockFragment");
        mStockPresenter = new StockPresenter(this);
    }

    @Override
    public void onCreate(Bundle savedInstance){
        System.out.println("onCreate-StockFragment");
        super.onCreate(savedInstance);
    }

    @Override
    public void onStart(){
        System.out.println("onStart-StockFragment");
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("onCreateView-StockFragment");
        // Inflate the layout for this fragment
        binding = FragmentStockBinding.inflate(inflater,container,false);
        mStockPresenter.enable();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        System.out.println("onDestroyView-StockFragment");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        System.out.println("onResume-StockFragment");
        super.onResume();
    }

    @Override
    public void onPause(){
        System.out.println("onPause-StockFragment");
        super.onPause();
    }

    public void onStop(){
        System.out.println("onStop-StockFragment");
        super.onStop();
    }

    public void onDestroy(){
        System.out.println("onDestroy-StockFragment");
        super.onDestroy();
    }

    public LinearLayout getNoDataView() {
        return binding.noDataView;
    }

    public DragSortListView getStockView() {
        return binding.stockView;
    }

    public SmartRefreshLayout getContentView() {
        return binding.contentView;
    }

    public FragmentStockBinding getBinding() {
        return binding;
    }

}
