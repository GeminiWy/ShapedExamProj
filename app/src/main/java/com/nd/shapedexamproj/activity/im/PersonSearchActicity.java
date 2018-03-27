/**  
 * Project Name:OpenUniversity  
 * File Name:PersonSearchActicity.java  
 * Package Name:com.tming.openuniversity.activity  
 * Date:2014-6-9下午11:34:16  
 * Copyright (c) 2014, XueWenJian All Rights Reserved.  
 *  
 */

package com.nd.shapedexamproj.activity.im;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.PersonSearchAdapter;
import com.nd.shapedexamproj.db.RelatedUserDBOperator;
import com.nd.shapedexamproj.entity.RelatedUserEntity;
import com.nd.shapedexamproj.im.model.PersonSearchResult;
import com.nd.shapedexamproj.im.model.RosterModel;
import com.nd.shapedexamproj.im.resource.IMConstants;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshListView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.activity.im
 * @文件名: PersonSearchActicity
 * @文件描述: PersonSearchActicity 用户检索
 * @创建人: XueWenJian
 * @创建时间: 2014/6/9 11:08
 * @修改记录:
 *     1、Modified by xuwenzhuo On 2014/12/1
 *       修改：根据交互需求修改功能
 *
 * @Copyright: 2014 Tming All rights reserved.
 */

public class PersonSearchActicity extends BaseActivity implements
		OnClickListener {

	private static final String TAG = "PersonSearchActicity";
	/**
	 * 添加通讯录数据
	 * */
	private static final int MSG_ADD_CONTACT_ITEM_DATA = 1;  
	private static final int IS_ADDED = 1;
	private static final int NOT_ADDED = 0;
	private Context mContext;

    RelatedUserDBOperator relatedUserDBOperator;
	private LinearLayout mLoadinglayout;
	private RelativeLayout mHeadRL;
	private ImageView mBackIV;
	private TextView mHeadTitleTV;
    private TextView mUserSearchWordsTV;
	private Button mHeadRightBtn;
	private EditText mSeachWordET;
	private ImageView mCleanIV;
	private Button mFindTV;
	private PullToRefreshListView mResultLV;
	private List<PersonSearchResult> mPersonSearchResults;
	private PersonSearchAdapter mPersonSearchAdapter;
	private int mPage = 1;
	private int mPageSize = 10;
    private List<RelatedUserEntity> mAddUserList;
    private Drawable mGreenButtonBg;
    private Drawable mWhiteButtonBg;
    private int mOldTextColor;
    private int mNewTextColor;
    private TextView mSearchResultCountTextView;
    private boolean mStateChanged;
    private int mCurrentPosition;

	@Override
	public int initResource() {
		return R.layout.im_personsearch_activity;
	}

	@Override
	public void initComponent() {
		mLoadinglayout = (LinearLayout) findViewById(R.id.loading_layout);
		mHeadRL = (RelativeLayout) findViewById(R.id.common_head_layout);
		mBackIV = (ImageView) findViewById(R.id.commonheader_left_iv);
		mHeadTitleTV = (TextView) findViewById(R.id.commonheader_title_tv);
		mHeadRightBtn = (Button) findViewById(R.id.commonheader_right_btn);

		mSeachWordET = (EditText) findViewById(R.id.personsearch_search_et);
        mSeachWordET.setCursorVisible(false);
		mCleanIV = (ImageView) findViewById(R.id.personsearch_clean_iv);
		mFindTV = (Button) findViewById(R.id.personsearch_find_tv);
		mResultLV = (PullToRefreshListView) findViewById(R.id.personsearch_result_lv);
        mUserSearchWordsTV=(TextView) findViewById(R.id.user_search_words_tv);
        mSearchResultCountTextView=(TextView) findViewById(R.id.im_search_about_count_tv);

		mHeadTitleTV.setText(R.string.im_personadd_title);
		mHeadRightBtn.setVisibility(View.INVISIBLE);

        mGreenButtonBg=getResources().getDrawable(R.drawable.null_foreground_button_pressed);
        mWhiteButtonBg=getResources().getDrawable(R.drawable.green_white_button);
        mOldTextColor= Color.WHITE;
        mNewTextColor=getResources().getColor(R.color.head_btn_border);
	}

	@Override
	public void initData() {
		mContext = this;
		mPersonSearchResults = new ArrayList<PersonSearchResult>();
		mPersonSearchAdapter = new PersonSearchAdapter(mContext,
				mPersonSearchResults);
		mResultLV.setAdapter(mPersonSearchAdapter);
        relatedUserDBOperator=new RelatedUserDBOperator(mContext);
        //获取添加的用户列表
        loadRelatedUserList();
        mStateChanged=false;
	}

	@Override
	public void addListener() {

		mCleanIV.setOnClickListener(this);
		mFindTV.setOnClickListener(this);
        mSeachWordET.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //修改EditTextView的course状态
                mSeachWordET.setCursorVisible(true);
            }
        });
		mResultLV.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						Toast.makeText(mContext, "onPullDownToRefresh",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<ListView> refreshView) {
						nextPage();
					}
				});

		mBackIV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
                if (mStateChanged){
                    //恢复状态
                    App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_CHANGED;
                    //修改状态
                    mStateChanged=false;
                }
				PersonSearchActicity.this.finish();
			}
		});
		
		mHeadTitleTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
                if (mStateChanged){
                    //恢复状态
                    App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_CHANGED;
                    //修改状态
                    mStateChanged=false;
                }
				PersonSearchActicity.this.finish();
			}
		});

		mSeachWordET.addTextChangedListener(new TextWatcher() {
			private CharSequence mTemp;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// s：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int start,
					int count, int after) {
				// s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
				mTemp = charSequence;
			}

			@Override
			public void afterTextChanged(Editable editable) {
				// s:变化后的所有字符
				if (mTemp.length() > 0) {
					showClearBtn(true);
				} else {
					showClearBtn(false);
				}
			}
		});
	}

	@Override
	public void onClick(View view) {
		if (R.id.personsearch_clean_iv == view.getId()) {
            //清除按钮
			String keyword = mSeachWordET.getText().toString();
			if(!"".equals(keyword)){
				mSeachWordET.setText("");
				cleanReult();
				showClearBtn(false);
                mUserSearchWordsTV.setVisibility(View.VISIBLE);
			}
		} else if (R.id.personsearch_find_tv == view.getId()) {
			if (PhoneUtil.checkNetworkEnable(mContext) == PhoneUtil.NETSTATE_DISABLE) {
				Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
				return;
			}
			
			String keyword = mSeachWordET.getText().toString();
			if(!"".equals(keyword)){
				cleanReult();
				UIHelper.closeInputWindow(mContext, view);
				findUsers(keyword);
			} else {
				UIHelper.ToastMessage(mContext, R.string.im_empty_error);
			}
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        //好友状态已经发生改变
        if (App.mRelatedUserStateFlag==Constants.USER_RELATEDSTAT_CHANGED){
            mPersonSearchAdapter.changeSingleItemState(mCurrentPosition);
            //恢复状态
            App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_NOTCHANGE;
            //修改状态
            mStateChanged=true;
            saveRelatedUserToDB(mPersonSearchAdapter.getData().get(mCurrentPosition));
        }
    }

	private void cleanReult() {
		mPage = 1;
		mPersonSearchResults.clear();
		mPersonSearchAdapter.notifyDataSetChanged();
	}

	private void nextPage() {
		String keyword = mSeachWordET.getText().toString();
		findUsers(keyword);
	}

    /**
     *  根据关键字查找用户 
     * @param keyWord
     */
	private void findUsers(String keyWord) {
		if (StringUtils.isEmpty(keyWord)) {
			mResultLV.onRefreshComplete();
			UIHelper.ToastMessage(mContext, R.string.im_empty_error);
			return;
		}

		mLoadinglayout.setVisibility(View.VISIBLE);
		String url = Constants.SEARCH_USER;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("keyword", keyWord);
		params.put("pageNum", mPage);
		params.put("pageSize", mPageSize);
		TmingHttp.asyncRequest(url, params, new TmingHttp.RequestCallback<List<PersonSearchResult>>() {

			@Override
			public List<PersonSearchResult> onReqestSuccess(String respones) throws Exception {
				// 数据组装
				JsonParseObject jsonParseObject = JsonParseObject
						.parseJson(respones);
				List<PersonSearchResult> result = null;
				if (Constants.SUCCESS_MSG == jsonParseObject.getFlag()) {
					try {
						if (Constants.SUCCESS_MSG == jsonParseObject.getResJs().getInt("code")) {
							if (!jsonParseObject.getResJs().getJSONObject("data").isNull("list")) {
								Gson gson = new GsonBuilder().setDateFormat(
										"yyyy-MM-dd").create();
								result = gson.fromJson(jsonParseObject.getResJs().getJSONObject("data")
												.getJSONArray("list")
												.toString(),
										new TypeToken<ArrayList<PersonSearchResult>>() {
										}.getType());

								if (result.size() > 0) {
									mPage++;
								}
								//重新设定添加关系,跟本地数据库进行对比，如果本地数据库中存在，则改变用户状态:已经添加
								//如果没有，保持原来状态
								result = resetUserRelatedFlag(result);
							}
						}
					} catch (JsonSyntaxException e) {
						e.printStackTrace();
						jsonParseObject.setFlag(0);
					}
				}
				return result;
			}

			@Override
			public void success(List<PersonSearchResult> result) {
				mResultLV.onRefreshComplete();
				// UI表现
				if (result != null) {
					mPersonSearchResults.addAll(result);
					mPersonSearchAdapter.notifyDataSetChanged();
					if (mPersonSearchResults.size() == 0) {
						UIHelper.ToastMessage(mContext,
								R.string.im_result_empty_text);
						mUserSearchWordsTV.setVisibility(View.VISIBLE);
					} else {
						mUserSearchWordsTV.setVisibility(View.GONE);
					}
					mSearchResultCountTextView.setText("( " + mPersonSearchResults.size() + " )人");
				} else {
					Toast.makeText(mContext, R.string.api_error,
							Toast.LENGTH_SHORT).show();
					mSearchResultCountTextView.setText("( 0 )人");
					return;
				}
				mLoadinglayout.setVisibility(View.GONE);
			}

			@Override
			public void exception(Exception exception) {
				exception.printStackTrace();

			}
		});
	}
	
	/**
	 * 显示隐藏清除按钮
	 * @param able
	 */
	private void showClearBtn(boolean able) {
		if (able) {
			mCleanIV.setVisibility(View.VISIBLE);
		} else {
			mCleanIV.setVisibility(View.GONE);
		}
	}

	private Button personsearchAddBtn;

	private class AddContactPersonAsyncTask extends
			AsyncTask<PersonSearchResult, Void, Integer> {

		@Override
		protected Integer doInBackground(PersonSearchResult... arg0) {
			PersonSearchResult personSearchResult = arg0[0];
			RosterModel model = new RosterModel();
			Integer reult = RosterModel.RESULT_SUCCESS;
			if(App.getUserId().equals(personSearchResult.getUserId())){
				//提示不能添加自己为好友
				reult = RosterModel.RESULT_ERROR_SELF;
			} else {
				reult = model.addRoster(mContext, personSearchResult.getUserId()
						+ "@" + IMConstants.areaServerName,
						personSearchResult.getUserName());
			}
			return reult;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (RosterModel.RESULT_SUCCESS == result) {
				UIHelper.ToastMessage(mContext, R.string.im_add_result_success);
				
			} else if(RosterModel.RESULT_ERROR_ADD == result){
				
				UIHelper.ToastMessage(mContext, R.string.im_add_result_error_add);
			} else if(RosterModel.RESULT_ERROR_EXIST == result){
				
				UIHelper.ToastMessage(mContext, R.string.im_add_result_error_exist);
			} else if(RosterModel.RESULT_ERROR_SELF == result){
				
				UIHelper.ToastMessage(mContext, R.string.im_add_result_error_self);
			} else {
				
				UIHelper.ToastMessage(mContext, R.string.im_add_result_error_undefined);
			}
			
			mLoadinglayout.setVisibility(View.GONE);
			super.onPostExecute(result);
		}
	}

    /**
     *  发送关注请求 ，在 im_personsearch_item 
     * @param view  cell控件
     *
     */
    public void addRelatedRequest(View view){
        try{
            int posision=(Integer)view.getTag();
            PersonSearchResult personSearchResult=mPersonSearchAdapter.getmPersonSearchResults().get(posision);
            if (personSearchResult!=null){
                String userId =personSearchResult.getUserId();
                //用户添加自己操作
                if (App.getUserId().equals(userId)){
                    Toast.makeText(PersonSearchActicity.this, R.string.im_add_result_error_self, Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String ,Object> params = new HashMap<String,Object>();
                params.put("userid", App.getUserId());
                params.put("followid", userId);
                params.put("type", Constants.PERSON_RELATION_FOLLOW);
                String url = Constants.ADD_FRIENDS_URL ;
                changeFriendState(url,params,(Button)view);
                mPersonSearchAdapter.changeSingleItemState(posision,1);
                saveRelatedUserToDB(personSearchResult);
                //设定标示
                App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_CHANGED;
            }
        } catch(Exception exception){
            Toast.makeText(PersonSearchActicity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
            //设定标示
            App.mRelatedUserStateFlag=Constants.USER_RELATEDSTAT_NOTCHANGE;
        }
    }

    /**
     *  加关注或取消关注 
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
				mLoadinglayout.setVisibility(View.GONE);
				if (respones != Constants.SUCCESS_MSG) {
					//添加关注失败
					Toast.makeText(PersonSearchActicity.this, R.string.api_error, Toast.LENGTH_SHORT).show();
					return;
				} else {
					//添加关注成功
					Toast.makeText(PersonSearchActicity.this, R.string.follow_add, Toast.LENGTH_SHORT).show();
					changeButtonState(view, 1);
				}
			}

			@Override
			public void exception(Exception exception) {
				mLoadinglayout.setVisibility(View.GONE);
				Toast.makeText(PersonSearchActicity.this, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
				exception.printStackTrace();
			}
		});
    }

    /**
     * 跳转到用户信息页面
     * @param view 用户圆形头像view
     */
    public void jumpToSearchUserDetail(View view){
        int posision=(Integer)view.getTag();
        if(mPersonSearchAdapter.getData()==null||mPersonSearchAdapter.getData().size()<=posision){
            return;
        } else {
            PersonSearchResult personSearchResult = mPersonSearchAdapter.getData().get(posision);
            //由于关注关系的原因，导致userid字段的不确定，程序中首先判定一次
            String userId = personSearchResult.getUserId();
            //不是点击用户自身
            if (!App.getUserId().equals(userId)){
                //跳转到好友信息界面
                UIHelper.showFriendInfoActivity(PersonSearchActicity.this,userId);
                mCurrentPosition=posision;
            } else{
                //选择用户自身

            }
        }
    }

    /**
     * 修改数据库中关系状态,根据现在设定，在数据库中增加一条记录
     * @param personSearchResult 需要保存的对象
     */
    private void saveRelatedUserToDB(PersonSearchResult personSearchResult){

        if (personSearchResult.getIsAdded()==1){
            relatedUserDBOperator.insertRelationShipWithSearchPersonObject(personSearchResult);
        } else{
            relatedUserDBOperator.deleteRelationShipWithSearchPersonObject(personSearchResult);
        }
        loadRelatedUserList();
    }

    /**
     * 修改按钮状态
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
        } else {
            //修改为没有添加
            view.setText(R.string.add);
            if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.JELLY_BEAN){
                view.setTextColor(mOldTextColor);
                view.setBackground(mGreenButtonBg);
            }
        }
    }

    /**
     * 获取已经添加的用户列表
     */
    private void loadRelatedUserList(){

        mAddUserList=relatedUserDBOperator.queryRelatedUsersWithId(App.getUserId());
    }

    /**
     * 根据查询的数据结果，在本地数据库中查询，并修改状态，是否为添加或者已添加
     * @param userList
     */
    private List<PersonSearchResult> resetUserRelatedFlag(List<PersonSearchResult> userList){
        List<PersonSearchResult> results=userList;
        boolean isself=false;
        for (int i=0;i<results.size();i++){
            PersonSearchResult result=userList.get(i);
            //判定是否为本人，如果为本人,则过滤
            /*
            if (!isself && result.getUserId().equals(App.getUserId())){
                isself=true;
                results.remove(result);
                i-=1;
                continue;
            }
            */
            //设定搜索结果状态“添加/已添加"
            result.setIsAdded(0);
            for (int j=0;j<mAddUserList.size();j++){
                if (mAddUserList.get(j).getmRelatedId().equals(result.getUserId())){
                    result.setIsAdded(1);
                }
            }
        }
        return  results;
    }

    /**
     *  添加好友结果字符串解析
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
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
