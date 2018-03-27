package com.nd.shapedexamproj.fragment;

import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.MainTabActivity;
import com.nd.shapedexamproj.activity.downloadmanage.DownloadManagerActivity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.ImageUtil;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.util.XKUIHelper;
import com.nd.shapedexamproj.view.CircularImage;
import com.tming.common.adapter.ImageBaseAdatapter;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.openuniversity.model.stu.Course;
import com.tming.openuniversity.model.stu.Student;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/3/12.
 */
public class XKCourseFragment extends BaseFragment implements TmingCacheHttp.RequestWithCacheCallBackV2<Course>{
    private static final String TAG = "XKCourseFragment";
    public static final String COURSEINFO_CHANGE="CourseInfoChange";
    public static final String MYPHOTO_CHANGE="MyPhotoChange";
    private final static int NOT_LOADED=1;
    private final static int COURSE_LOADED=2;
    private final static int USERINFO_LOADED=3;
    private final static int PHOTO_LOADED=4;
    private int mLoadSuccess = NOT_LOADED; //判定用户信息是否加载成功,初始化默认没有加载成功

    private ImageView /*mStudyHistoryImgView,*/mSexIV;
    private TextView mDoWorkImgView, mErrorImgView;
    private TextView mLearningCreditTextView;
    private TextView mLearnedCreditTextView;
    private TextView mUserNameTextView;
    private TextView mCourseCountTextView;
    private CircularImage mStudentHeadImg, mCourseCountImage;
    private RefreshableListView mCourseRListView;
    private CourseAdapter mCourseAdapter;
    private View mLoadingView,mNoLearningView,mNetWorkTipView;
    private TmingCacheHttp mTmingCacheHttp;
    private View.OnClickListener mClickListener;
    private View mSelectedCourseView;
    private Button mSeeAllCoursesButton;

    private String mPhotoUrl;
    private String mStepId;//学期Id
    private int pageNum = 1,pageSize = 30;
    private int mRefreshState;
    private SharedPreferences spf ;
    private SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.course_new, null);
        initComponent(view);
        initListener();
        initData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    public void initComponent(View mStudentFragmentView) {
        /*mStudyHistoryImgView = (ImageView) mStudentFragmentView.findViewById(R.id.list_head_history_img);*/
        mDoWorkImgView=(TextView) mStudentFragmentView.findViewById(R.id.course_dowork_iv);
        mDoWorkImgView.setVisibility(View.GONE);
        mErrorImgView=(TextView) mStudentFragmentView.findViewById(R.id.course_err_iv);
        mErrorImgView.setVisibility(View.GONE);

        mStudentHeadImg = (CircularImage) mStudentFragmentView
                .findViewById(R.id.course_my_photo_iv);
        mCourseCountImage = (CircularImage) mStudentFragmentView.findViewById(R.id.course_count_bg);
        mCourseCountImage.setImageDrawable(getResources().getDrawable(R.drawable.title_green));

        mCourseCountTextView = (TextView) mStudentFragmentView.findViewById(R.id.course_count_tv);
        mCourseRListView = (RefreshableListView) mStudentFragmentView
                .findViewById(R.id.course_learning_list);
        mCourseRListView.setDividerHeight(0);
        //加载提示
        mLoadingView = (View) mStudentFragmentView.findViewById(R.id.loading_layout);
        mLoadingView.setVisibility(View.GONE);
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
        mSeeAllCoursesButton.setVisibility(View.GONE);

        mCourseAdapter = new CourseAdapter(getActivity());
        mCourseRListView.setAdapter(mCourseAdapter);
        spf = getActivity().getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = spf.edit();
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

    /**
     *  在修课程信息监听，用于完成视频信息更改操作
     */
    private BroadcastReceiver mCourseInfoReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (COURSEINFO_CHANGE.equals(intent.getAction())){
                //在修课程信息发生改变
                pageNum=1;
                getCourseList();
            } else if(MYPHOTO_CHANGE.equals(intent.getAction())){
                //更换用户头像
                String newPhotoUrl = intent.getStringExtra("newImageUrl");
                if (newPhotoUrl != null) {
                    mPhotoUrl = newPhotoUrl;
                }
                removeImageCacheByUrl(mPhotoUrl);
                ImageCacheTool.asyncLoadImage(mStudentHeadImg, mPhotoUrl, R.drawable.all_use_icon_photo);
            }
        }
    };

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


    public void initData() {
        mRefreshState = Constants.PULL_DOWN_TO_REFRESH;
        requestPersonalInfo();
        getCourseList();
    }


    public void initListener() {
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.list_head_left:
                        getActivity().sendBroadcast(new Intent().setAction("openLeftMenu"));
                        break;
                    case R.id.list_head_right:
                        startActivity(new Intent(getActivity(), DownloadManagerActivity.class));
                        break;
                    /*case R.id.list_head_history_img:
                        // 观看历史
                        startActivity(new Intent(App.getAppContext(), PlayHistoryActivity.class));
                        break;*/
                    case R.id.course_my_photo_iv:
                        // 用户头像
                        Intent intent = new Intent();
                        intent.setAction(MainTabActivity.TAB_CHANGE);
                        intent.putExtra("tabIndex", 3);
                        Helper.sendLocalBroadCast(getActivity(), intent);
                        break;
                    case R.id.course_dowork_iv:
                        // 做作业，该功能暂时处于屏蔽状态
                    /*((ImageView) view).setImageResource(R.drawable.dowork1);*/
                        UIHelper.showHomeworkListActivity(getActivity(), null);
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
                        mCourseRListView.setRefreshing();
                        getCourseList();
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
        mCourseRListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        //在学视频内容刷新
        mCourseRListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //刷新操作
                pageNum = 1;
                mRefreshState = Constants.PULL_DOWN_TO_REFRESH;
                getCourseList();
            }

            @Override
            public void onLoadMore() {
                ++pageNum;
                mRefreshState = Constants.PULL_UP_TO_REFRESH;
                getCourseList();
            }
        });

    }

    public int XK_VERSION = 2;
    private void getCourseList () {
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("stu_id", App.getsStudentId());
        map.put("v",XK_VERSION);
        if (mStepId != null) {
            map.put("step_id", mStepId);
        }
        map.put("page",pageNum);
        map.put("size",100);
        PhpApiUtil.sendData(Constants.XK_STU_COURSES, map, this);
    }

    @Override
    public Course parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        Course course = new Course();
        course.initWithJsonObject(jsonObject);
        return course;
    }

    @Override
    public void cacheDataRespone(Course data) {
        if (mCourseAdapter != null) {
            mCourseAdapter.clear();
            if (data != null) {
                mCourseAdapter.addItemCollection(data.courses);
            }
            mCourseAdapter.notifyDataSetChanged();
            mCourseRListView.onRefreshComplete();
            if (mCourseAdapter.getCount() == 0) {
                mNoLearningView.setVisibility(View.VISIBLE);
            } else {
                mNoLearningView.setVisibility(View.GONE);
            }
            mCourseCountTextView.setText("" + mCourseAdapter.getCount());
        }
    }

    @Override
    public void requestNewDataRespone(Course cacheRespones, Course newRespones) {
        cacheDataRespone(newRespones);
    }

    @Override
    public void exception(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onFinishRequest() {
        mCourseRListView.onRefreshComplete();
    }

    /**
     *  请求用户信息
     */
    private void requestPersonalInfo() {

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("stu_id", App.getsStudentId());
        PhpApiUtil.sendData(Constants.XK_STU_INFO, map, new TmingCacheHttp.RequestWithCacheCallBackV2<Student>() {

            @Override
            public Student parseData(String data) throws Exception {
                JSONObject jsonObject = new JSONObject(data);
                Student student = new Student();
                student.initWithJsonObject(jsonObject);
                return student;
            }

            @Override
            public void cacheDataRespone(Student data) {
                if (getActivity() == null) {
                    return;
                }
                if (data == null) {
                    return;
                }
                setStudentInfoView(data);
            }

            @Override
            public void requestNewDataRespone(Student cacheRespones, Student newRespones) {
                if (getActivity() == null) {
                    return;
                }

                if (newRespones == null) {
                    return;
                }
                setStudentInfoView(newRespones);
            }

            @Override
            public void exception(Exception exception) {
                exception.printStackTrace();
            }

            @Override
            public void onFinishRequest() {

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
            mUserNameTextView.setText(studentInfo.getUserName());
            //加载完成第二步
            mLoadSuccess = USERINFO_LOADED;
            //再加载头像
            ImageCacheTool.getInstance().asyncLoadImage(new URL(mPhotoUrl),mStudentHeadImg,R.drawable.all_use_icon_photo,100,100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  在界面上设置学生信息
     *
     * @param studentInfo 学生个人信息
     */
    private void setStudentInfoView(Student studentInfo) {

        try {
            mPhotoUrl = studentInfo.avatar;

            mUserNameTextView.setText(studentInfo.studentName);
            //加载完成第二步
            mLoadSuccess = USERINFO_LOADED;
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
        if (PhoneUtil.checkNetworkEnable() != PhoneUtil.NETSTATE_DISABLE) {
            removeImageCacheByUrl(mPhotoUrl);
        }

        final String imgUrl=mPhotoUrl;
        //加载用户图片，完毕之后加载在修课程信息
        ImageUtil.asyncLoadImage(mStudentHeadImg, imgUrl, R.drawable.all_use_icon_photo, new ImageCacheTool.ImageLoadCallBack() {
            @Override
            public void progress(int progress) {
                if (0 != 0) {
                    mStudentHeadImg.setImageDrawable(App.getAppContext().getResources().getDrawable(0));
                }
            }

            @Override
            public void loadResult(Bitmap bitmap) {
                if (null == bitmap) {
                    Log.e(TAG, "未能加载图片:" + imgUrl);
                    if (0 != R.drawable.all_use_icon_photo) {
                        //没有获取到图片 设置错误图片
                        mStudentHeadImg.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.all_use_icon_photo));
                    }
                } else {
                    if (null != mStudentHeadImg) {
                        mStudentHeadImg.setImageBitmap(bitmap);
                        //完成加载第三步
                        mLoadSuccess = PHOTO_LOADED;
                    }
                }
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

    private class CourseAdapter extends ImageBaseAdatapter<Course> {

        public CourseAdapter(Context context) {
            super(context);
            defaultImageResourceId = R.drawable.default_cover_s;
        }

        public CourseAdapter(Context context, List<Course> datas) {
            super(context, datas);
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, final Course data) {
            ViewHolder viewHolder = (ViewHolder) holder;
            String courseType = "";
            if (data.type == 1) {
                courseType = "[专业]";
            } else if (data.type == 2){
                courseType = "[其他]";
            }
            viewHolder.courseNameTv.setText(courseType + data.courseName);
            String record = data.playRecord == null ? "暂无记录" : "看到:" + data.playRecord;
            viewHolder.courseTimeTv.setText("");


            if (data.exam_total > 0) {
                viewHolder.xkTaskBtn.setVisibility(View.VISIBLE);
                viewHolder.xkTaskBtn.setText("形考任务(" + data.exam_complete + "/" + data.exam_total + ")");
            } else {
                viewHolder.xkTaskBtn.setVisibility(View.INVISIBLE);
            }
            if (data.warmup_total > 0) {
                viewHolder.warmupBtn.setVisibility(View.VISIBLE);
                viewHolder.warmupBtn.setText("热身考试(" + data.warmup_complete + "/" + data.warmup_total + ")");
            } else {
                viewHolder.warmupBtn.setVisibility(View.INVISIBLE);
            }
            viewHolder.xkTaskBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    XKUIHelper.showTaskList(getActivity(),data.courseId,data.courseName,mStepId);
                }
            });
            viewHolder.warmupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO 跳转到课程热身考试
                    XKUIHelper.showWarmupExamActivity(getActivity(),data.courseId,data.courseName);
                }
            });
        }

        @Override
        public View infateItemView(Context context) {
            View view = LayoutInflater.from(context).inflate(R.layout.xk_course,null);
            ViewHolder holder = new ViewHolder();
            holder.imageView = (ImageView) view.findViewById(R.id.course_image);
            holder.courseNameTv = (TextView) view.findViewById(R.id.course_name_tv);
            holder.courseTimeTv = (TextView) view.findViewById(R.id.course_time_tv);
            holder.xkTaskBtn = (Button) view.findViewById(R.id.xk_task_btn);
            holder.warmupBtn = (Button) view.findViewById(R.id.warm_exam_btn);
            view.setTag(holder);
            return view;
        }

        @Override
        public URL getDataImageUrl(Course data) {
            URL url = null;
            try {
                url = new URL(data.cover);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return url;
        }

        class ViewHolder extends BaseViewHolder {
            TextView courseNameTv,courseTimeTv;
            Button xkTaskBtn,warmupBtn;
        }
    }

}
