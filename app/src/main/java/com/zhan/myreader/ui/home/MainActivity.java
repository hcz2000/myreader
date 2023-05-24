package com.zhan.myreader.ui.home;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.zhan.myreader.R;
import com.zhan.myreader.common.APPCONST;
import com.zhan.myreader.custom.CircleImageView;
import com.zhan.myreader.databinding.ActivityMainBinding;
import com.zhan.myreader.util.SystemBarTintManager;
import com.zhan.myreader.util.TextHelper;

public class MainActivity extends FragmentActivity {
    private MainPrensenter mMainPrensenter;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate-MainActivity");
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setStatusBar(R.color.sys_line);
        mMainPrensenter = new MainPrensenter(this);
        mMainPrensenter.enable();
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - APPCONST.exitTime > APPCONST.exitConfirmTime) {
            TextHelper.showText("再按一次退出");
            APPCONST.exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void onStart(){
        System.out.println("onStart-MainActivity");
        super.onStart();
    }

    @Override
    public void onRestart(){
        System.out.println("onRestart-MainActivity");
        super.onRestart();
    }

    @Override
    public void onPause(){
        System.out.println("onPause-MainActivity");
        super.onPause();
    }

    @Override
    public void onResume(){
        System.out.println("onResume-MainActivity");
        super.onResume();
    }

    @Override
    public void onStop(){
        System.out.println("onStop-MainActivity");
        super.onStop();
    }

    @Override
    public void onDestroy(){
        System.out.println("onDestroy-MainActivity");
        super.onDestroy();
    }

    public CircleImageView getCivAvatar() {
        return binding.civAvatar;
    }

    public TabLayout getTlTabMenu() {
        return binding.tlTabMenu;
    }

    public ImageView getIvSearch() {
        return binding.ivSearch;
    }

    public ViewPager getVpContent() {
        return binding.vpContent;
    }

    public RelativeLayout getRlCommonTitle() {
        return binding.rlCommonTitle;
    }

    public TextView getTvEditFinish() {
        return binding.tvEditFinish;
    }

    public TextView getTvEditAdd() {
        return binding.tvEditAdd;
    }


    public RelativeLayout getRlEditTitile() {
        return binding.rlEditTitile;
    }



    @TargetApi(19)
    protected void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    /**
     * 设置状态栏颜色
     * @param colorId
     */
    public void setStatusBar(int colorId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(colorId);//通知栏所需颜色ID
        }
    }

}
