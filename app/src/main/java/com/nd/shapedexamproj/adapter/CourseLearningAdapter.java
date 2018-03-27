package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.entity.LearningCourseInfoEntity;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.ImageBaseAdatapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.adapter
 * @文件名: CourseLearningAdapter
 * @文件描述: 在修课程adapter，用于学生课堂模块在修课程加载及显示
 * @创建人: xuwenzhuo
 * @创建时间: 2014/10/24
 * @Copyright: 2014 Tming All rights reserved.
 */

public class CourseLearningAdapter extends ImageBaseAdatapter<LearningCourseInfoEntity> {

    private Context mContext;

    public CourseLearningAdapter(Context context,List<LearningCourseInfoEntity> courseList) {
        super(context);
        mContext=context;
        defaultImageResourceId= R.drawable.empty_photo;
    }

    public  List<LearningCourseInfoEntity> getDatas(){return  super.datas;}

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void replaceItem(List<LearningCourseInfoEntity> oldData, List<LearningCourseInfoEntity> newData) {
        List<LearningCourseInfoEntity> courseList=super.datas;
        // 如果没有旧数据
        if (oldData == null || oldData.size() == 0) {
            courseList.clear();
            courseList.addAll(newData);
            notifyDataSetChanged();
            return;
        }
        // 做替换
        int oldStartIndex = courseList.indexOf(oldData.get(0));
        int oldEndIndex = courseList.indexOf(oldData.get(oldData.size() - 1));
        if (oldStartIndex != -1 && oldEndIndex != -1) {
            for (int i = oldStartIndex; i <= oldEndIndex; i++) {
                courseList.remove(oldStartIndex);
            }
            int insertStartIndex = oldStartIndex;
            for (int i = 0; i < newData.size(); i++) {
                courseList.add(insertStartIndex++, newData.get(i));
            }
        }
        super.notifyDataSetChanged();
    }

    @Override
    public View infateItemView(Context context) {
        View convertView=View.inflate(context,R.layout.course_in_item,null);
        courseViewHolder viewHolder=new courseViewHolder();
        viewHolder.mPercentBgView=(View) convertView.findViewById(R.id.percent_bg);
        viewHolder.mDescTV=(TextView) convertView.findViewById(R.id.course_to_tv);
        viewHolder.imageView=(ImageView) convertView.findViewById(R.id.course_face_iv);
        viewHolder.mCourseTitleTV=(TextView) convertView.findViewById(R.id.course_name_tv);
        viewHolder.mChapterTV=(TextView) convertView.findViewById(R.id.course_chapter_tv);
        viewHolder.mWatchToTV=(TextView) convertView.findViewById(R.id.course_time_tv);
        viewHolder.mLearnPercentTV=(TextView) convertView.findViewById(R.id.course_percent_tv);
        viewHolder.mCourseCountView = (View) convertView.findViewById(R.id.course_count_view);
        viewHolder.mPercentBgView=(View) convertView.findViewById(R.id.percent_bg);
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public URL getDataImageUrl(LearningCourseInfoEntity data) {
        URL url = null;
        try {
            url = new URL(data.mPhoto);//data.mPhoto

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public void setViewHolderData(BaseViewHolder holder, final LearningCourseInfoEntity data) {
        ((courseViewHolder) holder).mCourseTitleTV.setText(data.mName);
        if (data.mPlayRecord.mChapterName.equals("未开始")){
            ((courseViewHolder) holder).mDescTV.setText(R.string.no_records_tip);
            //视频图片上显示的学习进度百分比,暂时隐藏
            //((courseViewHolder) holder).mPercentBgView.setVisibility(View.GONE);
            //((courseViewHolder) holder).mLearnPercentTV.setVisibility(View.GONE);
            //设定默认的百分比
            ((courseViewHolder)holder).mLearnPercentTV.setText("0%");
            ((courseViewHolder) holder).mChapterTV.setVisibility(View.GONE);
            ((courseViewHolder) holder).mWatchToTV.setVisibility(View.GONE);
            ((courseViewHolder) holder).mLearnPercentTV.setVisibility(View.VISIBLE);

        } else{
            ((courseViewHolder) holder).mDescTV.setText(R.string.watch_to_text);
            //((courseViewHolder) holder).mPercentBgView.setVisibility(View.VISIBLE);
            //((courseViewHolder) holder).mLearnPercentTV.setVisibility(View.VISIBLE);
            ((courseViewHolder) holder).mChapterTV.setVisibility(View.VISIBLE);
            ((courseViewHolder) holder).mWatchToTV.setVisibility(View.VISIBLE);
            ((courseViewHolder) holder).mLearnPercentTV.setVisibility(View.VISIBLE);

            String message=data.mPlayRecord.mChapterName+" - "+((data.mPlayRecord.mLesson.trim().length()>0)?data.mPlayRecord.mLesson.trim():"");
            ((courseViewHolder) holder).mChapterTV.setText(setShortMessage(message,13));
            ((courseViewHolder) holder).mWatchToTV.setText("("+ DateUtils.getHMSStr(new Integer( data.mPlayRecord.mTime))+")");
            ((courseViewHolder) holder).mLearnPercentTV.setText(Math.round(data.mPercent*100)+"%");
        }
        autoLength(((courseViewHolder)holder).mPercentBgView,((courseViewHolder)holder).mLearnPercentTV.getText().length());
        //changePercentImg(data.mPercent,((courseViewHolder)holder).mLearnedPercentIV);
        //课程要素点击操作
        ((courseViewHolder)holder).mCourseCountView.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                //进入课程详情Activity界面
                UIHelper.showCourseDetail(mContext, data.mCourseId,data.mName,data.mPlayRecord.mChapterId);
            }
        });
    }

    /**
     * 自动设定背景长度 
     * @param view 修改视图
     * @param length 修改长度
     */
    private void autoLength(View view ,int length){
        if (null!=view){
            view.getLayoutParams().width=16+length*8;
        }
    }

    public void setViewHolderChapterAndTime(BaseViewHolder holder,String chapter,String lesson,int time){

        String message=((chapter.indexOf("章")==chapter.length()-1)?chapter:chapter+"章")+((lesson.trim().length()>0)?lesson.trim()+"节":"");
        ((courseViewHolder) holder).mChapterTV.setText(setShortMessage(message,17));
        ((courseViewHolder) holder).mWatchToTV.setText(DateUtils.getHMSStr(new Integer(time)));
    }

    private String setShortMessage(String message,int maxLength){

        if (message.length() <= maxLength){
            return message;
        } else{
            return message.substring(0,maxLength-1)+"...";
        }
    }

    /**
     * 修改背景图片
     * @param value
     * @param view
     */
    private void changePercentImg(double value,ImageView view){

        if (value==0){
            view.setImageResource(R.drawable.percen4);
        } else{
            if (value<1){
                view.setImageResource(R.drawable.percen1);
            } else if (value==1){
                view.setImageResource(R.drawable.percen3);
            }
        }
    }

    final class courseViewHolder extends BaseViewHolder{

        TextView mCourseTitleTV,mChapterTV,mWatchToTV,mLearnPercentTV,mDescTV;
        View mCourseCountView,mPercentBgView;
    }
}
