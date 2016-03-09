package com.qikoo.sportscamera.view;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.qikoo.sportscamera.R;
import com.qikoo.sportscamera.util.IconPagerAdapter;

public class FragAdapter extends FragmentPagerAdapter implements IconPagerAdapter{

    protected static final int[] ICONS = new int[] {
        R.drawable.dot,
        R.drawable.dot,
};
    
    private List<Fragment> mFragments;

    public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        mFragments=fragments;
    }

    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return mFragments.get(arg0);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mFragments.size();
    }

    @Override
    public int getIconResId(int index) {
        // TODO Auto-generated method stub
        return ICONS[index % ICONS.length];
    }

}
