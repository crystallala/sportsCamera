package com.qikoo.sportscamera.activity;

import java.util.ArrayList;
import java.util.List;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.fragment.MainActivity;
import com.qikoo.sportscamera.view.ViewPagerAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GuideViewPagerActivity extends Activity implements
        OnClickListener, OnPageChangeListener {

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private Button button;

    // ����ͼƬ��Դ
    private static final int[] pics = { R.drawable.guide1, R.drawable.guide2,
            R.drawable.guide3, R.drawable.guide4 };

    // �ײ�С���ͼƬ
    private ImageView[] dots;

    // ��¼��ǰѡ�е�λ��
    private int currentIndex;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guide_view);
        button = (Button) findViewById(R.id.button);
        views = new ArrayList<View>();

        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // ��ʼ������ͼƬ�б�
        for (int i = 0; i < pics.length; i++) {
            ImageView iv = new ImageView(this);
            iv.setLayoutParams(mParams);
            iv.setImageResource(pics[i]);
            views.add(iv);
        }
        vp = (ViewPager) findViewById(R.id.viewpager);

        // ��ʼ��Adapter
        vpAdapter = new ViewPagerAdapter(views);
        vp.setAdapter(vpAdapter);

        // �󶨻ص�
        vp.setOnPageChangeListener(this);

        // ��ʼ���ײ�С��
        initDots();

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent();
                intent.setClass(GuideViewPagerActivity.this, MainActivity.class);
                GuideViewPagerActivity.this.startActivity(intent);
                finish();
            }
        });
    }

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);

        dots = new ImageView[pics.length];

        // ѭ��ȡ��С��ͼƬ
        for (int i = 0; i < pics.length; i++) {
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setEnabled(false);// ����Ϊ��
            dots[i].setOnClickListener(this);
            dots[i].setTag(i);// ����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
        }

        currentIndex = 0;
        dots[currentIndex].setEnabled(true);//
    }

    // ���õ�ǰ������ҳ
    private void setCurView(int position) {
        if (position < 0 || position >= pics.length) {
            return;
        }

        vp.setCurrentItem(position);
    }

    // ���õ�ǰ�ײ�С���״̬
    private void setCurDot(int positon) {
        if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
            return;
        }

        dots[positon].setEnabled(true);
        dots[currentIndex].setEnabled(false);

        currentIndex = positon;
    }

    // ������״̬�ı�ʱ����
    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub

    }

    // ��ǰҳ�汻����ʱ����
    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub

    }

    // ���µ�ҳ�汻ѡ��ʱ����
    @Override
    public void onPageSelected(int arg0) {
        // TODO Auto-generated method stub
        // ���õײ�С��ѡ��״̬
        setCurDot(arg0);
        // ����Button���ֵ�ҳ��
        if (arg0 == 3) {
            button.setVisibility(View.VISIBLE);

        } else {
            button.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int position = (Integer) v.getTag();
        setCurView(position);
        setCurDot(position);
    }

}
