package com.nd.shapedexamproj.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.util.Helper;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 自动加载图片的TextView
 *
 * Created by yusongying on 2015/1/25.
 */
public class AutoImageLoadTextView extends android.support.v7.widget.AppCompatTextView {

    private LoadImageTask task = null;
    private int maxWidth = 0;

    public AutoImageLoadTextView(Context context) {
        super(context);
    }

    public AutoImageLoadTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setText(String text) {
        //text = text.replace("【", "[").replace("】", "]");
        super.setText(Html.fromHtml(text, new ShowLoadingImageGetter(), null));
        if (task != null) {
            task.cancel(true);
        }
        task = new LoadImageTask();
        task.execute(text);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (task != null) {
            task.cancel(true);
        }
    }

    private class LoadImageTask extends AsyncTask<String, Void, Spanned> {

        @Override
        protected Spanned doInBackground(String... params) {
            return Html.fromHtml(params[0], new LoadImageGetter(), null);
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            setText(spanned);
        }
    }

    private class ShowLoadingImageGetter implements Html.ImageGetter {

        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = getContext().getResources().getDrawable(R.drawable.img_bg);
            drawable.setBounds(0, 0 , getLineHeight(), getLineHeight());
            return drawable;
        }
    }

    private class LoadImageGetter implements Html.ImageGetter {

        @Override
        public Drawable getDrawable(String source) {
            try {
                Bitmap bitmap = ImageCacheTool.getInstance().loadImage(new URL(source));
                Drawable drawable = null;
                int lineHeight = getLineHeight();
                if (bitmap != null) {
                    maxWidth = Helper.dip2px(getContext(), 200);
                    drawable = new BitmapDrawable(getResources(), bitmap);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (height < lineHeight) { // 小于行高
                        drawable.setBounds(0, 0, width * lineHeight / height, lineHeight);
                    } else if (height > maxWidth) { // 超出大小
                        drawable.setBounds(0, 0, maxWidth, height * maxWidth / width);
                    } else {
                        drawable.setBounds(0, 0, width, height);
                    }
                    return drawable;
                } else {
                    drawable = getContext().getResources().getDrawable(R.drawable.loaded_fail);
                    drawable.setBounds(0, 0 , lineHeight, lineHeight);
                }
                return drawable;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}
