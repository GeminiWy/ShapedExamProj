package com.nd.shapedexamproj.activity.plaza;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.adapter.PlaygroundRelatemeAdapter;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.model.plaza.RelatemeBaseInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Log;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.RefreshableListView.OnRefreshListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * 
 * 类名: PlaygroundRelatemeActivity <br/>
 * 功能: TODO 与我相关<br/>
 * 描述: <br/>
 * 时间: 2014年5月22日 下午11:30:22 <br/>
 * 
 * @author xujs
 * @version
 * @since JDK 1.6
 */
public class PlaygroundRelatemeActivity extends BaseActivity {

    private MyComparator myComparator = null;
    private final String TAG = "PlaygroundRelatemeActivity";
    private Context mContext;
    private final static int LOAD_TYPE_REFRESH = 1;
    private final static int LOAD_TYPE_MORE = 2;
    public final static int LIST_REFRESH = 1, LIST_LOAD_MORE = 2;
    private int mLoadType = -1;
    private Button common_head_right_btn;
    private ImageView commonheader_left_iv;
    private TextView commonheader_title_tv, noDataTv;
    private RefreshableListView playgroundRelatemeRV;// 上拉加载更多 下拉刷新
    private PlaygroundRelatemeAdapter playgroundRelatemeAdapter;
    private View loadingView;
    private TmingCacheHttp cacheHttp;
    private ImageCacheTool imageCacheTool;// 用于缓存图片
    private List<RelatemeBaseInfo> saveRelatemeBaseInfoList = new ArrayList<RelatemeBaseInfo>();// 保存与我相关基本信息列表的临时集合
    //private List<RelatemeBaseInfo> relatemeInfoList = new ArrayList<RelatemeBaseInfo>();// 与我相关信息列表
    private int page_no = 1, page_Size = 10;
    private String userId;
    private String operateTime = "";
    // 以下是长按列表item项出现的弹窗
    private AlertDialog dialog;
    private LinearLayout relatemeReplyCommentLL, relatemeCheckInfoLL, relatemeDelCommentLL;// 回复评论、查看原动态、删除评论
    private RequestCallback<JsonParseObject<Object>> clearCommentRequestCallback;
    private PersonalInfo mPersonalInfo;// 个人信息model
    private Timeline timelineDynamicInfo;

    @Override
    public int initResource() {
        return R.layout.playground_relateme_stat;
    }

    @Override
    public void initComponent() {
        mContext = PlaygroundRelatemeActivity.this;
        operateTime = getIntent().getStringExtra("operateTime");
        // 获取导航内容
        // common_head_left_btn = (Button)
        // findViewById(R.id.common_head_left_btn);
        commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
        commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
        common_head_right_btn.setVisibility(View.GONE);
        // 获取下拉刷新，上拉加载控件
        playgroundRelatemeRV = (RefreshableListView) findViewById(R.id.playground_relateme_scroll_rv);
        playgroundRelatemeRV.setFootVisible(true);
        loadingView = findViewById(R.id.loading_layout);
        imageCacheTool = ImageCacheTool.getInstance();
    }

    @Override
    public void initData() {
        userId = App.getUserId();
        // common_head_left_btn.setText(getResources().getString(
        // R.string.relateme_title));
        commonheader_title_tv.setText(getResources().getString(R.string.relateme_title));
        common_head_right_btn.setText(getResources().getString(R.string.delete_comment_info));
        playgroundRelatemeAdapter = new PlaygroundRelatemeAdapter(PlaygroundRelatemeActivity.this);
        playgroundRelatemeRV.setAdapter(playgroundRelatemeAdapter);
        cacheHttp = TmingCacheHttp.getInstance();
        requestRelatemeInfoList(userId, page_no, page_Size);// 初始化数据
        requestClearCommentList();
    }

    @Override
    public void addListener() {
        commonheader_left_iv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*
         * common_head_right_btn.setOnClickListener(new OnClickListener() {
         * 
         * @Override public void onClick(View v) { // 清空评论记录
         * loadingView.setVisibility(View.VISIBLE); requestClearCommentList(); }
         * });
         */
        playgroundRelatemeRV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLongClickDialog(position - 1);
            }
        });
        playgroundRelatemeRV.setonRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mLoadType = LOAD_TYPE_REFRESH;
                page_no = 1;
                requestRelatemeInfoList(userId, page_no, page_Size);
            }

            @Override
            public void onLoadMore() {
                mLoadType = LOAD_TYPE_MORE;
                requestRelatemeInfoList(userId, ++page_no, page_Size);
            }
        });
    }

    /**
     * 获取与我相关动态列表
     * 
     * @param userId
     *            用户ID
     * @param pageno
     *            页码
     * @param pageSize
     *            每一页的个数
     */
    private void requestRelatemeInfoList(String userId, int pageno, int pageSize) {
        // 请求参数
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userid", userId);
        params.put("pageno", pageno);
        params.put("pagesize", pageSize);
        TmingHttp.asyncRequest(Constants.MICROBLOG_HOST + "timeline/relatemeV2.do", params,
                new RequestCallback<List<RelatemeBaseInfo>>() {

                    @Override
                    public List<RelatemeBaseInfo> onReqestSuccess(String respones) throws Exception {
                        return relatemeCommentBaseInfoJsonResult(respones);
                    }

                    @Override
                    public void success(List<RelatemeBaseInfo> respones) {
                        loadingView.setVisibility(View.GONE);
                        playgroundRelatemeRV.onRefreshComplete();
                        if (respones.size() == 0 && playgroundRelatemeAdapter.getCount() == 0) {
                            noDataTv.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            noDataTv.setVisibility(View.GONE);
                        }
                        if (mLoadType == LOAD_TYPE_REFRESH) {
                            playgroundRelatemeAdapter.clear();
                        }
                        getRelatemeListInfo(respones);
                        // requestReplyBaseInfo(respones);TODO 旧版使用，新版不使用
                    }

                    @Override
                    public void exception(Exception exception) {
                        loadingView.setVisibility(View.GONE);
                        playgroundRelatemeRV.onRefreshComplete();
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * 解析与我相关评论基本信息
     * 
     * @param result
     * @return
     */
    private List<RelatemeBaseInfo> relatemeCommentBaseInfoJsonResult(String result) {
        try {
            saveRelatemeBaseInfoList.clear();
            JSONObject object = new JSONObject(result);
            if (!object.isNull("flag") && object.getInt("flag") == 1) {
                if (!object.isNull("data")) {
                    JSONArray list = object.getJSONArray("data");
                    if (list != null && list.length() > 0) {
                        for (int i = 0; i < list.length(); i++) {
                            System.out.println("post i value:" + i);
                            RelatemeBaseInfo relatemeBaseInfo = new RelatemeBaseInfo();
                            JSONObject item = list.getJSONObject(i);
                            relatemeBaseInfo.setUserId(item.getInt("userId"));
                            relatemeBaseInfo.setTargetUserId(item.getInt("targetUserId"));
                            relatemeBaseInfo.setTargetId(item.getInt("targetId"));
                            relatemeBaseInfo.setOperateType(item.getInt("operateType"));
                            relatemeBaseInfo.setOperateTime(item.getLong("operateTime"));
                            if (!item.isNull("targetTimeline")) {
                                Timeline targetTimeline = new Timeline();
                                targetTimeline.initWithJsonObject(item.getJSONObject("targetTimeline"));
                                relatemeBaseInfo.setCommentTimelineInfo(targetTimeline);
                            } else {
                                continue;
                            }
                            if (!item.isNull("referTimeline")) {
                                Timeline referTimeline = new Timeline();
                                referTimeline.initWithJsonObject(item.getJSONObject("referTimeline"));
                                relatemeBaseInfo.setDynamicTimelineInfo(referTimeline);
                            } else {
                                continue;
                            }
                            saveRelatemeBaseInfoList.add(relatemeBaseInfo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saveRelatemeBaseInfoList;
    }
    
    /**
     * 获取与我相关回复列表
     */
    private void getRelatemeListInfo(List<RelatemeBaseInfo> respones) {
        if (myComparator == null) {
            myComparator = new MyComparator();
        }
        /*Collections.sort(respones, myComparator);*///TODO 服务端已经排好序了
        loadingView.setVisibility(View.GONE);
        /*
         * for(int i = 0;i < relatemeInfoList.size(); i++) {
         * playgroundRelatemeAdapter.addItem(relatemeInfoList.get(i));
         * 
         * }
         */
        playgroundRelatemeAdapter.addItemCollection(respones);
        playgroundRelatemeAdapter.notifyDataSetChanged();
        playgroundRelatemeRV.onRefreshComplete();
    }

    /**
     * 
     * 方法作用：showLongClickDialog: 长按事件<br/>
     * 
     * @author xujs
     * @since JDK 1.6
     */
    private void showLongClickDialog(final int position) {
        // 列表长按事件
        Builder builder = new Builder(PlaygroundRelatemeActivity.this);
        View view = LayoutInflater.from(PlaygroundRelatemeActivity.this).inflate(
                R.layout.playground_relateme_list_dialog, null);
        // 初始化Dialog中的控件
        relatemeReplyCommentLL = (LinearLayout) view.findViewById(R.id.relateme_reply_comment_ll);// 回复评论
        relatemeCheckInfoLL = (LinearLayout) view.findViewById(R.id.relateme_check_info_ll);// 查看原动态
        relatemeCheckInfoLL.setVisibility(View.GONE);// 入口放在适配器中
        relatemeDelCommentLL = (LinearLayout) view.findViewById(R.id.relateme_del_comment_ll);// 删除评论
        // 目前先屏蔽删除评论布局
        relatemeDelCommentLL.setVisibility(View.GONE);
        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        relatemeReplyCommentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击回复评论
                requestReplyComment((RelatemeBaseInfo) playgroundRelatemeAdapter.getItem(position));
                dialog.dismiss();
            }
        });
        relatemeCheckInfoLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击查看原动态
                dialog.dismiss();
                requestShowComment((RelatemeBaseInfo) playgroundRelatemeAdapter.getItem(position));
            }
        });
        relatemeDelCommentLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 点击删除评论
                Log.e("plaza", "click relatemeDelCommentLL...");
                dialog.dismiss();
            }
        });
    }

    /**
     * 请求回复评论
     */
    private void requestReplyComment(RelatemeBaseInfo relatemeBaseInfoes) {
        Timeline commentTimelineInfo = relatemeBaseInfoes.getCommentTimelineInfo();
        if (commentTimelineInfo == null) {
            return;
        }
        Intent replyCommentintent = new Intent(PlaygroundRelatemeActivity.this, PlaygroundSendCommentActivity.class);
        replyCommentintent.putExtra("send_type", Constants.REPLY_COMMENT);
        replyCommentintent.putExtra("userName", commentTimelineInfo.getUsername());
        replyCommentintent.putExtra("timelineid", commentTimelineInfo.getTimelineId() + "");// 如果是对评论的回复进行评论，修改timelineid为被评论的原动态ID，modified
                                                                                            // by Caiyx on 2014-07-30
        replyCommentintent.putExtra("classid", commentTimelineInfo.getClassId() + "");
        replyCommentintent.putExtra("teachingpointid", commentTimelineInfo.getTeachingPointId() + "");
        replyCommentintent.putExtra("originaltimelineid", commentTimelineInfo.getOriginalTimelineId() + "");
        PlaygroundRelatemeActivity.this.startActivityForResult(replyCommentintent,
                Constants.SEND_COMMENT_SUCCESS_BACK_CEOD);
    }

    /**
     * 
     * 方法作用：requestShowComment: 查看原动态<br/>
     * 
     * @author xujs
     * @param
     * @since JDK 1.6
     */
    private void requestShowComment(RelatemeBaseInfo relatemeBaseInfoes) {
        loadingView.setVisibility(View.VISIBLE);
        String timelineidVal = "";
        Timeline dynamicTimelineInfo = relatemeBaseInfoes.getDynamicTimelineInfo();
        Timeline commentTimelineInfo = relatemeBaseInfoes.getCommentTimelineInfo();
        if (StringUtils.isEmpty(dynamicTimelineInfo.getOriginalTimelineId())
                || "0".equals(dynamicTimelineInfo.getOriginalTimelineId())) {
            timelineidVal = String.valueOf(commentTimelineInfo.getReferId());
        } else {
            timelineidVal = String.valueOf(dynamicTimelineInfo.getOriginalTimelineId());
        }
        requestDynamicInfo(timelineidVal);
    }

    /**
     * 请求动态信息
     */
    private void requestDynamicInfo(String timelineid) {
        TmingHttp.asyncRequest(ServerApi.Timeline.getTimeline(timelineid), new RequestCallback<Timeline>() {

            @Override
            public Timeline onReqestSuccess(String respones) throws Exception {
                return dynamicInfoList(respones);
            }

            @Override
            public void success(Timeline respones) {
                loadingView.setVisibility(View.GONE);
                if (respones == null) {
                    UIHelper.ToastMessage(mContext, "无法获取原动态数据，请重试");
                    return;
                }
                UIHelper.showTimelineActivity(mContext, respones);
                /* requestUserInfo(); */
            }

            @Override
            public void exception(Exception exception) {
                loadingView.setVisibility(View.GONE);
                UIHelper.ToastMessage(mContext, "无法获取原动态数据，请重试");
                return;
            }
        });
    }

    /**
     * 解析返回数据
     * 
     * @param result
     * @return
     */
    private Timeline dynamicInfoList(String result) {
        try {
            JSONObject resObj = new JSONObject(result);
            if (!resObj.isNull("flag") && resObj.getInt("flag") == 1) {
                if (!resObj.isNull("data")) {
                    JSONArray dataAry = resObj.getJSONArray("data");
                    if (null != dataAry && dataAry.length() > 0) {
                        for (int i = 0; i < dataAry.length(); i++) {
                            Timeline timelineInfo = new Timeline();
                            JSONObject item = dataAry.getJSONObject(i);
                            timelineInfo.initWithJsonObject(item);
                            timelineDynamicInfo = timelineInfo;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return timelineDynamicInfo;
    }

    /**
     * 清除与我相关标记
     */
    private void requestClearCommentList() {
        clearCommentJsonResult();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userid", userId);
        TmingHttp
                .asyncRequest(Constants.MICROBLOG_HOST + "timeline/clearrelateme.do", map, clearCommentRequestCallback);
    }

    /**
     * 解析清空评论请求后的数据
     * 
     * @param
     *
     * @return 是否清空成功
     */
    private void clearCommentJsonResult() {
        clearCommentRequestCallback = new RequestCallback<JsonParseObject<Object>>() {

            @Override
            public void exception(Exception exception) {}

            @Override
            public JsonParseObject<Object> onReqestSuccess(String respones) throws Exception {
                return JsonParseObject.parseJson(respones);
            }

            @Override
            public void success(JsonParseObject<Object> respones) {
                saveLastUpdataTime(operateTime);// 等清除完上次的“与我相关”后，再记录新的时间
            }
        };
    }

    /**
     * 保存上一次最新与我相关信息时间
     * 
     * @param lastTime
     */
    private void saveLastUpdataTime(String lastTime) {
        if (StringUtils.isEmpty(lastTime)) {
            return;
        }
        // 获得SharedPreferences对象
        SharedPreferences saveLastUpdataTimePreferences = getSharedPreferences(Constants.PREFERENCES_NAME,
                Activity.MODE_PRIVATE);
        // 获得SharedPreferences.Editor
        SharedPreferences.Editor editor = saveLastUpdataTimePreferences.edit();
        // 保存组件中的值
        editor.putString(App.getUserId(), lastTime);
        // 提交保存的结果
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO 回复评论成功后刷新与我相关动态列表
        switch (resultCode) {
            case Constants.SEND_COMMENT_SUCCESS_BACK_CEOD:
                mLoadType = LOAD_TYPE_REFRESH;
                page_no = 1;
                playgroundRelatemeAdapter.clear();
                requestRelatemeInfoList(userId, page_no, page_Size);
                Log.d(TAG, "PlaygroundRelatemeActivity SEND_COMMENT_SUCCESS_BACK_CEOD");
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class MyComparator implements Comparator<RelatemeBaseInfo> {

        @Override
        public int compare(RelatemeBaseInfo object1, RelatemeBaseInfo object2) {
            if (object1.getOperateTime() < object2.getOperateTime()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
