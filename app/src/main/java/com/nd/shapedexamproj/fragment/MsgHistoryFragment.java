package com.nd.shapedexamproj.fragment;

import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.notice.NoticeListActivity;
import com.nd.shapedexamproj.activity.remind.MyRemindActivity;
import com.nd.shapedexamproj.db.ChatHistoryMsgDao;
import com.nd.shapedexamproj.db.ChatMsgDao;
import com.nd.shapedexamproj.db.ChatRoomMsgDao;
import com.nd.shapedexamproj.db.ChatUserInfoDao;
import com.nd.shapedexamproj.entity.ChatGroupMsgEntity;
import com.nd.shapedexamproj.entity.ChatHistoryEntity;
import com.nd.shapedexamproj.entity.ChatMsgEntity;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.im.manager.UserManager;
import com.nd.shapedexamproj.im.model.SendGroupMsgModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.TimelinePostTimeAgoUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.util.Utils;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>消息模块聊天记录</p>
 * <p>Created by zll on 2014-11-18</p>
 * <p>Modified by xuwenzhuo on 2014-11-26</p>
 *
 */
public class MsgHistoryFragment extends BaseFragment{


    private static final String TAG = "MsgFragment";
    private static final int FACESIZE = 25;
    // item类型 0-系统公告 1-提醒 2-聊天记录
    private static final int ITEM_TYPE_NOTICE = 0, ITEM_TYPE_REMIND = 1,
            ITEM_TYPE_CHAT = 2;

    private final int HEAD_NOTICE_POSITION = -1, HEAD_REMIND_POSITION = -100;//暂时关闭“我的提醒”，暂时写成-100，之后改为-1、-2
    private final int HEAD_SIZE = 2;// 加入头部的偏移量

    private View msg_fragment;
    private TmingCacheHttp cacheHttp;
    private ImageCacheTool imageCacheTool;
    private RefreshableListView listView;
    private List<MsgItemInfo> msgList;

    private View loadingView,errorLayout;// 网络指示标
    private Button mReconnetBTN;

    private View announcementTypeView;// 类型视图
    private PopupWindow announcementPopupWindow;

    private View msgOfflineHeadView;//没有网络控件提示
    private TextView mOfflineTextView;
    private View noticeHeadView;    // 系统公告
    private View remindHeadView;    // 我的提醒
    private ViewHolder noticeViewHolder;
    private ViewHolder remindViewHolder;

    private MsgItemInfo noticeMsgItemInfo;
    private MsgItemInfo remindMsgItemInfo;

    private int mLoadFlag;
    private MsgListAdapter msgListAdapter;
    private Map<String, PersonalInfo> mapPersonalInfo;

    private IMLoginReceiver loginReceiver;
    private ChatHistoryMsgDao chatHistoryMsgDao;
    private ChatMsgDao chatMsgDao;
    private ChatRoomMsgDao chatRoomMsgDao;
    private ChatUserInfoDao chatUserInfoDao;
    private int mCurrentImState;
    private SharedPreferences spf ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        msg_fragment = inflater
                .inflate(R.layout.msg_history_fragment, container, false);
        announcementTypeView = inflater.inflate(R.layout.msg_line_state_item,
                container, false);
        imageCacheTool = ImageCacheTool.getInstance();
        noticeHeadView = inflater.inflate(R.layout.msg_fragment_item, null);
        remindHeadView = inflater.inflate(R.layout.msg_fragment_item, null);
        msgOfflineHeadView=inflater.inflate(R.layout.msg_offline_head_view,null);
        mOfflineTextView=(TextView) msgOfflineHeadView.findViewById(R.id.msg_offline_tip_tv);
        spf = getActivity().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);

        initView();
        initListener();
        initData();
        return msg_fragment;
    }

    @Override
    public void onResume() {
        if(App.getUserType() != Constants.USER_TYPE_INNER){
            loadChatHistory();
        }
        super.onResume();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        // 网络指示标
        loadingView = msg_fragment.findViewById(R.id.loading_layout);
        errorLayout = msg_fragment.findViewById(R.id.error_layout);
        mReconnetBTN = (Button) errorLayout.findViewById(R.id.error_btn);

        // 设置列表
        listView = (RefreshableListView) msg_fragment.findViewById(R.id.msg_fragment_list_view);
        listView.getRefreshableView().addHeaderView(noticeHeadView);
        //暂时关闭“我的提醒”
        //listView.addHeaderView(remindHeadView);
        //列表下拉刷新
        listView.setonRefreshListener(new OnRefreshListener() {         
            @Override
            public void onRefresh() {
                requestNoticeData();
                requestRemindData();
                loadChatHistory();
            }
            @Override
            public void onLoadMore() {
                listView.onRefreshComplete();
            }
        });
        listView.setFootVisible(false);
        noticeViewHolder = new ViewHolder();
        noticeViewHolder.initView(noticeHeadView);
        remindViewHolder = new ViewHolder();
        remindViewHolder.initView(remindHeadView);
        
        chatHistoryMsgDao = new ChatHistoryMsgDao(getActivity());
        chatMsgDao = ChatMsgDao.getInstance(getActivity());;
        chatRoomMsgDao = ChatRoomMsgDao.getInstance(getActivity());
        chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
        
        loginStateChange(IMConstants.getUserState());
        //添加非游客登录判断
        if(App.getUserType() != Constants.USER_TYPE_INNER){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IMConstants.IM_STATE_ACTION);
            loginReceiver = new IMLoginReceiver();
            getActivity().registerReceiver(loginReceiver, intentFilter);
    
            IntentFilter ifChatSingleMsg = new IntentFilter(IMConstants.ACTION_SINGLE_RECEIVER_SECOND);
            ifChatSingleMsg.setPriority(2147483645);
            getActivity().registerReceiver(singleChatMsgBR, ifChatSingleMsg);
            
            IntentFilter ifChatRoomMsg = new IntentFilter();
            ifChatRoomMsg.addAction(IMConstants.ACTION_ROOM_RECEIVER_SECOND);
            ifChatRoomMsg.setPriority(2147483645);
            getActivity().registerReceiver(roomChatMsgBR, ifChatRoomMsg);
            
            ArrayList<GroupItemInfo> groupList = (ArrayList<GroupItemInfo>) IMConstants.getGroupList();
            for (int i = 0;i < groupList.size();i ++) {
                GroupItemInfo groupItemInfo = groupList.get(i);
                // 获取离线消息
                new SendGroupMsgModel().getOfflineMsg(groupItemInfo.getJid());
            }
        } else{
            //如果是游客，不能刷新和加载更多
            listView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);

        }
    }

    private void initData() {
        msgList = new ArrayList<MsgItemInfo>();

        mapPersonalInfo = new HashMap<String, PersonalInfo>();
        // 实现列表的显示
        msgListAdapter = new MsgListAdapter();
        listView.setAdapter(msgListAdapter);

        noticeMsgItemInfo = new MsgItemInfo();
        remindMsgItemInfo = new MsgItemInfo();

        cacheHttp = TmingCacheHttp.getInstance(App.getAppContext());
        loadingView.setVisibility(View.VISIBLE);
        requestNoticeData();
        requestRemindData();
        //设定默认的IM状态为连接状态
        mCurrentImState=IMConstants.getUserState();
    }

    private void setLoadLayout() {
        mLoadFlag++;
        /*if (mLoadFlag > 0) {
            loadingView.setVisibility(View.VISIBLE);
        }*/
    }

    private void dismissLoadLayout() {
        mLoadFlag--;
        /*if (mLoadFlag <= 0) {
            loadingView.setVisibility(View.GONE);
        }*/
    }

    private int refreshFlag;

    private void addRefreshFlag() {
        refreshFlag++;
    }

    private void reduceRefreshFlag() {
        refreshFlag--;
        if (refreshFlag <= 0) {
            notifyDataSetChanged();
        }
    }

    private void getAllUserInfo() {
        
        boolean isAllRoom = true;
        
        for (int index = 0; index < msgList.size(); index++) {
            MsgItemInfo msgItemInfo = msgList.get(index);
            if (msgItemInfo.getType() == ITEM_TYPE_CHAT
                    && msgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_SINGLE) {
                getUserInfo(msgItemInfo.getOppId());
                isAllRoom = false;
            }

        }
        
        if (isAllRoom) {
            notifyDataSetChanged();
        }
    }

    /**
     * 请求网络数据
     */
    private void requestNoticeData() {
        setLoadLayout();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("uid",App.getUserId());
        PhpApiUtil.sendData(Constants.ANNOUNCEMENT_LAST, map, new RequestCallback<String>() {

            @Override
            public String onReqestSuccess(String respones) throws Exception {
                return respones;
            }

            @Override
            public void success(String respones) {
                loadNoticeData(respones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
                loadingView.setVisibility(View.GONE);
                listView.onRefreshComplete();

                noticeMsgItemInfo.setType(ITEM_TYPE_NOTICE);
                noticeMsgItemInfo.setName(getResources().getString(R.string.notice_list_empty));
                noticeMsgItemInfo.setContent(getResources().getString(R.string.notice_list_empty));
                noticeMsgItemInfo.setTime("");
                noticeViewHolder.loadData(noticeMsgItemInfo);
            }
        });
    }

    private void requestRemindData() {

        remindMsgItemInfo.setType(ITEM_TYPE_REMIND);
        remindMsgItemInfo.setName("我的提醒");
        remindMsgItemInfo.setContent("福建开放大学移动学习平台上线运行");
        remindMsgItemInfo.setTime("2014-07-01 09:15:21");

        remindViewHolder.loadData(remindMsgItemInfo);
    }

    /**
     * 网络请求成功后，加载数据
     * 
     * @param data
     */
    private void loadNoticeData(String data) {
        JSONObject jobj = null;
        JSONObject infoJson = null;
        try {
            try {
                jobj = new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (null != jobj) {
                int code = jobj.getInt("_c");
                if (code == 0) {
                    try {
                        infoJson = jobj.getJSONObject("info");
                    } catch (JSONException e) {
                    }
                    if (infoJson != null) {
                        String title = infoJson.optString("title");
                        String content = infoJson.optString("content");
                        String time = "";
                        if(!infoJson.isNull("time")){
                            time = infoJson.optString("time");
                            time = DateUtils.getDate(time, "yyyy-MM-dd HH:mm:ss");
                        }

                        noticeMsgItemInfo.setType(ITEM_TYPE_NOTICE);
                        noticeMsgItemInfo.setName(title.trim());
                        noticeMsgItemInfo.setContent(content.trim());
                        noticeMsgItemInfo.setTime(time);
                    } else {
                        noticeMsgItemInfo.setType(ITEM_TYPE_NOTICE);
                        noticeMsgItemInfo.setName(getResources().getString(R.string.notice_list_empty));
                        noticeMsgItemInfo.setContent(getResources().getString(R.string.notice_list_empty));
                        noticeMsgItemInfo.setTime("");
                    }

                } else {
                    noticeMsgItemInfo.setType(ITEM_TYPE_NOTICE);
                    noticeMsgItemInfo.setName(getResources().getString(R.string.notice_list_empty));
                    noticeMsgItemInfo.setContent(getResources().getString(R.string.notice_list_empty));
                    noticeMsgItemInfo.setTime("");
                }
                noticeViewHolder.loadData(noticeMsgItemInfo);
            }
            dismissLoadLayout();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            loadingView.setVisibility(View.GONE);
            listView.onRefreshComplete();
        }
    }

    private final class ViewHolder {
        private ImageView msg_fragment_item_photo;
        private TextView msg_fragment_item_name;
        private TextView msg_fragment_item_content;
        private TextView msg_fragment_item_time;

        public void initView(View parent) {
            msg_fragment_item_photo = (ImageView) parent
                    .findViewById(R.id.msg_fragment_item_img_photo);
            msg_fragment_item_name = (TextView) parent
                    .findViewById(R.id.msg_fragment_item_name);
            msg_fragment_item_content = (TextView) parent
                    .findViewById(R.id.msg_fragment_item_content);
            msg_fragment_item_time = (TextView) parent
                    .findViewById(R.id.msg_fragment_item_time);
        }

        public void loadData(final MsgItemInfo msgItemInfo) {
            /**
             * 设置头像图片
             */
            msg_fragment_item_photo.setClickable(false);
            if (msgItemInfo.getType() == ITEM_TYPE_NOTICE) { 
                msg_fragment_item_photo.setImageResource(R.drawable.msg_notice_icon);
                //msg_fragment_item_name.setText(msgItemInfo.getName());
                msg_fragment_item_name.setText(R.string.msg_notice);//直接显示“系统公告”
                //msg_fragment_item_content.setText(msgItemInfo.getContent());
                String content = msgItemInfo.getContent();
                msg_fragment_item_content.setText(content);//内容改显示为公告的标题
                msg_fragment_item_time.setText(TimelinePostTimeAgoUtil
                        .TimelinePostTimeAgo(msgItemInfo.getTime()));

            } else if (msgItemInfo.getType() == ITEM_TYPE_REMIND) {

                msg_fragment_item_photo.setImageResource(R.drawable.msg_reminder_icon);
                //msg_fragment_item_name.setText(msgItemInfo.getName());
                msg_fragment_item_name.setText(R.string.msg_reminder);//直接显示“我的提醒”
                //msg_fragment_item_content.setText(msgItemInfo.getContent());
                String content = msgItemInfo.getName();
                msg_fragment_item_content.setText(content);//内容改显示为提醒的标题
                msg_fragment_item_time.setText(TimelinePostTimeAgoUtil
                        .TimelinePostTimeAgo(msgItemInfo.getTime()));
            } else if (msgItemInfo.getType() == ITEM_TYPE_CHAT) {

                if (msgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_ROOM) {

                    msg_fragment_item_photo.setImageResource(R.drawable.msg_group_icon);
                    String name = msgItemInfo.getName();
                    /*List<GroupItemInfo> groupItemInfos = IMConstants.getGroupList();//废弃
                    if (null != groupItemInfos) {
                        for(GroupItemInfo groupItemInfo:groupItemInfos){
                            if( groupItemInfo.getJid().equals(msgItemInfo.getOppId())) {
                                name = groupItemInfo.getName();
                            }
                        
                        }
                    }*/
                    
                    if (
                            StringUtils.isEmpty(name)) {
                        name = msgItemInfo.getOppId();
                    } else {
                        msgItemInfo.setName(name);
                    }

                    msg_fragment_item_name.setText(name);
                    String content = msgItemInfo.getContent();
                    if (content == null) {
                        msg_fragment_item_content.setText("");
                    } else {
                        int faceSize = (int) msg_fragment_item_content.getTextSize();
                        msg_fragment_item_content.setText(InputBottomBar.parseFaceByText(App.getAppContext(), content, faceSize));
                    }
                    msg_fragment_item_time.setText(TimelinePostTimeAgoUtil
                            .TimelinePostTimeAgo(msgItemInfo.getTime()));
                } else {
                    msg_fragment_item_photo.setClickable(true);
                    msg_fragment_item_photo.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            UIHelper.showFriendInfoActivity(MsgHistoryFragment.this.getActivity(), msgItemInfo.getOppId());
                        }
                    });

                    PersonalInfo personalInfo = getPersonInfo(msgItemInfo.getOppId());

                    String content = msgItemInfo.getContent();
                    if (content == null) {
                        msg_fragment_item_content.setText("");
                    } else {
                        int faceSize = (int) msg_fragment_item_content.getTextSize();
                        msg_fragment_item_content.setText(InputBottomBar.parseFaceByText(App.getAppContext(), content, faceSize));
                    }
                    msg_fragment_item_time.setText(TimelinePostTimeAgoUtil
                            .TimelinePostTimeAgo(msgItemInfo.getTime()));

                    if (null != personalInfo) {
                        msg_fragment_item_name.setText(personalInfo.getUserName());
                        ImageCacheTool.asyncLoadImage(msg_fragment_item_photo,
                                personalInfo.getPhotoUrl(),
                                R.drawable.all_use_icon_photo);
                    } else {
                        msg_fragment_item_name.setText(msgItemInfo.getOppId());
                        ImageCacheTool.asyncLoadImage(msg_fragment_item_photo,
                                "", R.drawable.all_use_icon_photo);
                    }

                }

            }

        }

    }

    private PersonalInfo getPersonInfo(String userId) {
        return mapPersonalInfo.get(userId);
    }

    private void loadChatHistory() {
        msgList.clear();
        
        List<ChatHistoryEntity> chatHistoryEntities = chatHistoryMsgDao.getChatHistoryMsgByUserId(IMConstants.getLoginId(), 0, 20);


        for (int index = 0; index < chatHistoryEntities.size(); index++) {
            
            ChatHistoryEntity chatHistoryEntity = chatHistoryEntities.get(index);
            if (chatHistoryEntity.getChatType() == ChatHistoryEntity.CHAT_TYPE_SINGLE) {
                ChatMsgEntity chatMsgEntity = chatMsgDao.getLastChatMsg(getChatTag(chatHistoryEntity.getOppId()));
                chatHistoryEntity.setContent(chatMsgEntity.getText());
                
            } else if (chatHistoryEntity.getChatType() == ChatHistoryEntity.CHAT_TYPE_ROOM) {
                
                ChatGroupMsgEntity chatGroupMsgEntity = chatRoomMsgDao.getLastChatRoomMsg(getChatTag(chatHistoryEntity.getOppId()));
                chatHistoryEntity.setContent(chatGroupMsgEntity.getText());
            }
            
            MsgItemInfo info = MsgItemInfo.getMsgItemInfo(chatHistoryEntity);
            msgList.add(info);
        }
        
        getAllUserInfo();

    }

    private static class MsgItemInfo {
        String oppId = "";
        int type;
        String name = "";
        String content = "";
        String time = "";
        int chatType;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getOppId() {
            return oppId;
        }

        public void setOppId(String userid) {
            this.oppId = userid;
        }

        public int getChatType() {
            return chatType;
        }

        public void setChatType(int chatType) {
            this.chatType = chatType;
        }

        public static MsgItemInfo getMsgItemInfo(
                ChatHistoryEntity chatHistoryEntity) {
            MsgItemInfo info = new MsgItemInfo();
            info.setType(ITEM_TYPE_CHAT);
            info.setContent(chatHistoryEntity.getContent());
            info.setChatType(chatHistoryEntity.getChatType());
            info.setOppId(chatHistoryEntity.getOppId());
            info.setTime(DateUtils.getDate(chatHistoryEntity.getUpdateTimeStamp()));
            info.setName(chatHistoryEntity.getName());

            return info;
        }
    }

    private class MsgListAdapter extends BaseAdapter {

        private ViewHolder holder;

        @Override
        public int getCount() {
            return msgList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return msgList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup arg2) {
            holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(App
                        .getAppContext());
                convertView = inflater
                        .inflate(R.layout.msg_fragment_item, null);

                holder.msg_fragment_item_photo = (ImageView) convertView
                        .findViewById(R.id.msg_fragment_item_img_photo);
                holder.msg_fragment_item_name = (TextView) convertView
                        .findViewById(R.id.msg_fragment_item_name);
                holder.msg_fragment_item_content = (TextView) convertView
                        .findViewById(R.id.msg_fragment_item_content);
                holder.msg_fragment_item_time = (TextView) convertView
                        .findViewById(R.id.msg_fragment_item_time);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MsgItemInfo msgItemInfo = msgList.get(position);
            if (msgItemInfo != null) {
                holder.loadData(msgList.get(position));
            }
            return convertView;
        }
    }

    /**
     * 点击事件监听
     */
    private void initListener() {

        // 绑定点击事件
        listView.setOnItemClickListener(itemClickListener);

//        common_head_left_btn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 点击通讯录，打开通讯录列表
//                Intent it = new Intent(App.getAppContext(),
//                        CommunicationListActivity.class);
//                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(it);
//            }
//        });
//
//        online_status_title_rl.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                //判断为游客则不添加事件
//                /*if(Constants.USER_TYPE != Constants.USER_TYPE_INNER){
//                    showLineStatusPopupWindow();
//                }*/
//            }
//
//        });
//
//        common_head_right_btn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // UIHelper.showChatGroupActivity(getActivity(),
//                // "1@conference.example1.com", "Friends");
//
//                UIHelper.showContactPersonAddActivity(getActivity());
//
//            }
//        });

        mReconnetBTN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                loginStateChange(IMConstants.STATE_LINKING);
                //启动IM登录
                UserManager.userLogin(getActivity(), true);
            }
        });
    }

    /*
     * 展示上线状态的泡泡窗口
     */
    private void showLineStatusPopupWindow() {
        announcementPopupWindow = new PopupWindow();

        // 设置SelectPicPopupWindow的View
        announcementPopupWindow.setContentView(announcementTypeView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        announcementPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        announcementPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        announcementPopupWindow.setFocusable(true);
        announcementPopupWindow.setOutsideTouchable(true);
        // 刷新状态
        announcementPopupWindow.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        announcementPopupWindow.setBackgroundDrawable(dw);
        // mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        announcementPopupWindow.setAnimationStyle(R.style.PopUpWindowAnimation);

//        announcementPopupWindow.showAsDropDown(online_status_title_rl,
//                online_status_title_rl.getLayoutParams().width / 2, 0);// 需要指定Gravity，默认情况是center.

    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            int currPosition = position - HEAD_SIZE;
            if (currPosition == HEAD_NOTICE_POSITION) {
                // 点击系统公告，打开系统公告列表
                Intent intent = new Intent(App.getAppContext(), NoticeListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getAppContext().startActivity(intent);
            }
            if (mCurrentImState!=IMConstants.STATE_ONLINE && App.getUserType() != Constants.USER_TYPE_INNER){
                //离线状态
                currPosition -=1;
            }

            if (currPosition == HEAD_REMIND_POSITION) {
                // 点击我的提醒，打开提醒界面
                Intent intent = new Intent(App.getAppContext(),
                        MyRemindActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getAppContext().startActivity(intent);

            } else if (currPosition >= 0 ) {
                MsgItemInfo msgItemInfo = msgList.get(currPosition);
                // 点击联系人，打开聊天界面
                if (msgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_SINGLE) {

                    UIHelper.showChatActivity(getActivity(),msgItemInfo.getOppId(),"");

                } else if (msgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_ROOM) {

                    UIHelper.showChatGroupActivity(getActivity(),
                            msgItemInfo.getOppId(), msgItemInfo.getName());
                }

            }
        }
    };

    public class IMLoginReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMConstants.IM_STATE_ACTION)) {
                Bundle bundle = intent.getExtras();
                Log.e(TAG, "getUserState:" + IMConstants.getUserState());
                //loginStateChange(IMConstants.getUserState());TODO 形考屏蔽IM模块
            } 
        }
    }

    /**
     * 用户状态改变
     * @param state
     */
    private void loginStateChange(final int state) {

        try{
            Log.d(TAG, "loginStateChange:" + state);
            if(App.getUserType() == Constants.USER_TYPE_INNER){
                return;
            } else {
                if (mCurrentImState==state){
                    return;
                } else{
                    //根据state状态进行切换
                    switch (state){
                        case IMConstants.STATE_ONLINE:
                            //上线
                            if (mCurrentImState!=IMConstants.STATE_ONLINE) {

                            }
                            removeOffLineHeader();
                            break;
                        case IMConstants.STATE_OFFLINE:
                            //掉线
                            if (mCurrentImState==IMConstants.STATE_ONLINE) {

                            }
                            showTipOnHeadView(getResources().getString(R.string.msg_offline_tip_text));
                            addOfflineHeader();
                            break;
                        case IMConstants.STATE_LINKING:
                            //正在连接
                            if (mCurrentImState==IMConstants.STATE_ONLINE) {

                            }
                            showTipOnHeadView(getResources().getString(R.string.msg_connecting_tip_text));
                            addOfflineHeader();
                            break;
                    }
                    //设定当前状态
                    mCurrentImState=state;
                }
            }
        } catch (Exception exception){

            Log.e(TAG,exception.getMessage());
        }
    }

    private void receiveMsg(MsgItemInfo newMsgItemInfo) {
        
        if (newMsgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_SINGLE) {
            
            ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
            chatHistoryEntity.setChatType(ChatHistoryEntity.CHAT_TYPE_SINGLE);
            
            chatHistoryEntity.setName(newMsgItemInfo.getName());
            chatHistoryEntity.setOppId(newMsgItemInfo.getOppId());
            chatHistoryEntity.setUserId(IMConstants.getLoginId());
            chatHistoryEntity.setUpdateTimeStamp(DateUtils.getTime(newMsgItemInfo.getTime()));
            chatHistoryEntity.setTag(getChatTag(newMsgItemInfo.getOppId()));
            
            chatHistoryMsgDao.replaceChatHistotyMsg(chatHistoryEntity);
            
        } else if (newMsgItemInfo.getChatType() == ChatHistoryEntity.CHAT_TYPE_ROOM) {
            ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
            chatHistoryEntity.setChatType(ChatHistoryEntity.CHAT_TYPE_ROOM);
            
            GroupItemInfo groupItemInfo = IMConstants.findGroupItemInfo(newMsgItemInfo.getOppId());
            if (null != groupItemInfo) {
                if (StringUtils.isEmpty(groupItemInfo.getName())) {//如果群组名称为空则不显示出来
                    return;
                }
                chatHistoryEntity.setName(groupItemInfo.getName());
            } else {
                chatHistoryEntity.setName(newMsgItemInfo.getOppId());
            }
            
            chatHistoryEntity.setOppId(newMsgItemInfo.getOppId());
            chatHistoryEntity.setUserId(IMConstants.getLoginId());
            chatHistoryEntity.setUpdateTimeStamp(DateUtils.getTime(newMsgItemInfo.getTime()));
            chatHistoryEntity.setTag(getChatTag(newMsgItemInfo.getOppId()));
            
            chatHistoryMsgDao.replaceChatHistotyMsg(chatHistoryEntity);
        }
        
        
        loadChatHistory();

    }

    private void receiveMsg(ChatMsgEntity chatMsgEntity) {
        MsgItemInfo newMsgItemInfo = new MsgItemInfo();
        newMsgItemInfo.setChatType(ChatHistoryEntity.CHAT_TYPE_SINGLE);
        newMsgItemInfo.setContent(chatMsgEntity.getText());
        newMsgItemInfo.setTime(chatMsgEntity.getDate());
        newMsgItemInfo.setType(ITEM_TYPE_CHAT);
        newMsgItemInfo.setOppId(chatMsgEntity.getFormId());
        newMsgItemInfo.setName(chatMsgEntity.getFormName());
        receiveMsg(newMsgItemInfo);
    }
    
    private void receiveMsg(ChatGroupMsgEntity chatMsgEntity) {
        MsgItemInfo newMsgItemInfo = new MsgItemInfo();
        newMsgItemInfo.setChatType(ChatHistoryEntity.CHAT_TYPE_ROOM);
        newMsgItemInfo.setContent(chatMsgEntity.getText());
        newMsgItemInfo.setTime(chatMsgEntity.getDate());
        newMsgItemInfo.setType(ITEM_TYPE_CHAT);
        newMsgItemInfo.setOppId(chatMsgEntity.getGroupId());
        newMsgItemInfo.setName(chatMsgEntity.getGroupId());
        receiveMsg(newMsgItemInfo);
    }
    
    private String getChatTag(String oppId) {
        String tag =  IMConstants.getLoginId() +"-"+ oppId;
        return tag;
    }
    
    /**
     * 广播接收者 - 单聊信息
     */
    private BroadcastReceiver singleChatMsgBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String toId = intent.getStringExtra("toId");
            String fromId = intent.getStringExtra("fromId");
            String fromName = intent.getStringExtra("fromName");
            String msg = intent.getStringExtra("msg");
            String date = intent.getStringExtra("date");
            Log.e(TAG, "toId:" + toId + ";fromId:" + fromId + ";fromName" + fromName + ";msg:" + msg
                    + ";date:" + date);
            // if(fromId.equals(getOpId()))
            {
                ChatMsgEntity chatMsgEntity = new ChatMsgEntity();
                chatMsgEntity.setText(msg);
                chatMsgEntity.setDate(date);
                chatMsgEntity.setFormId(fromId);
                chatMsgEntity.setFormName(fromName);
                chatMsgEntity.setToId(toId);
                receiveMsg(chatMsgEntity);
            }
        }

    };

    /**
     * 广播接收者 - 群聊信息
     */
    private BroadcastReceiver roomChatMsgBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMConstants.ACTION_ROOM_RECEIVER_SECOND)) {
                String toId = intent.getStringExtra("toId");
                String from = intent.getStringExtra("from");
                String msg = intent.getStringExtra("msg");
                String date = intent.getStringExtra("date");
                String fromRoomJid = intent.getStringExtra("fromRoomJid");
                String fromUserName = intent.getStringExtra("fromUserName");
                Log.e(TAG, "toId:" + toId + ";from:" + from + "; fromUserName :"
                        + fromUserName + ";msg:" + msg + ";date:" + date);
                ChatGroupMsgEntity chatMsgEntity = new ChatGroupMsgEntity();
                chatMsgEntity.setText(msg);
                chatMsgEntity.setDate(date);
                chatMsgEntity.setGroupId(fromRoomJid);
                chatMsgEntity.setFromUserId(fromUserName);
                chatMsgEntity.setToId(toId);
                receiveMsg(chatMsgEntity);
            } 
        }

    };

    /**
     * 获取用户信息
     */
    private void getUserInfo(String userid) {
        addRefreshFlag();
        // 获取用户信息  如果本地数据库存在该用户信息，则不请求网络
        PersonalInfo mPersonalInfo = chatUserInfoDao.getChatUserInfo(userid);
        if (mPersonalInfo != null) {
            mapPersonalInfo.put(mPersonalInfo.getUserId(), mPersonalInfo);
            reduceRefreshFlag();
            return;
        }
        String url = Constants.GET_USER_URL;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", userid);
        Log.i(TAG, url);
        TmingHttp.asyncRequest(url, params, new RequestCallback<Integer>() {

            @Override
            public Integer onReqestSuccess(String respones) throws Exception {
                Log.i(TAG, respones);
                PersonalInfo personalInfo = null;
                int result = Utils.jsonParsing(respones);
                personalInfo = PersonalInfo.personalInfoJSONPasing(respones);
                mapPersonalInfo.put(personalInfo.getUserId(), personalInfo);

                return result;
            }

            @Override
            public void success(Integer respones) {
                reduceRefreshFlag();
                if (respones != 1) {
                    return;
                }
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
                reduceRefreshFlag();
            }

        });
    }

    private void notifyDataSetChanged() {
        msgListAdapter.notifyDataSetChanged();
    }

    /**
     * 添加没有网络header
     */
    private void addOfflineHeader(){
        //先移除
        //listView.removeView(noticeHeadView);
        listView.getRefreshableView().removeHeaderView(noticeHeadView);
        //再添加
        listView.getRefreshableView().addHeaderView(msgOfflineHeadView);
        listView.getRefreshableView().addHeaderView(noticeHeadView);
    }

    /**
     * 移除没有网络header
     */
    private void removeOffLineHeader(){
        //移除没有网络的header提示
        if(listView.getRefreshableView().findViewById(msgOfflineHeadView.getId())!=null){
            listView.getRefreshableView().removeHeaderView(msgOfflineHeadView);
        }
    }

    /**
     * 显示离线/登录 提示
     * @param tipStr 显示文字
     */
    private void showTipOnHeadView(String tipStr){
        if (null!=mOfflineTextView){
            mOfflineTextView.setText(tipStr);
        }
    }
}
