package com.nd.shapedexamproj.view.search;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.search.CourseInfo;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * <p>全局搜索 相关课程水平列表适配器。</p>
 * <p>Created by xiezz on 2014/06/04</p>
 * <p>Modified by xuwenzhuo 2014/12/05</p>
 * */
public class CourseHorizontalAdapter extends BaseAdapter {
	private Context mContext;  
	private List<CourseInfo> mListItems; //数据集合
	/**
	 * 最多有多少项
	 * */
	private int mCountItems;  
	private int selectIndex = -1;

    //课程缩略图
    private ImageCacheTool imageCacheTool = null;
    private int defaultImageResourceId = R.drawable.all_use_icon_photo;

	/**
	 * @param count 用来控制是否要限制显示子项个数,传入的值必须小于图像的个数，否则自动限制
	 * */
	public CourseHorizontalAdapter(Context context, List<CourseInfo> data, int count){
		this.mContext = context;
		this.mListItems = data; 
		this.mCountItems = count;
        imageCacheTool = ImageCacheTool.getInstance();
	}

	@Override
	public int getCount() {
		if(mListItems != null){
			if((mCountItems > 0) && (mListItems.size() > mCountItems)){			
				return mCountItems; 
			}
			return mListItems.size();
		} 
		
		return 0;		 
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.search_course_horizontal_list_item, null);
            holder.mCourseImg=(ImageView) convertView.findViewById(R.id.course_imageview);

			holder.courseTv = (TextView)convertView.findViewById(R.id.course_introduce_tv);
			holder.lecturerTv = (TextView)convertView.findViewById(R.id.course_lecturer_tv);
			holder.creditsTv = (TextView)convertView.findViewById(R.id.course_credits_tv); 
			
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder)convertView.getTag();
		}
		 
		if(position == selectIndex){
			convertView.setSelected(true);
		}else{
			convertView.setSelected(false);
		}
		 
		String temp = mContext.getResources().getString(R.string.search_result_by_user_number); 
		CourseInfo courseInfo = mListItems.get(position);
		String lecture = courseInfo.getLecturer().equals("null") ? "暂无" : courseInfo.getLecturer();
		
		holder.courseTv.setText(courseInfo.getCourseName() + String.format(temp, courseInfo.getStuNum()));
		holder.lecturerTv.setText(mContext.getResources().getString(R.string.search_course_lecturer) + lecture); 
		
		holder.creditsTv.setText(mContext.getResources().getString(R.string.search_course_credits) + courseInfo.getCredit());
		holder.creditsTv.setVisibility(View.GONE);

        /**
         * <p>设置图片</p>
         * */
        if(!TextUtils.isEmpty(courseInfo.getImg())){
            try {
                URL url = new URL(courseInfo.getImg());
                imageCacheTool.asyncLoadImage(holder.mCourseImg, null, url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }else{
            holder.mCourseImg.setImageResource(defaultImageResourceId);
        }

        final String  cid=courseInfo.getCourseId();
        final String cName= courseInfo.getCourseName();
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //进入课程详情Activity界面
                UIHelper.showCourseDetail(mContext, cid,cName);
            }
        });

		return convertView;
	}

    /**
     * 
     * <p>清除适配器中的数据</P>
     *
     */
    public void clear() {
    	mListItems.clear();
        notifyDataSetChanged();
    }

	private static class ViewHolder { 
		private TextView courseTv; //课程名称
		private TextView lecturerTv; //讲师
		private TextView creditsTv; //学费
        private ImageView mCourseImg; //课程图片

	}
	 
	public void setSelectIndex(int i){
		selectIndex = i;
	}
}