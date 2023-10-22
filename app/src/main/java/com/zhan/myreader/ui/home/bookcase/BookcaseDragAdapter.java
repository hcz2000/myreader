package com.zhan.myreader.ui.home.bookcase;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.zhan.myreader.R;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.common.URLCONST;
import com.zhan.myreader.creator.DialogCreator;
import com.zhan.myreader.custom.DragAdapter;
import com.zhan.myreader.entity.BookCase;
import com.zhan.myreader.greendao.entity.Book;
import com.zhan.myreader.ui.home.reader.ReadActivity;
import com.zhan.myreader.util.StringHelper;
import java.util.List;


/**
 * Created by zhan on 2017/5/19.
 */

public class BookcaseDragAdapter extends DragAdapter {
    private int mResourceId;
    private Context mContext;
    private boolean mEditState;
    private BookCase mBookCase;

    public BookcaseDragAdapter(Context context, int textViewResourceId, BookCase bookcase, boolean editState) {
        mContext = context;
        mResourceId = textViewResourceId;
        mEditState = editState;
        mBookCase = bookcase;
    }

    @Override
    public void onDataModelMove(int from, int to) {
        List<Book> list=mBookCase.getBooks();
        Book b = list.remove(from);
        list.add(to, b);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSortCode(i);
        }
        mBookCase.update(list);
    }

    @Override
    public int getCount() {
        return mBookCase.getCount();
    }

    @Override
    public Book getItem(int position) {
        return mBookCase.getBook(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getSortCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(mResourceId, null);
            viewHolder.ivBookImg =  convertView.findViewById(R.id.iv_book_img);
            viewHolder.tvBookName = convertView.findViewById(R.id.tv_book_name);
            viewHolder.tvNoReadNum =  convertView.findViewById(R.id.tv_no_read_num);
            viewHolder.ivDelete = convertView.findViewById(R.id.iv_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        initView(position, viewHolder);
        return convertView;
    }

    private void initView(int position, ViewHolder viewHolder) {
        final Book book = getItem(position);
        if (StringHelper.isEmpty(book.getImgUrl())) {
            book.setImgUrl("");
        }

        Glide.with(mContext)
                .load(URLCONST.nameSpace_tianlai+book.getImgUrl())
                .error(R.mipmap.no_image)
                .placeholder(R.mipmap.no_image)
                .into(viewHolder.ivBookImg);

        viewHolder.tvBookName.setText(book.getName());
        viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCreator.createCommonDialog(mContext, "删除书籍", "确定删除《" + book.getName() + "》及其所有缓存吗？",
                        true, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mBookCase.remove(book);
                                notifyDataSetChanged();
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
            viewHolder.tvNoReadNum.setVisibility(View.GONE);
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.ivBookImg.setOnClickListener(null);
        } else {
            viewHolder.ivDelete.setVisibility(View.GONE);
            if (book.getUnReadNum() != 0) {
                viewHolder.tvNoReadNum.setVisibility(View.VISIBLE);
                if (book.getUnReadNum() > 99) {
                    viewHolder.tvNoReadNum.setText("+");
                } else {
                    viewHolder.tvNoReadNum.setText(String.valueOf(book.getUnReadNum()));
                }
            } else {
                viewHolder.tvNoReadNum.setVisibility(View.GONE);
            }
            viewHolder.ivBookImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent( mContext, ReadActivity.class);
                    intent.putExtra(APPCONST.BOOK, book);
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
        ImageView ivBookImg;
        TextView tvBookName;
        TextView tvNoReadNum;
        ImageView ivDelete;
    }

}
