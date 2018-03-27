package com.nd.shapedexamproj.view.myphoto;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.tming.common.adapter.ImagePagerAdapter;
import com.tming.common.cache.image.ImageCacheTool;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * 图片预览 删除操作
 * @author Administrator
 *
 */
public class PhotoActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 301;
    public static final int RESULT_CODE = 302;
    public static final String EXTRA_DATA = "data";

	private ViewPager pager;
	private MyPageAdapter adapter;

	public ArrayList<String> selectedImagePath = new ArrayList<String>();

    @Override
    public int initResource() {
        return R.layout.activity_photo;
    }

    @Override
    public void initComponent() {
        RelativeLayout photo_relativeLayout = (RelativeLayout) findViewById(R.id.photo_relativeLayout);
        photo_relativeLayout.setBackgroundColor(0x70000000);
        pager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    public void initData() {
        selectedImagePath = getIntent().getStringArrayListExtra(EXTRA_DATA);

        adapter = new MyPageAdapter(this, pager);
        pager.setAdapter(adapter);// 设置适配器
        pager.setOnPageChangeListener(adapter);

        Intent intent = getIntent();
        int id = intent.getIntExtra("ID", 0);
        pager.setCurrentItem(id);
    }

    @Override
    public void addListener() {
        findViewById(R.id.photo_bt_exit).setOnClickListener(this);
        findViewById(R.id.photo_bt_del).setOnClickListener(this);
        findViewById(R.id.photo_bt_enter).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo_bt_exit:
                finish();
                break;
            case R.id.photo_bt_del:
                selectedImagePath.remove(pager.getCurrentItem());
                adapter.notifyDataSetChanged();
                if (selectedImagePath.size() == 0) {
                    Intent intent = new Intent();
                    intent.putStringArrayListExtra(EXTRA_DATA, selectedImagePath);
                    setResult(RESULT_CODE, intent);
                    finish();
                }
                break;
            case R.id.photo_bt_enter:
                Intent intent = new Intent();
                intent.putStringArrayListExtra(EXTRA_DATA, selectedImagePath);
                setResult(RESULT_CODE, intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pager.removeAllViews();
    }

    private class MyPageAdapter extends ImagePagerAdapter {

        public MyPageAdapter(Activity activity, ViewPager pager) {
            super(activity, pager);
        }

        @Override
        public URL getItemImageURL(int index) {
            try {
                return new File(selectedImagePath.get(index)).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public int getCount() {
            return selectedImagePath.size();
        }

        @Override
        protected void loadImage(int index, boolean isLoadBigImage) {
            View view = showingViewHashMap.get(index);
            if (view == null) {
                return;
            }
            ImageView imageView = (ImageView) view.findViewById(showImageViewId);
            if (imageView != null) {
                URL url = getItemImageURL(index);
                if (isLoadBigImage) {
                    ImageCacheTool.getInstance().asyncLoadAdjustOritationBigImage(url, imageView, failLoadImageResourceId, windowSize[0], windowSize[1]);
                } else {
                    imageView.setImageResource(loadingImageResourceId);
                    ImageCacheTool.getInstance().asyncLoadAdjustOritationImage(url, imageView, failLoadImageResourceId, 50, 50);
                }
            } else {
                throw new RuntimeException("请设置图片显示控件ID，imageShowId");
            }
        }
    }
}
