package com.nd.shapedexamproj.view;


import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;

import com.nd.shapedexamproj.R;

/**
 * 
 * <p>聊天页底部带输入框布局</p>
 * <p>Created by zll on 2014-12-4</p>
 */
public class ChatBottomBar extends InputBottomBar {
    
    private ChatViewHolder mChatViewHolder;
    private OnSendBtnClickListener mOnSendBtnClickListener; 
    
    
    public ChatBottomBar(Context context, FragmentManager fragmentManager, View parentView, int maxLength) {
        super(context, fragmentManager, parentView, maxLength);
        mParentView = parentView;
    }
    
    @Override
    public void initView() {
        super.initView();
        mChatViewHolder = new ChatViewHolder();
        mChatViewHolder.chatInputEditText = (EditText) mParentView.findViewById(R.id.chat_text_et);
        mChatViewHolder.chatSendImgBtn = (Button) mParentView.findViewById(R.id.chat_send_imgbtn);
        
    }
    
    @Override
    public void addListener() {
        super.addListener();
        mChatViewHolder.chatInputEditText.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏表情选择器
                hideFace();
                return false;
            }
        });
        mChatViewHolder.chatSendImgBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mOnSendBtnClickListener.sendMsg();
            }
        });
        
        mChatViewHolder.chatInputEditText.addTextChangedListener(new TextWatcher() {
            private CharSequence mTemp;
            
            @Override
            public void afterTextChanged(Editable arg0) {
                //文本框内容为空时，发布按钮为不可用状态
                if (mTemp.length() > 0) {
                    sendClickable(true);
                } else {
                    sendClickable(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence text, int arg1, int arg2,
                    int arg3) {
                mTemp = text;
            }

        });
    }
    
    public EditText getEditText(){
        return mChatViewHolder.chatInputEditText;
    }
    
    public Button getSendImage() {
        return mChatViewHolder.chatSendImgBtn;
    }
    
    public void setText(String text){
        mChatViewHolder.chatInputEditText.setText(text);
    }
    
    public String getText() {
        return mChatViewHolder.chatInputEditText.getText().toString();
    }
    
    public void setTag(Object obj) {
        mChatViewHolder.chatInputEditText.setTag(obj);
    }
    
    public Object getTag() {
        return mChatViewHolder.chatInputEditText.getTag();
    }
    
    /**
     * 设置发送按钮是否可用
     * @param able
     */
    private void sendClickable(boolean able) {
        if (able) {
            mChatViewHolder.chatSendImgBtn.setClickable(able);
            mChatViewHolder.chatSendImgBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.common_btn_selector));
        } else {
            mChatViewHolder.chatSendImgBtn.setClickable(able);
            mChatViewHolder.chatSendImgBtn.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.btn_unused_gray));
        }
    }
    
    private class ChatViewHolder extends Holder{
        private EditText chatInputEditText;
        private Button chatSendImgBtn;
    }
    
    public void setOnSendBtnClickListener(OnSendBtnClickListener onSendBtnClickListener){
        this.mOnSendBtnClickListener = onSendBtnClickListener;
    }
    
    public interface OnSendBtnClickListener {
        /**
         * <p>发送消息</P>
         */
        public abstract void sendMsg();
    }
    
}
