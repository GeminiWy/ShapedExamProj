package com.nd.shapedexamproj.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.MainTabActivity;
import com.nd.shapedexamproj.adapter.CourseLearningAdapter;
import com.nd.shapedexamproj.entity.LearningCourseInfoEntity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @项目名称: OpenUniversity
 * @版本号 V1.1.0
 * @所在包: com.tming.openuniversity.fragment
 * @文件名: StudentCourseFragment
 * @文件描述: StudentCourseFragment 学生学习课堂Fragment
 * @创建人: xuwenzhuo
 * @创建时间: 2014/10/26 10:38
 * @Copyright: 2014 Tming All rights reserved.
 */

public class StudentCourseFragment extends BaseFragment {

    private static final String TAG = "StudentCourseFragment";
    public static final String COURSEINFO_CHANGE="CourseInfoChange";
    public static final String MYPHOTO_CHANGE="MyPhotoChange";
    private final static int NOT_LOADED=1;
    private final static int COURSE_LOADED=2;
    private final static int USERINFO_LOADED=3;
    private final static int PHOTO_LOADED=4;
    private final static int PAGESIZE=20;
    private final static int CONNECT_FAILED = 0;
    private final static int SUCCESS = 11;
    private final static int FAILED = 32;
    private final static int GETMESSAGE=33;
    private final static int LOADOVER=34;

    private View mStudentFragmentView;
    private View mEmptyHeaderView;
    private ImageView /*mStudyHistoryImgView,*/mSexIV;
    private TextView mDoWorkImgView, mErrorImgView;
    private TextView mLearningCreditTextView;
    private TextView mLearnedCreditTextView;
    private TextView mUserNameTextView;
    private TextView mCourseCountTextView;
    private CircularImage mStudentHeadImg, mCourseCountImage;
    private RefreshableListView mLearningCourseRListView;
    private CourseLearningAdapter mLearningCourseAdapter;
    private View mLoadingView,mNoLearningView,mNetWorkTipView;
    private TmingCacheHttp mTmingCacheHttp;
    private View.OnClickListener mClickListener;
    private View mSelectedCourseView;
    private Button mSeeAllCoursesButton;
    private int mPageNum=1;
    private int mLearningCredit, mLearnedCredit,mLearningCourseCount=0,mLearnedCourseCount=0;
    private boolean mLoadMore;
    private String mPhotoUrl;
    private int mLoadSuccess=NOT_LOADED; //判定用户信息是否加载成功,初始化默认没有加载成功
    private List<LearningCourseInfoEntity> mLearningCourses = new ArrayList<LearningCourseInfoEntity>();

    //自定义handler 用于处理学生课堂信息展示及相关界面处理
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CONNECT_FAILED:

                    break;
                case GETMESSAGE:
                    //在修学分
                    if (mLearningCredit >= 0) {
                        mLearningCreditTextView.setText("" + mLearningCredit);
                    }
                    //已修学分
                    if (mLearnedCredit >= 0) {
                        mLearnedCreditTextView.setText("" + mLearnedCredit);
                    }
                    //在修课程
                    if (mLearningCourseCount>=0){
                        mCourseCountTextView.setText(""+mLearningCourseCount);
                    }
                    //完成加载第一步
                    mLoadSuccess=COURSE_LOADED;
                    break;
                case SUCCESS:

                    break;
                case FAILED:

                    break;
                case LOADOVER:
                    mLoadingView.setVisibility(View.GONE);
                    break;
            }
        }
    };

    /**
     * 获取用户在修课程数和能获得的积分
     */
    Runnable mCourseCategoryLoadRunnable = new Runnable() {

        @Override
        public void run() {
            // 请求网络数据
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("userid", App.getUserId());
            JSONObject jsonObject = null;
            try {
                jsonObject = getJSONObject(Constants.COURSESTATIS_URL, paramMap);
                if (null != jsonObject) {
                    if (jsonObject.has("flag")
                            && jsonObject.getInt("flag") == 1) {
                        Log.e("resjson",((JSONObject) jsonObject.get("res")).toString());
                        JSONObject dataJson = ((JSONObject) jsonObject
                                .get("res")).getJSONObject("data");
                        Log.e("datajson", dataJson.toString());
                        Log.e("courses", "" + dataJson.getInt("course"));
                        //在修学分
                        mLearningCredit = dataJson.getInt("credit");
                        //已修学分
                        mLearnedCredit = dataJson.getInt("has_credit");
                        //在修课程数
                        mLearningCourseCount = dataJson.getInt("course");
                        //已修课程数
                        mLearnedCourseCount=dataJson.getInt("has_course");
                    }
                    //刷新界面，修改学生课程信息，学分数、在修课程数
                    handler.obtainMessage(GETMESSAGE).sendToTarget();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     *  在修课程信息监听，用于完成视频信息更改操作 
     */
    private BroadcastReceiver mCourseInfoReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (COURSEINFO_CHANGE.equals(intent.getAction())){
                //在修课程信息发生改变
                mPageNum=1;
                //clearListViewItems();
                //mLoadingView.setVisibility(View.VISIBLE);
                requestLearningCourses(mPageNum, PAGESIZE);
            } else if(MYPHOTO_CHANGE.equals(intent.getAction())){
                //更换用户头像
                String newPhotoUrl = intent.getStringExtra("newImageUrl");
                if (newPhotoUrl != null) {
                    mPhotoUrl = newPhotoUrl;
                    Log.d(TAG, "newImageUrl = " + mPhotoUrl);
                }
                removeImageCacheByUrl(mPhotoUrl);
                ImageCacheTool.asyncLoadImage(mStudentHeadImg, mPhotoUrl, R.drawable.all_use_icon_photo);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStudentFragmentView = inflater.inflate(R.layout.course_new, container,
                false);
        mEmptyHeaderView= inflater.inflate(R.layout.common_empty_head,null);

        initComponent();
        //initData();
        addListener();
        return mStudentFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUserInfo();
    }

    /**
     *  初始化数据,获取课程信息以及用户基本信息 
     */
    public void initData() {
        new Thread(mCourseCategoryLoadRunnable).start();
        requestPersonalInfo();
    }

    public void initComponent() {

//        mStudyHistoryImgView = (ImageView) mStudentFragmentView.findViewById(R.id.list_head_history_img);
        mDoWorkImgView=(TextView) mStudentFragmentView.findViewById(R.id.course_dowork_iv);
        mErrorImgView=(TextView) mStudentFragmentView.findViewById(R.id.course_err_iv);

        mStudentHeadImg = (CircularImage) mStudentFragmentView
                .findViewById(R.id.course_my_photo_iv);
        mCourseCountImage = (CircularImage) mStudentFragmentView.findViewById(R.id.course_count_bg);
        mCourseCountImage.setImageDrawable(getResources().getDrawable(R.drawable.title_green));

        mCourseCountTextView = (TextView) mStudentFragmentView.findViewById(R.id.course_count_tv);
        mLearningCourseRListView = (RefreshableListView) mStudentFragmentView
                .findViewById(R.id.course_learning_list);
        mLearningCourseRListView.getRefreshableView().addHeaderView(mEmptyHeaderView);
        mLearningCourseRListView.setDividerHeight(0);
        //加载提示
        mLoadingView = (View) mStudentFragmentView.findViewById(R.id.loading_layout);
        //没有在修课程
        mNoLearningView=(View)mStudentFragmentView.findViewById(R.id.nocourse_view);
        mNoLearningView.setVisibility(View.GONE);
        //没有网络提示
        mNetWorkTipView=(View)mStudentFragmentView.findViewById(R.id.network_unable_view);
        mNetWorkTipView.setVisibility(View.GONE);

        mLearnedCreditTextView = (TextView) mStudentFragmentView.findViewById(R.id.course_learnedcredit_tv);
        mLearningCreditTextView = (TextView) mStudentFragmentView.findViewById(R.id.course_learningcredit_tv);
        mUserNameTextView = (TextView) mStudentFragmentView.findViewById(R.id.course_username_tv);
        mSexIV = (ImageView) mStudentFragmentView.findViewById(R.id.course_user_sex_img);
        mTmingCacheHttp = TmingCacheHttp.getInstance(getActivity().getApplicationContext());
        mSeeAllCoursesButton=(Button) mStudentFragmentView.findViewById(R.id.see_allcourses);

        mLearningCourseAdapter = new CourseLearningAdapter(getActivity(),mLearningCourses);
        mLearningCourseRListView.setAdapter(mLearningCourseAdapter);

        //添加广播监听，用于课程信息修改操作
        registerBroadCast();
    }

    /**
     *  广播的注册和取消 
     */
    private void registerBroadCast(){
        IntentFilter intentFilter=new IntentFilter();
        //课程信息发生改变
        intentFilter.addAction(COURSEINFO_CHANGE);
        //用户头像发生改变
        intentFilter.addAction(MYPHOTO_CHANGE);
        getActivity().registerReceiver(mCourseInfoReciver,intentFilter);
    }

    private void unregisterBroadCast(){
        getActivity().unregisterReceiver(mCourseInfoReciver);
    }

    @Override
    public void onDestroy() {
        unregisterBroadCast();
        super.onDestroy();
    }

    public void addListener() {

        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {

                    /*case R.id.list_head_history_img:
                        // 观看历史
                        startActivity(new Intent(App.getAppContext(), PlayHistoryActivity.class));
                        break;*/
                    case R.id.course_my_photo_iv:
                        // 用户头像
                        Intent intent=new Intent();
                        intent.setAction(MainTabActivity.TAB_CHANGE);
                        intent.putExtra("tabIndex",3);
                        Helper.sendLocalBroadCast(getActivity(),intent);
                        break;
                    case R.id.course_dowork_iv:
                        // 做作业，该功能暂时处于屏蔽状态
                        /*((ImageView) view).setImageResource(R.drawable.dowork1);*/
                        UIHelper.showHomeworkListActivity(getActivity(),null);
                        break;
                    case R.id.course_err_iv:
                        // 错题集，该功能展示处于屏蔽状态
                        /*((ImageView) view).setImageResource(R.drawable.err_jh1);*/
                        UIHelper.showErrorActivity(getActivity());
                        break;
                    case R.id.see_allcourses:
                        //查看所有课程
                        showAllCourses();
                        break;
                    case R.id.network_unable_view:
                        //网络状况改变，刷新数据
                        netChangedRefresh();
                        break;
                    default:
                        break;
                }
            }
        };
        mStudentHeadImg.setOnClickListener(mClickListener);
        /*mStudyHistoryImgView.setOnClickListener(mClickListener);
        mStudyHistoryImgView.setOnClickListener(mClickListener);*/
        mDoWorkImgView.setOnClickListener(mClickListener);
        mErrorImgView.setOnClickListener(mClickListener);
        mSeeAllCoursesButton.setOnClickListener(mClickListener);
        mNetWorkTipView.setOnClickListener(mClickListener);
        //禁止上滑加载更多
        mLearningCourseRListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        //在学视频内容刷新
        mLearningCourseRListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作
                mNetWorkTipView.setVisibility(View.GONE);
                mNoLearningView.setVisibility(View.GONE);
                mLearningCourses.clear();
                mLearningCourseAdapter.clear();

                mPageNum=1;
                mLoadMore=false;
                requestLearningCourses(mPageNum, PAGESIZE);
                if (!checkNetWork()){
                    Toast.makeText(getActivity().getApplication(),getString(R.string.net_error),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoadMore() {
                Log.e(TAG, "-----------加载更多------------");
                mLoadMore=true;
                mLearningCourses.clear();
                requestLearningCourses(++mPageNum, PAGESIZE);
            }
        });
    }

    /**
     *  查看所有课程
     *     该过程调用mainActivity中的 mBroadcastReceiver 实现课程数据的加载
     * 
     */
    private void showAllCourses(){
        Constants.leftInnerId = "0";
        Constants.leftInnerType = 0;
        Constants.leftInnerName =getString(R.string.all_course_text);
        Intent intent = new Intent();
        //新增加的类型，用于非滑动的界面跳转
        intent.setAction("allcourse_noslider");
        Helper.sendLocalBroadCast(getActivity(), intent);
    }

    /**
     * 根据用户当前请求的 pageindex 判定是否继续加载
     * @param pageIndex 当前页面
     * @return 判定是否有更多课程记录
     */
    private boolean checkMoreCourse(int pageIndex){

        int totalPages=mLearningCourseCount/PAGESIZE;
        if (totalPages > pageIndex){
            return true;
        } else{
            return false;
        }
    }

    /**
     * 清除课程列表中的要素 
     */
    private void clearListViewItems(){
        if (null != mLearningCourseRListView && null!=mLearningCourseAdapter){
            mLearningCourseAdapter.clear();
            mLearningCourseAdapter.notifyDataSetChanged();
        }
    }

    /**
     *  请求用户信息 
     */
    private void requestPersonalInfo() {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userid", App.getUserId());
        Log.d(TAG, "userid:" + App.getUserId());
        mTmingCacheHttp.asyncRequestWithCache(Constants.GET_USER_URL, map, new TmingCacheHttp.RequestWithCacheCallBack<PersonalInfo>() {

            @Override
            public PersonalInfo onPreRequestCache(String cache)
                    throws Exception {
                return PersonalInfo.personalInfoJSONPasing(cache);
            }

            @Override
            public void onPreRequestSuccess(PersonalInfo data) {

                if (getActivity() == null) {
                    return;
                }

                if (data == null) {
                    return;
                }
                setStudentInfoView(data);
                //加载在修课程
                requestLearningCourses(mPageNum, PAGESIZE);
            }

            @Override
            public PersonalInfo onReqestSuccess(String respones)
                    throws Exception {
                return PersonalInfo.personalInfoJSONPasing(respones);
            }

            /**
             * 处理流程
             * 1）判定Activity是否为null
             * 2）判定返回数据是否为null
             * 3）设定学生信息
             * 4）学生信息设定完毕之后，再异步加载课程信息
             */
            @Override
            public void success(PersonalInfo cacheRespones, PersonalInfo newRespones) {
                if (getActivity() == null) {
                    return;
                }

                if (newRespones == null) {
                    return;
                }
                setStudentInfoView(newRespones);
                //加载在修课程
                requestLearningCourses(mPageNum, PAGESIZE);
            }

            @Override
            public void exception(Exception exception) {
                //发生异常照常获取用户在修课程信息
                requestLearningCourses(mPageNum, PAGESIZE);
            }
        });
    }

    /**
     *  在界面上设置学生信息 
     *
     * @param studentInfo 学生个人信息
     */
    private void setStudentInfoView(PersonalInfo studentInfo) {

        try {
            mPhotoUrl = studentInfo.photoUrl;
            App.sTermId = studentInfo.specialtyid;

            mUserNameTextView.setText(studentInfo.userName);
            //设定性别
            if (studentInfo.sex == PersonalInfo.SEX_FEMALE) {
                mSexIV.setBackgroundResource(R.drawable.classroom_icon_female);
            } else {
                mSexIV.setBackgroundResource(R.drawable.classroom_icon_male);
            }
            //加载完成第二步
            mLoadSuccess=USERINFO_LOADED;
            //再加载头像
            loadUserHeadImg();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载用户头像 
     *
     */
    private void loadUserHeadImg(){

        if (null==mStudentHeadImg || StringUtils.isEmpty(mPhotoUrl)){
            return;
        }
        //判定网络是否正常，正常情况下，清空头像
        if (PhoneUtil.checkNetworkEnable(getActivity()) != PhoneUtil.NETSTATE_DISABLE) {
            removeImageCacheByUrl(mPhotoUrl);
        }

        final String imgUrl=mPhotoUrl;
        //加载用户图片，完毕之后加载在修课程信息
        ImageUtil.asyncLoadImage(mStudentHeadImg,imgUrl, R.drawable.all_use_icon_photo,new ImageCacheTool.ImageLoadCallBack() {
            @Override
            public void progress(int progress) {
                if(0 != 0) {
                    mStudentHeadImg.setImageDrawable(App.getAppContext().getResources().getDrawable(0));
                }
            }
            @Override
            public void loadResult(Bitmap bitmap) {
                if(null == bitmap){
                    Log.e(TAG, "未能加载图片:"+imgUrl);
                    if(0 !=  R.drawable.all_use_icon_photo){
                        //没有获取到图片 设置错误图片
                        mStudentHeadImg.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.all_use_icon_photo));
                    }
                } else{
                    if(null != mStudentHeadImg) {
                        mStudentHeadImg.setImageBitmap(bitmap);
                        //完成加载第三步
                        mLoadSuccess=PHOTO_LOADED;
                    }
                }
            }
        });
    }

    /**
     *  刷新用户信息，根据mLoadSuccess 数值分步处理 
     */
    private void refreshUserInfo(){

        switch (mLoadSuccess){

            case NOT_LOADED:
                //重新加载课程信息及用户数据
                initData();
                break;
            case COURSE_LOADED:
                //重新加载用户信息
                requestPersonalInfo();
                break;
            case USERINFO_LOADED:
                //重新加载用户头像
                loadUserHeadImg();
                break;
            case PHOTO_LOADED:
                //加载完毕 over!
                break;
            default:
                mLoadSuccess=NOT_LOADED;
                initData();
                break;
        }
    }

    /**
     * 根据url地址清除图片缓存</P>
     *
     * @param photoUrl 图片网络地址
     */
    private void removeImageCacheByUrl(String photoUrl) {
        ImageCacheTool cacheTool = ImageCacheTool.getInstance();
        cacheTool.removeCache(photoUrl);
        int playGroundCacheWidth = Helper.dip2px(getActivity(),
                PlaygroundFragment.LOAD_HEAD_IMAGE_WH_DP);
        cacheTool.removeCache(photoUrl, playGroundCacheWidth,playGroundCacheWidth);
    }

    /**
     * 网络调用图片
     *
     * @param url 图片地址
     * @return Bitmap 图片对象
     */
    public Bitmap returnBitMap(String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            myFileUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 获取用户正在学习的课程列表
     *
     * @param pageNum 页号
     * @param PageSize 每页记录数
     */
    private void requestLearningCourses(int pageNum,int PageSize) {
        // mLoadingView.setVisibility(View.VISIBLE);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("pageNum", pageNum);
        params.put("pageSize", PageSize);
        mTmingCacheHttp.asyncRequestWithCache(Constants.HOST + "student/courses.html", params, requestCoursingCallBack);
    }

    /**
     * mTmingCacheHttp 请求回调函数 
     */
    private TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<LearningCourseInfoEntity>> requestCoursingCallBack = new TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<LearningCourseInfoEntity>>() {

        @Override
        public void cacheDataRespone(List<LearningCourseInfoEntity> data) {

            mNetWorkTipView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.GONE);
            mLearningCourseRListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mLearningCourseRListView.onRefreshComplete();

            //加载更多情况
            if (mLoadMore && data.size()==0){
                //返回的数据为空
                mPageNum--;
                Helper.ToastUtil(getActivity(), getString(R.string.no_more_record));
                return;
            }
            //加载的数据为空，而且之前数据也为空
            if (data.size()==0 && mLearningCourseAdapter.getCount() == 0) {
                mNoLearningView.setVisibility(View.VISIBLE);
                return;
            }
            //增加返回的新数据
            for (int i = 0; i < data.size(); i++) {
                LearningCourseInfoEntity course = data.get(i);
                if (!mLearningCourseAdapter.getDatas().contains(course)){
                    mLearningCourseAdapter.addItem(course);
                }
            }
            mLearningCourseAdapter.notifyDataSetChanged();
            //修改课程数
            mLearningCourseCount=mLearningCourseAdapter.getDatas().size();
            mCourseCountTextView.setText(""+mLearningCourseCount);
        }

        @Override
        public void requestNewDataRespone(
                List<LearningCourseInfoEntity> cacheRespones,
                List<LearningCourseInfoEntity> newRespones) {
            mNetWorkTipView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.GONE);
            mLearningCourseRListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mLearningCourseRListView.onRefreshComplete();

            //返回的数据为空
            if (newRespones.size() == 0) {
                //加载更多情况
                if (mLoadMore){
                    //返回的数据为空
                    mPageNum--;
                    Helper.ToastUtil(getActivity(), getString(R.string.no_more_record));
                }

                //之前也为空
                if (mLearningCourseAdapter.getCount() == 0) {
                    mNoLearningView.setVisibility(View.VISIBLE);
                }
                return;
            } else{

                //如果之前课程为空，清除没有课程提示
                if (mNoLearningView.getVisibility()!=View.GONE ){
                    mNoLearningView.setVisibility(View.GONE);
                }

                mLearningCourseAdapter.replaceItem(cacheRespones,newRespones);
                mLearningCourseAdapter.notifyDataSetChanged();
                //修改课程数
                mLearningCourseCount=mLearningCourseAdapter.getDatas().size();
                mCourseCountTextView.setText(""+mLearningCourseCount);
            }
        }

        @Override
        public List<LearningCourseInfoEntity> parseData(String data)
                throws Exception {
            return coursingJSONParsing(data);
        }

        @Override
        public void exception(Exception exception) {
            mLoadingView.setVisibility(View.GONE);
            //Toast.makeText(getActivity().getApplication(),getResources().getString(R.string.net_error_tip),Toast.LENGTH_SHORT).show();
            mCourseCountTextView.setText("0");
            checkNetWorkAndCourses();
            mLearningCourseRListView.onRefreshComplete();
        }

        @Override
        public void onFinishRequest() {
            if (mLearningCourseCount < 1) {
                mDoWorkImgView.setVisibility(View.GONE);
                mErrorImgView.setVisibility(View.GONE);
            } else {
                mDoWorkImgView.setVisibility(View.VISIBLE);
                mErrorImgView.setVisibility(View.VISIBLE);
            }
        }
    };

    /**
     * 网络状况改变，重新加载数据
     *    该情况发生于：由没有网络转到有网络状况,设定控件显示状态，同时请求数据
     */
    private void netChangedRefresh(){
        //之前没有网络，点击图标刷新
        if (checkNetWork()){
            //有网络状况
            mLearningCourseRListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mNetWorkTipView.setVisibility(View.VISIBLE);
            mLoadingView.setVisibility(View.VISIBLE);
            initData();
        } else{
            //无网络状况
            Toast.makeText(getActivity().getApplication(),getResources().getString(R.string.net_error),Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *  课程信息 
     */
    private void checkNetWorkAndCourses(){
        //网络状况异常
        if (null==mLearningCourseAdapter.getDatas()||mLearningCourseAdapter.getDatas().size()<=0){
            //没有课程数据
            mNetWorkTipView.setVisibility(View.VISIBLE);
            //设定不能滑动刷新
            mLearningCourseRListView.setMode(PullToRefreshBase.Mode.DISABLED);
            mNoLearningView.setVisibility(View.GONE);
        }
        Toast.makeText(getActivity().getApplication(),getResources().getString(R.string.net_error),Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查网络状况
     * @return boolean网络状态,true：连接上 false：掉线
     */
    private boolean checkNetWork(){

        return (PhoneUtil.checkNetworkEnable(getActivity())==PhoneUtil.NETSTATE_DISABLE)?false:true;
    }

    /**
     * 字符串封装为JSON数组 
     *
     * @param result JSON字符串
     * @return List<LearningCourseInfoEntify> 在修课程列表
     * @throws JSONException JSON转换异常
     */
    private List<LearningCourseInfoEntity> coursingJSONParsing(String result)
            throws JSONException {
        List<LearningCourseInfoEntity> coursings = new ArrayList<LearningCourseInfoEntity>();
        JSONObject object = new JSONObject(result);
        JSONObject dataObj = object.getJSONObject("res").getJSONObject("data");
        if (!dataObj.isNull("list")) {
            JSONArray list = dataObj.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                LearningCourseInfoEntity coursing = new LearningCourseInfoEntity();
                coursing.mCourseId = list.getJSONObject(i)
                        .getString("course_id");
                coursing.mCredit = list.getJSONObject(i).getString(
                        "course_credit");
                coursing.mName = list.getJSONObject(i).getString("name");
                coursing.mTimes = list.getJSONObject(i).getString("times");
                coursing.mPhoto=list.getJSONObject(i).getString("cover");
                coursing.mPercent = list.getJSONObject(i).getDouble(
                        "course_percent");
                if (!list.getJSONObject(i).isNull("play_recorded")){
                    coursing.setPlayRecord( list.getJSONObject(i).getJSONObject("play_recorded"));
                } else{
                    coursing.setPlayRecord(null);
                }
                coursings.add(coursing);
            }
        }
        return coursings;
    }

    /**
     * 网络获取Json对象
     *
     * @param url 网络接口地址
     * @param map 参数
     * @return JSONObject类型对象
     */
    private JSONObject getJSONObject(String url, HashMap<String, Object> map) {
        JSONObject jsonObject = null;
        TmingHttp tmingHttp = new TmingHttp();
        try {
            TmingResponse response = tmingHttp.tmingHttpRequest(url, map);
            jsonObject = response.asJSONObject();
            return jsonObject;
        } catch (TmingHttpException e) {

        } catch (OutOfMemoryError e) {

        }
        return null;
    }
}


