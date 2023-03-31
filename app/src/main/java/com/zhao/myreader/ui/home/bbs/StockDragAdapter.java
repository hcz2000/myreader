package com.zhao.myreader.ui.home.bbs;

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
import com.bumptech.glide.Glide;
import com.zhao.myreader.R;
import com.zhao.myreader.common.APPCONST;
import com.zhao.myreader.creator.DialogCreator;
import com.zhao.myreader.custom.DragAdapter;
import com.zhao.myreader.greendao.entity.Stock;
import com.zhao.myreader.greendao.service.StockService;
import com.zhao.myreader.ui.stock.UpdateStockActivity;

/**
 * Created by zhao on 2017/5/19.
 */

public class StockDragAdapter extends DragAdapter {
    private int mResourceId;
    private List<Stock> list;
    private Context mContext;
    private boolean mEditState;
    private StockService mStockService;


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
        viewHolder.tvStockInfo.setText(String.format("%6d",stock.getQuantity()));
        viewHolder.tvStockCost.setText(String.format("%5.2f",stock.getCost()));
        viewHolder.tvStockPrice.setText(String.format("%6.2f",stock.getPrice()));
        viewHolder.tvStockProfit.setText(String.format("%12.2f",(stock.getPrice()-stock.getCost())*stock.getQuantity()));
        if(stock.getPrice()>stock.getCost()){
            viewHolder.tvStockPrice.setTextColor(Color.rgb(255, 0, 0));
            viewHolder.tvStockProfit.setTextColor(Color.rgb(255, 0, 0));
        }else{
            viewHolder.tvStockPrice.setTextColor(Color.rgb(0, 100, 0));
            viewHolder.tvStockProfit.setTextColor(Color.rgb(0, 100, 0));
        }
        viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCreator.createCommonDialog(mContext, "删除纪录", "确定删除《" + stock.getName() + "》吗？",
                        true, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                remove(stock);
                                dialogInterface.dismiss();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
            }
        });


        if (mEditState) {
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.tvStockName.setOnClickListener(null);
        } else {
            viewHolder.ivDelete.setVisibility(View.GONE);
            viewHolder.tvStockName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, UpdateStockActivity.class);
                    intent.putExtra(APPCONST.STOCK, stock);
                    mContext.startActivity(intent);
                }
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

    class ViewHolder {
        TextView tvStockName;
        ImageView ivDelete;
        TextView tvStockInfo;
        TextView tvStockCost;
        TextView tvStockPrice;
        TextView tvStockProfit;
    }

}
