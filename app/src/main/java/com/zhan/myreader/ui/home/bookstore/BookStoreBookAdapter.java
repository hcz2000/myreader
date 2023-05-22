package com.zhan.myreader.ui.home.bookstore;

import android.content.Context;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zhan.myreader.R;

import com.zhan.myreader.callback.ResultCallback;
import com.zhan.myreader.databinding.ListviewBookStoreBookItemBinding;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.util.StringHelper;
import com.zhan.myreader.webapi.BookStoreApi;


import java.util.List;



public class BookStoreBookAdapter extends RecyclerView.Adapter<BookStoreBookAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final List<Book> mDatas;
    private final Context mContext;
    private RecyclerView rvContent;
    private OnItemClickListener onItemClickListener;
    private boolean isScrolling;



   public BookStoreBookAdapter(Context context,  List<Book> datas) {
        //System.out.println("Constructor-BookStoreBookAdapter");
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
        mContext = context;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ListviewBookStoreBookItemBinding binding;

        ViewHolder(ListviewBookStoreBookItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //System.out.println("onCreateViewHolder-BookStoreBookAdapter");
        if (rvContent == null) rvContent = (RecyclerView) parent;
        ListviewBookStoreBookItemBinding binding = ListviewBookStoreBookItemBinding.inflate(mInflater,parent,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //System.out.println("onBindViewHolder-BookStoreBookAdapter");
        initView(position, holder);
        if (onItemClickListener != null){
            holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(position,view));
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private void initView(final int position, final ViewHolder holder) {
       Book book = mDatas.get(position);
       holder.binding.tvBookName.setText(book.getName());
       holder.binding.tvBookAuthor.setText(book.getAuthor());
       holder.binding.tvBookDesc.setText("");
       holder.binding.tvBookName.setTag(position);//设置列表书的当前加载位置
       //System.out.println("initView-BookStoreBookAdapter:"+book.getName());
       initImgAndDec(position,holder);
    }

    private void initImgAndDec(final int position, final ViewHolder holder){
        Book book = mDatas.get(position);

        if (holder.binding.ivBookImg.getTag() != null && (int)holder.binding.ivBookImg.getTag() != position){
            Glide.with(mContext).clear(holder.binding.ivBookImg);
        }

        //图片
        Glide.with(mContext)
                .load(book.getImgUrl())
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                .into(holder.binding.ivBookImg);

        holder.binding.ivBookImg.setTag(position);
        //简介
        holder.binding.tvBookDesc.setText(book.getDesc());
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener{
        void onClick(int pos,View view);
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }
}
