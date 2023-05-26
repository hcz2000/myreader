package com.zhan.myreader.ui.home.bookstore;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.zhan.myreader.R;
import com.zhan.myreader.databinding.ListviewBookTypeItemBinding;
import com.zhan.myreader.entity.bookstore.Catalog;
import java.util.List;

public class BookStoreCatalogAdapter extends RecyclerView.Adapter<BookStoreCatalogAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private final List<Catalog> mDatas;
    private RecyclerView rvContent;
    private OnItemClickListener onItemClickListener;
    private int selectPos = 0;


   BookStoreCatalogAdapter(Context context, List<Catalog> datas) {
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
   }

    static class ViewHolder extends RecyclerView.ViewHolder {

       ListviewBookTypeItemBinding binding;

        ViewHolder(ListviewBookTypeItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
   }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (rvContent == null) rvContent = (RecyclerView) parent;
        ListviewBookTypeItemBinding binding = ListviewBookTypeItemBinding.inflate(mInflater,parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,final int position) {
        int adapterPosition=position;
        initView(adapterPosition, holder);
        if (adapterPosition == selectPos){
            holder.itemView.setBackgroundResource(R.color.white);
        }else{
            holder.itemView.setBackgroundResource(R.color.sys_book_type_bg);
        }

        if (onItemClickListener != null){

            holder.itemView.setOnClickListener(view -> {

                onItemClickListener.onClick(adapterPosition,view);
                selectPos = adapterPosition;
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void initView(final int position, final ViewHolder holder) {
       Catalog catalog = mDatas.get(position);
       holder.binding.tvTypeName.setText(catalog.getTypeName());
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(int pos,View view);
    }






}
