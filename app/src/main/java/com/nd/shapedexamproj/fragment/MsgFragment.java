package com.nd.shapedexamproj.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.ScrollDisableViewPager;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息页面
 * 
 * @author wyl create in 2014.04.08
 */
public class MsgFragment extends BaseFragment {

    private static final String TAG = "MsgFragment";
    private static int MESSAGE = 1;
    private static int CONTACTS = 2;
    private View msg_fragment,online_status_title_rl;
    private RelativeLayout common_head_layout;
    private Button common_head_left_btn, common_head_right_btn, common_head_login_btn;
    private TextView common_head_center_tv;
    private Button mHeadMsgBtn, mHeadContactsBtn;
    private ScrollDisableViewPager msgViewPager;
    private PagerAdapter pagerAdapter;
    private FragmentManager fragmentManager;
    private List<Fragment> fragmentList;
    private MsgConstactsFragment msgContactsFragment;
    private MsgHistoryFragment msgHistoryFragment;
    private IMLoginReceiver loginReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        msg_fragment = inflater.inflate(R.layout.msg_fragment, container, false);
        initView();
        initListener();
        initData();
        initAuthoriy();
        initViewPager();
        return msg_fragment;
    }

    private void initData() {}

    /**
     * 初始化视图
     */
    private void initView() {
        // 设置头部透明色
        common_head_layout = (RelativeLayout) msg_fragment.findViewById(R.id.msg_fragment_head);
        common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
        online_status_title_rl = (View) msg_fragment.findViewById(R.id.online_status_title_rl);
        common_head_left_btn = (Button) msg_fragment.findViewById(R.id.common_head_left_btn);
        common_head_right_btn = (Button) msg_fragment.findViewById(R.id.common_head_right_btn);
        common_head_login_btn = (Button) msg_fragment.findViewById(R.id.common_head_login_btn);
        common_head_center_tv = (TextView) msg_fragment.findViewById(R.id.common_head_center_tv);
        mHeadMsgBtn = (Button) msg_fragment.findViewById(R.id.common_head_msg_btn);
        mHeadContactsBtn = (Button) msg_fragment.findViewById(R.id.common_head_contacts_btn);
        msgViewPager = (ScrollDisableViewPager) msg_fragment.findViewById(R.id.msg_fragment_viewpager);
        fragmentList = new ArrayList<Fragment>();
        fragmentManager = getActivity().getSupportFragmentManager();
        pagerAdapter = new PagerAdapter(fragmentManager, fragmentList);
        msgViewPager.setAdapter(pagerAdapter);
        msgViewPager.setOffscreenPageLimit(2);
    }

    private void initListener() {
        common_head_left_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击通讯录，打开通讯录列表
                /*Intent it = new Intent(App.getAppContext(), CommunicationListActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(it);*/
            }
        });
        common_head_right_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //UIHelper.showContactPersonAddActivity(getActivity());
                //UIHelper.showNewFriendsActivity(getActivity());
                UIHelper.showPersonSearchActivity(getActivity());
            }
        });
        mHeadMsgBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                msgViewPager.setCurrentItem(0, false);
                setTitleStyle(MESSAGE);
            }
        });
        mHeadContactsBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                msgViewPager.setCurrentItem(1, false);
                setTitleStyle(CONTACTS);
            }
        });
    }

    /**
     * 
     * @Title: setAuthority
     * @Description: 配置权限信息
     * @Since: 2014-5-19下午4:48:49
     */
    public void initAuthoriy() {
        if (App.getUserType() == Constants.USER_TYPE_INNER) {
            common_head_left_btn.setVisibility(View.GONE);
            common_head_right_btn.setVisibility(View.GONE);
            common_head_center_tv.setVisibility(View.VISIBLE);
            online_status_title_rl.setVisibility(View.GONE);
            common_head_login_btn.setVisibility(View.VISIBLE);
            common_head_login_btn.setText(R.string.login);
            common_head_login_btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    UIHelper.showLogin(getBaseActivity());
                }
            });
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(IMConstants.IM_STATE_ACTION);
            loginReceiver = new IMLoginReceiver();
            getActivity().registerReceiver(loginReceiver, intentFilter);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
    }
    
    private void setTitleStyle(int style) {
        if (style == MESSAGE) {
            mHeadMsgBtn.setBackgroundResource(R.drawable.msg_msgbtn_on);
            mHeadMsgBtn.setTextColor(getResources().getColor(R.color.title_green));
            mHeadContactsBtn.setBackgroundResource(R.drawable.msg_contactbtn);
            mHeadContactsBtn.setTextColor(getResources().getColor(R.color.msg_head_contact_text));
        } else if (style == CONTACTS) {
            mHeadMsgBtn.setBackgroundResource(R.drawable.msg_msgbtn);
            mHeadMsgBtn.setTextColor(getResources().getColor(R.color.msg_head_contact_text));
            mHeadContactsBtn.setBackgroundResource(R.drawable.msg_contactbtn_on);
            mHeadContactsBtn.setTextColor(getResources().getColor(R.color.title_green));
        }
    }
    
    public class IMLoginReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMConstants.IM_STATE_ACTION)) {
                Bundle bundle = intent.getExtras();
                Log.e(TAG, "getUserState:" + IMConstants.getUserState());
                loginStateChange(IMConstants.getUserState());
            }
        }
    }

    private void loginStateChange(final int state) {
        Log.d(TAG, "loginStateChange:" + state);
        if (App.getUserType() == Constants.USER_TYPE_INNER) {
            // 游客登录时，隐藏下拉箭头，连接状态改为显示“消息”
            // online_status_title_tv.setText(R.string.msg_head);
            common_head_left_btn.setVisibility(View.GONE);
            common_head_right_btn.setVisibility(View.GONE);
        } else {
            // 设置登录状态
            if (state == IMConstants.STATE_ONLINE) {
                // 在线
                // online_status_title_tv.setText(R.string.msg_online);
            } else if (state == IMConstants.STATE_OFFLINE) {
                // 离线
                // online_status_title_tv.setText(R.string.msg_offline);
            } else if (state == IMConstants.STATE_LINKING) {
                // 连接中
                // online_status_title_tv.setText(R.string.msg_linking);
            }
        }
    }

    private void initViewPager() {
        msgHistoryFragment = new MsgHistoryFragment();
        fragmentList.add(msgHistoryFragment);
        pagerAdapter.notifyDataSetChanged();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {

        List<Fragment> fragmentList;
        public PagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    /**
     * <p>更新用户通讯录界面</p>
     */
    public void refreshContactPage(){
        if (msgContactsFragment!=null && App.mRelatedUserStateFlag==Constants.USER_RELATEDSTAT_CHANGED){
            msgContactsFragment.refreshConstacts();
        }
        if (msgContactsFragment != null) {
            msgContactsFragment.getNewFriendsNum();
        }
    }
}
