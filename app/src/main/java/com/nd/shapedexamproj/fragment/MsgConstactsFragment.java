package com.nd.shapedexamproj.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.db.ChatUserInfoDao;
import com.nd.shapedexamproj.db.NewFriendsDBOperator;
import com.nd.shapedexamproj.db.RelatedUserDBOperator;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.entity.RelatedUserListEntity;
import com.nd.shapedexamproj.im.model.CommunicationItemInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Mode;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 消息模块--通讯录，相当于“我的关注”
 * Created by zll on 2014-11-18
 * Modified by xuwenzhuo on 2014-11-23
 */
public class MsgConstactsFragment extends BaseFlowFragment{
    
    private final String TAG = "MsgConstactsFragment";

    private String[] firstChars ;
    private HashMap<String, Integer> selector;// 存放含有索引字母的位置
    private View loadingView;                 //网络指示标
    private Context context;
    private RelativeLayout msgGroupRl,msgFansRl;
    private Button mFansNumBtn;
    private PullToRefreshListView listView;
    private NewFriendsDBOperator newFriendsOperator;
    private View listHeaderView;
    private LinearLayout rosterNumberLL;
    private CommunicationListAdapter communicationListAdapter;
    private TmingCacheHttp cacheHttp ;
    private ChatUserInfoDao chatUserInfoDao;
    private int pageNum = 1, pageSize = 500;
    private int mNewFriendsNum;
    
    @Override
    public void initComponent(View view) {

        cacheHttp = TmingCacheHttp.getInstance();
        chatUserInfoDao = ChatUserInfoDao.getInstance(App.getAppContext());
        rosterNumberLL = (LinearLayout) view.findViewById(R.id.roster_number_ll);
        //网络指示标
        loadingView = view.findViewById(R.id.loading_layout);
        // 上下文
        context = getActivity();
        // 设置列表
        listView = (PullToRefreshListView) view.findViewById(R.id.msg_roster_list_view);
        //绑定点击事件
        listView.findViewById(R.id.msg_communication_item_area);
        newFriendsOperator = NewFriendsDBOperator.getInstance(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        listHeaderView = inflater.inflate(R.layout.msg_communication_item_header, null);

        /*msgRosterRl = (RelativeLayout) listHeaderView.findViewById(R.id.msg_communication_header_myfind_rl);*/
        msgFansRl = (RelativeLayout) listHeaderView.findViewById(R.id.msg_communication_header_fans_rl);
        msgGroupRl = (RelativeLayout) listHeaderView.findViewById(R.id.msg_communication_header_group_rl);
        mFansNumBtn = (Button) listHeaderView.findViewById(R.id.msg_communication_header_fans_num);
        //装载头部
        listView.getRefreshableView().addHeaderView(listHeaderView);
        listView.setMode(Mode.DISABLED);
        selector = new HashMap<String, Integer>();

    }

    @Override
    public void initListener() {

        msgFansRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //UIHelper.showMyFansActivity(context);
                UIHelper.showNewFriendsActivity(context);
            }
        });
        
        msgGroupRl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UIHelper.showGroupListActivity(context);
            }
        });
        
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                if (communicationListAdapter.getCount() > 0) {
                    //点击列表，跳转到聊天界面
                    Log.e(TAG, "position=" + position);
                    CommunicationItemInfo data = (CommunicationItemInfo) communicationListAdapter.getItem(position - 2);
                    UIHelper.showChatActivity(context, data);
                }
            }
        });

        listView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                /*new RosterAsyncTask().execute();*/
                pageNum = 1;
                communicationListAdapter.clear();
                getFriendsListWithCache(Constants.FOLLOW);
                getNewFriendsNum();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ++ pageNum;
                getFriendsListWithCache(Constants.FOLLOW);
            }});
    }

    @Override
    public int initResource() {
        return R.layout.msg_roster_list_activity;
    }
    
    /**
     * 刷新用户数
     */
    public void getNewFriendsNum() {
        String url = Constants.GET_FRIENDS_LIST;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        //查询别人关注我的列表
        params.put("type", Constants.PERSON_RELATION_FOLLOWED);
        params.put("pageno", 1);
        params.put("pagesize", 100);
        TmingHttp.asyncRequest(url, params, new RequestCallback<RelatedUserListEntity>() {

            @Override
            public RelatedUserListEntity onReqestSuccess(String respones) throws Exception {
                return relatedUserListJSONParsing(respones);
            }

            @Override
            public void success(RelatedUserListEntity respones) {
                if (respones == null) {
                    return;
                }
                List<RelatedUserEntity> newEntityList = respones.getmRelatedUserList();
                List<RelatedUserEntity> DBEntityList = newFriendsOperator.queryNewFriendsWithType(Constants.FANS);
                for(int i = 0;i < newEntityList.size();i ++) {
                    RelatedUserEntity entity = newEntityList.get(i);
                    if (DBEntityList.size() == 0) {
                        mNewFriendsNum = newEntityList.size();
                        break;
                    }
                    if (DBEntityList.contains(entity)) {
                        continue;
                    } else {//如果本地不包含新数据，则新朋友加1
                        mNewFriendsNum ++;
                    }
                }
                if (mNewFriendsNum > 0 && mNewFriendsNum <= 99) {
                    mFansNumBtn.setText("" + mNewFriendsNum);
                    mFansNumBtn.setVisibility(View.VISIBLE);
                } else if (mNewFriendsNum == 0) {
                    mFansNumBtn.setText("");
                    mFansNumBtn.setVisibility(View.GONE);
                } else if (mNewFriendsNum == 100) {
                    mFansNumBtn.setText("99+");
                    mFansNumBtn.setVisibility(View.VISIBLE);
                }
                mNewFriendsNum = 0;
            }

            @Override
            public void exception(Exception exception) {
                
            }});
    }

    /**
     *  返回关注对象 
     * @param result
     * @return JSONException JSON转换异常
     */
    private RelatedUserListEntity relatedUserListJSONParsing(String result) throws JSONException{
        RelatedUserListEntity relatedUserListEntity=new RelatedUserListEntity();
        JSONObject object = new JSONObject(result);
        relatedUserListEntity.setmFlag(object.getInt("flag"));
        relatedUserListEntity.setmInfo(object.getString("info"));
        relatedUserListEntity.setmTotalSize(object.getInt("totalSize"));
        relatedUserListEntity.setmRelatedUserList(relatedUserJSONParsing(result));
        return relatedUserListEntity;
    }

    /**
     * 字符串封装为JSON数组 
     *
     * @param result JSON字符串
     * @return List<RelatedUserEntity> 关注列表
     * @throws JSONException JSON转换异常
     */
    private List<RelatedUserEntity> relatedUserJSONParsing(String result)
            throws JSONException {
        List<RelatedUserEntity> relatedUsers = new ArrayList<RelatedUserEntity>();
        try{
            JSONObject object = new JSONObject(result);
            JSONArray list = object.getJSONArray("data");
            for (int i = 0; i < list.length(); i++) {
                RelatedUserEntity relatedUserEntity = new RelatedUserEntity();
                relatedUserEntity.setmType(list.getJSONObject(i).getInt("rtype"));
                relatedUserEntity.setmUserId(list.getJSONObject(i).getString("userId"));
                relatedUserEntity.setmRelatedId(list.getJSONObject(i).getString("followId"));
                relatedUserEntity.setmRelatedUserName(list.getJSONObject(i).getString("followUserName"));
                relatedUserEntity.setmUserDetail(list.getJSONObject(i).getString("explanation").equals("null")?"":list.getJSONObject(i).getString("explanation"));
                String followId=relatedUserEntity.getmRelatedId().equals(App.getUserId())?relatedUserEntity.getmUserId():relatedUserEntity.getmRelatedId();
                relatedUserEntity.setmRelatedImg(getUserHeadIcon(followId));
                relatedUsers.add(relatedUserEntity);
            }
        } catch (JSONException exception){
            Log.v(TAG,exception.getMessage());
        }
        return relatedUsers;
    }
    
    private String getUserHeadIcon(String userId){
        return Constants.USER_PHOTO_URL+userId;
    }
    
    private void getFriendsListWithCache(final int type){
        cacheHttp.asyncRequestWithCache(ServerApi.getFriendsList(pageNum, pageSize, type), new HashMap<String, Object>(),
                new RequestWithCacheCallBackV2<List<CommunicationItemInfo>>() {
                    @Override
                    public List<CommunicationItemInfo> parseData(String data) throws Exception {
                        List<CommunicationItemInfo> followList = CommunicationItemInfo.jsonParsing(data);
                        //首字母缩写排序
                        Collections.sort(followList, new Comparator<CommunicationItemInfo>() {
                            @Override
                            public int compare(CommunicationItemInfo lhs, CommunicationItemInfo rhs) {
                                return lhs.sortKey.compareTo(rhs.sortKey);
                            }
                        });
                        return followList;
                    }

                    @Override
                    public void cacheDataRespone(List<CommunicationItemInfo> data) {
                        //loadData(data);
                        //移除老数据，追加新数据
                        communicationListAdapter.replaceItem(communicationListAdapter.getDatas(), data);
                        getCharPositions(data);
                        getFirstChars(data);
                        communicationListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void requestNewDataRespone(List<CommunicationItemInfo> cacheRespones,List<CommunicationItemInfo> newRespones) {
                        //移除老数据，追加新数据
                        communicationListAdapter.replaceItem(cacheRespones, newRespones);
                        getCharPositions(newRespones);
                        getFirstChars(newRespones);
                        communicationListAdapter.notifyDataSetChanged();
                        chatUserInfoDao.saveUserInfo(newRespones);
                    }

                    @Override
                    public void exception(Exception exception) {
                        exception.printStackTrace();
                    }

                    @Override
                    public void onFinishRequest() {
                        //保存用户关系数据到本地数据库
                        storeRelatedUsersToDB(communicationListAdapter.getDatas());
                        listView.onRefreshComplete();
                        loadingView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void initData() {
        // 实现列表的显示
        communicationListAdapter = new CommunicationListAdapter(context);
        listView.setAdapter(communicationListAdapter);
        listView.setOnScrollListener(communicationListAdapter);
        //默认状态为没有修改
        App.mRelatedUserStateFlag =Constants.USER_RELATEDSTAT_NOTCHANGE;
        getFriendsListWithCache(Constants.FOLLOW);
        getNewFriendsNum();
    }
    
    public void refreshConstacts() {
        pageNum = 1;
        loadingView.setVisibility(View.VISIBLE);
        getFriendsListWithCache(Constants.FOLLOW);
    }
    
    /**
     *  存储网络获取的用户关系列表到本地数据库  
     * @param communicationItemInfos 用户关系列表
     */
    private void storeRelatedUsersToDB(List<CommunicationItemInfo> communicationItemInfos){

        if (null==communicationItemInfos){
            return;
        } else {
            RelatedUserDBOperator rDbAdapter=new RelatedUserDBOperator(context);
            //每次保存之前先清空数据库,保证只有一个记录
            rDbAdapter.clearAllRelatedRecords();
            try{
                for (int i=0;i<communicationItemInfos.size();i++){
                    //依次插入数据
                    rDbAdapter.insertRelationShipWithCommuObject(communicationItemInfos.get(i));
                }
            } catch(Exception exception){
                Log.e(TAG,exception.getMessage());
            }
        }
    }

    private void loadData(List<CommunicationItemInfo> cacheList) {
        communicationListAdapter.addItemCollection(cacheList);
    }
    
    /**
     * 是否只包含数字</P>
     * @param str
     * @return
     */
    public boolean isNumeric(String str){ 
        Pattern pattern = Pattern.compile("[0-9]*"); 
        return pattern.matcher(str).matches();    
    }  
    /**
     * 获取第一个字符列表</P>
     * @return
     */
    private void getFirstChars(List<CommunicationItemInfo> list) {
        TreeSet<String> set = new TreeSet<String>();
        
        for (CommunicationItemInfo itemInfo : list) {
            String label = itemInfo.getSortKey();
            String firstChar = getSortKey(label);
            set.add(firstChar);
        }
        firstChars = new String[set.size()];
        int i = 0;
        for (String string : set) {
            firstChars[i] = string;
            i++;
        }
        getIndexView(firstChars);
    }

    
    private void getCharPositions(List<CommunicationItemInfo> list) {
        if (list == null) {
            return;
        }
        
        for (int i = 0;i < list.size();i ++) {
            CommunicationItemInfo itemInfo = list.get(i);
            String label = itemInfo.getSortKey();
            String firstChar = getSortKey(label);
            if (i > 0) {
                CommunicationItemInfo preItemInfo = list.get(i - 1);
                String preLabel = preItemInfo.getSortKey();
                String preFirstChar = getSortKey(preLabel);
                if (!preFirstChar.equals(firstChar)) {
                    selector.put(firstChar, i);
                }
            } else {
                selector.put(firstChar, 0);
            }
            
        }
    }
    
    /**
     * 绘制索引列表
     */
    private void getIndexView(final String[] indexStr) {
        if (indexStr.length <= 0) {
            return;
        }
        rosterNumberLL.removeAllViews();
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < indexStr.length; i++) {
            final TextView tv = new TextView(context);
            tv.setLayoutParams(params);
            tv.setText(indexStr[i]);
            tv.setTextColor(getResources().getColor(R.color.light_black));
            tv.setPadding(5, 0, 5, 0);
            rosterNumberLL.addView(tv);
            rosterNumberLL.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event)

                {
                    float y = event.getY();
                    int index = (int) (y / tv.getHeight());
                    Log.e(TAG, "tv.getheight() = " + tv.getHeight());
                    if (index > -1 && index < indexStr.length) {// 防止越界
                        String key = indexStr[index];
                        if (selector.containsKey(key)) {
                            int pos = selector.get(key);
                            if (listView.getRefreshableView().getHeaderViewsCount() > 0) {// 防止ListView有标题栏，本例中没有。
                                listView.getRefreshableView().setSelectionFromTop(
                                        pos + listView.getRefreshableView().getHeaderViewsCount(), 0);
                            } else {
                                listView.getRefreshableView().setSelectionFromTop(pos, 0);// 滑动到第一项
                            }
                        }
                    }
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:

                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    }
                    return true;
                }
            });
        }
    }

    private class CommunicationListAdapter extends ImageBaseAdatapter<CommunicationItemInfo> {

        public CommunicationListAdapter(Context context) {
            super(context);
        }

        @Override
        public View infateItemView(Context context) {
            ViewHolder holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.msg_roster_item, null);

            holder.imageView = (ImageView) view.findViewById(R.id.msg_communication_item_img_photo);
            holder.msg_communication_item_name = (TextView) view.findViewById(R.id.msg_communication_item_name);
            holder.msg_communication_item_content = (TextView) view.findViewById(R.id.msg_communication_item_content);
            holder.msg_communication_item_sort_key = (TextView) view.findViewById(R.id.msg_communication_item_sort_key);
            holder.sort_key_layout = (RelativeLayout) view.findViewById(R.id.sort_key_layout);
            view.setTag(holder);
            return view;
        }

        @Override
        public URL getDataImageUrl(CommunicationItemInfo data) {
            //String userPhotoUrl = Constants.USER_PHOTO_URL + data.followId;
            try {
                return new URL(data.getUserPhotoUrl());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //下一行首字母和上一行首字母进行比较，一样就隐藏灰色字母标题，不一样就显示。
            View view = super.getView(position, convertView, parent);
            // 设置通讯录首字母
            CommunicationItemInfo itemInfo = (CommunicationItemInfo) getItem(position);
            String label = itemInfo.getSortKey();
            String name = itemInfo.getName();
            String firstChar = getSortKey(label);
            
            Log.e(TAG, "position = " + position + " name = " + name + " label = " + label + " firstChar = " + firstChar);
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            if (position == 0) {
                viewHolder.sort_key_layout.setVisibility(View.VISIBLE);
                viewHolder.msg_communication_item_sort_key.setText(firstChar);
            } else {
                CommunicationItemInfo preItemInfo = (CommunicationItemInfo) getItem(position - 1);
                String preLabel = preItemInfo.getSortKey();
                String preFirstChar = getSortKey(preLabel);
                if (!preFirstChar.equals(firstChar)) {
                    /*selector.put(firstChar, position);*/
                    //不同首字母，首字母分组行显示
                    viewHolder.sort_key_layout.setVisibility(View.VISIBLE);
                    viewHolder.msg_communication_item_sort_key.setText(firstChar);
                } else {
                    //同类首字母，首字母分组行隐藏
                    viewHolder.sort_key_layout.setVisibility(View.GONE);
                }
            }
            return view;
        }

        /**
         * 返回list控件现有的数据
         * @return
         */
        public List<CommunicationItemInfo> getDatas(){
            return datas;
        }

        @Override
        public void setViewHolderData(BaseViewHolder arg0, final CommunicationItemInfo itemInfo) {
            ViewHolder viewHolder = (ViewHolder) arg0;
            //查看好友基本信息
            viewHolder.imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIHelper.showFriendInfoActivity(context,itemInfo);
                }
            });
            //设置内容文字
            viewHolder.msg_communication_item_name.setText(itemInfo.getName());
            viewHolder.msg_communication_item_content.setText(itemInfo.getContent());
        }
        
        public class ViewHolder extends BaseViewHolder {
            private TextView msg_communication_item_name;
            private TextView msg_communication_item_content;
            private TextView msg_communication_item_sort_key;
            private RelativeLayout sort_key_layout;
        }
        
    }

    /**
     * 获取sort key的首个字符，如果是英文字母就直接返回，否则返回#。
     * @param sortKeyString 数据库中读取出的sort key
     * @return 英文字母或者#
     */
    private String getSortKey(String sortKeyString) {
        try {
            String key = sortKeyString.substring(0, 1).toUpperCase();
            if (key.matches("[A-Z]")) {
                return key;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "#";
    }

}
