package com.nd.shapedexamproj.view.course;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 
 * @ClassName: CoachTopicPopUpView
 * @Title:
 * @Description:课堂-辅导讨论区-帖子详情-更多操作框
 * @Author:XueWenJian
 * @Since:2014年5月29日10:08:08
 * @Version:1.0
 */
public class CoachTopicReplyPopUpView extends PopupWindow{
	
	private View conentView;
	
	private TextView mReplyUpdateTV;
	private TextView mReplyDelTV;
	private ImageView mReplyDelLineIV;
	 
    public CoachTopicReplyPopUpView(final Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = inflater.inflate(R.layout.course_coach_topic_reply_popup, null);
        
        mReplyUpdateTV = (TextView)conentView.findViewById(R.id.course_coach_topic_reply_update);
        mReplyDelTV = (TextView)conentView.findViewById(R.id.course_coach_topic_reply_delete);
        mReplyDelLineIV = (ImageView)conentView.findViewById(R.id.course_coach_topic_reply_delete_line);
        
       
        // 设置SelectPicPopupWindow的View
        this.setContentView(conentView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.PopUpWindowAnimation);
 
    }
 
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            this.showAsDropDown(parent, parent.getLayoutParams().width / 2, 0);
        } else {
            this.dismiss();
        }
    }
    
    public void setReplyDelVisiable(int visiable)
    {
    	mReplyDelTV.setVisibility(visiable);
    	mReplyDelLineIV.setVisibility(visiable);
    }
    
    public void setReplyUpdateListener(OnClickListener clickListener)
    {
    	mReplyUpdateTV.setOnClickListener(clickListener);
    }
    
    public void setReplyDelListener(OnClickListener clickListener)
    {
    	mReplyDelTV.setOnClickListener(clickListener);
    }
}
