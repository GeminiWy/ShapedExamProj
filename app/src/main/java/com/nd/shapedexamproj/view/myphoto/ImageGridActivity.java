package com.nd.shapedexamproj.view.myphoto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.activity.plaza.PlaygroundSendTrendsActivity;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.util.ActionUtil;
import com.tming.common.util.Helper;

import java.io.File;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 选择图片界面
 * @author Administrator
 *
 */
public class ImageGridActivity extends BaseActivity implements OnClickListener {

    public static final int CHOOSE_IMAGE_REQUEST_CODE = 201;
    public static final int CHOOSE_IMAGE_RESULT_CODE = 202;

	public static final String DATA_EXTRA_IMAGE_LIST = "imagelist";

	private static final String TAG = "ImageGridActivity";
	private GridView gridView;
	private ImageGridAdapter adapter;
	private TextView previewCountButTV;
	private GridView selectedImagesGv;
	private ImageGridPreviewAdapter gridPreviewAdapter;
	private List<URL> seletedImages = new ArrayList<URL>();

    @Override
    public int initResource() {
        return R.layout.activity_image_grid_stat;
    }

    @Override
    public void initComponent() {
        TextView titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
        titleTv.setText("相册");
        TextView rightBtn = (TextView) findViewById(R.id.common_head_right_btn);
        rightBtn.setText("取消");
        previewCountButTV = (TextView) findViewById(R.id.preview_count_but_tv);
        gridView = (GridView) findViewById(R.id.preview_confirm_pic_gv);
        selectedImagesGv = (GridView) findViewById(R.id.activity_image_grid_preview_gv);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> selectedImagePaths = new ArrayList<String>();
        for (URL url : seletedImages) {
            selectedImagePaths.add(url.getPath());
        }
        outState.putStringArrayList("seletedImages", selectedImagePaths);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<String> selectedImagePaths = getIntent().getStringArrayListExtra(DATA_EXTRA_IMAGE_LIST);
        seletedImages = new ArrayList<URL>();
        for (String path : selectedImagePaths) {
            try {
                seletedImages.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        gridPreviewAdapter.notifyDataSetChanged();
    }

    @Override
    public void initData() {
        // 初始化选中图片预览
        List<String> selectedImagePaths = getIntent().getStringArrayListExtra(DATA_EXTRA_IMAGE_LIST);
        for (String path : selectedImagePaths) {
            try {
                seletedImages.add(new File(path).toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        gridPreviewAdapter = new ImageGridPreviewAdapter(this, seletedImages);
        selectedImagesGv.setAdapter(gridPreviewAdapter);
        gridPreviewAdapter.notifyDataSetChanged();

        // 设置选中/最大图片数量
        previewCountButTV.setText(seletedImages.size() + "/"+ PlaygroundSendTrendsActivity.CHOOSEABLE_PIC_COUNT_TEXT);

        // 初始化图片数据
        adapter = new ImageGridAdapter(ImageGridActivity.this);
        gridView.setAdapter(adapter);
        gridView.setOnScrollListener(adapter);
        gridView.setOnItemClickListener(adapter);
        adapter.notifyDataSetChanged();
        new LoadImageDataTask().execute();
    }

    @Override
    public void addListener() {
        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        findViewById(R.id.common_head_right_btn).setOnClickListener(this);
        findViewById(R.id.activity_image_grid_preview_but_ll).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commonheader_left_iv:
                finish();
                break;
            case R.id.common_head_right_btn:
                finish();
                break;
            case R.id.activity_image_grid_preview_but_ll: {
                ArrayList<String> selectedImagePaths = new ArrayList<String>();
                for (URL url : seletedImages) {
                    selectedImagePaths.add(url.getPath());
                }
                Intent intent = new Intent();
                intent.putStringArrayListExtra(DATA_EXTRA_IMAGE_LIST, selectedImagePaths);
                setResult(CHOOSE_IMAGE_RESULT_CODE, intent);
                finish();
                break;
            }
        }
    }

    private class LoadImageDataTask extends AsyncTask<Void, Void, List<URL>> {

        List<Integer> selectedImageIndexs = new ArrayList<Integer>();

        @Override
        protected List<URL> doInBackground(Void... params) {
            List<String> datas =  AlbumHelper.getAlbumImages(ImageGridActivity.this);

            List<URL> result = new ArrayList<URL>();
            for (String path : datas) {
                try {
                    URL url = new File(path).toURI().toURL();

                    // 检查是否已经选中
                    for (URL selectedUrl : seletedImages) {
                        if (url.equals(selectedUrl)) {
                            selectedImageIndexs.add(result.size());
                        }
                    }

                    result.add(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<URL> aVoid) {
            for (Integer index : selectedImageIndexs) {
                adapter.setSelected(index);
            }
            adapter.addItemCollection(aVoid);
            adapter.notifyDataSetChanged();
            gridPreviewAdapter.notifyDataSetChanged();
        }
    }

    /**
     * <p> 图片选择适配器</p>
     * <p>Created by yusongying on 2014/10/21</p>
     */
    private class ImageGridAdapter extends ImageBaseAdatapter<URL> implements OnClickListener, AdapterView.OnItemClickListener {

        public ImageGridAdapter(Context context) {
            super(context);
            reqImageHeight = reqImageWidth = Helper.dip2px(context, 128);
            defaultImageResourceId = R.drawable.image_loading;
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final URL data) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.imageSelectIndicateCkb.setTag(data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            ViewHolder holder = (ViewHolder) view.getTag();
            if (selectedItemIndex != null) {
                if (selectedItemIndex.contains(position)) {
                    holder.imageSelectIndicateCkb.setImageResource(R.drawable.selected0513);
                } else {
                    holder.imageSelectIndicateCkb.setImageResource(R.drawable.noselect0513);
                }
            }
            return view;
        }

        @Override
        public View infateItemView(Context context) {
            View view = View.inflate(context, R.layout.select_image_item, null);
            ViewHolder holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.select_image_item_content_iv);
            holder.imageSelectIndicateCkb = (ImageView) view.findViewById(R.id.select_image_item_select_indicate_iv);
            holder.imageSelectIndicateCkb.setOnClickListener(this);
            view.setTag(holder);
            return view;
        }

        @Override
        public URL getDataImageUrl(URL data) {
            return data;
        }

        /**
         *  选择图片前的回调
         *
         * @param imageURL 图片URL
         * @param isSeleted 是否选中
         * @return 返回true，添加成功，否则不添加
         */
        public boolean onImageSelectedChange(URL imageURL, boolean isSeleted){
            if (isSeleted) {
                // 判断是否超出限制
                if (seletedImages.size() + 1 > PlaygroundSendTrendsActivity.CHOOSEABLE_PIC_COUNT) {
                    Toast.makeText(ImageGridActivity.this, "最多选择5张图片", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    previewCountButTV.setText((seletedImages.size() + 1) + "/" + PlaygroundSendTrendsActivity.CHOOSEABLE_PIC_COUNT_TEXT);
                    seletedImages.add(imageURL);
                    gridPreviewAdapter.notifyDataSetChanged();
                    return true;
                }
            } else  {
                // 移除操作
                previewCountButTV.setText((seletedImages.size() - 1) + "/" + PlaygroundSendTrendsActivity.CHOOSEABLE_PIC_COUNT_TEXT);
                seletedImages.remove(imageURL);
                gridPreviewAdapter.notifyDataSetChanged();
                return true;
            }
        }

        @Override
        public void onClick(View v) {
            URL url = (URL) v.getTag();
            boolean isSelected = false;
            int selectedSize = 0;
            if (selectedItemIndex != null) {
                isSelected = selectedItemIndex.contains(datas.indexOf(url));
                selectedSize = selectedItemIndex.size();
            }
            if (onImageSelectedChange(url, !isSelected)) {
                setSelected(datas.indexOf(url));
                notifyDataSetChanged();
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            URL data = datas.get(position);
            ActionUtil.Media.viewPictureOnAlbum(mContext, new File(data.getFile()));
        }

        private class ViewHolder extends BaseViewHolder {
            ImageView imageSelectIndicateCkb;
        }

        @Override
        public void startLoadImage() {
            Collection<URL> showNews = showNewsIV.values();
            for (URL news : showNews) {
                URL picUrl = getDataImageUrl(news);
                if (picUrl != null) {
                    String picUrlString = picUrl.toString();
                    SoftReference<Bitmap> imageRef = images.get(picUrlString);
                    if (imageRef == null || imageRef.get() == null) {
                        mImageCacheTool.asyncLoadAdjustOritationImage(picUrl, new LoadCallBack(news, picUrl.toString()), reqImageWidth, reqImageWidth);
                    }
                }
            }
        }
    }

    /**
     * 底部选中图片适配器
     */
    public class ImageGridPreviewAdapter extends ImageBaseAdatapter<URL> {

        public ImageGridPreviewAdapter(Context context, List<URL> list) {
            super(context, list);
            reqImageHeight = reqImageWidth = Helper.dip2px(context, 61);
            defaultImageResourceId = R.drawable.image_loading;
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, URL data) {
        }

        @Override
        public View infateItemView(Context context) {
            View view = View.inflate(context, R.layout.activity_image_grid_item, null);
            BaseViewHolder holder = new BaseViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.activity_image_grid_preview_iv);
            view.setTag(holder);
            return view;
        }

        @Override
        public URL getDataImageUrl(URL data) {
            return data;
        }

        @Override
        public void startLoadImage() {
            Collection<URL> showNews = showNewsIV.values();
            for (URL news : showNews) {
                URL picUrl = getDataImageUrl(news);
                if (picUrl != null) {
                    String picUrlString = picUrl.toString();
                    SoftReference<Bitmap> imageRef = images.get(picUrlString);
                    if (imageRef == null || imageRef.get() == null) {
                        mImageCacheTool.asyncLoadAdjustOritationImage(picUrl, new LoadCallBack(news, picUrl.toString()), reqImageWidth, reqImageWidth);
                    }
                }
            }
        }
    }
}
