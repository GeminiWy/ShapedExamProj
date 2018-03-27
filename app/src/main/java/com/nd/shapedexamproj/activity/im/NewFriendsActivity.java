package com.nd.shapedexamproj.activity.im;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.RelatedUserListAdapter;
import com.nd.shapedexamproj.db.NewFriendsDBOperator;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.entity.RelatedUserListEntity;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>NewFriendsActivity 新的朋友</p>
 * <p>Created by zll</p>
 * <p>Modified by xuwenzhuo 2014/11/18</p>
 */

public class NewFriendsActivity extends BaseActivity implements OnClickListener{

    private static final String TAG = "NewFriendsActivity";
    private static final int PAGESIZE=20;
    public static final String ADD_RELATEDUSER="addRelatedUser";

	private RefreshableListView mFriendsListView;
	private RelatedUserListAdapter mRelatedUserListAdapter ;
	private TmingCacheHttp mCacheHttp ;
	private NewFriendsDBOperator newFriendsOperator;
    private Button mHeaderRightButton;
    private TextView mTitleTextView;
    private boolean mLoadMore;
    private View mLoadingView;

    private Drawable mGreenButtonBg;
    private Drawable mWhiteButtonBg;
    private int mOldTextColor;
    private int mNewTextColor;
    private int mCurrentPosition;
    private int mCurrentQueryState;
    private int mPageNo;
    private boolean mStateChanged;

    @Override
	public int initResource() {
		return R.layout.related_new_activity;
	}

	@Override
	public void initComponent() {

		mFriendsListView = (RefreshableListView) findViewById(R.id.related_list_view);
		//添加朋友按钮
        mHeaderRightButton=(Button) findViewById(R.id.common_head_right_btn);
        mHeaderRightButton.setText(R.string.im_personadd_title);
        mHeaderRightButton.setTextSize(16);
        //隐藏按钮
        mHeaderRightButton.setVisibility(View.GONE);
        mTitleTextView=(TextView) findViewById(R.id.commonheader_title_tv);
		mTitleTextView.setText(R.string.add_newfriend_title);
        mLoadingView=(View) findViewById(R.id.loading_layout);
        mLoadingView.setVisibility(View.GONE);
        mRelatedUserListAdapter = new RelatedUserListAdapter(this,null);
        mFriendsListView.setAdapter(mRelatedUserListAdapter);
        mCacheHttp = TmingCacheHttp.getInstance(this);
        newFriendsOperator = NewFriendsDBOperator.getInstance(this);
        
        mGreenButtonBg=getResources().getDrawable(R.drawable.null_foreground_button_pressed);
        mWhiteButtonBg=getResources().getDrawable(R.drawable.green_white_button);
        mOldTextColor= Color.WHITE;
        mNewTextColor=getResources().getColor(R.color.head_btn_border);
	}

	@Override
	public void initData() {
		mPageNo=1;
        mCurrentQueryState=-1;
        //默认的选定行数，-1为无效行数
        mCurrentPosition=-1;
        mStateChanged=false;
        requestRelatedUser();
	}

    @Override
	public void addListener() {
        findViewById(R.id.commonheader_left_iv).setOnClickListener(this);
        mHeaderRightButton.setOnClickListener(this);

        mFriendsListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作
                mPageNo=1;
                mLoadMore=false;
                requestRelatedUser();
            }

            @Override
            public void onLoadMore() {
                //加载更多
                mPageNo+=1;
                mLoadMore=true;
                requestRelatedUser();
            }
        });
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.commonheader_left_iv:
				//修改状态
                if (mStateChanged){
                    App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_CHANGED;
                }
                //结束
                finish();
				break;
            case R.id.common_head_right_btn:
                //添加朋友
                UIHelper.showPersonSearchActivity(this);
                break;
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        //好友状态已经发生改变
        if (App.mRelatedUserStateFlag==Constants.USER_RELATEDSTAT_CHANGED){
            mRelatedUserListAdapter.changeItemState(mCurrentPosition);
            //恢复状态
            App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_NOTCHANGE;
            //修改状态
            mStateChanged=true;
        }
    }

    /**
     * <p> 请求用户关注列表 </p>
     * 用户关注列表查询 type ：0 用户关注的好友列表
     *                      1 双方关注的好友列表
     *                      2 被关注的用户列表
     */
    private void requestRelatedUser(){
        //关注列表
        String url = Constants.GET_FRIENDS_LIST;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        //查询别人关注我的列表
        params.put("type", Constants.PERSON_RELATION_FOLLOWED);
        params.put("pageno", mPageNo);
        params.put("pagesize", PAGESIZE);
        mCacheHttp.asyncRequestWithCache(url,params,requestCallBack);
    }

    RequestWithCacheCallBackV2 requestCallBack=new RequestWithCacheCallBackV2<RelatedUserListEntity>() {

        @Override
        public RelatedUserListEntity parseData(String data) throws Exception {
            return relatedUserListJSONParsing(data);
        }

        @Override
        public void cacheDataRespone(RelatedUserListEntity data) {
            //缓存数据
            if (mLoadMore){
                //加载更多
                mRelatedUserListAdapter.setData(data.getmRelatedUserList());
                mLoadMore=false;
            } else {
                mRelatedUserListAdapter.setmRelatedUserList(data.getmRelatedUserList());
            }
            mRelatedUserListAdapter.notifyDataSetChanged();
        }

        @Override
        public void requestNewDataRespone(RelatedUserListEntity cacheRespones, RelatedUserListEntity newRespones) {
            //新数据
            if (mLoadMore){
                //加载更多
                mRelatedUserListAdapter.setData(newRespones.getmRelatedUserList());
                mLoadMore=false;
            } else {
                mRelatedUserListAdapter.setmRelatedUserList(newRespones.getmRelatedUserList());
            }
            mRelatedUserListAdapter.notifyDataSetChanged();
        }

        @Override
        public void exception(Exception exception) {
            //异常状况

        }

        @Override
        public void onFinishRequest() {
            //结束
            mFriendsListView.onRefreshComplete();
            saveNewFriendsId(mRelatedUserListAdapter.getData());
            
        }
    };
    /**
     * <p>保存新朋友id</P>
     * @param relatedUserEntityList
     */
    private void saveNewFriendsId(List<RelatedUserEntity> relatedUserEntityList) {
        if (relatedUserEntityList == null) {
            return;
        } else if (relatedUserEntityList.size() > 0) {
            newFriendsOperator.deleteNewFriendsByType(Constants.FANS);
        }
        for (int i = 0;i < relatedUserEntityList.size();i ++) {
            RelatedUserEntity entity = relatedUserEntityList.get(i);
            
            newFriendsOperator.insertNewfriendsId(entity);
        }
    }
    
    /**
     * <p> 发送关注请求 ，在 related_user_cell中触发 </p>
     * @param view  cell控件
     *
     */
    public void sendAddRelatedRequest(View view){
        try{
            int posision=(Integer)view.getTag();
            RelatedUserEntity relatedUserEntity=mRelatedUserListAdapter.getData().get(posision);
            if (relatedUserEntity!=null){

                if (relatedUserEntity.getmType()==Constants.PERSON_RELATION_FRIEND){
                    //已经添加成为好友的情况
                    Toast.makeText(NewFriendsActivity.this, R.string.msg_have_added, Toast.LENGTH_SHORT).show();
                } else {
                    //没有添加成为好友
                    //由于关注关系的原因，导致userid字段的不确定，程序中首先判定一次
                    String userId = relatedUserEntity.getmUserId().equals(App.getUserId()) ? relatedUserEntity.getmRelatedId() : relatedUserEntity.getmUserId();
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("userid", App.getUserId());
                    params.put("followid", userId);
                    params.put("type", Constants.PERSON_RELATION_FOLLOW);
                    String url = Constants.ADD_FRIENDS_URL;
                    changeFriendState(url, params, (Button) view);
                    mCurrentPosition=posision;
                }
            }
        } catch(Exception exception){
            Toast.makeText(NewFriendsActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * <p>跳转到用户信息页面</p>
     * @param view 用户圆形头像view
     */
    public void jumpToUserDetailView(View view){
        int posision=(Integer)view.getTag();
        if(mRelatedUserListAdapter.getData()==null||mRelatedUserListAdapter.getData().size()<=posision){
            return;
        } else {
            RelatedUserEntity relatedUserEntity = mRelatedUserListAdapter.getData().get(posision);
            //由于关注关系的原因，导致userid字段的不确定，程序中首先判定一次
            String userId = relatedUserEntity.getmUserId().equals(App.getUserId())?relatedUserEntity.getmRelatedId():relatedUserEntity.getmUserId();
            //跳转到好友信息界面
            UIHelper.showFriendInfoActivity(NewFriendsActivity.this,userId);
            mCurrentPosition=posision;
        }
    }

    /**
     * <p> 加关注或取消关注 </p>
     * @param url
     * @param params
     */
    private void changeFriendState(String url,Map<String ,Object> params,final Button view){

        Log.d(TAG, url);
        TmingHttp.asyncRequest(url, params, new TmingHttp.RequestCallback<Integer>() {
            @Override
            public Integer onReqestSuccess(String respones) throws Exception {
                return addJsonParsing(respones);
            }

            @Override
            public void success(Integer respones) {
                //提示消失
                mLoadingView.setVisibility(View.GONE);
                if (respones != Constants.SUCCESS_MSG) {
                    //添加关注失败
                    Toast.makeText(NewFriendsActivity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    //添加关注成功
                    Toast.makeText(NewFriendsActivity.this, R.string.follow_add, Toast.LENGTH_SHORT).show();
                    //修改按钮状态为已经添加
                    changeButtonState(view, 1);
                    mStateChanged = true;
                    //修改要素状态，修改为朋友状态
                    mRelatedUserListAdapter.changeItemState(mCurrentPosition, Constants.PERSON_RELATION_FRIEND);
                }
            }

            @Override
            public void exception(Exception exception) {
                mLoadingView.setVisibility(View.GONE);
                Toast.makeText(NewFriendsActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
    }

    /**
     * <p>修改按钮状态</p>
     * @param view 按钮视图
     * @param state 状态参数 0 :没有添加
     *                     1 :已经添加
     */
    private void changeButtonState(final Button view,int state){

        if (state==1) {
            //修改为已经添加
            view.setText(R.string.msg_have_added);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                view.setTextColor(mNewTextColor);
                view.setBackground(mWhiteButtonBg);
            }
            view.setEnabled(false);
        } else {
            //修改为没有添加
            view.setText(R.string.add);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                view.setTextColor(mOldTextColor);
                view.setBackground(mGreenButtonBg);
            }
            view.setEnabled(true);
        }
    }

    /**
     *  <p>添加好友结果字符串解析</p>
     * @param result 返回的json字符串
     * @return int 型结果
     */
    private int addJsonParsing(String result){
        Log.d(TAG, result);
        int flag = 0;
        try {
            JSONObject jobj = new JSONObject(result);
            flag = jobj.getInt("flag");
            if(flag != 1 && !jobj.isNull("msg")){
                String msg = jobj.getString("msg");
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "=======");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * <p> 返回关注对象 </p>
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
     *<p> 字符串封装为JSON数组 </p>
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

    /**
     * <p> 根据返回的数据，重新拼接用户头像 前缀+userId </p>
     * @param userId
     * @return
     */
    private String getUserHeadIcon(String userId){
        return Constants.USER_PHOTO_URL+userId;
    }

    @Override
    public void onBackPressed() {
        //修改状态
        if (mStateChanged){
            App.mRelatedUserStateFlag= Constants.USER_RELATEDSTAT_CHANGED;
        }
        finish();
        super.onBackPressed();
    }
}
