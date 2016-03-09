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
 * ����������ײ�Ŀ��Activity�����е�Fragment���������ڴ�Activity�����ڵ�
 */

public class MainActivity extends FragmentActivity implements
        OnCheckedChangeListener {

    // ���ֽ���
    private DiscoveryFragment discovery_F;
    // �ҽ���
    private UserFragment user_F;
    // ���������ť
    private Button btn_SearchCamera;
    // ����������
    private TextView tv_top_title;
    // ���ﳵ
    private ImageView iv_cart;
    // ����
    private ImageView iv_more;
    // ��ǩѡ�ť
    private RadioGroup group;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);
        initViews();
    }

    // ��ʼ�����
    private void initViews() {
        group = (RadioGroup) findViewById(R.id.main_tab_bar);
        group.setOnCheckedChangeListener(this);
        // ��ʼ��Ĭ����ʾ�Ľ���
        tv_top_title = (TextView) findViewById(R.id.tv_top_title);
        tv_top_title.setText("����");
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
                // ��ת�����ﳵ
                Intent intent = new Intent(MainActivity.this,
                        CartActivity.class);
                startActivity(intent);
            }
        });

        iv_more = (ImageView) findViewById(R.id.iv_more);
        iv_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ��ת�����ý���
                Intent intent = new Intent(MainActivity.this,
                        SettingActivity.class);
                startActivity(intent);
            }
        });

        btn_SearchCamera = (Button) findViewById(R.id.btnSearchCamera);
        btn_SearchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ��ת�������������
                Intent intent = new Intent(MainActivity.this,
                        CameraSearchActivity.class);
                startActivity(intent);
            }
        });
    }

    /** ���Fragment **/
    public void addFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        ft.replace(R.id.main_framelayout, fragment);
        ft.commit();
    }

    /** ɾ��Fragment **/
    public void removeFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        ft.remove(fragment);
        ft.commit();
    }

    /** ��ʾFragment **/
    public void showFragment(Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager()
                .beginTransaction();
        // ����Fragment���л�����
        // ft.setCustomAnimations(R.anim.cu_push_right_in,
        // R.anim.cu_push_left_out);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        // �ж�ҳ���Ƿ��Ѿ�����������Ѿ���������ô�����ص�
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
            // ���ֽ���
            tv_top_title.setText("����");
            if (discovery_F == null) {
                discovery_F = new DiscoveryFragment();
                // �жϵ�ǰ�����Ƿ����أ�������ؾͽ��������ʾ��false��ʾ��ʾ��true��ʾ��ǰ��������
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
            // �ҽ���
            tv_top_title.setText("��");
            if (user_F == null) {
                user_F = new UserFragment();
                // �жϵ�ǰ�����Ƿ����أ�������ؾͽ��������ʾ��false��ʾ��ʾ��true��ʾ��ǰ��������
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
