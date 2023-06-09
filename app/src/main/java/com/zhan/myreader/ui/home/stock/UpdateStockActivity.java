package com.zhan.myreader.ui.home.stock;


import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhan.myreader.R;
import com.zhan.myreader.base.BaseActivity;
import com.zhan.myreader.databinding.ActivityInputStockBinding;


public class UpdateStockActivity extends BaseActivity {

    private UpdateStockPrensenter mUpdateStockPrensenter;
    private ActivityInputStockBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInputStockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(R.color.sys_line);
        mUpdateStockPrensenter = new UpdateStockPrensenter(this);
        mUpdateStockPrensenter.enable();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public LinearLayout getLlTitleBack() {
        return binding.title.llTitleBack;
    }

    public TextView getTvTitleText() {
        return binding.title.tvTitleText;
    }

    public TextView getTvLabel1() { return binding.label1;}

    public EditText getEtStockCode() {
        return binding.etStockCode;
    }

    public EditText getEtStockQuantity() {
        return binding.etStockQuantity;
    }

    public EditText getEtStockCost() {
        return binding.etStockCost;
    }

    public EditText getEtStockUpperThreshold() { return binding.etStockUpperThreshold; }

    public EditText getEtStockLowerThreshold() { return binding.etStockLowerThreshold; }

    public TextView getTvSuccess() {
        return binding.tvSuccess;
    }

    public TextView getTvInsert() {
        return binding.tvInsert;
    }

    public ProgressBar getPbLoading() {
        return binding.pbLoading;
    }

    public ActivityInputStockBinding getBinding() {
        return binding;
    }
}
