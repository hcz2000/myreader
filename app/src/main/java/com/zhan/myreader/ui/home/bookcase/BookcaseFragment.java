package com.zhan.myreader.ui.home.bookcase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.zhan.myreader.custom.DragSortGridView;
import com.zhan.myreader.databinding.FragmentBookcaseBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookcaseFragment extends Fragment {

    private BookcasePresenter mBookcasePresenter;
    private FragmentBookcaseBinding binding;

    public BookcaseFragment() {
        mBookcasePresenter = new BookcasePresenter(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("onCreateView-BookcaseFragment");
        // Inflate the layout for this fragment
        binding = FragmentBookcaseBinding.inflate(inflater,container,false);
        mBookcasePresenter.enable();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        System.out.println("onDestroyView-BookcaseFragment");
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onPause() {
        System.out.println("onPause-BookcaseFragment");
        super.onPause();
    }
    @Override
    public void onResume() {
        System.out.println("onResume-BookcaseFragment");
        super.onResume();
    }
    @Override
    public void onStop() {
        System.out.println("onStop-BookcaseFragment");
        super.onStop();
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
