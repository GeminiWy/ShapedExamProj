/**  
 * Project Name:OpenUniversity  
 * File Name:PersonSearchAdapter.java  
 * Package Name:com.tming.openuniversity.adapter  
 * Date:2014-6-10上午12:53:38  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.im.model.PersonSearchResult;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.view.CircularImage;

import java.util.List;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.adapter
 * @文件名: PersonSearchAdapter
 * @文件描述: 用户检索结果适配器
 * @创建人: XueWenJian
 * @创建时间: 2014/6/10
 * @修改记录:
 *     1、Modified by xuwenzhuo On 2014/11/24
 *       修改：根据交互需求修改功能
 *
 * @Copyright: 2014 Tming All rights reserved.
 */

public class PersonSearchAdapter extends BaseAdapter {

	private Context mContext;
	private List<PersonSearchResult> mPersonSearchResults;
    private Drawable mGreenButtonBg;
    private Drawable mWhiteButtonBg;
    private int mOldTextColor;
    private int mNewTextColor;
    private boolean mButtonVisiable; //设定按钮是否可见

	public PersonSearchAdapter(Context context,List<PersonSearchResult> personSearchResults) {

		mContext = context;
		mPersonSearchResults = personSearchResults;

        mGreenButtonBg= App.getAppContext().getResources().getDrawable(R.drawable.null_foreground_button_pressed);
        mWhiteButtonBg=App.getAppContext().getResources().getDrawable(R.drawable.green_white_button);
        mOldTextColor= Color.WHITE;
        mNewTextColor=App.getAppContext().getResources().getColor(R.color.head_btn_border);
        //默认添加按钮可见
        mButtonVisiable=true;
	}

    public boolean ismButtonVisiable() {
        return mButtonVisiable;
    }

    public void setmButtonVisiable(boolean mButtonVisiable) {
        this.mButtonVisiable = mButtonVisiable;
    }

    /**
     *  修改要素状态 
     * @param posision 要素所在位置下标
     */
    public void changeSingleItemState(int posision ){

        if (mPersonSearchResults==null||posision<0||mPersonSearchResults.size()<=posision){
            return;
        } else{
            PersonSearchResult personSearchResult=mPersonSearchResults.get(posision);
            //修改状态为相反状态
            personSearchResult.setIsAdded(personSearchResult.getIsAdded()==1?0:1);
            super.notifyDataSetChanged();
        }
    }

    /**
     *  修改要素状态 
     * @param posision 要素所在位置下标
     * @param isAdded 是否已经添加
     */
    public void changeSingleItemState(int posision ,final int isAdded){

        if (mPersonSearchResults==null||posision<0||mPersonSearchResults.size()<=posision){
            return;
        } else{
            PersonSearchResult personSearchResult=mPersonSearchResults.get(posision);
            personSearchResult.setIsAdded(isAdded);
            super.notifyDataSetChanged();
        }
    }

    /**
     * 获取列表中的数据
     * @return
     */
    public List<PersonSearchResult> getData(){
        return  mPersonSearchResults;
    }

	@Override
	public int getCount() {
		return mPersonSearchResults.size();
	}

	@Override
	public Object getItem(int arg0) {

		return mPersonSearchResults.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (null == convertView) {
			viewHolder = new ViewHolder();
			convertView = View.inflate(mContext, R.layout.im_personsearch_item,
					null);
			viewHolder.personsearch_userimage_iv = (CircularImage) convertView
	                .findViewById(R.id.personsearch_userimage_iv);
	        viewHolder.personsearch_username_tv = (TextView) convertView
	                .findViewById(R.id.personsearch_username_tv);
	        viewHolder.personsearch_add_btn = (Button) convertView
	                .findViewById(R.id.personsearch_add_btn);
            viewHolder.personsearch_add_btn.setVisibility((mButtonVisiable)?View.VISIBLE:View.GONE);//设定添加按钮是否可见
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		PersonSearchResult personSearchResult = null;
		if (position < mPersonSearchResults.size()) {
			personSearchResult = mPersonSearchResults.get(position);
			viewHolder.init(personSearchResult);
            //设定tag，在listView的使用中会用到
			viewHolder.personsearch_add_btn.setTag(position);
            viewHolder.personsearch_userimage_iv.setTag(position);
		}

		return convertView;
	}

    public List<PersonSearchResult> getmPersonSearchResults() {
        return mPersonSearchResults;
    }

    public void setmPersonSearchResults(List<PersonSearchResult> mPersonSearchResults) {
        this.mPersonSearchResults = mPersonSearchResults;
    }

    class ViewHolder {
		CircularImage personsearch_userimage_iv;
		TextView personsearch_username_tv;
		TextView personsearch_userid_tv;
		Button personsearch_add_btn;

		public void init(PersonSearchResult personSearchResult) {
			if (null != personSearchResult) {

				ImageUtil.asyncLoadImage(personsearch_userimage_iv, personSearchResult.getImgUrl(), R.drawable.all_use_icon_photo);
				personsearch_username_tv.setText(personSearchResult.getUserName()); 
				if (personSearchResult.getIsAdded() == 1) {
				    personsearch_add_btn.setText(R.string.msg_have_added);
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN) {
                        personsearch_add_btn.setTextColor(mNewTextColor);
                        personsearch_add_btn.setBackground(mWhiteButtonBg);
                    }
                    personsearch_add_btn.setEnabled(false);
				} else {
				    personsearch_add_btn.setText(R.string.add);
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                        personsearch_add_btn.setTextColor(mOldTextColor);
                        personsearch_add_btn.setBackground(mGreenButtonBg);
                    }
                    personsearch_add_btn.setEnabled(true);
				}
			}
		}
	}

}
