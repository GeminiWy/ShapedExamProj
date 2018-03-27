package com.nd.shapedexamproj.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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
import com.nd.shapedexamproj.activity.im.NewFriendsActivity;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.view.CircularImage;
import com.tming.common.util.Helper;

import java.util.List;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.adapter
 * @文件名: RelatedUserListAdapter
 * @文件描述: 用户关注列表适配器
 * @创建人: xuwenzhuo
 * @创建时间: 2014/11/19
 * @Copyright: 2014 Tming All rights reserved.
 */

public class RelatedUserListAdapter extends BaseAdapter {

    private Context mContext;
    private List<RelatedUserEntity> mRelatedUserList;
    private Drawable mGreenButtonBg;
    private Drawable mWhiteButtonBg;
    private int mOldTextColor;
    private int mNewTextColor;

    public RelatedUserListAdapter(Context context,List<RelatedUserEntity> relatedUserEntityList) {
        mContext = context;
        mRelatedUserList = relatedUserEntityList;
        mGreenButtonBg= App.getAppContext().getResources().getDrawable(R.drawable.null_foreground_button_pressed);
        mWhiteButtonBg=App.getAppContext().getResources().getDrawable(R.drawable.green_white_button);
        mOldTextColor=Color.WHITE;
        mNewTextColor=App.getAppContext().getResources().getColor(R.color.head_btn_border);
    }

    /**
     * 更换要素状态，添加/未添加
     * @param posision 要素标
     */
    public void changeItemState(int posision){
        if (posision<0||mRelatedUserList==null||posision>mRelatedUserList.size()){
            return;
        } else {
            RelatedUserEntity relatedUserEntity=mRelatedUserList.get(posision);
            relatedUserEntity.setmType((relatedUserEntity.getmType()==Constants.PERSON_RELATION_FRIEND)?Constants.PERSON_RELATION_EMPTY:Constants.PERSON_RELATION_FRIEND);
            singleItemRefresh(relatedUserEntity,posision);
        }
    }

    /**
     * 修改要素状态为指定的state 状态
     * @param posision 要素位置
     * @param state 状态
     */
    public void changeItemState(int posision,final int state){

        if (posision<0||mRelatedUserList==null||posision>mRelatedUserList.size()){
            return;
        } else {
            RelatedUserEntity relatedUserEntity=mRelatedUserList.get(posision);
            relatedUserEntity.setmType((state!=Constants.PERSON_RELATION_FRIEND)?Constants.PERSON_RELATION_EMPTY:Constants.PERSON_RELATION_FRIEND);
            singleItemRefresh(relatedUserEntity,posision);
        }
    }

    /**
     * 针对某一个要素进行刷新
     * @param relatedUserEntity 更新后的对象
     * @param index  数据所在位置
     */
    public void singleItemRefresh(RelatedUserEntity relatedUserEntity,int index){
        if (mRelatedUserList==null||mRelatedUserList.size()<index){
            return;
        } else {
            mRelatedUserList.set(index,relatedUserEntity);
            super.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        if (mRelatedUserList==null){
            return 0;
        } else {
            return mRelatedUserList.size();
        }
    }

    public List<RelatedUserEntity> getData(){

        return mRelatedUserList;
    }

    /**
     * 设定数据
     * @param data
     */
    public void setData(List<RelatedUserEntity> data){
        for (int i=0;i<data.size();i++){

            if (mRelatedUserList.contains(data.get(i))){
                continue;
            } else {
                mRelatedUserList.add(data.get(i));
            }
        }
    }

    @Override
    public Object getItem(int arg0) {
        return mRelatedUserList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    public List<RelatedUserEntity> getmRelatedUserList() {
        return mRelatedUserList;
    }

    public void setmRelatedUserList(List<RelatedUserEntity> mRelatedUserList) {
        this.mRelatedUserList = mRelatedUserList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.related_user_cell,null);
            viewHolder.related_userimage_iv = (CircularImage) convertView.findViewById(R.id.related_userimage_iv);
            viewHolder.related_username_tv = (TextView) convertView.findViewById(R.id.related_username_tv);
            viewHolder.related_add_btn = (Button) convertView.findViewById(R.id.related_add_btn);
            viewHolder.related_userdetail_tv=(TextView) convertView.findViewById(R.id.related_userinfo_detail_tv);
            //viewHolder.related_add_btn.setOnClickListener(new AddRelatedListener());
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //设定参数
        RelatedUserEntity relatedUserEntity = null;
        if (position < mRelatedUserList.size()) {
            relatedUserEntity = mRelatedUserList.get(position);
            viewHolder.init(relatedUserEntity);
            viewHolder.related_add_btn.setTag(position);
            viewHolder.related_userimage_iv.setTag(position);
        }
        return convertView;
    }


    public class AddRelatedListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Intent intent=new Intent();
            intent.setAction(NewFriendsActivity.ADD_RELATEDUSER);
            intent.putExtra("posistion",new Integer((String)view.getTag()));
            Helper.sendLocalBroadCast(App.getAppContext(),intent);
        }
    }

    class ViewHolder {

        CircularImage related_userimage_iv;
        TextView related_username_tv;
        TextView related_userdetail_tv;
        Button related_add_btn;

        @TargetApi(16)
        public void init(RelatedUserEntity relatedUserEntity) {
            if (null != relatedUserEntity) {

                ImageUtil.asyncLoadImage(related_userimage_iv, relatedUserEntity.getmRelatedImg(), R.drawable.all_use_icon_photo);
                related_username_tv.setText(relatedUserEntity.getmRelatedUserName());
                related_userdetail_tv.setText(relatedUserEntity.getmUserDetail());
                if (relatedUserEntity.getmType() == Constants.PERSON_RELATION_FRIEND) {
                    //已经是好友,包括 关注对方  以及双方相互关注
                    related_add_btn.setText(R.string.msg_have_added);
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                        related_add_btn.setTextColor(mNewTextColor);
                        related_add_btn.setBackground(mWhiteButtonBg);
                    }
                    related_add_btn.setEnabled(false);
                } else {
                    //还没有关注
                    related_add_btn.setText(R.string.add);
                    if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                        related_add_btn.setTextColor(mOldTextColor);
                        related_add_btn.setBackground(mGreenButtonBg);
                    }
                    related_add_btn.setEnabled(true);
                }
            }
        }
    }

}
