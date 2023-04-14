package com.zhao.myreader.ui.home.stock;


import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhao.myreader.custom.DragSortListView;
import com.zhao.myreader.databinding.FragmentStockBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockFragment extends Fragment {

    private StockPresenter mStockPresenter;
    private FragmentStockBinding binding;

    public StockFragment() {
        mStockPresenter = new StockPresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStockBinding.inflate(inflater,container,false);
        mStockPresenter.enable();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mStockPresenter.resume();
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