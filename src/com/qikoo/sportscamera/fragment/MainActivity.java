package com.qikoo.sportscamera.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.activity.CameraConnectionActivity;
import com.qikoo.sportscamera.activity.CameraSearchActivity;
import com.qikoo.sportscamera.activity.CartActivity;
import com.qikoo.sportscamera.activity.SettingActivity;

/**
 * 整个程序最底层的框架Activity，所有的Fragment都是依赖于此Activity而存在的
 */

public class MainActivity extends FragmentActivity implements
        OnCheckedChangeListener {

    // 发现界面
    private DiscoveryFragment discovery_F;
    // 我界面
    private UserFragment user_F;
    // 连接相机按钮
    private Button btn_SearchCamera;
    // 顶部标题栏
    private TextView tv_top_title;
    // 购物车
    private ImageView iv_cart;
    // 更多
    private ImageView iv_more;
    // 标签选项按钮
    private RadioGroup group;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);
        initViews();
    }

    // 初始化组件
    private void initViews() {
        group = (RadioGroup) findViewById(R.id.main_tab_bar);
        group.setOnCheckedChangeListener(this);
        // 初始化默认显示的界面
        tv_top_title = (TextView) findViewById(R.id.tv_top_title);
        tv_top_title.setText("发现");
        if (discovery_F == null) {
            discovery_F = new DiscoveryFragment();
            addFragment(discovery_F);
            showFragment(discovery_F);
        } else {
            showFragment(discovery_F);
        }

        iv_cart = (ImageView) findViewById(R.id.iv_cart);
        iv_cart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 跳转到购物车
                Intent intent = new Intent(MainActivity.this,
                        CartActivity.class);
                startActivity(intent);
            }
        });

        iv_more = (ImageView) findViewById(R.id.iv_more);
        iv_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 跳转到设置界面
                Intent intent = new Intent(MainActivity.this,
                        SettingActivity.class);
                startActivity(intent);
            }
        });

        btn_SearchCamera = (Button) findViewById(R.id.btnSearchCamera);
        btn_SearchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 跳转到搜索相机界面
                Intent intent = new Intent(MainActivity.this,
                        CameraSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    /** 添加Fragment **/
    public void addFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.main_framelayout, fragment);
        ft.commit();
    }

    /** 删除Fragment **/
    public void removeFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    /** 显示Fragment **/
    public void showFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        // 设置Fragment的切换动画
        // ft.setCustomAnimations(R.anim.cu_push_right_in,
        // R.anim.cu_push_left_out);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        // 判断页面是否已经创建，如果已经创建，那么就隐藏掉
        if (discovery_F != null) {
            ft.hide(discovery_F);
        }
        if (user_F != null) {
            ft.hide(user_F);
        }

        ft.show(fragment);
        ft.commitAllowingStateLoss();

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        int childCount = group.getChildCount();
        int checkedIndex = 0;
        RadioButton btnButton = null;
        for (int i = 0; i < childCount; i++) {
            btnButton = (RadioButton) group.getChildAt(i);
            if (btnButton.isChecked()) {
                checkedIndex = i;
                break;
            }
        }

        switch (checkedIndex) {
        case 0:
            // 发现界面
            tv_top_title.setText("发现");
            if (discovery_F == null) {
                discovery_F = new DiscoveryFragment();
                // 判断当前界面是否隐藏，如果隐藏就进行添加显示，false表示显示，true表示当前界面隐藏
                if (!discovery_F.isHidden()) {
                    addFragment(discovery_F);
                    showFragment(discovery_F);
                }

            } else {
                if (discovery_F.isHidden()) {
                    removeFragment(discovery_F);
                    addFragment(discovery_F);
                    showFragment(discovery_F);
                }
            }
            break;
        case 1:
            // 我界面
            tv_top_title.setText("我");
            if (user_F == null) {
                user_F = new UserFragment();
                // 判断当前界面是否隐藏，如果隐藏就进行添加显示，false表示显示，true表示当前界面隐藏
                if (!user_F.isHidden()) {
                    addFragment(user_F);
                    showFragment(user_F);
                }
            } else {
                if (user_F.isHidden()) {
                    removeFragment(user_F);
                    addFragment(user_F);
                    showFragment(user_F);
                }
            }
            break;

        default:
            break;
        }

    }
}
