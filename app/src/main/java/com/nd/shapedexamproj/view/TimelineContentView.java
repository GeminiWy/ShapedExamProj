package com.nd.shapedexamproj.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.model.playground.TimelineImageInfo;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p> 动态列表内容布局</p>
 * <p>Created by zll on 2014-12-5</p>
 */
public class TimelineContentView extends LinearLayout implements View.OnClickListener{
    
    private LinkView timelineContentTV;// 识别超链接
    private ImageView mContentBigIv;
    private LinearLayout mContentImageLL;
    private ImageView[] mContentSmallImageViews;
    private Timeline mTimelineRef;
    
    /**
     * 默认图片资源文件
     */
    protected int defaultImageResourceId = R.drawable.image_loading;
    private int mBigImageMaxWidth, mBigImageMaxHeight, mBigImageDefaultHeight,contentSmallImageSize;
    
    public TimelineContentView(Context context) {
        super(context);
    }
    
    public TimelineContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mBigImageDefaultHeight = getResources().getDimensionPixelSize(R.dimen.common_timeline_item_big_image_default_height);
        mBigImageMaxWidth = getResources().getDimensionPixelSize(R.dimen.common_timeline_item_big_image_max_width);
        mBigImageMaxHeight = getResources().getDimensionPixelSize(R.dimen.common_timeline_item_big_image_max_height);
        contentSmallImageSize = getResources().getDimensionPixelSize(R.dimen.common_timeline_item_small_image_size);
        
        timelineContentTV = (LinkView) findViewById(R.id.timeline_content);
        mContentImageLL = (LinearLayout) findViewById(R.id.common_timeline_item_view_content_image_ll);
        mContentBigIv = (ImageView) findViewById(R.id.common_timeline_item_view_content_big_image_iv);//单张动态图
        mContentSmallImageViews = new ImageView[]{
                (ImageView) findViewById(R.id.common_timeline_item_view_content_image_1_iv),
                (ImageView) findViewById(R.id.common_timeline_item_view_content_image_2_iv),
                (ImageView) findViewById(R.id.common_timeline_item_view_content_image_3_iv),
                (ImageView) findViewById(R.id.common_timeline_item_view_content_image_4_iv),
                (ImageView) findViewById(R.id.common_timeline_item_view_content_image_5_iv),
        };
        
        initClickListener();
    }
    
    private void initClickListener() {
        mContentBigIv.setOnClickListener(this);
        timelineContentTV.setFocusable(false);
        for (View view : mContentSmallImageViews) {
            view.setOnClickListener(this);
        }
        
    }
    /**
     * <p>类的入口</P>
     * @param prefix 动态内容的拼接的前缀，可以为空
     * @param timeline
     */
    public void setTimeline(String prefix,Timeline timeline) {
        this.mTimelineRef = timeline;
        updateTimelineData(prefix,timeline);
    }
    
    private void updateTimelineData(String prefix,Timeline timeline) {
        if (timeline == null) {
            timelineContentTV.setText(getResources().getString(R.string.timeline_has_been_delete));
            updateImages(null);
            return;
        }
        String content = timeline.getContent();
        if (!TextUtils.isEmpty(prefix)) {//与我相关界面需要拼接用户名"原动态："或"原评论："
            content = prefix + timeline.getContent();
        }
        timelineContentTV.setText(InputBottomBar.parseFaceByText(getContext(), content, (int) timelineContentTV.getTextSize()));
        updateImages(timeline.getImages());
    }
    
    private void updateImages(List<TimelineImageInfo> imageInfos) {
        mContentBigIv.setImageResource(defaultImageResourceId);
        if (imageInfos == null || imageInfos.size() == 0) {
            showSmallImages(null);
            mContentBigIv.setVisibility(View.GONE);
        } else if (imageInfos.size() == 1) {
            showSmallImages(null);
            mContentBigIv.setVisibility(View.VISIBLE);
            try {
                TimelineImageInfo imageInfo = imageInfos.get(0);
                URL imageUrl = new URL(imageInfo.getUrlString());
                if (imageInfo.getWidth() == 0 || imageInfo.getHeight() == 0) {
                    mContentBigIv.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, mBigImageDefaultHeight));
                    ImageCacheTool.getInstance().asyncLoadImage(mContentBigIv, null, imageUrl, Integer.MAX_VALUE, mBigImageDefaultHeight);
                } else {
                    float scale = 1;
                    if (imageInfo.getWidth() > mBigImageMaxWidth || imageInfo.getHeight() > mBigImageMaxHeight) {
                        float scaleX = (float) mBigImageMaxWidth / (float) imageInfo.getWidth();
                        float scaleY = (float) mBigImageMaxHeight / (float) imageInfo.getHeight();
                        scale = Math.min(scaleX, scaleY);
                    }
                    //int width = (int) (scale * imageInfo.getWidth());
                    int height = (int) (scale * imageInfo.getHeight());
                    mContentBigIv.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, height));
                    ImageCacheTool.getInstance().asyncLoadImage(mContentBigIv, null, imageUrl, Integer.MAX_VALUE, height);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            mContentBigIv.setVisibility(View.GONE);
            showSmallImages(imageInfos);
        }
    }

    private void showSmallImages(List<TimelineImageInfo> imageInfos) {
        for (int i =  0; i < mContentSmallImageViews.length; i++) {
            if (imageInfos != null && i < imageInfos.size()) {
                mContentSmallImageViews[i].setVisibility(VISIBLE);
                mContentSmallImageViews[i].setImageResource(defaultImageResourceId);
                TimelineImageInfo imageInfo = imageInfos.get(i);
                try {
                    URL imageUrl = new URL(imageInfo.getUrlString());
                    ImageCacheTool.getInstance().asyncLoadImage(mContentSmallImageViews[i], null, imageUrl, contentSmallImageSize, contentSmallImageSize);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } else {
                mContentSmallImageViews[i].setImageBitmap(null);
                mContentSmallImageViews[i].setVisibility(GONE);
            }
        }
        if (imageInfos != null && imageInfos.size() > 3) {
            mContentImageLL.setVisibility(VISIBLE);
        } else {
            mContentImageLL.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (mTimelineRef == null) {
            return;
        }
        switch(v.getId()) {
            
            case R.id.common_timeline_item_view_content_big_image_iv:
                UIHelper.showImageBrower(getContext(), 0, new String[]{mTimelineRef.getImages().get(0).getUrlString()});
                break;
            case R.id.common_timeline_item_view_content_image_1_iv:
                showImage(0, mTimelineRef.getImages());
                break;
            case R.id.common_timeline_item_view_content_image_2_iv:
                showImage(1, mTimelineRef.getImages());
                break;
            case R.id.common_timeline_item_view_content_image_3_iv:
                showImage(2, mTimelineRef.getImages());
                break;
            case R.id.common_timeline_item_view_content_image_4_iv:
                showImage(3, mTimelineRef.getImages());
                break;
            case R.id.common_timeline_item_view_content_image_5_iv:
                showImage(4, mTimelineRef.getImages());
                break;
        
        }
            
    }
    
    private void showImage (int position, List<TimelineImageInfo> imageInfos) {
        String[] imageUrls = new String[imageInfos.size()];
        for (int i = 0; i < imageUrls.length; i++) {
            imageUrls[i] = imageInfos.get(i).getUrlString();
        }
        UIHelper.showImageBrower(getContext(), position, imageUrls);
    }
    
}
