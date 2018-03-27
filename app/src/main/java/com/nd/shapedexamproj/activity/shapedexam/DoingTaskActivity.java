package com.nd.shapedexamproj.activity.shapedexam;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.xkhomework.XKHandInHomeworkTask;
import com.nd.shapedexamproj.model.xkhomework.XKHomework;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.view.xkhomework.XKDoingProgressFragment;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.PhoneUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 做作业
 * Created by yusongying on 2015/1/19.
 */
public class DoingTaskActivity extends BaseFragmentActivity implements View.OnClickListener, TmingCacheHttp.RequestWithCacheCallBackV2<XKHomework>{

    private final static String TAG = "DoingHomeworkActivity";
    protected XKHomework mHomework;
    protected DoingTaskFragment doingHomeworkFragment;
    protected TextView titleTv,centerTitleTv;
    protected Button rightBtn,negativeBtn,saveBtn,commitBtn;
    protected String workId = "";//形考任务ID
    protected String mTaskName;
    protected String answerId ;//答题记录ID - 非必须，查看或是继续答题的时候需要
    protected boolean isEditable = true;//答题可编辑，查看详情不可编辑
    private boolean isPartEditable;//设置部分题目是否可编辑
    protected int mTaskStatus;//任务的状态 0=未答题 1=继续答题，2=已提交，3=正在评阅，4=已经退回，5=已经评阅
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

        negativeBtn = (Button) findViewById(R.id.task_negative_btn);
        saveBtn = (Button) findViewById(R.id.save_task_btn);
        commitBtn = (Button) findViewById(R.id.commit_task_btn);
        saveBtn.setOnClickListener(this);
        commitBtn.setOnClickListener(this);
        negativeBtn.setOnClickListener(this);

        setData();

    }

    protected void setData() {
        mTaskStatus = getIntent().getIntExtra("status",0);
        mTaskName = getIntent().getStringExtra("taskName");
        answerId = getIntent().getStringExtra("answerId");
        workId = getIntent().getStringExtra("workId");
        titleTv.setText(mTaskName);

        if (mTaskStatus == 1 || mTaskStatus == 0) {
            isEditable = true;
        } else {
            isEditable = false;
        }
        setBtnVisible(isEditable);
        setTaskStatus(mTaskStatus);
    }

    private void setBtnVisible(boolean isEditable) {
        if (!isEditable) {
            saveBtn.setVisibility(View.GONE);
            commitBtn.setVisibility(View.GONE);
            negativeBtn.setVisibility(View.VISIBLE);
        } else {
            saveBtn.setVisibility(View.VISIBLE);
            commitBtn.setVisibility(View.VISIBLE);
            negativeBtn.setVisibility(View.GONE);
        }
    }

    private void setTaskStatus(int status) {
        switch (status) {
            case 2:
                centerTitleTv.setText(getResources().getString(R.string.xk_has_commited));
                negativeBtn.setText(getResources().getString(R.string.xk_cancel_commit));
                break;
            case 3:
                centerTitleTv.setText(getResources().getString(R.string.xk_correcting));
                negativeBtn.setVisibility(View.GONE);
                break;
            case 4:
                centerTitleTv.setText(getResources().getString(R.string.xk_reject));
                negativeBtn.setText(getResources().getString(R.string.xk_reject_detail));
                break;
            case 5:
                centerTitleTv.setText(getResources().getString(R.string.xk_has_correct));
                negativeBtn.setVisibility(View.GONE);
                break;
            default:
                centerTitleTv.setText("");
                negativeBtn.setVisibility(View.GONE);
                break;
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
        //String urlString = ServerApi.XKHomework.getWorkList(App.getUserId(), workId);
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id",App.getsStudentId());
        params.put("task_id",workId);
        if (answerId != null) {
            params.put("answer_id",answerId);
        }
        PhpApiUtil.sendData(Constants.XK_GET_SUBJECTS, params, this);
    }

    @Override
    public XKHomework parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        XKHomework homework = new XKHomework();
        homework.initWithJsonObject(jsonObject);
        return homework;
    }

    @Override
    public void cacheDataRespone(XKHomework data) {
        mHomework = data;
    }

    @Override
    public void requestNewDataRespone(XKHomework cacheRespones, XKHomework newRespones) {
        mHomework = newRespones;
    }

    @Override
    public void exception(Exception exception) {
        exception.printStackTrace();
        if (errorCode != -7 && mHomework == null) {
            if (answerId != null) {
                mHomework = XKHomework.readHomework(answerId);
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
        if ((mTaskStatus == 1 || mTaskStatus == 0) && mHomework.submitTime > 0) {//TODO
            isPartEditable = true;
            doingHomeworkFragment.setPartEditable(true);
        } else {
            isPartEditable = false;
            doingHomeworkFragment.setEditable(isEditable);
        }
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
            case R.id.task_negative_btn:
                if (negativeBtn.getText().equals(getResources().getString(R.string.xk_reject_detail))) {
                    showRejectDetailDialog();
                } else if (negativeBtn.getText().equals(getResources().getString(R.string.xk_cancel_commit))){
                    showCancelCommitDialog();
                }
                break;
        }
    }

    @Override
        public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            changeTitle(false);
            setTaskStatus(mTaskStatus);
            return;
        }
        if (doingHomeworkFragment != null && (mTaskStatus == 1 || mTaskStatus == 0 || isPartEditable)) {//可编辑状态才弹出是否保存对话框
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

    public void showCancelCommitDialog() {
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                cancelCommit();
            }
        };
        dialog.setContent("撤销将只能修改未批阅的主观题");
        dialog.setTitle("确定要撤销重做吗？");
        dialog.setPositiveButton("确定");
        dialog.setNegativeButton("取消");
        dialog.show();
    }

    public void cancelCommit() {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id",App.getsStudentId());
        params.put("answer_id",answerId);
        PhpApiUtil.sendData(Constants.XK_UNDO, params, new TmingHttp.RequestCallback<Integer>() {

            @Override
            public Integer onReqestSuccess(String respones) throws Exception {
                JSONObject jsonObject = new JSONObject(respones);
                return jsonObject.getInt("result");
            }

            @Override
            public void success(Integer respones) {
                if (respones == 0) {
                    Toast.makeText(DoingTaskActivity.this, "撤销失败", Toast.LENGTH_SHORT).show();
                } else if (respones == 1) {
                    //刷新界面
                    /*isEditable = true;
                    mTaskStatus = 1;
                    setBtnVisible(isEditable);
                    setTaskStatus(mTaskStatus);*/
                    finish();
                    Toast.makeText(DoingTaskActivity.this, "撤消成功", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void exception(Exception e) {
                e.printStackTrace();
            }
        });
    }

    boolean isDialogShowing = false;

    class handler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    isDialogShowing = true;
                    break;
                case 0:
                    isDialogShowing = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void showRejectDetailDialog() {
        if (isDialogShowing) {
            return;
        }
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("stu_id", App.getsStudentId());
        if (answerId == null) {
            answerId = mHomework.getHomeworkId();
        }
        params.put("answer_id",answerId);
        TmingHttp.asyncRequest(Constants.XK_REJECT_DETAIL, params, new TmingHttp.RequestCallback<JSONObject>() {
            @Override
            public JSONObject onReqestSuccess(String respones) throws Exception {
                JSONObject jsonObject = new JSONObject(respones);
                return jsonObject;
            }

            @Override
            public void success(JSONObject respones) {
                String reason = "";
                try {
                    reason = respones.getJSONObject("info").getString("reason");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                SimpleAlertDialog dialog = new SimpleAlertDialog(DoingTaskActivity.this) {
                    @Override
                    public void onPositiveButtonClick() {
                        new handler().sendEmptyMessage(0);
                    }
                };
                dialog.setContent(reason);
                dialog.setTitle("退回详情");
                dialog.setPositiveButton("确定");
                dialog.setNegativeButtonGone();
                dialog.setCancelable(false);
                dialog.show();

                new handler().sendEmptyMessage(1);
            }

            @Override
            public void exception(Exception e) {
                e.printStackTrace();
            }
        });

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
            Toast.makeText(this,getResources().getString(R.string.net_error),Toast.LENGTH_SHORT).show();
            return;
        }
        XKHandInHomeworkTask task = new XKHandInHomeworkTask(DoingTaskActivity.this, mHomework.getHomeworkId(), isHandIn) {

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
