package com.nd.shapedexamproj.activity.course;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.CourseLearningAdapter;
import com.nd.shapedexamproj.entity.LearningCourseInfoEntity;
import com.nd.shapedexamproj.view.CircularImage;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.view.RefreshableListView;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> CourseMainActivity类 ,新增用户课程查看主界面</p>
 * <p> Created by xuwenzhuo  on 2014/10/24.</p>
 */
public class CourseMainActivity extends BaseActivity implements View.OnClickListener{

    private ImageView mStudyHistoryImgView,mDownloadImgView;
    private ImageView mDoWorkImgView,mErrorImgView;
    private CircularImage mStudentHeadImg;
    private RefreshableListView mLearningCourseRListView;
    private CourseLearningAdapter mLearningCourseAdapter;
    private View mLoadingView;
    private TmingCacheHttp cacheHttp;
    private List<LearningCourseInfoEntity> mLearningCourses=new ArrayList<LearningCourseInfoEntity>();
    private int mPageSize;
    private int mPageNum;

    @Override
    public int initResource() {
        return R.layout.course_new;
    }

    @Override
    public void initComponent() {
        //mStudyHistoryImgView=(ImageView) findViewById(R.id.list_head_right_history_img);
        //mDownloadImgView=(ImageView) findViewById(R.id.list_head_right_history_img);
        mStudentHeadImg=(CircularImage) findViewById(R.id.course_my_photo_iv);
        mLearningCourseRListView=(RefreshableListView) findViewById(R.id.course_learning_list);
        mLoadingView=(View) findViewById(R.id.loading_layout);
        cacheHttp = TmingCacheHttp.getInstance(this);
    }

    @Override
    public void initData() {

        mLearningCourseRListView.setAdapter(mLearningCourseAdapter);
    }

    @Override
    public void addListener() {
        mStudyHistoryImgView.setOnClickListener(this);
        mDownloadImgView.setOnClickListener(this);
        mStudyHistoryImgView.setOnClickListener(this);
    }

    @Override
    public void initAuthoriy() {
        super.initAuthoriy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            /*case R.id.list_head_history_img:
                //观看历史

                break;*/
            case R.id.course_my_photo_iv:
                //用户头像

                break;
            default:
                break;
        }
    }





}
