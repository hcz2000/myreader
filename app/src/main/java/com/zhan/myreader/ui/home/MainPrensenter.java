package com.zhan.myreader.ui.home;

import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zhan.myreader.base.BasePresenter;
import com.zhan.myreader.ui.home.bookcase.BookcaseFragment;
import com.zhan.myreader.ui.home.bookstore.BookStoreFragment;
import com.zhan.myreader.ui.home.stock.StockFragment;
import com.zhan.myreader.ui.search.SearchBookActivity;

import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

/**
 * Created by zhan on 2017/7/25.
 */

public class MainPrensenter extends BasePresenter {

    private final MainActivity mMainActivity;
    private final ArrayList<Fragment> mFragments = new ArrayList<>();
    private final String[] tabTitle = {"金融","书架","书城"};

    public MainPrensenter(MainActivity mainActivity){
        super(mainActivity,mainActivity.getLifecycle());
        mMainActivity = mainActivity;
    }

    @Override
    public void create() {
        init();
        mMainActivity.getIvSearch().setOnClickListener(view -> {
            Intent intent = new Intent(mMainActivity, SearchBookActivity.class);
            mMainActivity.startActivity(intent);
        });

    }

    /**
     * 初始化
     */
    private void init(){
        mFragments.clear();
        mFragments.add(new StockFragment());
        mFragments.add(new BookcaseFragment());
        mFragments.add(new BookStoreFragment());

        mMainActivity.getVpContent().setAdapter(new FragmentPagerAdapter(mMainActivity.getSupportFragmentManager(),BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position >= tabTitle.length) return null;
                return tabTitle[position];
            }

        });
        mMainActivity.getTlTabMenu().setupWithViewPager( mMainActivity.getVpContent());
        mMainActivity.getVpContent().setCurrentItem(1);
    }
}
