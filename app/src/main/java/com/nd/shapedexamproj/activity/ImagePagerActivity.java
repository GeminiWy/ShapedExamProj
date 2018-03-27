package com.nd.shapedexamproj.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.fragment.ImageDetailFragment;
import com.nd.shapedexamproj.view.HackyViewPager;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.CommonApp;

import java.util.HashMap;

/**
 * 
 * <p>多图预览界面</p>
 * <p>Created by zll on 2014-8-26</p>
 */
public class ImagePagerActivity extends BaseFragmentActivity {

    private static final String STATE_POSITION = "STATE_POSITION";
    public static final String EXTRA_IMAGE_INDEX = "image_index";
    public static final String EXTRA_IMAGE_URLS = "image_urls";
    private HackyViewPager mPager;
    private ImagePagerAdapter mAdapter;
    private int pagerPosition;
    private TextView indicator;

    private HashMap<Integer, ImageDetailFragment> showingFragments = new HashMap<Integer, ImageDetailFragment>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);
        pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        String[] urls = getIntent().getStringArrayExtra(EXTRA_IMAGE_URLS);
        
        mPager = (HackyViewPager) findViewById(R.id.pager);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urls);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(0);
        indicator = (TextView) findViewById(R.id.indicator);
        CharSequence text = getString(R.string.viewpager_indicator, 1, mPager.getAdapter().getCount());
        indicator.setText(text);
        // 更新下标
        mPager.setOnPageChangeListener(mAdapter);
        if (savedInstanceState != null) {
            pagerPosition = savedInstanceState.getInt(STATE_POSITION);
        }
        mPager.setCurrentItem(pagerPosition);

        CommonApp.getAppHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mAdapter.startLoadImage(mPager.getCurrentItem(), true);
            }

        }, 500);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_POSITION, mPager.getCurrentItem());
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter implements OnPageChangeListener {

        public String[] fileList;

        public ImagePagerAdapter(FragmentManager fm, String[] fileList) {
            super(fm);
            this.fileList = fileList;
        }

        @Override
        public int getCount() {
            return fileList == null ? 0 : fileList.length;
        }

        @Override
        public Fragment getItem(int position) {
            String url = fileList[position];
            ImageDetailFragment fragment = ImageDetailFragment.newInstance(url);
            showingFragments.put(position, fragment);

            return fragment;
        }
        
        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }


        @Override
        public void onPageSelected(int i) {
            CharSequence text = getString(R.string.viewpager_indicator, i + 1, mPager.getAdapter().getCount());
            indicator.setText(text);

            if (i - 1 >= 0) {
                startLoadImage(i - 1, false);
            }
            if (i + 1 < fileList.length) {
                startLoadImage(i + 1, false);
            }
            startLoadImage(i, true);
        }

        private void startLoadImage(int index, boolean isBigImage) {
            ImageDetailFragment fragment = showingFragments.get(index);
            if (fragment != null) {
                fragment.startLoadImage(isBigImage);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }
}