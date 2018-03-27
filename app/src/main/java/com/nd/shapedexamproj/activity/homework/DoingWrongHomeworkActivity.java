package com.nd.shapedexamproj.activity.homework;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.Homework;
import com.nd.shapedexamproj.model.homework.WrongHomework;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.view.homework.DoingWrongHomeworkFragment;
import com.tming.common.cache.net.TmingCacheHttp;
import org.json.JSONObject;

/**
 * 错题集Activity
 * Created by yusongying on 2015/2/4.
 */
public class DoingWrongHomeworkActivity extends DoingHomeworkActivity {

    private String mCourseId;
    private String mCourseName;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        rightBtn.setVisibility(View.INVISIBLE);
        mCourseId = getIntent().getStringExtra("courseId");
        mCourseName = getIntent().getStringExtra("courseName");
        titleTv.setText(mCourseName);
    }

    @Override
    protected void requestData() {
        showLoadingView();
        String urlString = ServerApi.Homework.getWronSubjectCollect(App.getUserId(), mCourseId);
        task = TmingCacheHttp.getInstance().asyncRequestWithCache(urlString, null, this);
    }

    @Override
    public Homework parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        if (JsonUtil.checkPhpApiALLIsOK(jsonObject)) {
            WrongHomework wrongHomework = new WrongHomework();
            wrongHomework.initWithJsonObject(jsonObject.getJSONObject("data"));
            return wrongHomework;
        }
        throw new Exception("service api error!");
    }

    @Override
    public void exception(Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public void onFinishRequest() {
        if (mHomework != null) {
            showContentView();
            if (isActivityRunningForeground) {
                createDoingHomeworkFragment();
            }
        } else {
            showErrorView();
        }
        task = null;
    }

    @Override
    protected void createDoingHomeworkFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        doingHomeworkFragment = new DoingWrongHomeworkFragment();
        doingHomeworkFragment.setHomework(mHomework);
        transaction.replace(R.id.doing_homework_content_frame_lay, doingHomeworkFragment);
        transaction.commit();
    }

    @Override
    protected void saveHomework() {
        // ignore
    }

    @Override
    public void pauseDoingHomework() {
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                finish();
            }
        };
        dialog.setContent(mHomework.getDoingProfileString(false) + "\r\n将不保存做题记录。");
        dialog.setTitle("您是否要停止复习错题?");
        dialog.setPositiveButton("停止复习");
        dialog.show();
    }
}
