package com.zhao.myreader.ui.stock;


import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhao.myreader.R;
import com.zhao.myreader.base.BaseActivity;
import com.zhao.myreader.databinding.ActivityInputStockBinding;


public class InputStockActivity extends BaseActivity {

    private InputStockPrensenter mInputStockPrensenter;
    private ActivityInputStockBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInputStockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(R.color.sys_line);
        mInputStockPrensenter = new InputStockPrensenter(this);
        mInputStockPrensenter.enable();
    }

    @Override
    public void onBackPressed() {
       if (!mInputStockPrensenter.onBackPressed()){
           super.onBackPressed();
       }
    }

    public LinearLayout getLlTitleBack() {
        return binding.title.llTitleBack;
    }

    public TextView getTvTitleText() {
        return binding.title.tvTitleText;
    }

    public EditText getEtStockCode() {
        return binding.etStockCode;
    }

    public EditText getEtStockQuantity() {
        return binding.etStockQuantity;
    }

    public EditText getEtStockCost() {
        return binding.etStockCost;
    }

    public EditText getEtStockUpperThreshold() { return binding.etStockUpperThredshold; }

    public EditText getEtStockLowerThreshold() { return binding.etStockLowerThredshold; }

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
