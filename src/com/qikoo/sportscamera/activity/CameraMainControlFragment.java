package com.qikoo.sportscamera.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.qikoo.sportscamera.R;

public class CameraMainControlFragment extends Fragment implements OnCheckedChangeListener{


    private View view;
    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton1, mRadioButton2;
    private HorizontalScrollView mHorizontalScrollView;// 上面的水平滚动控件
    private ImageView mImageView;
    private float mCurrentCheckedRadioLeft;
    private List<Fragment> mFragmentList;
    private MyFragmentAdapter adapter;
    


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        view = inflater.inflate(R.layout.camera_main_control, null);
        initView();
        initListener();
        return view;
    }

    public void initView() {
        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(new FragmentCameraFhoto());
        mFragmentList.add(new FragmentCameraVideo());
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mRadioGroup = (RadioGroup) view.findViewById(R.id.radio);
        mRadioButton1 = (RadioButton) view.findViewById(R.id.btn1);
        mRadioButton2 = (RadioButton) view.findViewById(R.id.btn2);
        mImageView = (ImageView) view.findViewById(R.id.img_bottom_line);
        mHorizontalScrollView = (HorizontalScrollView) view
                .findViewById(R.id.horizontalScrollView);
        adapter = new MyFragmentAdapter(getChildFragmentManager(),
                mFragmentList);
        mViewPager.setAdapter(adapter);
        mRadioButton1.setChecked(true);
        mCurrentCheckedRadioLeft = getCurrentCheckedRadioLeft();
    }

    private void initListener() {
        // TODO Auto-generated method stub
        mRadioGroup.setOnCheckedChangeListener(this);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // TODO Auto-generated method stub
                ((RadioButton) mRadioGroup.getChildAt(position))
                        .setChecked(true);

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.RadioGroup.OnCheckedChangeListener#onCheckedChanged(android
     * .widget.RadioGroup, int)
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        switch (checkedId) {
        case R.id.btn1:

            imageTranslateAnimation(getResources().getDimension(R.dimen.rdo1));
            mViewPager.setCurrentItem(0);
            break;
        case R.id.btn2:

            imageTranslateAnimation(getResources().getDimension(R.dimen.rdo2));
            mViewPager.setCurrentItem(1);
            break;

        default:
            break;
        }

        mCurrentCheckedRadioLeft = getCurrentCheckedRadioLeft();
        mHorizontalScrollView.smoothScrollTo((int) mCurrentCheckedRadioLeft
                - (int) getResources().getDimension(R.dimen.rdo2), 0);
    }

    public void imageTranslateAnimation(float f) {
        AnimationSet animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(
                mCurrentCheckedRadioLeft, f, 0f, 0f);

        animationSet.addAnimation(translateAnimation);
        animationSet.setFillBefore(false);
        animationSet.setFillAfter(true);
        animationSet.setDuration(100);

        // mImageView.bringToFront();
        mImageView.startAnimation(animationSet);
    }

    private float getCurrentCheckedRadioLeft() {
        // TODO Auto-generated method stub
        if (mRadioButton1.isChecked()) {
            // Log.i("zj",
            // "currentCheckedRadioLeft="+getResources().getDimension(R.dimen.rdo1));
            return getResources().getDimension(R.dimen.rdo1);
        } else if (mRadioButton2.isChecked()) {
            // Log.i("zj",
            // "currentCheckedRadioLeft="+getResources().getDimension(R.dimen.rdo2));
            return getResources().getDimension(R.dimen.rdo2);
        } 
        return 0f;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 当fragment销毁时通过反射控制mChildFragmentManager也重置
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


}
