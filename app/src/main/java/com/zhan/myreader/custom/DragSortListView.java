package com.zhan.myreader.custom;

import android.content.Context;
import android.util.AttributeSet;

public class DragSortListView extends DragSortGridView {
    public DragSortListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setNumColumns(1);
    }

    public DragSortListView(Context context) {
        super(context);
        setNumColumns(1);
    }
}
