package com.nd.shapedexamproj;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.nd.shapedexamproj.activity.SettingActivity;
import com.nd.shapedexamproj.im.manager.IMManager;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.model.listener.ChatMsgRecBroadcastReceiver;
import com.nd.shapedexamproj.im.model.listener.ChatRoomMsgRecBroadcastReceiver;
import com.nd.shapedexamproj.im.model.listener.RoomUserInfoBroadcastReceiver;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.HttpUtil;
import com.nd.shapedexamproj.util.SharedPreferUtils;
import com.tming.common.CommonApp;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

//import org.videolan.vlc.VLCApplication;

public class App extends CommonApp {

    private final static String LOG_TAG = "OpenUniversityApp";
    /**
     * 服务器当前时间
     */
    //private static long currentTime = 0;
    private ChatMsgRecBroadcastReceiver myBroadcastReceiver;
    private ChatRoomMsgRecBroadcastReceiver chatRoomMsgRecBroadcastReceiver;
    private RoomUserInfoBroadcastReceiver roomUserInfoBroadcastReceiver;
    private static int wifiNotificationId = 1;
    private static String sUserId = "";//用户id,临时文件夹中存放int类型
    private static String sStudentId = "";//学生id
    private static String sTokenId = "";//用户登录获取的令牌

    private static int sUserType = -1;//用户类型（整数）：0 =教师，1=学生
    public static String sTermId = "";//学期专业id
    public static String sClassId = "";// 班级ID
    public static String sTeachingPointId = "";// 教学点ID
    public static int sTeacherClassesNum = 0;//老师负责的班级数量

    /**
     *消息模块
     */
    //用户关注操作标签,用于在IM 通讯录的刷新操作
    public static int mRelatedUserStateFlag= Constants.USER_RELATEDSTAT_NOTCHANGE;
    @Override
    public void onCreate() {
        super.onCreate();
       
        HttpUtil.init(getAppHandler());
        /* UmengUpdateAgent.update(this); *///友盟更新功能
        /*VLCApplication.initVLCApp(this);*///VLC视频
        //Vitamio.initialize(this, R.raw.libarm);
        MobclickAgent.openActivityDurationTrack(false);//禁止默认的页面统计方式，这样将不会再自动统计Activity
        MobclickAgent.updateOnlineConfig(this);
        App.onNetStateChange();
        /*Log.e(LOG_TAG, Utils.getDeviceInfo(getAppContext()));*/
        myBroadcastReceiver = new ChatMsgRecBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(IMConstants.ACTION_SINGLE_RECEIVER_NAME);
        registerReceiver(myBroadcastReceiver, intentFilter);
        chatRoomMsgRecBroadcastReceiver = new ChatRoomMsgRecBroadcastReceiver();
        IntentFilter intentFilterRoom = new IntentFilter(IMConstants.ACTION_ROOM_RECEIVER_NAME);
        intentFilterRoom.setPriority(2147483646);
        registerReceiver(chatRoomMsgRecBroadcastReceiver, intentFilterRoom);
        roomUserInfoBroadcastReceiver = new RoomUserInfoBroadcastReceiver();
        IntentFilter intentFilterRoomUserInfo = new IntentFilter(IMConstants.ACTION_ROOM_USERINFO_NAME);
        intentFilterRoomUserInfo.setPriority(2147483646);
        registerReceiver(roomUserInfoBroadcastReceiver, intentFilterRoomUserInfo);
        // 网络变化监听
        IntentFilter netIntentFilter = new IntentFilter();
        netIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netChangeBroadcastReceiver, netIntentFilter);
    }
    
    private BroadcastReceiver netChangeBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                //onNetStateChange();
            }
        }
    };

    public static void onNetStateChange() {
        Log.d(LOG_TAG, "onNetStateChange");
        ConnectivityManager connectivityManager = (ConnectivityManager) getAppContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return;
        }
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            if (info.getType() != ConnectivityManager.TYPE_WIFI) {
                // 是否在非wifi网络下开启提醒
                boolean notice = SharedPreferUtils.getBoolean(getAppContext(), Constants.IS_NOTIFY_WITHOUT_WIFI);
                Log.d(LOG_TAG, "notice:" + notice);
                if (notice) {
                    // 生成一个通知
                    createWifiNotification();
                }
            } else {
                // 清除通知
                cleanWifiNotification();
            }
            Log.e(LOG_TAG, "onNetStateChange：" + name);
        } else {
            if (info == null) {
                // 是否在非wifi网络下开启提醒
                boolean notice = SharedPreferUtils.getBoolean(getAppContext(), Constants.IS_NOTIFY_WITHOUT_WIFI);
                Log.e(LOG_TAG, "info == null notice:" + notice);
                if (notice) {
                    // 生成一个通知
                    createWifiNotification();
                } else {
                    // 清除通知
                    cleanWifiNotification();
                }
            }
        }
        if (null != IMManager.getConnection()) {
            // 网络状态改变 判断im时候可用
            if (IMManager.getConnection().isConnected()) {
                Log.d(LOG_TAG, "IM is Connected");
            } else {
                Log.d(LOG_TAG, "IM is unConnected");
                IMManager.getConnection().disconnect();
                IMConstants.setUserState(IMConstants.STATE_OFFLINE);
                Intent intent = new Intent();
                intent.setAction(IMConstants.IM_STATE_ACTION);
                getAppContext().sendBroadcast(intent);
                if (info != null && info.isAvailable()) {
                    // 网络仍然可用 重新连接
                    UserManager.userLogin(getAppContext(), true);
                }
            }
        }
    }

    private static void createWifiNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getAppContext())
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getAppContext().getResources().getString(R.string.no_wifi_notification_title_text))
                .setContentText("");
        Intent notificationIntent = new Intent(getAppContext(), SettingActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getAppContext(), 1, notificationIntent, 0);
        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(wifiNotificationId, builder.build());
    }

    public static void cleanWifiNotification() {
        NotificationManager nm = (NotificationManager) getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(wifiNotificationId);
    }

    public static String createDownloadPath() {
        String download_path = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断SD卡是否存在
        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();// 获取根目录
            download_path = sdDir.getPath() + "/com.nd.hy.android.app.xk" + "/download/" + Helper.getMD5String(getUserId()) + "/";
            Log.e(LOG_TAG, download_path);
            File file = new File(download_path);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            getAppHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.download_no_sdcard), Toast.LENGTH_SHORT);
                }
            });
        }
        return download_path;
    }

    /**
     * 记录异常
     * 
     * @param flag
     */
    public static void dealWithFlag(final int flag) {
        getAppHandler().post(new Runnable() {

            @Override
            public void run() {
                if (flag == -1) {
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.param_error),
                            Toast.LENGTH_SHORT).show();
                    Log.e("flag", "参数错误");
                } else if (flag == -2) {
                    Toast.makeText(getAppContext(),
                            getAppContext().getResources().getString(R.string.record_not_exist), Toast.LENGTH_SHORT)
                            .show();
                    Log.e("flag", "记录不存在");
                } else if (flag == -3) {
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.db_operation),
                            Toast.LENGTH_SHORT).show();
                    Log.e("flag", "数据库操作");
                } else if (flag == -4) {
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.system_error),
                            Toast.LENGTH_SHORT).show();
                    Log.e("flag", "系统错误");
                } else if (flag == -5) {
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.api_error),
                            Toast.LENGTH_SHORT).show();
                    Log.e("flag", "远程接口出错");
                } else if (flag == 0) {
                    Log.e("flag", "网络异常");
                    Toast.makeText(getAppContext(), getAppContext().getResources().getString(R.string.net_error),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
                  
    /**
     * 获取用户ID
     * @return
     */
    public static String getUserId() {
        if (sUserId.equals("")) {
            SharedPreferences spf = getAppContext().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sUserId = spf.getString("userId","");
        }
        return sUserId;
    }

    public static void setsUserId(String sUserId) {
        App.sUserId = sUserId;
    }

    public static int getUserType() {
        if (sUserType == -1) {
            sUserType = SharedPreferUtils.getInt(getAppContext(), "userType", -1);
        }
        return sUserType;
    }

    public static void setsUserType(int sUserType) {
        App.sUserType = sUserType;
    }

    /**
     * 清除UserId
     */
    public static void clearUserId() {
        sUserId = "";
        sStudentId = "";
    }
    public static String getsTokenId() {
        if (sTokenId.equals("")) {
            SharedPreferences spf = getAppContext().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sTokenId = spf.getString("tokenId", "");
        }
        return sTokenId;
    }

    public static void setsTokenId(String sTokenId) {
        App.sTokenId = sTokenId;
    }

    public static String getsStudentId() {
        if (sStudentId.equals("")) {
            SharedPreferences spf = getAppContext().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
            sStudentId = spf.getString("stuId", "");
        }
        return sStudentId;
    }

    public static void setsStudentId(String sStudentId) {
        App.sStudentId = sStudentId;
    }
}