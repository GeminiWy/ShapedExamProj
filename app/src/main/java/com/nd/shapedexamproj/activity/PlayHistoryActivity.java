package com.nd.shapedexamproj.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.activity.im.CheckAllStudyResourceActivity;
import com.nd.shapedexamproj.model.CourseDetail;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Helper;
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
 * <p> PlayHistoryActivity 播放历史 </p>
 * <p> Created by xuwenzhuo on 2014/10/27.</p>
 */

public class PlayHistoryActivity  extends BaseActivity{

    private static final String TAG = "PlayHistoryActivity";
    private ImageView mTurnBackImgView;
    private RefreshableListView mHistoryRefreshListView;
    private PlayHistoryAdapter mPlayHistoryAdapter;
    private View playHistoryLoadingView;
    
    private Handler mHandler;
    private TmingCacheHttp mTmingCacheHttp;
    private TextView mNoPlayHistoryLV;
    private View mPlayHistoryLoadingView;

    private List<PlayHistory> mPlayHistorys = new ArrayList<PlayHistory>();
    private int mPlayHistoryPage = 1;

    private final static int PAGESIZE = 10;
    private boolean isEdit = false;

    @Override
    public int initResource() {
        return R.layout.play_history;
    }

    @Override
    public void initComponent() {

        mTurnBackImgView=(ImageView) findViewById(R.id.playhistory_course_left_iv);
        mHistoryRefreshListView=(RefreshableListView) findViewById(R.id.playhistory_refreshlv);
        playHistoryLoadingView = findViewById(R.id.loading_layout);
        //Drawable divider=getResources().getDrawable(R.drawable.playground_user_age_bg);
        mPlayHistoryLoadingView=(View) findViewById(R.id.loading_layout);
        mNoPlayHistoryLV=(TextView) findViewById(R.id. no_data_tv);

        mPlayHistoryAdapter = new PlayHistoryAdapter(mPlayHistorys);
        mHistoryRefreshListView.setAdapter(mPlayHistoryAdapter);
        mTmingCacheHttp = TmingCacheHttp.getInstance(this);
        //返回按钮
        findViewById(R.id.playhistory_course_left_iv).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mHistoryRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playHistoryLoadingView.setVisibility(View.VISIBLE);
                requestVideoUrl(position);
            }
        });

        mHistoryRefreshListView.setonRefreshListener(new RefreshableListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPlayHistoryPage = 1;
                mPlayHistorys.clear();
                requestPlayHistoryData();
            }

            @Override
            public void onLoadMore() {
                ++mPlayHistoryPage;
                requestPlayHistoryData();
            }
        });

    }

    @Override
    public void initData() {

    }

    @Override
    public void addListener() {

    }

    /**
     * <p>检测播放历史是否含有空的字段数据，true表示含有空字段</p>
     * @param history 播放历史对象
     * @return
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

    /**
     * <p> 获取视频地址 </P>
     * @param position 视屏位置
     */
    private void requestVideoUrl(final int position) {
        final PlayHistory playHistory = mPlayHistorys.get(position - 1);
        String courseawareid = playHistory.videoId;
        TmingHttp.asyncRequest(ServerApi.getCoursewareDetailUrl(courseawareid), null, new TmingHttp.RequestCallback<CourseDetail>() {

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
                Intent it = new Intent(PlayHistoryActivity.this, CourseDetailActivity.class);
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

    /**
     * <p>请求播放历史数据</p>
     */
    private void requestPlayHistoryData() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", App.getUserId());
        params.put("pageNum", mPlayHistoryPage);
        params.put("pageSize", PAGESIZE);
        mTmingCacheHttp.asyncRequestWithCache(Constants.HOST + "student/playhistory.html", params, getPlayHistoryCallBack);
    }

    private TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<PlayHistory>> getPlayHistoryCallBack = new TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<PlayHistory>>() {

        @Override
        public void requestNewDataRespone(List<PlayHistory> cacheRespones, List<PlayHistory> newRespones) {
            mPlayHistoryLoadingView.setVisibility(View.GONE);
            mHistoryRefreshListView.onRefreshComplete();
            if (newRespones.size() == 0 && mPlayHistorys.size() == 0) {
                mNoPlayHistoryLV.setVisibility(View.VISIBLE);
                mNoPlayHistoryLV.setText(getResources().getString(R.string.no_play_history));
                /*viewPager.setCurrentItem(1);*///播放历史为空的时候下拉刷新，不自动跳转到在修课程页面 add by zll
                return;
            } else {
                mNoPlayHistoryLV.setVisibility(View.GONE);
            }
            mPlayHistoryAdapter.replaceItem(cacheRespones, newRespones);
            mPlayHistoryAdapter.notifyDataSetChanged();
        }

        @Override
        public List<PlayHistory> parseData(String data) throws Exception {
            return playHistoryJSONPasing(data);
        }

        @Override
        public void cacheDataRespone(List<PlayHistory> data) {
            mPlayHistoryLoadingView.setVisibility(View.GONE);
            mHistoryRefreshListView.onRefreshComplete();
            if (data.size() == 0 && mPlayHistorys.size() == 0) {
                mNoPlayHistoryLV.setVisibility(View.VISIBLE);
                mNoPlayHistoryLV.setText(getResources().getString(R.string.no_play_history));
                /*viewPager.setCurrentItem(1);*///播放历史为空的时候下拉刷新，不自动跳转到在修课程页面 add by zll
                return;
            } else {
                mNoPlayHistoryLV.setVisibility(View.GONE);
            }
            for (int i = 0; i < data.size(); i++) {
                PlayHistory history = data.get(i);
                // 检测关键字段是否为空
                if (checkPlayHistoryItemIsEmpty(history)) {
                    Log.e(TAG, "requestPlayHistoryData 11");
                    continue;
                }
                Log.e(TAG, "requestPlayHistoryData 12");
                if (!mPlayHistorys.contains(history)) {
                    mPlayHistorys.add(history);
                }
            }
            mPlayHistoryAdapter.notifyDataSetChanged();
        }

        public void exception(Exception exception) {
            mPlayHistoryLoadingView.setVisibility(View.GONE);
            mHistoryRefreshListView.onRefreshComplete();
            Toast.makeText(PlayHistoryActivity.this, getResources().getString(R.string.net_error_tip),
                    Toast.LENGTH_LONG).show();
        }
    };

    private class PlayHistoryAdapter extends BaseAdapter {

        List<PlayHistory> mPlayHistorys = null;

        public PlayHistoryAdapter(List<PlayHistory> mPlayHistorys) {
            this.mPlayHistorys = mPlayHistorys;
        }

        @Override
        public int getCount() {
            return mPlayHistorys.size();
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
                mPlayHistorys.addAll(newData);
                notifyDataSetChanged();
                return;
            }
            // 做替换
            int oldStartIndex = mPlayHistorys.indexOf(oldData.get(0));
            int oldEndIndex = mPlayHistorys.indexOf(oldData.get(oldData.size() - 1));
            if (oldStartIndex != -1 && oldEndIndex != -1) {
                for (int i = oldStartIndex; i <= oldEndIndex; i++) {
                    mPlayHistorys.remove(oldStartIndex);
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
                    mPlayHistorys.add(insertStartIndex++, newData.get(i));
                }
            }
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            PlayHistoryHolder holder = null;
            if (convertView == null) {
                holder = new PlayHistoryHolder();
                convertView = LayoutInflater.from(PlayHistoryActivity.this).inflate(
                        R.layout.listen_course_history_item, null);
                holder.titleTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_title_tv);
                holder.rateTv = (TextView) convertView.findViewById(R.id.listen_course_history_item_rate_tv);
                holder.closeIv = (ImageView) convertView.findViewById(R.id.listen_course_history_item_closed_iv);
                convertView.setTag(holder);
            } else {
                holder = (PlayHistoryHolder) convertView.getTag();
            }
            holder.titleTv.setText(mPlayHistorys.get(position).name);
            holder.rateTv.setText("观看至：《" + mPlayHistorys.get(position).lesson + "》 "
                    + getTime(mPlayHistorys.get(position).time));
            holder.closeIv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mPlayHistorys.remove(position);
                    mHandler.sendEmptyMessage(1);
                }
            });
            if (mPlayHistorys.get(position).flag != 0) {
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

    private View.OnClickListener onClickListenerImpl = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.listen_course_edit_btn:
                    if (!isEdit) {
                        isEdit = true;
                        for (int i = 0; i < mPlayHistorys.size(); i++) {
                            mPlayHistorys.get(i).flag = 1;
                        }
                        mHandler.sendEmptyMessage(1);
                        // editBtn.setText("完成");
                    } else {
                        isEdit = false;
                        for (int i = 0; i < mPlayHistorys.size(); i++) {
                            mPlayHistorys.get(i).flag = 0;
                        }
                        mHandler.sendEmptyMessage(1);
                        // editBtn.setText("编辑");
                    }
                    break;
            }
        }
    };

    /**
     * 查看全部学习资源入口
     * */
     private void OpenCheckAllStudyResourceActivity() {
        Intent intent = new Intent(PlayHistoryActivity.this, CheckAllStudyResourceActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayHistoryPage = 1;

        if (mPlayHistorys!=null){
            mPlayHistorys.clear();
        }
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
                mPlayHistoryPage = 1;
                mPlayHistorys.clear();
                requestPlayHistoryData();
            }
        }
    };
}
