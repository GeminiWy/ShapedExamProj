package com.nd.shapedexamproj.view.course;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.util.UIHelper;

import java.util.Iterator;
import java.util.Set;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.view.course
 * @文件名: MultiLineTextView
 * @文件描述: 多行多textview展现
 * @创建人: xuwenzhuo
 * @创建时间: 2014/12/18 11:26
 * @Copyright: 2014 Tming All rights reserved.
 */

public class MultiLineTextView extends LinearLayout{

    private Context mContext;
    private Course mCourse;                //课程
    private int mMaxWidth;                 //最大宽度
    private int mCursorX=0,mCursorY=0;     //游标

    public MultiLineTextView(Context context,Course course,int width) {
        super(context);
        mContext=context;
        this.mCourse=course;
        this.mMaxWidth=width;
        this.setOrientation(VERTICAL);
        redrawView();
    }

    /**
     * 重绘视图
     */
    private void redrawView(){

        //LinearLayout newRowLayout=null;
        if (null!=mCourse){
            Set<String> coacherIdSet = mCourse.coacherMap.keySet();
            Iterator<String> coacherIdItr = coacherIdSet.iterator();

            while (coacherIdItr.hasNext()) {

                String coacherId = coacherIdItr.next();
                String coacher = mCourse.coacherMap.get(coacherId) + " ";

                //新生成一个 textview
                TextView newTextView = createTextView(coacher, coacherId);
                this.addView(newTextView);
            }
        }
    }

    /**
     *
     * @param name	 用户名
     * @param userId 用户id
     * @return
     */
    private TextView createTextView (String name, final String userId) {
        TextView textView = new TextView (mContext);
        RelativeLayout.LayoutParams params =new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);//new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.leftMargin = 5;
        textView.setLayoutParams(params);
        textView.setTextColor(getResources().getColor(R.color.title_green));
        textView.setText(name);
        textView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AuthorityManager.getInstance()
                        .isInnerAuthority()) {
                    AuthorityManager.getInstance().showInnerDialog(mContext);
                    return;
                }
                UIHelper.showFriendInfoActivity(mContext, userId);
            }
        });
        return textView ;
    }
}
