package com.nd.shapedexamproj.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.GridViewFaceAdapter;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.util.Log;

/**
 * 
 * <p>
 * 聊天表情页
 * </p>
 * <p>
 * Created by zll on 2014-8-8
 * </p>
 */
public class ChatFacesFragment extends BaseFlowFragment {

    private static String TAG = "ChatFacesFragment";
    private GridView mFaceGridView;
    private GridViewFaceAdapter mGVFaceAdapter;
    private int[] facesImgList;
    private int[] tempImgList;
    private static int PAGE_SIZE = InputBottomBar.PAGE_SIZE;// 一页的数量
    private int currentPageNum;// 第几页,从0开始
    private int currentPageSize = PAGE_SIZE;// 当前页数量
    private EditText mContentET;
    private int mMaxLength;

    public ChatFacesFragment(EditText mContentET, int mMaxLength) {
        this.mContentET = mContentET;
        this.mMaxLength = mMaxLength;
    }
    public ChatFacesFragment(){}
    
    public int[] getFacesImgList() {
        return facesImgList;
    }

    public void setFacesImgList(int[] facesImgList) {
        this.facesImgList = facesImgList;
    }

    public int getCurrentPageNum() {
        return currentPageNum;
    }

    public void setCurrentPageNum(int currentPageNum) {
        this.currentPageNum = currentPageNum;
    }

    @Override
    public int initResource() {
        return R.layout.chat_faces_fragment;
    }

    @Override
    public void initComponent(View view) {
        mFaceGridView = (GridView) view.findViewById(R.id.tweet_pub_faces);
    }

    @Override
    public void initData() {
        initGridView();
    }

    // 初始化表情控件
    private void initGridView() {
        
        if (facesImgList == null || facesImgList.length == 0) {
            return;
        }
        
        currentPageSize = facesImgList.length - PAGE_SIZE * currentPageNum;
        currentPageSize = currentPageSize > PAGE_SIZE ? PAGE_SIZE : currentPageSize;
        
        tempImgList = new int[currentPageSize];
        
        for (int i = 0;i < currentPageSize;i ++) {
            tempImgList[i] = facesImgList[PAGE_SIZE * currentPageNum + i];
        }
        
        mGVFaceAdapter = new GridViewFaceAdapter(getActivity(),tempImgList,currentPageNum);
        mFaceGridView.setAdapter(mGVFaceAdapter);
        mFaceGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == (mGVFaceAdapter.getCount() - 1)) {
                    Log.d(TAG, "删除一个表情" + mContentET.getSelectionStart());
                    int selection = mContentET.getSelectionStart();
                    String text = mContentET.getText().toString();
                    if (selection > 0) {
                        String text2 = text.substring(selection - 1);
                        if ("]".equals(text2)) {
                            int start = text.lastIndexOf("[");
                            int end = selection;
                            mContentET.getText().delete(start, end);
                            return;
                        }
                        mContentET.getText().delete(selection - 1, selection);
                    }
                } else {
                    Log.d(TAG, "插入一个表情" + mContentET.getSelectionStart() + ";length:" + view.getTag().toString());
                    // 插入的表情
                    SpannableString ss = new SpannableString(view.getTag().toString());
                    Drawable d = getActivity().getResources().getDrawable((int) mGVFaceAdapter.getItemId(position));
                    d.setBounds(0, 0, (int) mContentET.getTextSize(), (int) mContentET.getTextSize());// 设置表情图片的显示大小
                    ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                    ss.setSpan(span, 0, view.getTag().toString().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    Log.d(TAG, "ImageSpan length:" + ss.length());
                    if (mMaxLength > 0) {
                        if (mMaxLength >= ss.length() + mContentET.getText().length()) {
                            // 在光标所在处插入表情
                            mContentET.getText().insert(mContentET.getSelectionStart(), ss);
                        }
                    } else {
                        mContentET.getText().insert(mContentET.getSelectionStart(), ss);
                    }
                }
            }
        });
    }



    @Override
    public void initListener() {}
}
