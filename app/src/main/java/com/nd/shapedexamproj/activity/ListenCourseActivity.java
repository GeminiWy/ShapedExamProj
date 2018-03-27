package com.nd.shapedexamproj.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.activity.im.CheckAllStudyResourceActivity;
import com.nd.shapedexamproj.model.CourseDetail;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2Adapter;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 听课
 * 
 * @author linlg
 * 
 */
public class ListenCourseActivity extends BaseActivity {

    private static final String TAG = "ListenCourseActivity";
    private ViewPager viewPager;// 页卡内容
    private ImageView cursorIv;// 动画图片
    private List<View> pages;// Tab页面列表
    private int offset = 0;// 动画图片偏移量
    private int currentIndex = 0;// 当前页卡编号
    private int width = 0;// 动画图片宽度
    private TextView playHistoryTv, doingTv;
    private Handler handler;
    private TmingCacheHttp cacheHttp;
    private RefreshableListView playHistoryLV, coursingLV;
    private TextView playHistoryNoDataTv, coursingNoDataTv;
    private PlayHistoryAdapter playHistoryAdapter;
    private CoursingAdapter coursingAdapter;
    private View playHistoryLoadingView, coursingLoadingView;
    private List<PlayHistory> playHistorys = new ArrayList<PlayHistory>();
    private List<Coursing> coursings = new ArrayList<Coursing>();
    private int playHistoryPage = 1, pageSize = 10, coursingPage = 1;
    // ListView底部View
    public View footView;
    public Button checkAllBtn; // 查看所有按钮
    // private Button /* cleanBtn, */editBtn;
    // private ImageView lineIv;
    private boolean isEdit = false;

    @Override
    public int initResource() {
        return R.layout.listen_course;
    }

    @Override
    public void initComponent() {
        cursorIv = (ImageView) findViewById(R.id.listen_course_cursor_iv);
        viewPager = (ViewPager) findViewById(R.id.listen_course_content_vp);
        playHistoryTv = (TextView) findViewById(R.id.listen_course_history_tv);
        doingTv = (TextView) findViewById(R.id.listen_course_doing_tv);
        // cleanBtn = (Button) findViewById(R.id.listen_course_clean_btn);
        // editBtn = (Button) findViewById(R.id.listen_course_edit_btn);
        findViewById(R.id.listen_course_edit_btn).setVisibility(View.GONE);
        // cleanBtn.setOnClickListener(onClickListenerImpl);
        // lineIv = (ImageView) findViewById(R.id.listen_course_line_iv);
        // editBtn.setOnClickListener(onClickListenerImpl);
        cacheHttp = TmingCacheHttp.getInstance(this);
        initHandler();
        initViewPager();
        findViewById(R.id.listen_course_left_iv).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.listen_course_history_tv).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        findViewById(R.id.listen_course_doing_tv).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        // width = ((ImageView)
        // findViewById(R.id.listen_course_line_iv)).getWidth();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        offset = (dm.widthPixels / 2 - width) / 2;
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursorIv.setImageMatrix(matrix);
        pages = new ArrayList<View>();
        pages.add(getPlayHistoryView());
        pages.add(getCoursingView());
        viewPager.setAdapter(pagerAdapterImpl);
        viewPager.setOnPageChangeListener(new OnPageChangeListenerImpl());
    }

    /**
     * ViewPager页面切换监听
     */
    private class OnPageChangeListenerImpl implements OnPageChangeListener {

        private int mWidth = offset * 2 + width;

        @Override
        public void onPageScrollStateChanged(int arg0) {}

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                playHistoryTv.setTextColor(getResources().getColor(R.color.title_green));
                doingTv.setTextColor(getResources().getColor(R.color.black));
                // lineIv.setVisibility(View.VISIBLE);
                // cleanBtn.setVisibility(View.VISIBLE);
                // editBtn.setVisibility(View.VISIBLE);
            } else {
                doingTv.setTextColor(getResources().getColor(R.color.title_green));
                playHistoryTv.setTextColor(getResources().getColor(R.color.black));
                // lineIv.setVisibility(View.GONE);
                // cleanBtn.setVisibility(View.GONE);
                // editBtn.setVisibility(View.GONE);
            }
            Animation animation = new TranslateAnimation(mWidth * currentIndex, mWidth * position, 0, 0);
            currentIndex = position;
            animation.setFillAfter(true);
            animation.setDuration(300);
            cursorIv.startAnimation(animation);
        }
    }

    private void initHandler() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1: // 播放历史视图数据加载完成
                        playHistoryLoadingView.setVisibility(View.GONE);
                        playHistoryAdapter.notifyDataSetChanged();
                        break;
                    case 2: // 在修课程视图数据加载完成
                        if (coursings.size() > 0) {
                            footView.setVisibility(View.VISIBLE);
                        }
                        coursingLoadingView.setVisibility(View.GONE);
                        coursingAdapter.notifyDataSetChanged();
                        break;
                }
            }
        };
    }

    /**
     * ViewPager适配器
     */
    PagerAdapter pagerAdapterImpl = new PagerAdapter() {

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pages.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(pages.get(position), 0);
            return pages.get(position);
        }
    };

    /**
     * 播放历史视图
     */
    private View getPlayHistoryView() {
        View playHistoryView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
        playHistoryLoadingView = playHistoryView.findViewById(R.id.loading_layout);
        playHistoryNoDataTv = (TextView) playHistoryView.findViewById(R.id.no_data_tv);
        playHistoryLV = (RefreshableListView) playHistoryView.findViewById(R.id.refreshable_listview);
        playHistoryAdapter = new PlayHistoryAdapter();
        playHistoryLV.setAdapter(playHistoryAdapter);
        playHistoryLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playHistorys.size() == 0) {
                    return;
                }
                // 请求网络获取url
                playHistoryLoadingView.setVisibility(View.VISIBLE);
                requestVideoUrl(position);
            }
        });
        playHistoryLV.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                playHistoryPage = 1;
                playHistorys.clear();
                requestPlayHistoryData();
            }

            @Override
            public void onLoadMore() {
                ++playHistoryPage;
                requestPlayHistoryData();
            }
        });
        return playHistoryView;
    }

    /**
     * 
     * <p>
     * 获取视频地址
     * </P>
     * 
     * @param position
     */
    private void requestVideoUrl(final int position) {
        final PlayHistory playHistory = playHistorys.get(position - 1);
        String courseawareid = playHistory.videoId;
        TmingHttp.asyncRequest(ServerApi.getCoursewareDetailUrl(courseawareid), null, new RequestCallback<CourseDetail>() {

            @Override
            public CourseDetail onReqestSuccess(String respones) throws Exception {
                return CourseDetail.JSONPasring(respones);
            }

            @Override
            public void success(CourseDetail respones) {
                playHistoryLoadingView.setVisibility(View.GONE);
                if (respones == null) {
                    return;
                }
                Intent it = new Intent(ListenCourseActivity.this, CourseDetailActivity.class);
                it.putExtra("course_id", playHistory.courseId);
                it.putExtra("course_name", playHistory.name);
                it.putExtra("video_name", playHistory.lesson);
                it.putExtra("video_id", playHistory.videoId);
                it.putExtra("video_url", respones.url);
                it.putExtra("local", 2);
                startActivity(it);
            }

            @Override
            public void exception(Exception exception) {
                playHistoryLoadingView.setVisibility(View.GONE);
            }
        });
    }

    private void requestPlayHistoryData() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("pageNum", playHistoryPage);
        params.put("pageSize", pageSize);
        cacheHttp.asyncRequestWithCache(Constants.HOST + "student/playhistory.html", params, getPlayHistoryCallBack);
    }
    
    private RequestWithCacheCallBackV2Adapter<List<PlayHistory>> getPlayHistoryCallBack = new RequestWithCacheCallBackV2Adapter<List<PlayHistory>>() {
        
        @Override
        public void requestNewDataRespone(List<PlayHistory> cacheRespones, List<PlayHistory> newRespones) {
            playHistoryLoadingView.setVisibility(View.GONE);
            playHistoryLV.onRefreshComplete();
            if (newRespones.size() == 0 && playHistorys.size() == 0) {
                playHistoryNoDataTv.setVisibility(View.VISIBLE);
                playHistoryNoDataTv.setText(getResources().getString(R.string.no_play_history));
                /*viewPager.setCurrentItem(1);*///播放历史为空的时候下拉刷新，不自动跳转到在修课程页面 add by zll
                return;
            } else {
                playHistoryNoDataTv.setVisibility(View.GONE);
            }
            playHistoryAdapter.replaceItem(cacheRespones, newRespones);
            playHistoryAdapter.notifyDataSetChanged();
        }
        
        @Override
        public List<PlayHistory> parseData(String data) throws Exception {
            return playHistoryJSONPasing(data);
        }
        
        @Override
        public void cacheDataRespone(List<PlayHistory> data) {
            playHistoryLoadingView.setVisibility(View.GONE);
            playHistoryLV.onRefreshComplete();
            if (data.size() == 0 && playHistorys.size() == 0) {
                playHistoryNoDataTv.setVisibility(View.VISIBLE);
                playHistoryNoDataTv.setText(getResources().getString(R.string.no_play_history));
                /*viewPager.setCurrentItem(1);*///播放历史为空的时候下拉刷新，不自动跳转到在修课程页面 add by zll
                return;
            } else {
                playHistoryNoDataTv.setVisibility(View.GONE);
            }
            for (int i = 0; i < data.size(); i++) {
                PlayHistory history = data.get(i);
                // 检测关键字段是否为空
                if (checkPlayHistoryItemIsEmpty(history)) {
                    Log.e(TAG, "requestPlayHistoryData 11");
                    continue;
                }
                Log.e(TAG, "requestPlayHistoryData 12");
                if (!playHistorys.contains(history)) {
                    playHistorys.add(history);
                }
            }
            playHistoryAdapter.notifyDataSetChanged();
        }
        
        public void exception(Exception exception) {
            playHistoryLoadingView.setVisibility(View.GONE);
            playHistoryLV.onRefreshComplete();
            Toast.makeText(ListenCourseActivity.this, getResources().getString(R.string.net_error_tip),
                    Toast.LENGTH_LONG).show();
        }
    };

    /**
     * 检测播放历史是否含有空的字段数据，true表示含有空字段，
     */
    private boolean checkPlayHistoryItemIsEmpty(PlayHistory history) {
        if (history == null) {
            return true;
        }
        if (StringUtils.isEmpty(history.lesson) || StringUtils.isEmpty(history.name)
                || StringUtils.isEmpty(history.courseId)) {
            return true;
        }
        return false;
    }

    private class PlayHistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return playHistorys.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void replaceItem(List<PlayHistory> oldData, List<PlayHistory> newData) {
            // 如果不存在旧数据
            if (oldData == null || oldData.size() == 0) {
                playHistorys.addAll(newData);
                notifyDataSetChanged();
                return;
            }
            // 做替换
            int oldStartIndex = playHistorys.indexOf(oldData.get(0));
            int oldEndIndex = playHistorys.indexOf(oldData.get(oldData.size() - 1));
            if (oldStartIndex != -1 && oldEndIndex != -1) {
                for (int i = oldStartIndex; i <= oldEndIndex; i++) {
                    playHistorys.remove(oldStartIndex);
                }
                int insertStartIndex = oldStartIndex;
                for (int i = 0; i < newData.size(); i++) {
                    /**
                     * 检测关键字段是否为空
                     * */
                    if (checkPlayHistoryItemIsEmpty(newData.get(i))) {
                        Log.e(TAG, "replaceItem 11");
                        continue;
                    }
                    Log.e(TAG, "replaceItem 12");
                    playHistorys.add(insertStartIndex++, newData.get(i));
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            PlayHistoryHolder holder = null;
            if (convertView == null) {
                holder = new PlayHistoryHolder();
                convertView = LayoutInflater.from(ListenCourseActivity.this).inflate(
                        R.layout.listen_course_history_item, null);
                holder.titleTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_title_tv);
                holder.rateTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_rate_tv);
                holder.closeIv = (ImageView) convertView.findViewById(R.id.listen_course_history_item_closed_iv);
                convertView.setTag(holder);
            } else {
                holder = (PlayHistoryHolder) convertView.getTag();
            }
            holder.titleTv.setText(playHistorys.get(position).name);
            holder.rateTv.setText("进度：《" + playHistorys.get(position).lesson + "》 "
                    + getTime(playHistorys.get(position).time));
            holder.closeIv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    playHistorys.remove(position);
                    handler.sendEmptyMessage(1);
                }
            });
            if (playHistorys.get(position).flag != 0) {
                holder.closeIv.setVisibility(View.VISIBLE);
            } else {
                holder.closeIv.setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    private String getTime(String time) {
        int second = Integer.parseInt(time);
        return "" + second / 60 + "分" + second % 60 + "秒";
    }

    /**
     * 播放历史
     */
    public class PlayHistory {

        public String courseId;
        public String time;
        public String name;
        public String lesson;
        public String url;
        public String videoId;
        public int flag;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PlayHistory)) {
                return false;
            }
            PlayHistory playHistory = (PlayHistory) o;
            if (playHistory.courseId.equals(this.courseId) && playHistory.lesson.equals(this.lesson)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public class PlayHistoryHolder {

        public TextView titleTv;
        public TextView rateTv;
        public ImageView closeIv;
    }

    private List<PlayHistory> playHistoryJSONPasing(String result) throws JSONException {
        List<PlayHistory> tmpPlayHistorys = new ArrayList<PlayHistory>();
        JSONObject object = new JSONObject(result);
        JSONObject dataObj = object.getJSONObject("res").getJSONObject("data");
        int total = dataObj.getInt("total");
        if (total == 0) {
            return tmpPlayHistorys;
        }
        JSONArray list = dataObj.getJSONArray("list");
        for (int i = 0; i < list.length(); i++) {
            PlayHistory playHistory = new PlayHistory();
            playHistory.time = list.getJSONObject(i).getString("time");
            playHistory.lesson = list.getJSONObject(i).getString("lesson");
            playHistory.name = list.getJSONObject(i).getString("name");
            playHistory.courseId = list.getJSONObject(i).getString("course_id");
            playHistory.videoId = list.getJSONObject(i).getString("chapter_id");
            playHistory.flag = 0;
            // 检测关键字段是否为空
            if (checkPlayHistoryItemIsEmpty(playHistory)) {
                //Log.e(TAG, "playHistoryJSONPasing 11");
                continue;
            }
            //Log.e(TAG, "playHistoryJSONPasing 12");
            tmpPlayHistorys.add(playHistory);
        }
        return tmpPlayHistorys;
    }

    private View getCoursingView() {
        View coursingView = LayoutInflater.from(this).inflate(R.layout.refreshable_listview, null);
        coursingNoDataTv = (TextView) coursingView.findViewById(R.id.no_data_tv);
        coursingLV = (RefreshableListView) coursingView.findViewById(R.id.refreshable_listview);
        // 实例化listView底部布局
        footView = LayoutInflater.from(this).inflate(R.layout.check_all_study_resource, null);
        checkAllBtn = (Button) footView.findViewById(R.id.check_all_study_resource_btn);
        footView.setVisibility(View.INVISIBLE);
        /*coursingLV.setFootVisible(false);*/
        // 加上底部View，注意要放在setAdapter方法前
        /*coursingLV.addFooterView(footView);*///去掉“查看全部学习资源”
        coursingLoadingView = coursingView.findViewById(R.id.loading_layout);
        coursingAdapter = new CoursingAdapter();
        coursingLV.setAdapter(coursingAdapter);
        requestCoursingData();
        coursingLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UIHelper.showCourseDetail(ListenCourseActivity.this, coursings.get(position - 1).courseId,
                        coursings.get(position - 1).name);
            }
        });
        coursingLV.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                coursingPage = 1;
                coursings.clear();
                requestCoursingData();
            }

            @Override
            public void onLoadMore() {
                ++coursingPage;
                requestCoursingData();
            }
        });
        checkAllBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                OpenCheckAllStudyResourceActivity();
            }
        });
        return coursingView;
    }

    public class Coursing {

        public String name;
        public String times;
        public String credit;
        public String courseId;
        public double percent;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Coursing)) {
                return false;
            }
            Coursing coursing = (Coursing) o;
            if (coursing.courseId.equals(this.courseId)) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void requestCoursingData() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("pageNum", coursingPage);
        params.put("pageSize", pageSize);
        cacheHttp.asyncRequestWithCache(Constants.HOST + "student/courses.html", params, requestCoursingCallBack);
    }

    private RequestWithCacheCallBackV2Adapter<List<Coursing>> requestCoursingCallBack = new RequestWithCacheCallBackV2Adapter<List<Coursing>>() {

        @Override
        public void cacheDataRespone(List<Coursing> data) {
            coursingLoadingView.setVisibility(View.GONE);
            coursingLV.onRefreshComplete();
            if (data.size() == 0 && coursings.size() == 0) {
                coursingNoDataTv.setVisibility(View.VISIBLE);
                coursingNoDataTv.setText(getResources().getString(R.string.no_study_course));
                return;
            } else {
                coursingNoDataTv.setVisibility(View.GONE);
            }
            for (int i = 0; i < data.size(); i++) {
                Coursing mCoursing = data.get(i);
                if (!coursings.contains(mCoursing)) {
                    coursings.add(mCoursing);
                }
            }
            handler.sendEmptyMessage(2);
        }

        @Override
        public void requestNewDataRespone(List<Coursing> cacheRespones, List<Coursing> newRespones) {
            coursingLoadingView.setVisibility(View.GONE);
            coursingLV.onRefreshComplete();
            if (newRespones.size() == 0 && coursings.size() == 0) {
                coursingNoDataTv.setVisibility(View.VISIBLE);
                coursingNoDataTv.setText(getResources().getString(R.string.no_study_course));
                return;
            } else {
                coursingNoDataTv.setVisibility(View.GONE);
            }
            coursingAdapter.replaceItem(cacheRespones, newRespones);
            handler.sendEmptyMessage(2);
        }

        @Override
        public List<Coursing> parseData(String data) throws Exception {
            return coursingJSONParsing(data);
        }

        @Override
        public void exception(Exception exception) {
            coursingLoadingView.setVisibility(View.GONE);
            coursingLV.onRefreshComplete();
            Toast.makeText(ListenCourseActivity.this, getResources().getString(R.string.net_error_tip),
                    Toast.LENGTH_SHORT).show();
        }
    };

    private List<Coursing> coursingJSONParsing(String result) throws JSONException {
        List<Coursing> coursings = new ArrayList<Coursing>();
        JSONObject object = new JSONObject(result);
        JSONObject dataObj = object.getJSONObject("res").getJSONObject("data");
        if (!dataObj.isNull("list")) {
            JSONArray list = dataObj.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                Coursing coursing = new Coursing();
                coursing.courseId = list.getJSONObject(i).getString("course_id");
                coursing.credit = list.getJSONObject(i).getString("course_credit");
                coursing.name = list.getJSONObject(i).getString("name");
                coursing.times = list.getJSONObject(i).getString("times");
                coursing.percent = list.getJSONObject(i).getDouble("course_percent");
                coursings.add(coursing);
            }
        }
        return coursings;
    }

    private class CoursingAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return coursings.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void replaceItem(List<Coursing> oldData, List<Coursing> newData) {
            // 如果不存在旧数据
            if (oldData == null || oldData.size() == 0) {
                coursings.addAll(newData);
                notifyDataSetChanged();
                return;
            }
            // 做替换
            int oldStartIndex = coursings.indexOf(oldData.get(0));
            int oldEndIndex = coursings.indexOf(oldData.get(oldData.size() - 1));
            if (oldStartIndex != -1 && oldEndIndex != -1) {
                for (int i = oldStartIndex; i <= oldEndIndex; i++) {
                    coursings.remove(oldStartIndex);
                }
                int insertStartIndex = oldStartIndex;
                for (int i = 0; i < newData.size(); i++) {
                    coursings.add(insertStartIndex++, newData.get(i));
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PlayHistoryHolder holder = null;
            if (convertView == null) {
                holder = new PlayHistoryHolder();
                convertView = LayoutInflater.from(ListenCourseActivity.this).inflate(
                        R.layout.listen_course_history_item, null);
                holder.titleTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_title_tv);
                holder.rateTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_rate_tv);
                convertView.setTag(holder);
            } else {
                holder = (PlayHistoryHolder) convertView.getTag();
            }
            holder.titleTv.setText(coursings.get(position).name);
            holder.rateTv.setText(coursings.get(position).credit + "分" + " | " + coursings.get(position).times + "课时"
                    + " | " + "学习进度：" + (int) (100 * coursings.get(position).percent) + "%");
            return convertView;
        }
    }

    private OnClickListener onClickListenerImpl = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            // case R.id.listen_course_clean_btn:
            // playHistorys.clear();
            // handler.sendEmptyMessage(1);
            // break;
                case R.id.listen_course_edit_btn:
                    if (!isEdit) {
                        isEdit = true;
                        for (int i = 0; i < playHistorys.size(); i++) {
                            playHistorys.get(i).flag = 1;
                        }
                        handler.sendEmptyMessage(1);
                        // editBtn.setText("完成");
                    } else {
                        isEdit = false;
                        for (int i = 0; i < playHistorys.size(); i++) {
                            playHistorys.get(i).flag = 0;
                        }
                        handler.sendEmptyMessage(1);
                        // editBtn.setText("编辑");
                    }
                    break;
            }
        }
    };

    @Override
    public void initData() {}

    @Override
    public void addListener() {}

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_BACK) {
    // if (isEdit) {
    // isEdit = false;
    // for (int i = 0; i < playHistorys.size(); i++) {
    // playHistorys.get(i).flag = 0;
    // }
    // handler.sendEmptyMessage(1);
    // } else {
    // finish();
    // }
    // }
    // return false;
    // }
    /**
     * 查看全部学习资源入口
     * */
    private void OpenCheckAllStudyResourceActivity() {
        Intent intent = new Intent(ListenCourseActivity.this, CheckAllStudyResourceActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        playHistoryPage = 1;
        playHistorys.clear();
        requestPlayHistoryData();
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.tming.sumbitPlayPosition");
        Helper.registLocalReciver(this, mBroadcastReceiver, intentFilter);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Helper.unregistLocalReciver(this, mBroadcastReceiver);
    }
    
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.tming.sumbitPlayPosition".equals(action)) {
                // 刷新播放历史
                playHistoryPage = 1;
                playHistorys.clear();
                requestPlayHistoryData();
            }
        }
    };
}
