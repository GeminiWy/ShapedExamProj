package com.nd.shapedexamproj.activity.search;

import android.content.*;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.TimelineAdapter;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.search.CourseHorizontalAdapter;
import com.nd.shapedexamproj.view.search.CustomAutoCompleteSearchAdapter;
import com.nd.shapedexamproj.view.search.CustomAutoCompleteSearchView;
import com.nd.shapedexamproj.view.search.UserHorizontalAdapter;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.activity.search
 * @文件名: PlaygroundSearchActivity
 * @文件描述: 操场搜索类
 *           用户、专业、课程或者内容
 * @创建人: xuwenzhuo
 * @创建时间: 2014/12/11 10:38
 * @Copyright: 2014 Tming All rights reserved.
 */

public class PlaygroundSearchActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG="PlaygroundSearchActivity";
    public static final String BRODCAST_SEARCHTEXT_CHANGED="SearchTextChanged";
    private static final int mPageSize=10;
    private static final int MAX_USER_SEARCH_NUMBER = mPageSize*10; // 在控件上最多能够显示的数据条目数
    private static final int MAX_SEARCH_ORIGINAL_VALUES_NUM = 5;    // 最大的搜索记录值

    //检索类别
    private static final int SEARCHTYPE_COURSE=0;
    //private static final int SEARCHTYPE_SUBJECT=1;
    private static final int SEARCHTYPE_USER=1;
    private static final int SEARCHTYPE_TIMELINE=2;

    // 列表类型，0-我的班级，1-我的教学点，2-开放大学
    private static final String SEARCHPUB_TYPE="2";
    //控件
    private Context mContext;
    private TextView mCourseTextView,mUserTextView,mTimeLineTextView;
    private ImageView mBg1,mBg2,mBg3;
    private Button mSearchButton;
    private CustomAutoCompleteSearchView mCustomSearchEd;
    private View mLoadingView;
    private View mPubListView;    //动态列表
    private RefreshableListView mPubListRefreshListView;//下拉控件
    private View mCourseListView; //课程列表
    private RefreshableListView mCourseRefreshListView;
    private View mUserListView;   //用户列表
    private RefreshableListView mUserRefreshListView;

    private ViewPager mResultViewPager;  //列表视图
    private View mUserSearchTipView;
    private View mHotPubSearchTipView;
    private View mCourseTipView;
    private View mSeeAllCourseView;      //查看全部课程

    //RefreshListView 控件头部视图
    private View mCommonRLVHeaderViewCourseList;
    private View mCommonRLVHeaderViewUserList;
    private View mCommonRLVHeaderViewPubList;

    //适配器
    private SearchResultViewPagerAdapter mSearchResultAdapter;
    private UserHorizontalAdapter mUserResultAdapter;	//用户检索
    private CourseHorizontalAdapter mCourseHlvAdapter;  //课程
    private TimelineAdapter mPubResultAdapter;          //动态
    private CustomAutoCompleteSearchAdapter mCustomSearchAdapter;

    //数据
    private SharedPreferences mSearchRecordSp = null;
    private List<CourseInfo> mCourseInfoList = new ArrayList<CourseInfo>();           //课程数据
    private List<UserInfo> mUserInfoList = new ArrayList<UserInfo>();               //用户数据
    private List<SpecialtyInfo> mSpecialtyInfoList = new ArrayList<SpecialtyInfo>();     //专业数据
    private List<Timeline> mTimelineList = new ArrayList<Timeline>();               //动态数据
    private List<View> mPagerViewList = new ArrayList<View>();
    private ArrayList<String> mOriginalValues = new ArrayList<String>();

    private int mCurrentSearchType;
    private int mPageNo=1;               //当前检索页面，包含三个子页面

    private int mUserResultCount=0;      //检索的用户数量
    private int mCourseResultCount=0;    //检索的课程数量
    private int mPubResultCount=0;       //检索的动态数量

    private int mCurrentPageNoUser=1;    //用户当前加载页面
    private int mCurrentPageNoCourse=1;  //课程当前加载页面
    private int mCurrentPageNoPublish=1; //动态当前加载页面

    private boolean mCourseLoadMore=false;//课程，加载更多/刷新
    private boolean mUserLoadMore=false;  //用户，加载更多/刷新
    private boolean mPubLoadMore=false;   //动态，加载更多/刷新

    /**
     * 搜索说明：
     * 1、输入框中输入关键字，没有点击“搜索”按钮之前，不记录关键字到  mSearchedKeyWords 变量
     * 2、点击“搜索”按钮之后，根据当前所在的tab 进行检索，同时记录 mSearchedKeyWords 变量
     * 3、tab 切换时：1）检查当前tab页面是否已经执行 mSearchedKeyWords 的关键词检索
     *              2）如果没有执行，执行检索 保存 mSearchedKeyWords 关键词到对应的关键词变量
     *              3）如果执行了，跳过
     * 
     *
     */
    private String mSearchedKeyWords="";      //输入框中，已经执行了检索的关键词 ,点击了“搜索”按钮，不一定和输入框中的关键字一致
    private String mQueryCourseKeyWords="";   //课程模块最近检索关键词
    private String mQueryUserKeyWords="";     //用户模块最近检索关键词
    private String mQueryPubKeyWords="";      //热点模块最近检索关键词

    /**
     * 用来保持搜索类型
     * */
    private SharedPreferences mShareP;
    private SharedPreferences.Editor mEditor;

    //消息处理，用于检索出结果后对界面更新,主要用于解除refreshlistview的刷新状态
    private Handler mHandler=new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEARCHTYPE_COURSE:
                    //检索课程
                    mCourseRefreshListView.onRefreshComplete();
                    break;
                case SEARCHTYPE_USER:
                    //检索用户
                    mUserRefreshListView.onRefreshComplete();
                    break;
                case SEARCHTYPE_TIMELINE:
                    //检索动态
                    mPubListRefreshListView.onRefreshComplete();
                    break;
                default:

                    break;
            }
        }
    };

    /**
     * 广播，主要用于监听关键字输入状态发生的变化
     */
    private BroadcastReceiver mSearchBrodCastReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BRODCAST_SEARCHTEXT_CHANGED.equals(intent.getAction())) {
                //在修课程信息发生改变
                setRefreshListState();
            }
        }
    };

    /**
     *  广播的注册和取消 
     */
    private void registerBroadCast(){
        IntentFilter intentFilter=new IntentFilter();
        //输入框发生改变
        intentFilter.addAction(BRODCAST_SEARCHTEXT_CHANGED);
        registerReceiver(mSearchBrodCastReciver, intentFilter);
    }

    private void unregisterBroadCast(){
        unregisterReceiver(mSearchBrodCastReciver);
    }

    @Override
    public int initResource() {
        return R.layout.playground_search_activity;
    }

    @Override
    public void initComponent() {

        mContext=PlaygroundSearchActivity.this;

        mLoadingView=(View) findViewById(R.id.loading_layout);
        mLoadingView.setVisibility(View.GONE);
        mCourseTextView=(TextView) findViewById(R.id.course_textview);
        mUserTextView=(TextView) findViewById(R.id.user_textview);
        mTimeLineTextView=(TextView) findViewById(R.id.timeline_textview);

        mBg1=(ImageView) findViewById(R.id.bg_1);
        mBg2=(ImageView) findViewById(R.id.bg_2);
        mBg3=(ImageView) findViewById(R.id.bg_3);
        mBg1.setVisibility(View.GONE);
        mBg2.setVisibility(View.GONE);
        mBg3.setVisibility(View.GONE);
        //搜索框
        mCustomSearchEd = (CustomAutoCompleteSearchView) findViewById(R.id.search_all_auto_complete_ed);

        //搜索结果viewpager
        mResultViewPager=(ViewPager) findViewById(R.id.playground_search_result_viewpager);
        //动态列表
        mPubListView= LayoutInflater.from(mContext).inflate(R.layout.playground_search_result_publish_layout, null);
        mPubListRefreshListView=(RefreshableListView)mPubListView.findViewById(R.id.search_result_timeline_lv);
        //课程列表
        mCourseListView= LayoutInflater.from(mContext).inflate(R.layout.playground_search_result_course_layout,null);
        mCourseRefreshListView=(RefreshableListView) mCourseListView.findViewById(R.id.search_result_course_lv);
        //用户列表
        mUserListView= LayoutInflater.from(mContext).inflate(R.layout.playground_search_result_user_layout,null);
        mUserRefreshListView=(RefreshableListView)mUserListView.findViewById(R.id.search_result_user_lv);
        //查看全部课程
        mSeeAllCourseView=(View) mCourseListView.findViewById(R.id.see_allcourse_view);
        //头部视图
        mCommonRLVHeaderViewCourseList=LayoutInflater.from(mContext).inflate(R.layout.playground_search_refreshlistview_header_view,null);
        mCommonRLVHeaderViewCourseList.setTag("mCommonRLVHeaderViewCourseList");

        mCommonRLVHeaderViewUserList=LayoutInflater.from(mContext).inflate(R.layout.playground_search_refreshlistview_header_view,null);
        mCommonRLVHeaderViewUserList.setTag("mCommonRLVHeaderViewUserList");

        mCommonRLVHeaderViewPubList=LayoutInflater.from(mContext).inflate(R.layout.playground_search_refreshlistview_header_view,null);
        mCommonRLVHeaderViewPubList.setTag("mCommonRLVHeaderViewPubList");
        //搜索按钮
        mSearchButton=(Button) findViewById(R.id.custom_auto_complete_confirm_btn);
        //tip视图
        mUserSearchTipView=(View) mUserListView.findViewById(R.id.friends_tip_view);
        mHotPubSearchTipView=(View) mPubListView.findViewById(R.id.hot_tip_view);
        mCourseTipView=(View)mCourseListView.findViewById(R.id.course_tip_view);
        registerBroadCast();
        //初始化设定下拉列表不可拉动
        setRefreshState(false);
    }

    @Override
    public void initData() {

        //用户检索结果
        mUserResultAdapter = new UserHorizontalAdapter(PlaygroundSearchActivity.this, mUserInfoList, MAX_USER_SEARCH_NUMBER);
        mUserRefreshListView.setAdapter(mUserResultAdapter);
        mUserRefreshListView.setDividerHeight(0);
        //动态检索结果
        mPubResultAdapter=new TimelineAdapter(PlaygroundSearchActivity.this);
        mPubListRefreshListView.setAdapter(mPubResultAdapter);
        mPubListRefreshListView.setDividerHeight(0);

        //课程检索结果
        mCourseHlvAdapter = new CourseHorizontalAdapter(PlaygroundSearchActivity.this, mCourseInfoList, MAX_USER_SEARCH_NUMBER);
        mCourseRefreshListView.setAdapter(mCourseHlvAdapter);
        mCourseRefreshListView.setDividerHeight(0);

        //搜索框的设定
        mSearchRecordSp = getSharedPreferences(UIHelper.getCurrentUserNameSPName(mContext), Context.MODE_PRIVATE);
        mCustomSearchAdapter=new CustomAutoCompleteSearchAdapter(this, mOriginalValues, MAX_SEARCH_ORIGINAL_VALUES_NUM, mSearchRecordSp);
        //TODO 暂时屏蔽历史记录功能
        //mCustomSearchEd.setAdapter(mCustomSearchAdapter);
        //加载历史搜索过的关键字
        //getSearchHistoryData();
        //mCustomSearchAdapter.notifyDataSetChanged();

        //viewpager设定
        mPagerViewList.add(mCourseListView);
        mPagerViewList.add(mUserListView);
        mPagerViewList.add(mPubListView);
        mSearchResultAdapter=new SearchResultViewPagerAdapter();
        mResultViewPager.setAdapter(mSearchResultAdapter);
        //默认搜索类别 "相关课程"
        mCurrentSearchType=SEARCHTYPE_COURSE;
        initSreachTypeSharedPreferences();
        //默认设定为pagerview的第一个视图
        int tabIndex=getIntent().getIntExtra("tabIndex",0);
        //切换到制定视图
        changeTabUI(tabIndex);
        mResultViewPager.setCurrentItem(tabIndex);
        mCurrentSearchType=tabIndex;
    }

    /**
     * 获取搜索类型 SharedPreferences对象 ，为分类搜索保持搜索类别提供搜索类型
     * */
     private void initSreachTypeSharedPreferences(){
        mShareP = PlaygroundSearchActivity.this.getSharedPreferences(Constants.SHARE_PREFERENCES_SEARCH_TYPE_FILE_NAME, Context.MODE_PRIVATE);
        mEditor = mShareP.edit();
    }

    @Override
    public void initAuthoriy() {
        super.initAuthoriy();
    }

    @Override
    public void addListener() {
        mTimeLineTextView.setOnClickListener(this);
        mCourseTextView.setOnClickListener(this);
        mUserTextView.setOnClickListener(this);
        mSearchButton.setOnClickListener(this);
        mResultViewPager.setOnPageChangeListener(new SearchResultPagerChangeListener());

        //跳转到全部课程页面
        mSeeAllCourseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //查看全部课程
                Constants.leftInnerType = 0;
                //设定title
                Constants.leftInnerName= getString(R.string.all_course_text);
                UIHelper.showAllCourseListActivity(PlaygroundSearchActivity.this,"checkallstudyres");
            }
        });

        //用户加载、刷新操作
        mUserRefreshListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新
                mUserLoadMore=false;
                mCurrentPageNoUser=1;
                doSearchWithType(SEARCHTYPE_USER,mCurrentPageNoUser);
            }

            @Override
            public void onLoadMore() {
                //检索更多
                mUserLoadMore=true;
                mCurrentPageNoUser+=1;
                doSearchWithType(SEARCHTYPE_USER,mCurrentPageNoUser);
            }
        });

        //课程、专业刷新加载操作
        mCourseRefreshListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新
                mCourseLoadMore=false;
                mCurrentPageNoCourse=1;
                doSearchWithType(SEARCHTYPE_COURSE,mCurrentPageNoCourse);
            }

            @Override
            public void onLoadMore() {
                //加载更多
                mCourseLoadMore=true;
                mCurrentPageNoCourse+=1;
                doSearchWithType(SEARCHTYPE_COURSE,mCurrentPageNoCourse);
            }
        });

        //动态刷新、加载更多
        mPubListRefreshListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
           @Override
           public void onRefresh() {
               //刷新
               mPubLoadMore=false;
               mCurrentPageNoPublish=1;
               doSearchWithType(SEARCHTYPE_TIMELINE,mCurrentPageNoPublish);
           }

           @Override
           public void onLoadMore() {
                //加载更多
                mPubLoadMore=true;
                mCurrentPageNoPublish+=1;

               doSearchWithType(SEARCHTYPE_TIMELINE,mCurrentPageNoPublish);
           }
       });

        //历史记录listview点击操作
        mCustomSearchEd.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                Object selectedItem = mCustomSearchAdapter.getItem(position);
                CharSequence charSequence = mCustomSearchAdapter.getFilter().convertResultToString(selectedItem);
                mCustomSearchEd.mContentEd.setText(charSequence);
                //设置光标显示位置
                mCustomSearchEd.mContentEd.setSelection(charSequence.toString().length());
                mCustomSearchEd.listView.setVisibility(View.GONE);
                //直接检索
                doSubSearchWithType(mCurrentSearchType);
                //doSearchWithType(mCurrentSearchType,1);
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.course_textview:
                //课程信息
                mCurrentSearchType=SEARCHTYPE_COURSE;
                mResultViewPager.setCurrentItem(0);
                break;
            case R.id.user_textview:
                //用户信息
                mCurrentSearchType=SEARCHTYPE_USER;
                mResultViewPager.setCurrentItem(1);
                break;
            case R.id.timeline_textview:
                //用户动态信息
                mCurrentSearchType=SEARCHTYPE_TIMELINE;
                mResultViewPager.setCurrentItem(2);
                break;
            case R.id.custom_auto_complete_confirm_btn:
                //点击搜索按钮操作:1）搜索框有内容：搜索 2）搜索框没有内容：退出
                if(mCustomSearchEd.mIsKeyWordVaild){
                    //判定是否为第一次检索，是，则开启下拉框的刷新操作
                    if (StringUtils.isEmpty(mSearchedKeyWords)){
                        //设定下拉框为可以拉动
                        setRefreshState(true);
                    }
                    //根据当前的检索状态开始搜索,默认为检索第一页
                    if (checkInputText()){
                        //判定关键字有更换，复位操作
                        mPubLoadMore=mUserLoadMore=mCourseLoadMore=false;
                        mCurrentPageNoCourse=mCurrentPageNoUser=mCurrentPageNoPublish=1;
                    }
                    doSubSearchWithType(mCurrentSearchType);
                    //历史记录框消失
                    mCustomSearchEd.listView.setVisibility(View.GONE);
                } else{
                    //没有关键字,直接退出
                    finish();
                }
                break;
            default:

                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterBroadCast();
        super.onDestroy();
    }

    /**
     * 验证用户输入的关键字，并操作，是否保存
     */
    private boolean checkInputText(){
        boolean changed=false;
        String newKeys=mCustomSearchEd.getSearchContent();
        if (StringUtils.isEmpty(newKeys)){
            //关键字为空，提示用户
            Toast.makeText(mContext, getResources().getString(R.string.search_empty_error), Toast.LENGTH_SHORT).show();
        } else{
            //两个关键字相等
            if (newKeys.equals(mSearchedKeyWords)){
                //不做任何操作

            } else{
                mSearchedKeyWords=newKeys;
                changed=true;
            }
        }
        return changed;
    }

    /**
     * 更换tab界面，用户选择 tab的操作 
     * @param tabIndex
     */
    private void changeTabUI(int tabIndex){

       switch (tabIndex){
           case 0:
               mCourseTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_selectcolor));
               mUserTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mTimeLineTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mBg1.setVisibility(View.VISIBLE);
               mBg2.setVisibility(View.INVISIBLE);
               mBg3.setVisibility(View.INVISIBLE);
               break;
           case 1:
               mCourseTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mUserTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_selectcolor));
               mTimeLineTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mBg1.setVisibility(View.INVISIBLE);
               mBg2.setVisibility(View.VISIBLE);
               mBg3.setVisibility(View.INVISIBLE);
               break;
           case 2:
               mCourseTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mUserTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mTimeLineTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_selectcolor));
               mBg1.setVisibility(View.INVISIBLE);
               mBg2.setVisibility(View.INVISIBLE);
               mBg3.setVisibility(View.VISIBLE);
               break;
           default:
               mCourseTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_selectcolor));
               mUserTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               mTimeLineTextView.setTextColor(getResources().getColor(R.color.playgorund_search_tabtext_normalcolor));
               break;
       }
    }

    /**
     *  设定刷新视图的文字及要素个数 
     * @param headerView  RLV头文字
     * @param text 文字
     * @param countStr 条目数
     */
    private void changeRLVHeaderView(View headerView,String text,String countStr){

        if (null==headerView){
            return;
        } else {
            if (!StringUtils.isEmpty(text)){
                ((TextView)headerView.findViewById(R.id.search_rlv_header_text)).setText(text);
            }
            ((TextView)headerView.findViewById(R.id.search_rlv_header_count_text)).setText(countStr);
        }
    }

    /**
     * 根据数据类别查找数据，用于tab切换时进行数据查询操作
     * @param type
     */
    private void doSubSearchWithType(int type){

        int pageNo=1;
        switch (type){
            case SEARCHTYPE_COURSE:
                //首先判定是否已经检索过，检索过直接跳过查询步骤
                if (mSearchedKeyWords!=null && mQueryCourseKeyWords.equals(mSearchedKeyWords)){
                    return;
                }
                pageNo=mCurrentPageNoCourse;
                break;
            case SEARCHTYPE_USER:
                if (mSearchedKeyWords!=null && mQueryUserKeyWords.equals(mSearchedKeyWords)){
                    return;
                }
                pageNo=mCurrentPageNoUser;
                break;
            case SEARCHTYPE_TIMELINE:
                if (mSearchedKeyWords!=null && mQueryPubKeyWords.equals(mSearchedKeyWords)){
                    return;
                }
                pageNo=mCurrentPageNoPublish;
                break;
        }
        //设定当前检索类别
        mCurrentSearchType=type;
        doSearchWithType(type,pageNo);
    }

    /**
     * 分类检索，课程、用户、动态信息
     * @param type    检索类型
     * @param PageNo  当前加载的页面，刷新为：1
     *                             更多为：> 1
     */
    private void doSearchWithType(int type,int PageNo){

        // 关键字以执行搜索操作的最近一次搜索框内容
        // mSearchedKeyWords 为用户在点击“搜索”按钮之后记录
        // 关键字以用户点击按钮操作为主，不与输入框的实际内容统一
        String keyWords =mSearchedKeyWords ;
        //检索关键字已经发生变化
        String pageNoStr=(PageNo>0)?PageNo+"":"1";
        //关键字验证，如果为空，返回
        if (StringUtils.isEmpty(keyWords)){
            return;
        }

        mLoadingView.setVisibility(View.VISIBLE);
        switch (type){
            case SEARCHTYPE_COURSE:
                requestSearchCourseResult(pageNoStr,keyWords);
                //记录检索关键字
                mQueryCourseKeyWords=keyWords;
                break;
            case SEARCHTYPE_USER:
                requestSearchUserResult(pageNoStr,keyWords);
                //记录检索关键字
                mQueryUserKeyWords=keyWords;
                break;
            case SEARCHTYPE_TIMELINE:
                requestSearchPubResult(SEARCHPUB_TYPE,PageNo,keyWords);
                //记录检索关键字
                mQueryPubKeyWords=keyWords;
                break;
            default:

                break;
        }
        //保存检索数据到本地
        saveSearchTypeData(type);
        saveSearchKeyWords();
        //隐藏软键盘
        setKeyBoradVisiable(false);
    }

    /**
     * 设定用户输入软键盘是否课件
     * @param visiable
     */
    private void setKeyBoradVisiable(boolean visiable){

        if (!visiable){
            // 收起软键盘
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(PlaygroundSearchActivity.this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } else{
            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(PlaygroundSearchActivity.this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     *  利用消息，修改界面 
     * @param whatType
     */
    private void msgHandlerChanged(int whatType){

        Message msg = new Message();
        msg.what = whatType;
        mHandler.sendMessage(msg);
    }

    /**
     * 请求搜索相关课程接口
     * */
    private void requestSearchCourseResult(String pageNum, String keyWords) {

        TmingHttp.asyncRequest(ServerApi.getSearchCourseList(pageNum, mPageSize + "", keyWords), null, new TmingHttp.RequestCallback<List<CourseInfo>>() {

            @Override
            public List<CourseInfo> onReqestSuccess(String respones) throws Exception {

                return searchCourseResultJSONParsing(respones);
            }

            @Override
            public void success(List<CourseInfo> respones) {
                mLoadingView.setVisibility(View.GONE);
                mCourseRefreshListView.onRefreshComplete();
                if(!mCourseLoadMore){
                    mCourseInfoList.clear();
                }
                mCourseInfoList.addAll(respones);
                mCourseHlvAdapter.notifyDataSetChanged();
                //如果检索结果>1，隐藏tip视图
                if (respones.size()>0){
                    mCourseTipView.setVisibility(View.GONE);
                }
                changeCourseListHeadView();
            }

            @Override
            public void exception(Exception exception) {
                mLoadingView.setVisibility(View.GONE);
                mCourseRefreshListView.onRefreshComplete();
                mCourseResultCount=0;
                changeCourseListHeadView();
                mQueryCourseKeyWords="";
            }
        });
    }

    /**
     * 修改Course List的Head View
     */
    private void changeCourseListHeadView(){
        //添加头view
        if (null!=mCourseRefreshListView.getRefreshableView().findViewWithTag(mCommonRLVHeaderViewCourseList.getTag())){
            //如果已经有header，修改
            changeRLVHeaderView(mCourseRefreshListView,null,"（"+mCourseResultCount+"门）");
        } else {
            //如果没有header，添加
            mCourseRefreshListView.getRefreshableView().addHeaderView(mCommonRLVHeaderViewCourseList);
            changeRLVHeaderView(mCourseRefreshListView, "相关课程","（"+mCourseResultCount+"门）");
        }
    }

    /**
     * 请求搜索相关用户接口
     * */
    private void requestSearchUserResult(String pageNum, String keyWords) {

        TmingHttp.asyncRequest(ServerApi.getSearchUserInfoList(pageNum, mPageSize + "", keyWords), null, new TmingHttp.RequestCallback<List<UserInfo>>() {

            @Override
            public List<UserInfo> onReqestSuccess(String respones) throws Exception {

                return searchUserResultJSONParsing(respones);
            }

            @Override
            public void success(List<UserInfo> respones) {
                mLoadingView.setVisibility(View.GONE);
                //刷新结束
                mUserRefreshListView.onRefreshComplete();
                //数据修改
                if (!mUserLoadMore){
                    mUserInfoList.clear();
                }
                mUserInfoList.addAll(respones);
                mUserResultAdapter.notifyDataSetChanged();
                //如果检索结果>1，隐藏tip视图
                if (respones.size()>0){
                    mUserSearchTipView.setVisibility(View.GONE);
                }
                changeUserListHeadView();
            }

            @Override
            public void exception(Exception exception) {
                mLoadingView.setVisibility(View.GONE);
                mUserRefreshListView.onRefreshComplete();
                mUserResultCount=0;
                changeUserListHeadView();
                mQueryUserKeyWords="";
            }});
    }

    /**
     * 修改用户list视图head view
     */
    private void changeUserListHeadView(){

        //添加头view
        if (null!=mUserRefreshListView.getRefreshableView().findViewWithTag(mCommonRLVHeaderViewUserList.getTag())){
            //如果已经有header，修改
            changeRLVHeaderView(mUserRefreshListView,null,"（"+mUserResultCount+"人）");
        } else {
            //如果没有header，添加
            mUserRefreshListView.getRefreshableView().addHeaderView(mCommonRLVHeaderViewUserList);
            changeRLVHeaderView(mUserRefreshListView, "相关用户","（"+mUserResultCount+"人）");
        }
    }

    /**
     * 类微博数据请求
     * */
    private void requestSearchPubResult(String typeValue, int pageNo,String keyWords) {

        TmingHttp.asyncRequest(ServerApi.Timeline.getSearchTimelineListUrl(typeValue, pageNo, mPageSize, keyWords), null, new TmingHttp.RequestCallback<List<Timeline>>() {

            @Override
            public List<Timeline> onReqestSuccess(String respones) throws Exception {
                JSONObject jsonObject = new JSONObject(respones);
                //设定全部结果数目
                mPubResultCount=jsonObject.getInt("totalSize") ;
                if (JsonUtil.checkResultIsOK(jsonObject)) {
                    List<Timeline> timelines = JsonUtil.paraseJsonArray(jsonObject.getJSONArray("data"), Timeline.class);
                    return timelines;
                }
                throw new Exception("发生错误!");
            }

            @Override
            public void success(List<Timeline> respones) {
                mLoadingView.setVisibility(View.GONE);
                mPubListRefreshListView.onRefreshComplete();

                if (!mPubLoadMore){
                    //刷新操作
                    mPubResultAdapter.clear();
                }
                mTimelineList.clear();
                mTimelineList.addAll(respones);
                //追加的方式加载到后面
                mPubResultAdapter.addItemCollection(mTimelineList);
                mPubResultAdapter.notifyDataSetChanged();

                //如果检索结果>1，隐藏tip视图
                if (respones.size()>0){
                    mHotPubSearchTipView.setVisibility(View.GONE);
                }
                changePubListHeadView();
            }

            @Override
            public void exception(Exception exception) {
                mLoadingView.setVisibility(View.GONE);
                mPubListRefreshListView.onRefreshComplete();
                mQueryPubKeyWords="";
                mPubResultCount=0;
                changePubListHeadView();
                Toast.makeText(mContext, getResources().getString(R.string.net_error_tip), Toast.LENGTH_SHORT).show();
            }});
    }

    /**
     * 修改publist头部view
     */
    private void changePubListHeadView(){
        //添加头view
        if (null!=mPubListRefreshListView.getRefreshableView().findViewWithTag(mCommonRLVHeaderViewPubList.getTag())){
            //如果已经有header，修改
            changeRLVHeaderView(mPubListRefreshListView,null,"（"+mPubResultCount+"个）");
        } else {
            //如果没有header，添加
            mPubListRefreshListView.getRefreshableView().addHeaderView(mCommonRLVHeaderViewPubList);
            changeRLVHeaderView(mPubListRefreshListView, "相关动态", "（"+mPubResultCount+"个）");
        }
    }

    /**
     * 请求搜索相关专业接口
     * */
    private void requestSearchSubjectsResult(String pageNum, String key) {

        TmingHttp.asyncRequest(ServerApi.getSearchSpecialtyList(pageNum, mPageSize + "", key), null, new TmingHttp.RequestCallback<List<SpecialtyInfo>>() {

            @Override
            public List<SpecialtyInfo> onReqestSuccess(String respones) throws Exception {
                return searchSpecialtyResultJSONParsing(respones);
            }

            @Override
            public void success(List<SpecialtyInfo> respones) {
                Message msg = new Message();
                //msg.what = SEARCHTYPE_SUBJECT;
                msg.obj = respones;
                mHandler.sendMessage(msg);
            }

            @Override
            public void exception(Exception exception) {
                mLoadingView.setVisibility(View.GONE);
            }});
    }

    /**
     * 相关课程数据解析
     */
    private List<CourseInfo> searchCourseResultJSONParsing(String result) {
        List<CourseInfo> courseInfos = new ArrayList<CourseInfo>();
        try {
            JSONObject data = new JSONObject(result);
            JSONArray list = data.getJSONObject("res").getJSONObject("data").getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                CourseInfo info = new CourseInfo();
                info.setCourseId(item.getString("course_id"));
                info.setCourseName(item.getString("course_name"));
                info.setCredit(item.getString("credit"));
                info.setLecturer(item.getString("lecturer"));
                info.setStuNum(item.getInt("stu_num"));
                info.setImg(item.getString("img"));
                courseInfos.add(info);
            }
            mCourseResultCount=data.getJSONObject("res").getJSONObject("data").getInt("total");
        } catch (JSONException e) {
            mCourseResultCount=0;
            e.printStackTrace();
        }
        return courseInfos;
    }

    /**
     * 相关专业数据解析
     */
    private List<SpecialtyInfo> searchSpecialtyResultJSONParsing(String result) {
        List<SpecialtyInfo> specialtyInfos = new ArrayList<SpecialtyInfo>();
        try {
            JSONObject data = new JSONObject(result);
            JSONArray list = data.getJSONObject("res").getJSONObject("data").getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                SpecialtyInfo info = new SpecialtyInfo();
                info.setProId(item.getString("pro_id"));
                info.setProName(item.getString("pro_name"));
                info.setYears(item.getInt("years"));
                info.setCash(item.getString("cash"));
                info.setTermId(item.getString("term_id"));
                Log.e(TAG, "term_id :=" + item.getString("term_id"));
                specialtyInfos.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSpecialtyInfoList = specialtyInfos;
        Log.e(TAG, "mSpecialtyInfoList size：=" + mSpecialtyInfoList.size());
        return specialtyInfos;
    }

    /**
     * 相关用户数据解析
     */
    private List<UserInfo> searchUserResultJSONParsing(String result) {
        List<UserInfo> userInfos = new ArrayList<UserInfo>();
        try {
            JSONObject data = new JSONObject(result);
            JSONArray list = data.getJSONObject("res").getJSONObject("data").getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                UserInfo info = new UserInfo();
                info.setUserId(item.getString("user_id"));
                info.setUserName(item.getString("user_name"));
                info.setType(item.getInt("type"));
                info.setAvatar(item.getString("avatar"));
                userInfos.add(info);
            }
            mUserResultCount=data.getJSONObject("res").getJSONObject("data").getInt("total");
        } catch (JSONException e) {
            mUserResultCount=0;
            e.printStackTrace();
        }
        return userInfos;
    }

    /**
     * 获取相关用户数据
     */
    private void getUserData(){
        requestSearchUserResult("1", mCustomSearchEd.getSearchContent());
    }

    /**
     * 获取相关专业数据
     */
    private void getSpecialtyData(){
        requestSearchSubjectsResult("1", mCustomSearchEd.getSearchContent());
    }

    /**
     * 获取相关课程数据
     */
    private void getCourseData(){
        requestSearchCourseResult("1", mCustomSearchEd.getSearchContent());
    }

    /**
     * 获取相关类微博数据
     */
    private void getPubData(){
        Log.e(TAG, "getPubData 11");
        mLoadingView.setVisibility(View.VISIBLE);
        //默认检索全开放大学的动态信息
        String keyWords = mCustomSearchEd.getSearchContent();
        requestSearchPubResult(SEARCHPUB_TYPE, mPageNo, keyWords);
    }

    /**
     *  搜索结果展示页面适配器 
     */
    private class SearchResultViewPagerAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return mPagerViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view==o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mPagerViewList.get(position), 0);
            return mPagerViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    /**
     *  pager 控件切换监听 
     */
    private class SearchResultPagerChangeListener implements ViewPager.OnPageChangeListener{


        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            i=(i>3||i<0)?0:i;
            //修改tab视图
            changeTabUI(i);
            //切换tab执行 操作判别、关键字查询操作
            doSubSearchWithType(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }

    /**
     *  根据搜索框内容设定refreshlistview 刷新框的状态
     */
    private void setRefreshListState(){
        //设定refreshlistview为不可拉动
        String keyWords = mCustomSearchEd.getSearchContent();
        if (StringUtils.isEmpty(keyWords)){
            mCourseRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mUserRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mPubListRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        } else{
            mCourseRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            mUserRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            mPubListRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        }
    }

    /**
     * 设定下拉框是否可用,主要用于在初始状态下，禁用下拉列表的下拉操作
     * @param refreshState 下拉框状态
     */
    private void setRefreshState(boolean refreshState){

        if (!refreshState){
            mCourseRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mUserRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mPubListRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        } else {
            mCourseRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            mUserRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
            mPubListRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);
        }
    }

    /**
     * 获取搜索的历史关键字
     */
    private void getSearchHistoryData() {
        String history = mSearchRecordSp.getString(Constants.SHARE_PREFERENCES_HISTORY_DATA, "");
        if (!history.trim().equals("")) {
            String[] results = history.split(",");
            for (int i = 0; i < results.length; i++) {
                mOriginalValues.add(results[i]);
            }
        }
    }

    /**
     * 保存搜索的内容
     */
    private void saveSearchKeyWords() {
        String msg = mCustomSearchEd.getSearchContent();
        if (msg != null && msg.trim().length() != 0) {
            String history = mSearchRecordSp.getString(Constants.SHARE_PREFERENCES_HISTORY_DATA, "");
            boolean contain = isContain(msg);
            if (!contain) {
                // 保证最多只保存8次历史记录,每次都删除最老的（最前面），添加都添加在最后面
                if (mOriginalValues.size() >= MAX_SEARCH_ORIGINAL_VALUES_NUM) {
                    history = history.substring(history.indexOf(",") + 1);
                    mOriginalValues.remove(0);
                }
                StringBuilder sb = new StringBuilder(history);
                sb.append(msg.trim() + ",");
                mSearchRecordSp.edit().putString(Constants.SHARE_PREFERENCES_HISTORY_DATA, sb.toString()).commit();
                mOriginalValues.add(msg.trim());
                mCustomSearchAdapter.notifyDataSetInvalidated();
            }
        }
    }

    /**
     * 保持搜索类型数据
     * @param type 搜索类型
     */
    private void saveSearchTypeData(int type){
        //存入数据
        mEditor.putInt(Constants.SHARE_PREFERENCES_SEARCH_TYPE_DATA, type);
        mEditor.commit();
    }

    /**
     * 判断是否已经保存该字符串
     * @param msg 需要判断的字符串
     * @return 不包含：false；包含：true
     */
    private boolean isContain(String msg){
        String history = mSearchRecordSp.getString(Constants.SHARE_PREFERENCES_HISTORY_DATA, "");
        if (!history.trim().equals("")) {
            String[] results = history.split(",");
            for (int i = 0; i < results.length; i++) {
                if(results[i].equals(msg.trim())){
                    return true;
                }
            }
            return false;
        }else{
            return false;
        }
    }

}
