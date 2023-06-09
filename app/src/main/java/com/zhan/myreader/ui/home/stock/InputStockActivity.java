package com.zhan.myreader.ui.home.stock;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zhan.myreader.R;
import com.zhan.myreader.base.BaseActivity;
import com.zhan.myreader.databinding.ActivityInputStockBinding;

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

    public LinearLayout getLlStock() { return binding.llStock;}

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

    public EditText getEtStockLowerThreshold() { return binding.etStockLowerThreshold;}

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
