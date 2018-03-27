package com.nd.shapedexamproj.activity.homework;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.homework.HandInHomeworkTask;
import com.nd.shapedexamproj.model.homework.Homework;
import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.view.homework.DoingHomeworkFragment;
import com.nd.shapedexamproj.view.homework.DoingProgressFragment;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttpRequestWithCacheTask;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import org.json.JSONObject;

/**
 * 做作业
 * Created by yusongying on 2015/1/19.
 */
public class DoingHomeworkActivity extends BaseFragmentActivity implements View.OnClickListener, TmingCacheHttp.RequestWithCacheCallBackV2<Homework>{

    private final static String TAG = "DoingHomeworkActivity";
    protected Homework mHomework;
    protected DoingHomeworkFragment doingHomeworkFragment;
    protected TextView titleTv;
    protected Button rightBtn;
    private String workId = "54bdf1e23f689fe1";
    protected TmingHttpRequestWithCacheTask task = null;
    protected boolean isActivityRunningForeground = false;
    private Object doingClockLock = new Object();
    private int errorCode = 0;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.doing_homework_activity);

        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        findViewById(R.id.error_btn).setOnClickListener(this);
        rightBtn = (Button) findViewById(R.id.common_head_right_btn);
        rightBtn.setOnClickListener(this);
        rightBtn.setText("完成");
        titleTv = (TextView) findViewById(R.id.commonheader_title_tv);
        titleTv.setGravity(Gravity.CENTER);
        titleTv.setPadding(Helper.dip2px(this, 45), 0, 0, 0);

        workId = getIntent().getStringExtra("workId");
        boolean continueDoing = getIntent().getBooleanExtra("continueDoing", true);
        if (!continueDoing) {
            Homework.clearUserAnswer(workId);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null) {
            task.cancel();
            task = null;
        }
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
        if (mHomework == null && task == null) {
            requestData();
        }
        isActivityRunningForeground = true;
        synchronized (doingClockLock) {
            doingClockLock.notify();
        }
    }

    protected void requestData() {
        showLoadingView();
        String urlString = ServerApi.Homework.getWorkList(App.getUserId(), workId);
        task = TmingCacheHttp.getInstance().asyncRequestWithCache(urlString, null, this);
    }

    @Override
    public Homework parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        if (JsonUtil.checkPhpApiALLIsOK(jsonObject)) {
            Homework homework = new Homework();
            homework.setHomeworkId(workId);
            homework.initWithJsonObject(jsonObject.getJSONObject("data"));
            return homework;
        }
        errorCode = jsonObject.getInt("code");
        throw new Exception("service api error!");
    }

    @Override
    public void cacheDataRespone(Homework data) {
        mHomework = data;
    }

    @Override
    public void requestNewDataRespone(Homework cacheRespones, Homework newRespones) {
        mHomework = newRespones;
    }

    @Override
    public void exception(Exception exception) {
        exception.printStackTrace();
        if (errorCode != -7 && mHomework == null) {
            mHomework = Homework.readHomework(workId);
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

            new DoingClockThread().start();
        } else if (errorCode == -7){
            // 作业已经过期
            showErrorView("作业已经过期");
        } else {
            showErrorView();
        }
        task = null;
    }

    protected void createDoingHomeworkFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        doingHomeworkFragment = new DoingHomeworkFragment();
        doingHomeworkFragment.setHomework(mHomework);
        transaction.replace(R.id.doing_homework_content_frame_lay, doingHomeworkFragment);
        transaction.commit();
    }

    private class DoingClockThread extends Thread {

        DoingClockThread() {
            setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            super.run();
            int time = 0;
            while (true) {
                if (isFinishing()) {
                    return;
                }
                if (mHomework != null) {
                    if (mHomework.isOutOfTime()) {
                        if (doingHomeworkFragment != null) {
                            App.getAppHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    doingHomeworkFragment.setEditable(false);
                                    // 显示时间到
                                    showTimeOutDialog();
                                }
                            });
                        }
                        updateRemainingTime();
                        Log.d(TAG, "---------------====== out of time =====--------------");
                        return;// 退出线程
                    }
                    updateRemainingTime();
                    if (time % 10 == 0) { // 每隔10s钟保存一次数据
                        mHomework.saveUserAnswer();
                    }
                }
                if (!isActivityRunningForeground) {
                    synchronized (doingClockLock) {
                        try {
                            doingClockLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                time++;
                SystemClock.sleep(1000);
            }
        }
    }

    private void showTimeOutDialog() {
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                handInHomeworkAction();
            }

            @Override
            public void onNegativeButtonClick() {
                super.onNegativeButtonClick();
                finish();
            }
        };
        dialog.setTitle("答题时间到，无法继续作答");
        dialog.setContent(mHomework.getDoingProfileString(true));
        dialog.setPositiveButton("交卷");
        dialog.setNegativeButton("暂时保存");
        dialog.setCancelable(false);
        dialog.show();
    }

    private void updateRemainingTime() {
        App.getAppHandler().post(new Runnable() {
            @Override
            public void run() {
                titleTv.setText(mHomework.getShowRemainingTime());
            }
        });
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
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    showHandInHomeworkDialog();
                } else {
                    showProgress();
                }
                break;
            case R.id.error_btn:
                requestData();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            rightBtn.setText("完成");
            return;
        }
        if (doingHomeworkFragment != null) {
            pauseDoingHomework();
        } else {
            finish();
        }
    }

    public void pauseDoingHomework() {
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                finish();
            }
        };
        dialog.setContent(mHomework.getDoingProfileString(false) + "\r\n做题记录已经被保存，但不提交。");
        dialog.setTitle("您是否要停止做题?");
        dialog.setPositiveButton("暂停做题");
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

    public void handInHomeworkAction() {
        mHomework.saveHomework();
        mHomework.saveUserAnswer();
        if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
            Toast.makeText(this,getResources().getString(R.string.net_error),Toast.LENGTH_SHORT).show();
            return;
        }
        HandInHomeworkTask task = new HandInHomeworkTask(DoingHomeworkActivity.this, mHomework.getHomeworkId(), true) {

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
        SimpleAlertDialog dialog = new SimpleAlertDialog(this) {
            @Override
            public void onPositiveButtonClick() {
                handInHomeworkAction();
            }
        };
        dialog.setContent(mHomework.getDoingProfileString(true));
        dialog.setTitle("确定交卷?");
        dialog.setPositiveButton("交卷");
        dialog.setNegativeButton("暂不");
        dialog.show();
    }

    /**
     * 显示做题进度
     */
    public void showProgress() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        DoingProgressFragment fragment = new DoingProgressFragment();
        fragment.setSubjects(mHomework);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.doing_homework_content_frame_lay, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        rightBtn.setText("交卷");
    }

    /**
     * 重做题目
     *
     * @param subject
     */
    public void redoSubject(Subject subject) {
        getSupportFragmentManager().popBackStack();
        if (subject != null) {
            doingHomeworkFragment.redoSubject(subject);
        }
        rightBtn.setText("完成");
    }
}
