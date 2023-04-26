package com.zhao.myreader.ui.home.stock;

import android.view.View;
import android.widget.Toast;

import com.zhao.myreader.base.BasePresenter;
import com.zhao.myreader.common.APPCONST;
import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.greendao.service.StockService;

/**
 * Created by zhan on 2017/7/26.
 */

public class UpdateStockPrensenter extends BasePresenter {

    private UpdateStockActivity mUpdateStockActivity;
    private StockService mStockService;
    private Stock mStock;

    UpdateStockPrensenter(UpdateStockActivity updateStockActivity) {
        super(updateStockActivity,updateStockActivity.getLifecycle());
        mUpdateStockActivity = updateStockActivity;
        mStockService = new StockService();
    }

    @Override
    public void create() {
        mStock = (Stock) mUpdateStockActivity.getIntent().getSerializableExtra(APPCONST.STOCK);
        mUpdateStockActivity.getTvTitleText().setText(mStock.getId()+"-"+mStock.getName());
        mUpdateStockActivity.getLlTitleBack().setOnClickListener(view -> mUpdateStockActivity.finish());
        mUpdateStockActivity.getEtStockQuantity().setText(String.valueOf(mStock.getQuantity()));
        mUpdateStockActivity.getEtStockCost().setText(String.valueOf(mStock.getCost()));
        mUpdateStockActivity.getEtStockUpperThreshold().setText(String.valueOf(mStock.getUpperThreshold()));
        mUpdateStockActivity.getEtStockLowerThreshold().setText(String.valueOf(mStock.getLowerThreshold()));
        mUpdateStockActivity.getTvInsert().setOnClickListener(view -> update());
        mUpdateStockActivity.getTvLabel1().setVisibility(View.GONE);
        mUpdateStockActivity.getEtStockCode().setVisibility(View.GONE);
        mUpdateStockActivity.getPbLoading().setVisibility(View.GONE);
        mUpdateStockActivity.getTvSuccess().setVisibility(View.GONE);
        mUpdateStockActivity.getTvInsert().setVisibility(View.VISIBLE);
    }


   /**
     * 获取搜索数据
G     */
    private void update() {
        String quantityStr= mUpdateStockActivity.getEtStockQuantity().getText().toString();
        int quantity=Integer.parseInt(quantityStr);
        String costStr= mUpdateStockActivity.getEtStockCost().getText().toString();
        double cost=Double.parseDouble(costStr);
        String upperThresholdStr= mUpdateStockActivity.getEtStockUpperThreshold().getText().toString();
        double upperThreshold=Double.parseDouble(upperThresholdStr);
        String lowerThresholdStr= mUpdateStockActivity.getEtStockLowerThreshold().getText().toString();
        double lowerThreshold=Double.parseDouble(lowerThresholdStr);
        mStock.setQuantity(quantity);
        mStock.setCost(cost);
        mStock.setUpperThreshold(upperThreshold);
        mStock.setLowerThreshold(lowerThreshold);
        mStockService.updateStock(mStock);
        Toast.makeText(mUpdateStockActivity.getApplicationContext(), "更新成功",Toast.LENGTH_LONG).show();
    }
}

