package com.nd.shapedexamproj.view.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import com.nd.shapedexamproj.model.KnowledgeInfo;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;
;

/**
 * 
 * 菜单目录视图
 * 
 * @author Linlg
 * 
 *         Create on 2014-3-27
 */
public class MenuListView extends RelativeLayout implements OnClickListener, OnItemClickListener {
	private Context context;
	private ViewPager viewPager;// 页卡内容
	private ImageView cursorIv;// 动画图片
	private List<View> pages;// Tab页面列表
	private int offset = 0;// 动画图片偏移量
	private int currentIndex = 0;// 当前页卡编号
	private int width;// 动画图片宽度
	private View directoryView;
	private TextView knowledgeTv, listTv;
	private String videoId = "";
	private int pageNum = 1,pageSize = 30;
    
	private List<TextView> knowledgeTvs = new ArrayList<TextView>();
	private List<ImageView> imgs = new ArrayList<ImageView>();
	private List<KnowledgeInfo> knowledgeInfos = new ArrayList<KnowledgeInfo>();
	private List<ChapterInfo> listInfos = new ArrayList<ChapterInfo>();
	private String courseId = "";
	private TmingCacheHttp cacheHttp;
	private List<Integer> catePositionList = new ArrayList<Integer>();
	private int currentKnowledge = -1;
	private KnowledgeAdapter knowledgeAdapter;
    private DirectoryListAdapter mDirectoryAdapter;

	public MenuListView(Context context, List<KnowledgeInfo> knowledges, String courseId, String videoId) {
		super(context);
		this.context = context;
		this.knowledgeInfos = knowledges;
		this.courseId = courseId;
		this.videoId = videoId;
		cacheHttp = TmingCacheHttp.getInstance(context);
		requestListData();
		initBroadcastReceiver();
		initView();
		initViewPager();
	}

	/**
	 * 初始化广播
	 */
	private void initBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.tming.kd.knowledge");
		context.registerReceiver(receiver, filter);
	}

	/**
	 * 广播接收
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setKnowledgeTextColor(intent.getIntExtra("setKnowledge", -1));
		}

	};

	/**
	 * 初始化视图
	 */
	private void initView() {
		directoryView = LayoutInflater.from(context).inflate(R.layout.videoview_menu_list, this);
		knowledgeTv = (TextView) directoryView.findViewById(R.id.videoviewlist_kownledge_tv);
		listTv = (TextView) directoryView.findViewById(R.id.videoviewlist_directory_tv);
		cursorIv = (ImageView) directoryView.findViewById(R.id.videoviewlist_cursor_iv);
		viewPager = (ViewPager) directoryView.findViewById(R.id.videoviewlist_content_vp);
		findViewById(R.id.videoviewlist_kownledge_tv).setOnClickListener(this);
		findViewById(R.id.videoviewlist_directory_tv).setOnClickListener(this);
	}

	/**
	 * 初始化ViewPager
	 */
	private void initViewPager() {
		width = BitmapFactory.decodeResource(getResources(), R.drawable.line).getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		offset = (Helper.dip2px(context, 180) / 2 - width) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursorIv.setImageMatrix(matrix);
		pages = new ArrayList<View>();
		pages.add(knowledgeView());
		pages.add(directoryListView());
		// pages.add(LayoutInflater.from(context).inflate(R.layout.videoview_menu_download,
		// null));
		viewPager.setAdapter(pagerAdapterImpl);
		// knowledgeTv.setTextColor(getResources().getColor(R.color.title_green));
		viewPager.setOnPageChangeListener(new OnPageChangeListenerImpl());
	}

	/**
	 * 知识点视图
	 */
	private View knowledgeView() {
		View view = LayoutInflater.from(context).inflate(R.layout.videoview_list_knowledge, null);
		ListView lv = (ListView) view.findViewById(R.id.videoviewlistknowledge_content_lv);
		knowledgeAdapter = new KnowledgeAdapter();
		lv.setAdapter(knowledgeAdapter);
		lv.setOnItemClickListener(knowledgeAdapter);
		return view;
	}

	/**
	 * 目录视图
	 */
	private View directoryListView() {
		View view = LayoutInflater.from(context).inflate(R.layout.videoview_list_knowledge, null);
		ListView lv = (ListView) view.findViewById(R.id.videoviewlistknowledge_content_lv);
        mDirectoryAdapter = new DirectoryListAdapter();
		lv.setAdapter(mDirectoryAdapter);
        lv.setOnItemClickListener(this);
		return view;
	}

	private void requestListData() {
        Map<String ,Object> map = new HashMap<String,Object>();
        map.put("courseid", courseId);
        map.put("pageNum", pageNum);
        map.put("pageSize", pageSize);
        map.put("userid", App.getUserId());
		cacheHttp.asyncRequestWithCache(Constants.COURSEWARE_LIST, map,
				new TmingCacheHttp.RequestWithCacheCallBackV2Adapter<List<ChapterInfo>>() {

                    @Override
                    public List<ChapterInfo> parseData(String data) throws Exception {
                        return jsonParsing(data);
                    }

                    @Override
                    public void cacheDataRespone(List<ChapterInfo> data) {
                        setListData(data);
                    }

                    @Override
                    public void requestNewDataRespone(List<ChapterInfo> cacheRespones, List<ChapterInfo> newRespones) {
                        setListData(newRespones);
                    }

				});
	}
	
	private List<ChapterInfo> jsonParsing(String result){
        List<ChapterInfo> vedio_list = new ArrayList<ChapterInfo>();
        JSONObject jobj;
        int flag = 0;
        try {
            jobj = new JSONObject(result);
            flag = jobj.getInt("flag");
            if(flag != 1){
                App.dealWithFlag(flag);
                return null;
            }
            JSONArray listArr = jobj.getJSONObject("res").getJSONObject("data").getJSONArray("list");
            for(int i=0;i < listArr.length();i ++){
                ChapterInfo chapterInfo = new ChapterInfo();
                JSONObject vedio_jobj = listArr.getJSONObject(i);
                
                chapterInfo.chaterId = vedio_jobj.getString("chapter_id") ;
                chapterInfo.name = vedio_jobj.getString("name");
                if(vedio_jobj.isNull("url")) {
                    chapterInfo.url = "";//有些没有播放地址
                } else {
                    chapterInfo.url = vedio_jobj.getString("url");
                }
                
                vedio_list.add(chapterInfo);
            }
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return vedio_list;
    }
	
	private void setListData(List<ChapterInfo> infos) {
        listInfos = infos;
        mDirectoryAdapter.notifyDataSetChanged();
	}

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ChapterInfo chapterInfo = listInfos.get(position);
        CourseDetailActivity.playOnlineVideo(getContext(), chapterInfo.chaterId);
    }

    private class ChapterInfo {
		private String name;
		private String chaterId;
        private String url;
	}


	private class DirectoryListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = View.inflate(context, R.layout.videoview_listknowledge_item, null);
			ChapterInfo chapterInfo = listInfos.get(position);

			TextView tv = (TextView) convertView.findViewById(R.id.videoviewlistitem_knowledge_tv);
			tv.setText(chapterInfo.name);
			// holder.iv = (ImageView) convertView
			// .findViewById(R.id.videoviewlistitem_knowledge_iv);

            //if (chapterInfo.chaterId.equals(videoId)) {
            //    tv.setText(chapterInfo.name);
            //}

			/*if (object instanceof String) {
				tv.setText((String) object);
                if (knowl)
			} else if (object instanceof KnowledgeInfo) {
				final KnowledgeInfo knowledgeInfo = (KnowledgeInfo) object;
				tv.setText("    " + knowledgeInfo.knowledge);
				if (knowledgeInfo.videoId.equals(videoId)) {
					convertView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent it = new Intent("com.tming.kd.seekto");
							it.putExtra("seekto", knowledgeInfo.time);
							context.sendBroadcast(it);
						}
					});
				} else {
					convertView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent it = new Intent("com.tming.videoview.list.setVideo");
							it.putExtra("video_url", knowledgeInfo.videoUrl);
							it.putExtra("video_id", knowledgeInfo.videoId);
							context.sendBroadcast(it);
						}
					});
				}

			}*/

			return convertView;
		}

	}

	/**
	 * ViewPager页面切换监听
	 */
	private class OnPageChangeListenerImpl implements OnPageChangeListener {

		private int mWidth = offset * 2 + width;

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int position) {
			if (position != 0) {
				listTv.setTextColor(getResources().getColor(R.color.title_green));
				knowledgeTv.setTextColor(Color.WHITE);
			} else {
				knowledgeTv.setTextColor(getResources().getColor(R.color.title_green));
				listTv.setTextColor(Color.WHITE);
			}
			Animation animation = new TranslateAnimation(mWidth * currentIndex, mWidth * position, 0, 0);
			currentIndex = position;
			animation.setFillAfter(true);
			animation.setDuration(300);
			cursorIv.startAnimation(animation);
		}

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
	 * 知识点适配器
	 */
	private class KnowledgeAdapter extends BaseAdapter implements OnItemClickListener {

		@Override
		public int getCount() {
			return knowledgeInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.videoview_listknowledge_item, null);
				holder.tv = (TextView) convertView.findViewById(R.id.videoviewlistitem_knowledge_tv);
				holder.iv = (ImageView) convertView.findViewById(R.id.videoviewlistitem_knowledge_iv);
				knowledgeTvs.add(holder.tv);
				imgs.add(holder.iv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tv.setText(knowledgeInfos.get(position).knowledge);
			if (position == currentKnowledge) {
				holder.tv.setTextColor(getResources().getColor(R.color.title_green));
				holder.iv.setVisibility(View.VISIBLE);
			} else {
				holder.tv.setTextColor(getResources().getColor(R.color.white));
				holder.iv.setVisibility(View.GONE);
			}
			
			return convertView;

		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Log.e("seekto", "" + knowledgeInfos.get(position).time);
			Intent it = new Intent("com.tming.kd.seekto");
			it.putExtra("seekto", knowledgeInfos.get(position).time);
			context.sendBroadcast(it);
		}

	}

	/**
	 * 知识点适配器重用对象
	 */
	private class ViewHolder {
		public TextView tv;
		public ImageView iv;
	}

	/**
	 * 设置知识点播放处文字状态
	 */
	private void setKnowledgeTextColor(int position) {
//		for (int i = 0; i < knowledgeTvs.size(); i++) {
//			imgs.get(i).setVisibility(View.GONE);
//		}
//		knowledgeTvs.get(position).setTextColor(getResources().getColor(R.color.title_green));
//		imgs.get(position).setVisibility(View.VISIBLE);
		currentKnowledge = position;
		knowledgeAdapter.notifyDataSetChanged();
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.videoviewlist_kownledge_tv:
			viewPager.setCurrentItem(0);
			break;
		case R.id.videoviewlist_directory_tv:
			viewPager.setCurrentItem(1);
			break;
		}
	}

    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(receiver);
        super.onDetachedFromWindow();
    }
}
