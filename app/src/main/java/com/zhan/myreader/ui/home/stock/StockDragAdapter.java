package com.zhan.myreader.ui.home.stock;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

import com.zhan.myreader.R;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.creator.DialogCreator;
import com.zhan.myreader.custom.DragAdapter;
import com.zhan.myreader.greendao.entity.Stock;
import com.zhan.myreader.greendao.service.StockService;
import com.zhan.myreader.ui.home.BrowserActivity;

/**
 * Created by zhan on 2017/5/19.
 */

public class StockDragAdapter extends DragAdapter {
    private final int mResourceId;
    private final List<Stock> list;
    private final Context mContext;
    private boolean mEditState;
    private final StockService mStockService;


    public StockDragAdapter(Context context, int textViewResourceId, List<Stock> objects, boolean editState) {
        mContext = context;
        mResourceId = textViewResourceId;
        list = objects;
        mEditState = editState;
        mStockService = new StockService();
    }

    @Override
    public void onDataModelMove(int from, int to) {
        Stock b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSortCode(i);
        }
        mStockService.updateStocks(list);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Stock getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getSortCode();
    }

    public void remove(Stock item) {
        list.remove(item);
        notifyDataSetChanged();
        mStockService.deleteStockByID(item.getId());
    }

    public void add(Stock item) {
        list.add(item);
        notifyDataSetChanged();
        mStockService.addStock(item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResourceId, null);
            viewHolder.tvStockName = convertView.findViewById(R.id.tv_stock_name);
            viewHolder.ivDelete = convertView.findViewById(R.id.iv_delete);
            viewHolder.tvStockInfo = convertView.findViewById(R.id.tv_stock_info);
            viewHolder.tvStockCost = convertView.findViewById(R.id.tv_stock_cost);
            viewHolder.tvStockPrice = convertView.findViewById(R.id.tv_stock_price);
            viewHolder.tvStockProfit = convertView.findViewById(R.id.tv_stock_profit);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position, viewHolder);
        return convertView;
    }

    private void initView(int position, ViewHolder viewHolder) {
        final Stock stock = getItem(position);
        viewHolder.tvStockName.setText(stock.getName());
        viewHolder.tvStockInfo.setText(String.format(Locale.US,"%6d",stock.getQuantity()));
        viewHolder.tvStockCost.setText(String.format(Locale.US,"%5.2f",stock.getCost()));
        viewHolder.tvStockPrice.setText(String.format(Locale.US,"%6.2f",stock.getPrice()));
        viewHolder.tvStockProfit.setText(String.format(Locale.US,"%12.2f",(stock.getPrice()-stock.getCost())*stock.getQuantity()));
        if(stock.getPrice()>stock.getCost()){
            viewHolder.tvStockProfit.setTextColor(Color.rgb(255, 0, 0));
        }else{
            viewHolder.tvStockProfit.setTextColor(Color.rgb(0, 100, 0));
        }

        //System.out.println("StockDragAdapter-Position/Price/Lastprice:"+position+"-"+stock.getPrice()+"-"+stock.getLastPrice());
        if(stock.getPrice()==stock.getLastPrice()){
            viewHolder.tvStockPrice.setBackgroundColor(Color.rgb(0, 0, 128));
        }else if(stock.getPrice()>stock.getLastPrice()){
            viewHolder.tvStockPrice.setBackgroundColor(Color.rgb(128, 0, 0));
        }else{
            viewHolder.tvStockPrice.setBackgroundColor(Color.rgb(0, 128, 0));
        }
        viewHolder.ivDelete.setOnClickListener((View v)-> {
                DialogCreator.createCommonDialog(mContext, "删除纪录", "确定删除《" + stock.getName() + "》吗？",
                        true, (DialogInterface dialogInterface, int i) -> {
                                remove(stock);
                                dialogInterface.dismiss();
                        }, (DialogInterface dialogInterface, int i) -> dialogInterface.dismiss());
        });


        if (mEditState) {
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.tvStockName.setOnClickListener(null);
            viewHolder.tvStockPrice.setVisibility(View.GONE);
            viewHolder.tvStockProfit.setVisibility(View.GONE);
        } else {
            viewHolder.ivDelete.setVisibility(View.GONE);
            viewHolder.tvStockPrice.setVisibility(View.VISIBLE);
            viewHolder.tvStockProfit.setVisibility(View.VISIBLE);
            viewHolder.tvStockName.setOnClickListener((View v)-> {
                    Intent intent = new Intent(mContext, UpdateStockActivity.class);
                    intent.putExtra(APPCONST.STOCK, stock);
                    mContext.startActivity(intent);
            });

            viewHolder.tvStockPrice.setOnClickListener( view -> {
                String url;
                String stockCode=stock.getId();
                if (stockCode.startsWith("6")) {
                    url = "https://xueqiu.com/s/SH" + stockCode;
                } else {
                    url = "https://xueqiu.com/s/SZ" + stockCode;
                }
                //Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                Intent it=new Intent(mContext, BrowserActivity.class);
                it.putExtra("URL",url);
                mContext.startActivity(it);
            });
        }

    }

    public void setEditState(boolean mEditState) {
        this.mEditState = mEditState;
        notifyDataSetChanged();
    }

    public boolean inEditState() {
        return mEditState;
    }

    static class ViewHolder {
        TextView tvStockName;
        ImageView ivDelete;
        TextView tvStockInfo;
        TextView tvStockCost;
        TextView tvStockPrice;
        TextView tvStockProfit;
    }

}
