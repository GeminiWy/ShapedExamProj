package com.tming.common.adapter;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.tming.common.CommonApp;
import com.tming.common.R;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.image.ImageCacheTool.ImageLoadCallBack;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/**
 * <p>图片列表适配器，继承于CommonBaseAdapter, 适用于带单张图片的ListView、GridView和ViewPage等。</p>
 * <ul>
 *     <li>该类实现了OnScrollListener接口，可以设置视图的setOnScrollListener为该类，实现滚动停止图片自动加载</li>
 *     <li>如果无法设置setOnScrollListener，需要主动调用startLoadImage进行加载图片</li>
 * </ul>
 * <P>Created by yusongying on 2014/7/14</P>
 * @param <T> 数据类型
 */
public abstract class ImageBaseAdatapter<T> extends BaseAdapter implements OnScrollListener {

    /**
     * 列表页数据
     */
    protected List<T> datas = new ArrayList<T>();

    /**
     * 图片显示控件和数据的对应结构
     */
    protected HashMap<ImageView, T> showNewsIV = new HashMap<ImageView, T>();

    /**
     * 图片加载工具
     */
    protected ImageCacheTool mImageCacheTool = null;

    /**
     * 图片软引用
     */
    protected HashMap<String, SoftReference<Bitmap>> images = new HashMap<String, SoftReference<Bitmap>>();

    /**
     * 选中项的索引
     */
    protected List<Integer> selectedItemIndex;

    /**
     * Android 上下文信息
     */
    protected Context mContext;

    /**
     * 默认图片资源文件
     */
    protected int defaultImageResourceId = R.drawable.all_use_icon_photo;

    /**
     * 指定加载图片的宽高
     */
    protected int reqImageWidth = -1, reqImageHeight = -1;

    /**
     * 构造方法
     *
     * @param context Android 上下文信息
     * @see {@link #ImageBaseAdatapter(Context, List)}
     */
    public ImageBaseAdatapter(Context context) {
        mContext = context;
        mImageCacheTool = ImageCacheTool.getInstance();
    }

    /**
     * 构造方法
     *
     * @param context Android 上下文信息
     * @param datas   数据列表
     * @see {@link #ImageBaseAdatapter(Context)}
     */
    public ImageBaseAdatapter(Context context, List<T> datas) {
        mContext = context;
        this.datas = datas;
        mImageCacheTool = ImageCacheTool.getInstance();
    }

    /**
     * 构造方法
     *
     * @param context        Android 上下文
     * @param reqImageWidth  指定加载图片的宽度
     * @param reqImageHeight 指定加载图片的高度
     */
    public ImageBaseAdatapter(Context context, int reqImageWidth, int reqImageHeight) {
        mContext = context;
        mImageCacheTool = ImageCacheTool.getInstance();
        this.reqImageWidth = reqImageWidth;
        this.reqImageHeight = reqImageHeight;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItemId(datas.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = infateItemView(mContext);
        }
        BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
        T data = datas.get(position);
        URL imageURL = getDataImageUrl(data);
        if (imageURL != null) {
            SoftReference<Bitmap> softReference = images.get(imageURL.toString());
            Bitmap bitmap = null;
            if (softReference != null && (bitmap = softReference.get()) != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(defaultImageResourceId);
            }
        }
        showNewsIV.put(holder.imageView, data);
        setViewHolderData(holder, data);
        return convertView;
    }

    /**
     * <p/>
     * 将数据显示到控件上
     * </P>
     *
     * @param holder 控件Holder
     * @param data   列表数据
     */
    public abstract void setViewHolderData(BaseViewHolder holder, T data);

    /**
     * <p/>
     * 创建一个新列表显示视图
     * </P>
     *
     * @param context Android上下文
     * @return 显示视图
     */
    public abstract View infateItemView(Context context);

    /**
     * <p/>
     * 从数据中取出图片地址
     * </P>
     *
     * @param data 列表数据中的一项
     * @return 图片地址URL对象
     */
    public abstract URL getDataImageUrl(T data);

    /**
     * @deprecated
     * <p/>
     * 获取数据的标识ID
     * </P>
     *
     * @param data 列表数据中的一项
     * @return 标识ID
     */
    public long getItemId(T data) {
        return data.hashCode();
    }

    /**
     * @deprecated
     * <p>获取数据的标识ID</P>
     *
     * @param data 数据
     * @return 标识ID
     */
    public Object getItemObjectId(T data) {
        return data;
    }

    /**
     * @deprecated
     * <p>判断使用哪个方法得到数据的唯一标识ID</P>
     *
     * @param data 数据
     * @return 标识
     */
    private Object getItemDataIdentifier(T data) {
        // 判断子类是否有重写该方法，如果没有使用getItemId方法
        Object indentifier = getItemObjectId(data);
        if (indentifier != data) {
            return getItemId(data);
        }
        return indentifier;
    }

    /**
     * <p> 图片加载回调 </p>
     */
    protected class LoadCallBack implements ImageLoadCallBack {

        private T data = null;
        private String urlString = null;

        public LoadCallBack(T data, String urlString) {
            this.data = data;
            this.urlString = urlString;
        }

        @Override
        public void loadResult(Bitmap bitmap) {
            if (bitmap == null)
                return;
            SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(bitmap);
            images.put(urlString, softReference);
            Set<ImageView> imageViews = showNewsIV.keySet();
            for (ImageView imageView : imageViews) {
                T data2 = showNewsIV.get(imageView);
                if (data == data2) {
                    imageView.setImageBitmap(bitmap);
                    break;
                }
            }
        }

        @Override
        public void progress(int progress) {
        }
    }

    /**
     * 开始加载图片
     */
    public void startLoadImage() {
        Collection<T> showNews = showNewsIV.values();
        for (T news : showNews) {
            URL picUrl = getDataImageUrl(news);
            if (picUrl != null) {
                String picUrlString = picUrl.toString();
                SoftReference<Bitmap> imageRef = images.get(picUrlString);
                if (imageRef == null || imageRef.get() == null) {
                    mImageCacheTool.asyncLoadImage(picUrl, new LoadCallBack(news, picUrl.toString()), reqImageWidth, reqImageWidth);
                }
            }
        }
    }

    /**
     * <p> 基础视图存储类，包含一个ImageView，子类需要继承该类添加其他视图</p>
     */
    public class BaseViewHolder {
        public ImageView imageView;
    }

    /**
     * <p>添加一条数据</P>
     *
     * @param data 数据内容
     */
    public void addItem(T data) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call addItem method on main thread!");
        }
        datas.add(data);
    }

    /**
     * <p>添加多条数据内容</P>
     *
     * @param datas 数据集合
     */
    public void addItemCollection(Collection<T> datas) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call addItemCollection method on main thread!");
        }
        this.datas.addAll(datas);
    }

    /**
     * <p>替换列表中的旧数据</P>
     *
     * @param oldData 旧数据
     * @param newData 新数据
     */
    public void replaceItem(T oldData, T newData) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call replaceItem method on main thread!");
        }
        if (oldData == null) {
            datas.add(newData);
            notifyDataSetChanged();
            return;
        }
        int oldIndex = datas.indexOf(oldData);
        datas.remove(oldIndex);
        datas.add(oldIndex, newData);
        super.notifyDataSetChanged();
        CommonApp.getAppHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startLoadImage();
            }
        }, 100);
    }

    /**
     * <p>替换多条数据</P>
     *
     * @param oldData 旧数据集合
     * @param newData 新数据集合
     */
    public void replaceItem(List<T> oldData, List<T> newData) {
        // 判断是否在主线程中调用
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("Please call replaceItem method on main thread!");
        }
        // 如果不存在旧数据
        if (oldData == null || oldData.size() == 0) {
            datas.addAll(newData);
            notifyDataSetChanged();
            return;
        }
        // 做替换
        int oldStartIndex = datas.indexOf(oldData.get(0));
        int oldEndIndex = datas.indexOf(oldData.get(oldData.size() - 1));
        if (oldStartIndex != -1 && oldEndIndex != -1) {
            int removeLen = oldData.size();
            for (int i = 0; i < removeLen; i++) {
                datas.remove(oldStartIndex);
            }
            int insertStartIndex = oldStartIndex;
            int addLen = newData.size();
            for (int i = 0; i < addLen; i++) {
                datas.add(insertStartIndex++, newData.get(i));
            }
        }
        super.notifyDataSetChanged();
        CommonApp.getAppHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startLoadImage();
            }
        }, 100);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        CommonApp.getAppHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startLoadImage();
            }
        }, 100);
    }

    /**
     * <p>清除适配器中的数据</P>
     */
    public void clear() {
        datas.clear();
        /*notifyDataSetChanged();*/
    }

    /**
     * <p>设置第index项被选中</P>
     *
     * @param index 要选中的序号
     */
    public void setSelected(int index) {
        if (selectedItemIndex == null) {
            selectedItemIndex = new ArrayList<Integer>();
        }
        if (selectedItemIndex.contains(Integer.valueOf(index))) {
            selectedItemIndex.remove(Integer.valueOf(index));
        } else {
            selectedItemIndex.add(Integer.valueOf(index));
        }
    }

    /**
     * <p>获取设置为选中的数据集合</P>
     *
     * @return 选中的数据集合
     * @see {@link #setSelected(int)}
     */
    public List<T> getSelectedItem() {
        ArrayList<T> list = new ArrayList<T>();
        if (selectedItemIndex != null) {
            for (int i = 0; i < selectedItemIndex.size(); i++) {
                int index = selectedItemIndex.get(i);
                list.add(datas.get(index));
            }
        }
        return list;
    }

    /**
     * <p>获取所有选中项的索引</P>
     *
     * @return 所有选中项的索引
     */
    public List<Integer> getSelectedItemIndex() {
        return selectedItemIndex;
    }

    /**
     * <p>清除选中项</P>
     */
    public void clearSelected() {
        if (selectedItemIndex != null) {
            selectedItemIndex.clear();
            notifyDataSetChanged();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            startLoadImage();
        }
    }
}
