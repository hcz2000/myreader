package com.zhan.myreader.ui.search;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhan.myreader.R;
import com.zhan.myreader.greendao.entity.SearchHistory;

import java.util.ArrayList;

/**
 * Created by zhan on 2017/7/26.
 */

public class SearchHistoryAdapter extends ArrayAdapter<SearchHistory> {

    private int mResourceId;

    public SearchHistoryAdapter(Context context, int resourceId, ArrayList<SearchHistory> datas){
        super(context,resourceId,datas);
        mResourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(mResourceId,null);
            viewHolder.tvContent = convertView.findViewById(R.id.tv_history_content);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position,viewHolder);
        return convertView;
    }

    private void initView(int postion,ViewHolder viewHolder){
        SearchHistory searchHistory = getItem(postion);
        viewHolder.tvContent.setText(searchHistory.getContent());

    }

    class ViewHolder{
        TextView tvContent;
   }

}
