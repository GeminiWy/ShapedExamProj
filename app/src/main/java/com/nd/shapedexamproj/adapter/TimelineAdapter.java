package com.nd.shapedexamproj.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.view.TimelineView;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.net.TmingHttp.RequestCallback;

/**
 * Created by Administrator on 2014/11/27.
 */
public class TimelineAdapter extends CommonBaseAdapter<Timeline> {
    private int layoutType ;
    private RequestCallback callBack;
    
    public TimelineAdapter(Context context) {
        super(context);
    }
    
    public TimelineAdapter(Context context,RequestCallback callBack,int layoutType) {
        super(context);
        this.layoutType = layoutType;
        this.callBack = callBack;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.common_timeline_item_view, null);
        }
        TimelineView timelineView = (TimelineView) convertView;
        timelineView.setTimeline(datas.get(position),layoutType);
        timelineView.setDeleteCallBack(callBack);
        return convertView;
    }

    @Override
    public void setViewHolderData(BaseViewHolder holder, Timeline data) {}

    @Override
    public View infateItemView(Context context) {
        return null;
    }

}
