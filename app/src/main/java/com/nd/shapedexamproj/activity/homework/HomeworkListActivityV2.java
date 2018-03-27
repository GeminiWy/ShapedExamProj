package com.nd.shapedexamproj.activity.homework;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.Homework;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonParseObject;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.BaseActivity;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.cache.net.TmingCacheHttp.RequestWithCacheCallBackV2;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.view.RefreshableListView;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * <p> 第二版本作业列表</p>
 * <p>Created by zll on 2014-12-26</p>
 */
public class HomeworkListActivityV2 extends BaseActivity{
    
    private final static int LOAD_TYPE_REFRESH = 1;
    private final static int LOAD_TYPE_MORE = 2;
    private int mLoadType = LOAD_TYPE_REFRESH;
    
    private ImageView commonheaderLeftIV;
    private TextView commonheaderTitleTV,noDataTv;
    private Button commonHeadRightBtn;
    
    private RefreshableListView mHomeworkLV;
    private HomeworkListAdapter adapter;
    private List<Homework> datas ;
    private String mCourseId;
    private String order = "desc";
    private int pageNum = 1,pageSize = 10;
    private TmingCacheHttp cacheHttp ;
    private Drawable tip1;
    private long currentTime = 0;
    
    
    @Override
    public int initResource() {
        return R.layout.homework_list_activity_v2;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends View> T $(int id) {
        return (T)super.findViewById(id);
        
    }
    
    @Override
    public void initComponent() {
        commonheaderLeftIV = (ImageView) $(R.id.commonheader_left_iv);
        commonheaderTitleTV = (TextView) $(R.id.commonheader_title_tv);
        commonheaderTitleTV.setText(R.string.do_homework);
        noDataTv = (TextView) findViewById(R.id.no_data_tv);
        commonHeadRightBtn = (Button) $(R.id.common_head_right_btn);
        mHomeworkLV = $(R.id.refreshable_listview);
        
        mCourseId = getIntent().getStringExtra("course_id");
        tip1 = getResources().getDrawable(R.drawable.classroom_icon_homework1);
        datas = new ArrayList<Homework>();
        adapter = new HomeworkListAdapter(this, datas);
        mHomeworkLV.setAdapter(adapter);
        
    }

    @Override
    public void initData() {
        cacheHttp = TmingCacheHttp.getInstance();
    }

    @Override
    protected void onResume() {
        requestCurrentTime();
        super.onResume();
    }

    private void requestCurrentTime() {
        TmingHttp.asyncRequest(Constants.CURRENT_TIME_IN_MILLIS, new RequestCallback<Long>() {

            @Override
            public Long onReqestSuccess(String respones) throws Exception {
                JSONObject timeObj = new JSONObject(respones);
                long time = timeObj.getLong("data");
                return time == 0 ? System.currentTimeMillis() : time;
            }

            @Override
            public void success(Long respones) {
                currentTime = respones;
                requestHomeworkList();
            }

            @Override
            public void exception(Exception exception) {
                currentTime = System.currentTimeMillis();
                requestHomeworkList();
            }

        });
    }
    
    private void requestHomeworkList() {
        String url = Constants.HOMEWORK_URL_V2 ;
        Map<String ,Object> map = new HashMap<String,Object>();
        map.put("userid", App.getUserId());
        map.put("type", "undone");
        if (mCourseId != null) {
            map.put("course_id", mCourseId);
        }
        map.put("order","asc");
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        cacheHttp.asyncRequestWithCache(url, map, new RequestWithCacheCallBackV2<JsonParseObject<Homework>>() {

            @Override
            public JsonParseObject<Homework> parseData(String data) throws Exception {
                return getParseObjectList(data, new TypeToken<List<Homework>>(){}.getType());
            }

            @Override
            public void cacheDataRespone(JsonParseObject<Homework> data) {
                if (Constants.SUCCESS_MSG == data.getFlag()) {
                    try {
                        if (data.getResJs().getInt("code") == Constants.SUCCESS_MSG) {
                            List<Homework> cacheList = data.getResourseList();
                            if (mLoadType == LOAD_TYPE_REFRESH) {
                                adapter.clear();
                            } 
                            adapter.addItemCollection(cacheList);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    
                }
            }

            @Override
            public void requestNewDataRespone(JsonParseObject<Homework> cacheRespones,
                    JsonParseObject<Homework> newRespones) {
                if (Constants.SUCCESS_MSG == newRespones.getFlag()) {
                    List<Homework> oldData = null;
                    if (cacheRespones != null) {
                        oldData = cacheRespones.getResourseList();
                    }
                    List<Homework> newData = newRespones.getResourseList();
                    adapter.replaceItem(oldData, newData);
                } else {
                    
                }
            }

            @Override
            public void exception(Exception exception) {
                noDataTv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinishRequest() {
                mHomeworkLV.onRefreshComplete();
                adapter.notifyDataSetChanged();
                if (adapter.getCount() == 0) {
                    noDataTv.setVisibility(View.VISIBLE);
                } else {
                    noDataTv.setVisibility(View.GONE);
                }
            }
            
        });
    }
    
    @Override
    public void addListener() {
        mHomeworkLV.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mLoadType = LOAD_TYPE_REFRESH;
                pageNum = 1;
                requestHomeworkList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mLoadType = LOAD_TYPE_MORE;
                ++ pageNum ;
                requestHomeworkList();
            }
            
        });
        
        commonheaderLeftIV.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        commonHeadRightBtn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                UIHelper.showFinishedHomework(HomeworkListActivityV2.this, mCourseId);
            }
        });
        
    }
    
    /**
     * 
     * @Title: getParseObjectList
     * @Description: 将json数据转化为 List数据
     * @param @param respones json数据
     * @param @param typenew e.g TypeToken<List<Homework>>()
     *        {}.getType()
     * @param @return
     * @return JsonParseObject<E> 返回类型
     * @throws
     */
    private <E> JsonParseObject<E> getParseObjectList(String respones, Type type) {
        JsonParseObject<E> jsonParse = new JsonParseObject<E>();
        try {
            JSONObject responesJs = new JSONObject(respones);
            jsonParse.setFlag(responesJs.getInt("flag"));
            if (Constants.SUCCESS_MSG == jsonParse.getFlag()) {
                if (responesJs.isNull("res")) {
                    jsonParse.setResJs(new JSONObject());
                } else {
                    JSONObject resObj = responesJs.getJSONObject("res");
                    jsonParse.setResJs(resObj);
                    
                    int code = resObj.getInt("code");
                    if (code == Constants.SUCCESS_MSG) {
                        JSONObject dataObj = resObj.getJSONObject("data");
                        JSONArray listArr = dataObj.getJSONArray("list");
                        jsonParse.setResListJs(listArr);
                    }
                }
                
            } else {
                
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Gson gson = new GsonBuilder().create();
        List<E> entityList = gson.fromJson(jsonParse.getResListJs().toString(), type);
        jsonParse.setResourseList(entityList);
        return jsonParse;
    }
    
    private class HomeworkListAdapter extends CommonBaseAdapter<Homework>{

        public HomeworkListAdapter(Context context, List<Homework> datas) {
            super(context, datas);
        }

        public List<Homework> getDatas() {
            return datas;
        }

        @Override
        public View infateItemView(Context context) {
            ViewHolder holder = new ViewHolder();
            View convertView = LayoutInflater.from(context).inflate(R.layout.homework_list_item, null);
            holder.itemHomeworkRL = (RelativeLayout) convertView.findViewById(R.id.item_homework_rl);
            holder.homeworkNameTV = (TextView) convertView.findViewById(R.id.item_homework_name);
            holder.homeworkFromTV = (TextView) convertView.findViewById(R.id.item_homework_from);
            holder.homeworkFinishTimeTV = (TextView) convertView.findViewById(R.id.item_homework_finishtime);
            holder.homeworkIcon = (Button) convertView.findViewById(R.id.item_homework_icon);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public void setViewHolderData(BaseViewHolder arg0, final Homework homework) {
            ViewHolder holder = (ViewHolder) arg0;
            holder.homeworkNameTV.setText(homework.homework_name);
            holder.homeworkFromTV.setText(getResources().getString(R.string.from) + "《" + homework.course_name + "》");
            
            SimpleDateFormat spf = new SimpleDateFormat("yyyy年MM月dd日 HH：mm");
            String finish_time = spf.format(new Date(homework.finish_time * 1000));
            holder.homeworkFinishTimeTV.setText(finish_time);
            
            long time_lag = getTimeLag(homework.finish_time * 1000);
            
            if(time_lag >= 0 && time_lag <= 259200000) {    //距离当前日期≤3天的，标为橙色
                homework.isOutofDate = 0;
                holder.homeworkFinishTimeTV.setTextColor(getResources().getColor(R.color.orange));
            } else if(time_lag > 259200000) {   //距离当前日期＞3天的，标为绿色
                homework.isOutofDate = 0;
                holder.homeworkFinishTimeTV.setTextColor(getResources().getColor(R.color.title_green));
            } else if(time_lag < 0) {           //已过期
                homework.isOutofDate = 1;
                holder.homeworkFinishTimeTV.setTextColor(getResources().getColor(R.color.out_of_date_txt_bg));
            }
            boolean hasDone = com.nd.shapedexamproj.model.homework.Homework.checkUserHasDoHomework(homework.homework_id);
            if (hasDone) {//做了没提交
                holder.homeworkIcon.setVisibility(View.VISIBLE);
            } else {//没做过
                holder.homeworkIcon.setVisibility(View.GONE);
            }
            
            holder.itemHomeworkRL.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    UIHelper.showHomeworkDetailActivity(mContext, homework.homework_id,homework.homework_name,0);
                }
            });
        }
        
        private class ViewHolder extends BaseViewHolder{
            RelativeLayout itemHomeworkRL;
            TextView homeworkNameTV ,homeworkFromTV,homeworkFinishTimeTV;
            Button homeworkIcon;
        }
        
    }
    
    /**
     * 获取作业时间节点与当前时间的时间差（服务器时间）
     * @return
     */
    private long getTimeLag(long finishTime) {
        long timeLag = 0;  //时间差（毫秒）
        timeLag = finishTime - currentTime ; //接口中的时间单位为秒
        return timeLag;
    }
    
}
