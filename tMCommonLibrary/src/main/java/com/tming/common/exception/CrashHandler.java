package com.tming.common.exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.text.AndroidCharacter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.tming.common.AppManager;
import com.tming.common.R;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.Constants;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;

/**
 * <p>UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告。</p>
 *
 * <ul>
 *     <li> 默认 {@link com.tming.common.CommonApp#iniCrashHandler(android.content.Context)} 会进行初始化,并发送崩溃日志到默认服务器。</li>
 *     <li>如果需要自定义日志发送可以调用 {@link #startSendCrashReportTask(SendCrashReportTask)}</li>
 *     <li>如果存在 android.permission.SYSTEM_ALERT_WINDOW 权限，将弹出窗口进行提示用户重启或者退出</li>
 *     <li>自定义弹出对话框 {@link #setCrashDialogLayoutResourceId(int)} 进行设定
 * </ul>
 *
 */
public final class CrashHandler implements UncaughtExceptionHandler {

    /**
     * Debug Log tag
     */
    private static final String TAG = "CrashHandler";
    private static final String VERSION_NAME = "versionName";
    private static final String VERSION_CODE = "versionCode";
    /**
     * 错误报告文件的扩展名
     */
    private static final String CRASH_REPORTER_EXTENSION = ".cr";
    /**
     * CrashHandler实例
     */
    private static CrashHandler sInstance = new CrashHandler();
    /**
     * 程序的Context对象
     */
    private Context mContext;
    /**
     * 使用Properties来保存设备的信息和错误堆栈信息
     */
    private Properties mDeviceCrashInfo = new Properties();
    
    /**
     * 发送崩溃日志任务
     */
    private SendCrashReportTask mSendCrashReportTask = null;

    /**
     * 崩溃对话框资源布局ID
     */
    private int mCrashDialogLayoutId = 0;
    
    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {}

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return sInstance;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     * 
     * @param ctx
     */
    public void init(Context ctx) {
        mContext = ctx;
        Thread.setDefaultUncaughtExceptionHandler(this);
        mCrashDialogLayoutId = R.layout.common_crash_dialog;
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(TAG, ex.getClass().getName(), ex);
        handleException(thread, ex);
        new Thread() {
            public void run() {
                Looper.prepare();
                Log.d(TAG, "app has crash. open crash dialog!");
                openCrashDialog();
                Looper.loop();
            }
        }.start();
    }
    
    /**
     * 
     * <p>退出应用</P>
     *
     */
    private void exitApplication() {
        AppManager.getAppManager().finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    /**
     * 
     * <p>重启应用</P>
     *
     */
    private void restartApplication() {
        Intent it = PhoneUtil.getStartAppIntent(mContext, mContext.getPackageName());
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(it);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    /**
     * 
     * <p>打开崩溃对话框</P>
     *
     */
    private void openCrashDialog() {
        if (!PhoneUtil.checkPermissions(mContext, "android.permission.SYSTEM_ALERT_WINDOW")) {
            Log.e(TAG, "no permission:android.permission.SYSTEM_ALERT_WINDOW exit application.");
            exitApplication();
        } else {
            Log.d(TAG, "app has crash. open crash dialog!");
        }
        AlertDialog.Builder builder = new Builder(mContext);
        View view = View.inflate(mContext, mCrashDialogLayoutId, null);
        // 初始化Dialog中的控件
        Button exitBtn = (Button) view.findViewById(R.id.common_crash_dialog_exit_btn);
        Button restartBtn = (Button) view.findViewById(R.id.common_crash_dialog_restart_btn);
        builder.setView(view);// 设置自定义布局view
        final Dialog dialog = builder.create();
        exitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                exitApplication();
            }
        });

        restartBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                restartApplication();
            }
        });
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 
     * <p>自定义崩溃对话框 </P>
     * <ul>
     *  <li>使用崩溃对话框需要权限{@link android.permission.SYSTEM_ALERT_WINDOW}</li>
     *  <li>自定义布局文件需要包含两个Button id为{@link R.id.common_crash_dialog_exit_btn}, {@link R.id.common_crash_dialog_restart_btn}</li>
     * </ul>
     * 
     * @param layoutResourceId 布局资源ID
     */
    public void setCrashDialogLayoutResourceId(int layoutResourceId) {
        this.mCrashDialogLayoutId = layoutResourceId;
    }
    
    /**
     * 收集错误信息
     * 
     * @param thread 发生异常的线程
     * @param ex 异常
     */
    private void handleException(Thread thread, Throwable ex) {
        if (ex == null) {
            Log.w(TAG, "handleException --- ex==null");
        }
        // 收集设备信息
        collectCrashDeviceInfo();
        // 保存错误报告文件
        saveCrashInfoToFile(thread, ex);
    }

    /**
     * 把错误报告发送给服务器,包含新产生的和以前没发送的.
     */
    private void sendCrashReportsToServer() {
        File[] crFiles = getCrashReportFiles();
        if (crFiles != null && crFiles.length > 0) {
            for (File crFile : crFiles) {
                if (mSendCrashReportTask != null && mSendCrashReportTask.sendCrashFile(crFile)) {
                    crFile.delete();// 删除已发送的报告
                }
            }
        }
    }

    /**
     * 获取错误报告文件名
     * @return 崩溃日志文件
     */
    private File[] getCrashReportFiles() {
        File filesDir = getCrashReportFileDirectoryPath();
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(CRASH_REPORTER_EXTENSION);
            }
        };
        return filesDir.listFiles(filter);
    }

    /**
     * 保存错误信息到文件中
     * 
     * @param ex 异常
     * @param thread 线程
     */
    private void saveCrashInfoToFile(Thread thread, Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String stackInfo = info.toString();
        printWriter.close();
        mDeviceCrashInfo.put("EXEPTION", ex.getClass().getName());
        mDeviceCrashInfo.put("THREAD", thread.getName());
        FileOutputStream fos = null;
        try {
            String fileName = "crash-" + (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                    + CRASH_REPORTER_EXTENSION;
            File outputFile = new File(getCrashReportFileDirectoryPath(), fileName);
            fos = new FileOutputStream(outputFile);
            mDeviceCrashInfo.store(fos, "");
            // 输出错误栈信息
            fos.write("\r\n\r\n".getBytes("UTF-8"));
            fos.write(stackInfo.getBytes("UTF-8"));
            fos.flush();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing report file...", e);
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * <p>获取崩溃日志存储目录</P>
     *
     * @return 崩溃日志存储目录
     */
    private File getCrashReportFileDirectoryPath() {
        String SDPath = PhoneUtil.getSDPath();
        File crashDir;
        if (SDPath != null) {
            crashDir = new File(SDPath + "/data/" + mContext.getPackageName() + "/crashInfo/");
        } else {
            crashDir = mContext.getFilesDir();
        }
        if (!crashDir.exists()) {
            crashDir.mkdirs();
        }
        return crashDir;
    }

    /**
     * 收集程序崩溃的设备信息
     */
    private void collectCrashDeviceInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                mDeviceCrashInfo.put(VERSION_NAME, pi.versionName == null ? "not set" : pi.versionName);
                mDeviceCrashInfo.put(VERSION_CODE, "" + pi.versionCode);
                mDeviceCrashInfo.put("PackageName", mContext.getPackageName());
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
        }
        // 使用反射来收集设备信息.在Build类中包含各种设备信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                mDeviceCrashInfo.put(field.getName(), field.get(null).toString());
                if (Constants.DEBUG) {
                    Log.d(TAG, field.getName() + " : " + field.get(null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
    }
    
    /**
     * <p>发送异常任务接口</p>
     * <p>Created by yusongying on 2014年7月25日</p>
     */
    public static interface SendCrashReportTask {
        
        /**
         *<p>发送错误报告到服务器</P>
         * @param file 崩溃的日志文件
         * @return 是否发送成功
         */
        public boolean sendCrashFile(File file);
    }
    
    /**
     * <p>开始发送错误任务日志，该方法会立即触发一个低优先级线程进行异步发送</P>
     * @param sendCrashReportTask 发送错误任务
     * @see {@link #startSendCrashReportToDefault()}
     */
    public void startSendCrashReportTask(SendCrashReportTask sendCrashReportTask) {
        mSendCrashReportTask = sendCrashReportTask;
        Thread sendThread = new Thread() {
            public void run() {
                super.run();
                sendCrashReportsToServer();
            }
        };
        sendThread.setPriority(Thread.MIN_PRIORITY);
        sendThread.start();
    }
    
    /**
     * 发送崩溃日志到默认服务端
     * 
     * @see {@link #startSendCrashReportTask(SendCrashReportTask)}
     */
    public void startSendCrashReportToDefault() {
        startSendCrashReportTask(new SendCrashReportTask() {
            
            @Override
            public boolean sendCrashFile(File file) {
                TmingResponse response = TmingHttp.syncSendFile("http://clientlog.tming.net/log/upload.do", "logfile", file);
                if (response != null && response.getStatusCode() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.asString());
                        int flag = jsonObject.getInt("flag");
                        return flag == 1;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (TmingHttpException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
    }
}
