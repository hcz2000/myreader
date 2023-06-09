package com.zhan.myreader.ui.home.bookcase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.databinding.FragmentBookcaseBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookcaseFragment extends Fragment {

    private final BookcasePresenter mBookcasePresenter;
    private FragmentBookcaseBinding binding;

    public BookcaseFragment() {
        mBookcasePresenter = new BookcasePresenter(this);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookcaseBinding.inflate(inflater,container,false);
        mBookcasePresenter.enable();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public LinearLayout getNoDataView() {
        return binding.noDataView;
    }

    public DragSortGridView getBookView() {
        return binding.bookView;
    }

    public SmartRefreshLayout getContentView() {
        return binding.contentView;
    }

    public FragmentBookcaseBinding getBinding() {
        return binding;
    }
}
