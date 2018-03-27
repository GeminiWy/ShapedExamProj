package com.nd.shapedexamproj.activity.shapedexam;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.xkhomework.XKHandInWarmupTask;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.model.xkhomework.XKWarmup;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.view.xkhomework.XKDoingProgressFragment;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.PhoneUtil;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 热身考试
 * Created by zll on 2015/3/19.
 */
public class DoingWarmupActivity extends BaseFragmentActivity implements View.OnClickListener, TmingCacheHttp.RequestWithCacheCallBackV2<XKWarmup>{

    private final static String TAG = "DoingWarmupActivity";
    protected XKWarmup mHomework;
    protected DoingTaskFragment doingHomeworkFragment;
    protected TextView titleTv,centerTitleTv;
    protected RelativeLayout bottomBtnLayout;
    protected Button rightBtn,negativeBtn,saveBtn,commitBtn;
    protected String mWarmupId = "";//热身考试ID
    protected String mWarmupName;
    protected boolean isEditable = true;//答题可编辑，查看详情不可编辑
    protected int mTaskStatus;//任务的状态
    protected boolean isActivityRunningForeground = false;
    private Object doingClockLock = new Object();
    private int errorCode = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.doing_task_activity);

        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        findViewById(R.id.error_btn).setOnClickListener(this);
        rightBtn = (Button) findViewById(R.id.common_head_right_btn);
        rightBtn.setOnClickListener(this);
        rightBtn.setText(getResources().getString(R.string.xk_answer_sheet));
        titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
        centerTitleTv = (TextView) findViewById(R.id.commonheader_center_tv);
        centerTitleTv.setText("");

        bottomBtnLayout = (RelativeLayout) findViewById(R.id.bottom_btn_layout);
        negativeBtn = (Button) findViewById(R.id.task_negative_btn);
        negativeBtn.setVisibility(View.GONE);
        saveBtn = (Button) findViewById(R.id.save_task_btn);
        commitBtn = (Button) findViewById(R.id.commit_task_btn);

        saveBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);

        setData();
        titleTv.setText(mWarmupName);
        setBtnVisible(isEditable);

    }

    protected void setData() {
        isEditable = getIntent().getBooleanExtra("isEditable", true);
        mTaskStatus = getIntent().getIntExtra("status", 0);
        mWarmupName = getIntent().getStringExtra("warmupName");
        mWarmupId = getIntent().getStringExtra("warmupId");
    }

    private void setBtnVisible(boolean isEditable) {
        if (!isEditable) {
            bottomBtnLayout.setVisibility(View.GONE);
        } else {
            bottomBtnLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveHomework();
        isActivityRunningForeground = false;
    }

    protected void saveHomework() {
        if (mHomework != null) {
            mHomework.saveHomework();
            mHomework.saveUserAnswer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHomework != null) {
            if (doingHomeworkFragment == null) {
                createDoingHomeworkFragment();
            }
            mHomework.setCheckTimeToCurrent();
        }
        if (mHomework == null) {
            requestData();
        }
        isActivityRunningForeground = true;
        synchronized (doingClockLock) {
            doingClockLock.notify();
        }
    }

    protected void requestData() {
        showLoadingView();
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id", App.getsStudentId());
        params.put("id",mWarmupId);//热身考试id
        PhpApiUtil.sendData(Constants.XK_WARM_UP_SUBJECTS, params, this);
    }

    @Override
    public XKWarmup parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        XKWarmup homework = new XKWarmup();
        homework.setHomeworkId(mWarmupId);
        homework.initWithJsonObject(jsonObject);
        return homework;

    }

    @Override
    public void cacheDataRespone(XKWarmup data) {
        mHomework = data;
    }

    @Override
    public void requestNewDataRespone(XKWarmup cacheRespones, XKWarmup newRespones) {
        mHomework = newRespones;
    }

    @Override
    public void exception(Exception exception) {
        exception.printStackTrace();
        if (errorCode != -7 && mHomework == null) {
            if (mWarmupId != null) {

            }
        }
    }

    @Override
    public void onFinishRequest() {
        if (mHomework != null) {
            mHomework.readUserAnswer();

            // 显示题目内容
            showContentView();
            if (isActivityRunningForeground) {
                createDoingHomeworkFragment();
            }

            //new DoingClockThread().start();
        } else if (errorCode == -7){
            // 作业已经过期
            showErrorView("作业已经过期");
        } else {
            showErrorView();
        }
    }

    protected void createDoingHomeworkFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        doingHomeworkFragment = new DoingTaskFragment();
        doingHomeworkFragment.setHomework(mHomework);
        doingHomeworkFragment.setEditable(isEditable);
        transaction.replace(R.id.doing_homework_content_frame_lay, doingHomeworkFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commonheader_left_iv:
                onBackPressed();
                break;
            case R.id.common_head_right_btn:
                if (mHomework == null) {
                    return;
                }
                showProgress();
                break;
            case R.id.error_btn:
                requestData();
                break;
            case R.id.save_task_btn:
                pauseDoingHomework();
                break;
            case R.id.commit_task_btn:
                showHandInHomeworkDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            changeTitle(false);
            return;
        }
        if (doingHomeworkFragment != null && isEditable) {//可编辑状态才弹出是否保存对话框
            pauseDoingHomework();
        } else {
            finish();
        }
    }

    public void pauseDoingHomework() {
        if (mHomework == null) return;
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                handInHomeworkAction(false);
            }

            @Override
            public void onNegativeButtonClick() {
                finish();
            }
        };
        dialog.setContent(mHomework.getDoingProfileString(false) + "\r\n保存做题记录可在不同手机上答题。");
        dialog.setTitle("您是否要保存做题记录到服务器?");
        dialog.setPositiveButton("保存");
        dialog.setNegativeButton("暂不");
        dialog.show();
    }

    public void showContentView() {
        findViewById(R.id.doing_homework_content_frame_lay).setVisibility(View.VISIBLE);
        findViewById(R.id.doing_homework_error_lay).setVisibility(View.GONE);
        findViewById(R.id.doing_homework_loading_lay).setVisibility(View.GONE);
    }

    public void showLoadingView() {
        findViewById(R.id.doing_homework_content_frame_lay).setVisibility(View.GONE);
        findViewById(R.id.doing_homework_error_lay).setVisibility(View.GONE);
        findViewById(R.id.doing_homework_loading_lay).setVisibility(View.VISIBLE);
    }

    public void showErrorView() {
        findViewById(R.id.doing_homework_content_frame_lay).setVisibility(View.GONE);
        findViewById(R.id.doing_homework_error_lay).setVisibility(View.VISIBLE);
        findViewById(R.id.doing_homework_loading_lay).setVisibility(View.GONE);
        findViewById(R.id.error_icon).setVisibility(View.VISIBLE);
        findViewById(R.id.error_btn).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.error_tv);
        textView.setText(getText(R.string.network_ungeilivable));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

    public void showErrorView(String msg) {
        findViewById(R.id.doing_homework_content_frame_lay).setVisibility(View.GONE);
        findViewById(R.id.doing_homework_error_lay).setVisibility(View.VISIBLE);
        findViewById(R.id.doing_homework_loading_lay).setVisibility(View.GONE);

        findViewById(R.id.error_icon).setVisibility(View.GONE);
        findViewById(R.id.error_btn).setVisibility(View.GONE);
        TextView textView = (TextView) findViewById(R.id.error_tv);
        textView.setText(msg);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    }

    public void handInHomeworkAction(boolean isHandIn) {
        mHomework.saveHomework();
        mHomework.saveUserAnswer();
        if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
            finish();
            return;
        }
        XKHandInWarmupTask task = new XKHandInWarmupTask(DoingWarmupActivity.this, mHomework.getHomeworkId(), isHandIn) {

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    mHomework = null;
                    finish();
                }
            }

        };
        task.execute();
    }

    public void showHandInHomeworkDialog() {
        if (mHomework == null) return;
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                handInHomeworkAction(true);
            }
        };
        dialog.setContent(mHomework.getDoingProfileString(true));
        dialog.setTitle("确定交卷吗?");
        dialog.setPositiveButton("交卷");
        dialog.setNegativeButton("暂不");
        dialog.show();
    }

    /**
     * 显示做题进度
     */
    public void showProgress() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        XKDoingProgressFragment fragment = new XKDoingProgressFragment();
        fragment.setSubjects(mHomework);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.doing_homework_content_frame_lay, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        changeTitle(true);
    }

    /**
     * 重做题目
     *
     * @param subject
     */
    public void redoSubject(XKSubject subject) {
        getSupportFragmentManager().popBackStack();
        if (subject != null) {
            doingHomeworkFragment.redoSubject(subject);
        }
        changeTitle(false);
    }

    private void changeTitle(boolean isShowAnswerSheet) {
        if (isShowAnswerSheet) {
            rightBtn.setVisibility(View.GONE);
            centerTitleTv.setText(getResources().getString(R.string.xk_answer_sheet));
        } else {
            rightBtn.setVisibility(View.VISIBLE);
            centerTitleTv.setText("");
        }
    }

}
