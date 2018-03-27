package com.nd.shapedexamproj.view.course;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.adapter.CourseQuestionAdapter;
import com.nd.shapedexamproj.model.course.CourseQuestion;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * 课程答疑
 * 
 * @author Linlg
 * 
 *         Create on 2014-5-12
 */
public class CourseAnswer extends RelativeLayout {

    private final static String TAG = "CourseAnswer";
    private View courseAnswerView;
    private RefreshableListView lv;
    private View mListFootView,mListHeadView,mErrorView;
    private Button courseAnswerQuestionBtn,mErrorBtn;
    private String courseId;
    private String courseName;
    private Context context;
    private TmingCacheHttp cacheHttp;
    private int page = 1,pageSize = 10;
    private List<CourseQuestion> questions = new ArrayList<CourseQuestion>();
    // private QuestionAdapter adapter;
    private CourseQuestionAdapter adapter;
    private LinearLayout loadingLayout;
    /*private LinearLayout answerLayout;
    private LinearLayout questionLayout;*/
    private TextView mHeadNoDataTV;

    public CourseAnswer(Context context, String courseId, String courseName) {
        super(context);
        this.courseId = courseId;
        this.courseName = courseName;
        this.context = context;
        cacheHttp = TmingCacheHttp.getInstance(context);
        initView();
        requestData();
        addListener();
        initAuthority();
    }

    public void initAuthority() {
        if (!AuthorityManager.getInstance().isStudentAuthority()) {
            /*questionLayout.setVisibility(View.GONE);
            answerLayout.setVisibility(View.GONE);*/
            //游客用户,教师用户 隐藏“发起提问”按钮
            courseAnswerQuestionBtn.setVisibility(View.GONE);
        }
    }

    private void initView() {
        courseAnswerView = LayoutInflater.from(context).inflate(R.layout.course_answer_view, this);
        loadingLayout = (LinearLayout) courseAnswerView.findViewById(R.id.loading_layout);
        /*answerLayout = (LinearLayout) courseAnswerView.findViewById(R.id.course_detail_question_ll);*/
        lv = (RefreshableListView) courseAnswerView.findViewById(R.id.refreshable_listview);
        lv.setMode(Mode.PULL_FROM_START);
        mErrorView = (View) courseAnswerView.findViewById(R.id.error_layout);
        mErrorBtn = (Button) courseAnswerView.findViewById(R.id.error_btn);
        adapter = new CourseQuestionAdapter(context, questions);
        mListFootView = LayoutInflater.from(context).inflate(R.layout.course_coach_foot_item, null);
        mListHeadView = LayoutInflater.from(context).inflate(R.layout.course_answer_list_head,null);
        mHeadNoDataTV = (TextView) mListHeadView.findViewById(R.id.course_answer_head_tv);
        //发起提问按钮
        courseAnswerQuestionBtn = (Button) mListHeadView.findViewById(R.id.course_answer_question_btn);

        /*questionLayout = (LinearLayout) findViewById(R.id.course_detail_question_ll);*/
        lv.addHeaderView(mListHeadView, null, false);
        lv.addFooterView(mListFootView);
        lv.setAdapter(adapter);
    }

    public void addListener() {
        lv.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        Thread.sleep(100);
                        App.getAppHandler().post(new Runnable() {
                            
                            @Override
                            public void run() {
                                lv.onRefreshComplete();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                requestData();
            }

            @Override
            public void onLoadMore() {}
        });
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int currPosition = position - 2;
                Log.e(TAG, "position=="+ position);
                if (questions.size() > currPosition) {
                    CourseQuestion courseQuestion = questions.get(currPosition);
                    MyQuestion myQuestion = new MyQuestion();
                    myQuestion.setCourse_id(courseId);
                    myQuestion.setCourseName(courseName);
                    myQuestion.setAsk_id(courseQuestion.getAskId());
                    myQuestion.setQuestion(courseQuestion.getQuestion());
                    myQuestion.setTitle(courseQuestion.getTitle());
                    myQuestion.setAdd_time(courseQuestion.getAskTime());
                    myQuestion.setAnswer(courseQuestion.getAnswer());
                    myQuestion.setAn_time(courseQuestion.getAnswerTime());
                    myQuestion.setAnswerAvatar(courseQuestion.getAnswerAvatar());
                    myQuestion.setAnswerName(courseQuestion.getAnswerName());
                    UIHelper.showCourseQuestionDetailActivty(context, myQuestion);
                } else {
                    UIHelper.showCourseQuestionListActivity(context, courseId, courseName);
                }
            }
        });
        courseAnswerQuestionBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                UIHelper.showCourseNewQuestionActivity(context, courseId, courseName, "", "");
            }   
        });
        mErrorBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                questions.clear();
                requestData();
            }
        });
    }

    private void requestData() {
        TmingHttp.asyncRequest(ServerApi.getQuestionList(pageSize, page, courseId, App.getUserId()),
                new HashMap<String, Object>(), new TmingHttp.RequestCallback<List<CourseQuestion>>() {

                    @Override
                    public List<CourseQuestion> onReqestSuccess(String respones) throws Exception {
                        // 数据组装
                        Log.i(TAG, respones);
                        JsonParseObject jsonParseObject = JsonParseObject.parseJson(respones);
                        // Log.d(TAG, jsonParseObject.getDataJs().toString());
                        if (Constants.SUCCESS_MSG == jsonParseObject.getFlag()) {
                            try {
                                if (!jsonParseObject.getResJs().isNull("data")) {
                                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
                                    boolean isListNull = jsonParseObject.getResJs().getJSONObject("data").isNull("list");
                                    if (!isListNull) {
                                        List<CourseQuestion> result = gson.fromJson(jsonParseObject.getResJs().getJSONObject("data").getJSONArray("list").toString(),
                                                new TypeToken<ArrayList<CourseQuestion>>() {
                                                }.getType());
                                        return result;
                                    }
                                }
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                                jsonParseObject.setFlag(0);
                            }
                        }
                        return null;
                    }

                    @Override
                    public void success(List<CourseQuestion> result) {
                        loadingLayout.setVisibility(View.GONE);
                        mErrorView.setVisibility(View.GONE);
                        lv.onRefreshComplete();
                        // UI表现
                        if (result != null) {
                            questions.clear();
                            questions.addAll(result);
                            adapter.notifyDataSetChanged();
                            setQuestionView();
                        } else {
                            mHeadNoDataTV.setVisibility(View.VISIBLE);
                            mListFootView.setVisibility(View.GONE);
                            return;
                        }
                    }

                    @Override
                    public void exception(Exception exception) {
                        loadingLayout.setVisibility(View.GONE);
                        lv.onRefreshComplete();
                        mErrorView.setVisibility(View.VISIBLE);
                        exception.printStackTrace();
                    }
                });
    }

    private void setQuestionView() {
        if (questions.size() == 0 && adapter.getCount() == 0) {
            mHeadNoDataTV.setVisibility(View.VISIBLE);
            return;
        } else {
            mHeadNoDataTV.setVisibility(View.GONE);
        }
        if (questions.size() > pageSize - 1) {
            mListFootView.setVisibility(View.VISIBLE);
        } else {
            lv.getRefreshableView().removeFooterView(mListFootView);
        }
        adapter.notifyDataSetChanged();
    }
}
