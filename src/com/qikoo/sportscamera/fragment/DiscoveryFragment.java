package com.qikoo.sportscamera.fragment;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.view.AbOnItemClickListener;
import com.qikoo.sportscamera.view.AbSlidingPlayView;

public class DiscoveryFragment extends Fragment {    
    
    //首页轮播
    private AbSlidingPlayView viewPager;     
    
    /**存储首页轮播的界面*/
    private ArrayList<View> allListView;
    /**首页轮播的界面的资源*/
    private int[] resId = { R.drawable.menu_viewpager_0, R.drawable.menu_viewpager_1, R.drawable.menu_viewpager_2, R.drawable.menu_viewpager_3, R.drawable.menu_viewpager_4, R.drawable.menu_viewpager_5 };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.discovery_f, null);
        initView(view);
        return view;
    }
    
    private void initView(View view) {
                        
        viewPager = (AbSlidingPlayView) view.findViewById(R.id.viewPager_menu);
        //设置播放方式为顺序播放
        viewPager.setPlayType(1);
        //设置播放间隔时间
        viewPager.setSleepTime(3000);
        
        initViewPager();
    }
        
        private void initViewPager() {

            if (allListView != null) {
                allListView.clear();
                allListView = null;
            }
            allListView = new ArrayList<View>();

            for (int i = 0; i < resId.length; i++) {
                //导入ViewPager的布局
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.pic_item, null);
                ImageView imageView = (ImageView) view.findViewById(R.id.pic_item);
                imageView.setImageResource(resId[i]);
                allListView.add(view);
            }            
            
            viewPager.addViews(allListView);
            //开始轮播
            viewPager.startPlay();
            viewPager.setOnItemClickListener(new AbOnItemClickListener() {
                @Override
                public void onClick(int position) {
                    //跳转到详情界面
                    //Intent intent = new Intent(getActivity(), DetailActivity.class);
                    //startActivity(intent);
                }
            });
        }
    
}
