package com.nd.shapedexamproj.view.video;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.adapter.MyQuestionAdapter;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;

import java.util.HashMap;
import java.util.List;

/**
 * 
 * 视频提问
 * 
 * @author Linlg
 * 
 *         Create on 2014-5-19
 */
public class MenuQuestionView extends RelativeLayout implements OnClickListener {

    private Activity mContext;
    private MyQuestionAdapter questionAdapter;
    private RefreshableListView questionLv;
    private String courseId, courseName;
    private TextView noQuestionTv;
    private int page = 1;

    public MenuQuestionView(Activity context, String courseId, String courseName) {
        super(context);
        this.mContext = context;
        this.courseId = courseId;
        this.courseName = courseName;
        page = 1;
        initView();
        requestData();
    }

    private void initView() {
        View myQuestionView = LayoutInflater.from(mContext).inflate(R.layout.videoview_question, this);
        findViewById(R.id.videoview_question_btn).setOnClickListener(this);
        noQuestionTv = (TextView) myQuestionView.findViewById(R.id.videoview_menu_note_noquestion_tv);
        questionLv = (RefreshableListView) myQuestionView.findViewById(R.id.refreshable_listview);
        questionAdapter = new MyQuestionAdapter(mContext, MyQuestionAdapter.VIDEO);
        questionLv.setAdapter(questionAdapter);
        questionLv.setOnItemClickListener(questionAdapter);
        questionLv.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                page = 1;
                questionAdapter.clear();
                requestData();
            }

            @Override
            public void onLoadMore() {
                ++page;
                requestData();
            }
        });
    }
    
    private void requestData() {
        TmingHttp.asyncRequest(ServerApi.getMyQuestionList(courseId, page, 10), new HashMap<String, Object>(),
                requestWithCacheCallBack);
    }
    
    private RequestCallback<List<MyQuestion>> requestWithCacheCallBack = new RequestCallback<List<MyQuestion>>() {

        @Override
        public List<MyQuestion> onReqestSuccess(String respones) throws Exception {
            return MyQuestion.jsonParsing(respones);
        }

        @Override
        public void success(List<MyQuestion> respones) {
            if (respones.size() == 0 && questionAdapter.getCount() == 0) {
                noQuestionTv.setVisibility(View.VISIBLE);
                return;
            } else {
                noQuestionTv.setVisibility(View.GONE);
            }
            questionAdapter.addItemCollection(respones);
            questionAdapter.notifyDataSetChanged();
            questionLv.onRefreshComplete();
        }

        @Override
        public void exception(Exception exception) {
            Toast.makeText(mContext, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
        }
        
    };
    
    public void refresh() {
        page = 1;
        questionAdapter.clear();
        requestData();
    }
    
    /*private RequestCallBack<List<MyQuestion>> requestWithCacheCallBack = new RequestCallBack<List<MyQuestion>>() {

        @Override
        public List<MyQuestion> onPreRequestCache(String cache) throws Exception {
            return MyQuestion.jsonParsing(cache);
        }

        @Override
        public void onPreRequestSuccess(List<MyQuestion> data) {
            if (data.size() == 0 && questionAdapter.getCount() == 0) {
                noQuestionTv.setVisibility(View.VISIBLE);
                return;
            } else {
                noQuestionTv.setVisibility(View.GONE);
            }
            questionAdapter.addItemCollection(data);
            questionAdapter.notifyDataSetChanged();
            questionLv.onRefreshComplete();
        }

        @Override
        public List<MyQuestion> onReqestSuccess(String respones) throws Exception {
            return MyQuestion.jsonParsing(respones);
        }

        @Override
        public void success(List<MyQuestion> cacheRespones, List<MyQuestion> newRespones) {
            if (newRespones == null && questionAdapter.getCount() == 0) {
                noQuestionTv.setVisibility(View.VISIBLE);
                return;
            } else {
                noQuestionTv.setVisibility(View.GONE);
            }
            questionAdapter.replaceItem(cacheRespones, newRespones);
            questionAdapter.notifyDataSetChanged();
            questionLv.onRefreshComplete();
        }

        @Override
        public void exception(Exception exception) {
            Toast.makeText(mContext, getResources().getString(R.string.net_error), Toast.LENGTH_SHORT).show();
        }
    };*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.videoview_question_btn:
                UIHelper.showCourseNewQuestionActivity(mContext, courseId, courseName, "", "");
                break;
            default:
                break;
        }
    }
}
