package com.nd.shapedexamproj.activity.im;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.entity.GroupItemInfo;
import com.nd.shapedexamproj.im.model.RoomModel;
import com.nd.shapedexamproj.im.model.RosterModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: GroupListActivity
 * @Title:
 * @Description:IM通讯录界面-群组列表
 * @Author:WuYuLong
 * @Since:2014-4-29下午10:16:44
 * @Version:1.0
 */
public class GroupListActivity extends BaseActivity {
	private final String TAG = "GroupListActivity";
	
	private RosterModel rosterModel = new RosterModel();
	private View loadingView;//网络指示标
	private Context context;
	private RelativeLayout common_head_layout;
	private Button common_head_right_btn;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv, noDataTv;
	private ListView listView;
	private View listHeaderView;
	private List<GroupItemInfo> groupList;
	private GroupAdapter groupAdapter;
	
	@Override
	public int initResource() {
		return R.layout.msg_group_list_activity;
	}
	
	@Override
	public void initComponent() {
		// 设置头部透明色
		common_head_layout = (RelativeLayout) findViewById(R.id.group_list_head);
		common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
		// 头部按钮
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText(R.string.msg_group);
		commonheader_title_tv.setText(" " + commonheader_title_tv.getText());
		noDataTv = (TextView) findViewById(R.id.no_data_tv);
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(R.string.msg_group);
//		common_head_left_btn.setText(" " + common_head_left_btn.getText());
		common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
		common_head_right_btn.setVisibility(View.GONE);
		
		//网络指示标
		loadingView = findViewById(R.id.loading_layout);
		
		// 上下文
		context = this;
		
		// 设置列表
		listView = (ListView) findViewById(R.id.msg_group_list_view);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(IMConstants.ACTION_ROOM_LIST);
		registerReceiver(receiver, filter);
		
	}
	private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(IMConstants.ACTION_ROOM_LIST)) {//获取群组列表
                loadingView.setVisibility(View.GONE);
                List<GroupItemInfo> rosterList = (List<GroupItemInfo>) intent.getSerializableExtra("group_list");
                if(rosterList != null){
                    GroupItemInfo groupItemInfo ;
                    
                    int size = rosterList.size();
                    for (int i = 0; i < size; i++) {
                        groupItemInfo = new GroupItemInfo();
                        groupItemInfo.setName(rosterList.get(i).getName());
                        groupItemInfo.setJid(rosterList.get(i).getJid());
                        if (!groupList.contains(groupItemInfo)) {
                            groupList.add(groupItemInfo);
                        }
                    }
                    
                    if (size > 0) {
                        noDataTv.setVisibility(View.GONE);
                        IMConstants.getGroupList().clear();
                        IMConstants.getGroupList().addAll(groupList);
                        groupAdapter.notifyDataSetChanged();
                    } else {
                        noDataTv.setVisibility(View.VISIBLE);
                    }
                }
                
            }
        }
        
    };
	
//	private class GroupAsyncTask extends AsyncTask<PersonSearchResult, Void, Integer> {
//		
//		@Override
//		protected void onPreExecute() {
//			//开启网络指示标
//			loadingView.setVisibility(View.VISIBLE);
//			super.onPreExecute();
//		}
//		
//		@Override
//		protected Integer doInBackground(PersonSearchResult... arg0) {
//			groupList = new ArrayList<GroupItemInfo>();
//			GroupItemInfo groupItemInfo ;
//
//			try{
//				List<HostedRoom> rosterList = rosterModel.getHostRooms(context, IMConstants.roomServerName);
//				if(rosterList != null){
//					int size = rosterList.size();
//					for (int i = 0; i < size; i++) {
//						groupItemInfo = new GroupItemInfo();
//						groupItemInfo.setName(rosterList.get(i).getName());
//						groupItemInfo.setJid(rosterList.get(i).getJid());
//						groupList.add(groupItemInfo);
//					}
//				}
//			} catch (Exception e){
//				Log.e(TAG, e.getMessage());
//			}
//			return null;
//		}
//		
//		@Override
//		protected void onPostExecute(Integer result) {
//			//通知列表更新
//			if(groupList != null){
//				groupAdapter.notifyDataSetChanged();
//			}
//		    
//		    //关闭网络指示标
//		    loadingView.setVisibility(View.GONE);
//		}
//	}
	
	@Override
	public void initData() {
		// 实现列表的显示
	    groupList = new ArrayList<GroupItemInfo>();
		groupAdapter = new GroupAdapter();
		listView.setAdapter(groupAdapter);
		
		if (IMConstants.getGroupList().size() == 0) {
		    //登录聊天室，并获取群列表
            RoomModel roomModel = new RoomModel();
            roomModel.sendJoinRoomRequest();
        } else {
            
            groupList.addAll(IMConstants.getGroupList());
            groupAdapter.notifyDataSetChanged();
            if (groupList.size() > 0) {
                loadingView.setVisibility(View.GONE);
            }
        }
		
	}

	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// backMain();
				finish();
			}
		});
		
		common_head_right_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UIHelper.showContactPersonAddActivity(context);
				
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
				//点击列表，跳转到聊天界面
				ListView listView = (ListView) parent;
				GroupItemInfo data = (GroupItemInfo) groupList.get(position);
	            String name = data.getName();
	            String jid = data.getJid();
	            UIHelper.showChatGroupActivity(GroupListActivity.this, jid, name);
			}
		});
	}

	private final class ViewHolder {
		private ImageView msg_group_item_img_photo;
		private TextView msg_group_item_name;
	}

	private class GroupAdapter extends BaseAdapter {
		private ViewHolder holder;

		@Override
		public int getCount() {
			int size = 0;
			try{
				size = groupList.size();
			} catch (Exception e){
				Log.e(TAG, "groupList = " + groupList);
			}
			return size;
		}

		@Override
		public Object getItem(int arg0) {
			return groupList.get(arg0);
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
				LayoutInflater inflater = LayoutInflater.from(GroupListActivity.this);
				convertView = inflater.inflate(R.layout.msg_group_item, null);

				holder.msg_group_item_img_photo = (ImageView) convertView.findViewById(R.id.msg_group_item_img_photo);
				holder.msg_group_item_name = (TextView) convertView.findViewById(R.id.msg_group_item_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.msg_group_item_img_photo.setImageResource(R.drawable.msg_group_icon);
			holder.msg_group_item_name.setText(groupList.get(position).getName());


			return convertView;
		}

	}

	/**
	 * 返回到主界面
	 */
	private void backMain() {
		Intent intent = new Intent();
		intent.setAction("backToMain");
		Helper.sendLocalBroadCast(this, intent);
	}
	
	@Override
	protected void onDestroy() {
	    unregisterReceiver(receiver);
	    super.onDestroy();
	}
}
