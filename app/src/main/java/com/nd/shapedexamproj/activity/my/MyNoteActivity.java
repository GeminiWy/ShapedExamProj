package com.nd.shapedexamproj.activity.my;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.Course;
import com.nd.shapedexamproj.model.note.MyNote;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshExpandableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 我的笔记
 * @author zll
 *
 */
public class MyNoteActivity extends BaseActivity implements OnTouchListener{
	
	private static final String TAG ="MyNoteActivity";
	private static final int TYPE_REFRESH = 0, TYPE_LOAD_MORE = 1;
	private Button common_head_right_btn, errorBtn ;
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private PullToRefreshExpandableListView pelv;
	private ExpandableListView expand_ppt_list ;
	private RelativeLayout mynoteitem_rl ;
	private RelativeLayout errorLayout;
	
	private List<Course> parentList = new ArrayList<Course>();	    //课程名称列表
	private List<List<MyNote>> childList = new ArrayList<List<MyNote>>();	//课件列表
//	private List<List<MyNote>> tmpList = new ArrayList<List<MyNote>>();	//课件列表
	private MyNoteAdapter adapter ;
	
	private PopupWindow pop ;
	private View pop_layout;
	private Button btnShowNote,btnPlayVideo,btnDelete ;
	
	private TmingCacheHttp cacheHttp ;
	private int screen_height;	//屏幕高度
//	private View child_view ;
	
	private int pageNum = 1, pageSize = 10;
	private boolean isRefreshing = false ;
	@Override
	public int initResource() {
		return R.layout.my_note_activity;
	}

	@Override
	public void initData() {
		cacheHttp = TmingCacheHttp.getInstance();
		
		adapter = new MyNoteAdapter(this);
		expand_ppt_list.setAdapter(adapter);
		requestMyNoteList(TYPE_REFRESH);
		
	}
	
//	/**
//	 * 查找本地数据库
//	 */
//	private void searchLocalCourseware(){
//		parentList = new ArrayList<String>();
//		childList = new ArrayList<List<DownloadInfo>>();
//		
//		List<DownloadInfo> video_list = operator.getDownloads();	//获取所有数据
//		List<Course> course_list = new LinkedList<Course>();
//		
//		Set<Integer> course_id_set = new TreeSet<Integer>();	//获取所有课程id（不包含重复的数据）
//		Set<String> course_name_set = new TreeSet<String>();	//获取所有课程名称（不包含重复的数据）
//		
//		for(int i =0;i<video_list.size();i ++){
//			DownloadInfo video = video_list.get(i);
//			course_id_set.add(video.coursecateid);
//			course_name_set.add(video.coursecatename);
//		}
//		
//		Log.e("size", "本地课程=" + course_id_set.size());
//		Iterator<String> sIterator = course_name_set.iterator();
//		while (sIterator.hasNext()) {
//			String courseName =  sIterator.next();
//			parentList.add(courseName);
//		}
//		
//		Iterator<Integer> itr = course_id_set.iterator();
//		while(itr.hasNext()){								//
//			int courseId = itr.next();
//			
//			List<DownloadInfo>  child_list = operator.getListByCourseId(courseId);
//			
//			childList.add(child_list);
//		}
//		
//	}

	
	private void requestMyNoteList(final int type){

		TmingHttp.asyncRequest(ServerApi.getTextNoteList(pageNum, pageSize), null, new TmingHttp.RequestCallback<Note2>() {

			@Override
			public Note2 onReqestSuccess(String respones) throws Exception {
				return Note2.itemJSONParsing(respones);
			}

			@Override
			public void success(Note2 respones) {
				if (TYPE_LOAD_MORE == type) {
					addMyNoteView(respones);
				} else {
					parentList.clear();
					childList.clear();
					setMyNoteView(respones);
				}
			}

			@Override
			public void exception(Exception exception) {
				errorLayout.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void addMyNoteView(Note2 note2){
		isRefreshing = false;
		pelv.onRefreshComplete();
		
		
		List<List<MyNote>> notes = note2.childList;//网络数据
		if (notes.size() > 0) {
		    for (int i = 0;i < notes.size();i ++) {
		        List<MyNote> newNote = notes.get(i);
	            if (newNote.size() > 0) {
	                MyNote myNote = newNote.get(0);//网络数据
	                
	                int size = childList.size();
	                for(int index=0; index<size ; index++) {
	                    List<MyNote> myNotesAdded = childList.get(index);
	                    if (myNotesAdded.size() > 0) {
	                        MyNote myNoteAdded = myNotesAdded.get(0);
	                        if (myNoteAdded.courseId.equals(myNote.courseId)) {//往相应的子列表中添加新的列表
	                            myNotesAdded.addAll(newNote);
	                            break;
	                        }
	                    }
	                }
	                
	            }
		    }
			
		} 

		adapter.notifyDataSetChanged();
	}
	
	
	private void setMyNoteView(Note2 note2){
		isRefreshing = false;
		pelv.onRefreshComplete();
		//
		parentList.addAll(note2.parentList);
		
		//子
		List<List<MyNote>> notes = note2.childList;
		if (notes == null) {
			return ;
		}
		if (notes.size() == 0 && adapter.isEmpty()) {
			mynoteitem_rl.setVisibility(View.VISIBLE);
		} else {
			mynoteitem_rl.setVisibility(View.GONE);
		}
		/*parentList = MyNote.types;*/
		if (!isEmpty(notes)) {
			childList = notes;
		}
		adapter.notifyDataSetChanged();
	}
	
	private boolean isEmpty (List<List<MyNote>> notes ) {
		if (notes.size() == 0) {
			return true ;
		}
		List<String> tempList = new ArrayList<String>();
		for (int i = 0;i < notes.size();i ++) {
			List<MyNote> noteList = notes.get(i);
			if (noteList.size() != 0) {
				tempList.add("1");
			}
		}
		if (tempList.size() == 0) {
			return true ;
		} else {
			return false ;
		}
	}
	
	@Override
	public void initComponent() {
		screen_height = getWindowManager().getDefaultDisplay().getHeight();
		Log.e("高度", "屏幕" + screen_height);
		
		//头部按钮
//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText("我的笔记");
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		commonheader_title_tv.setText("我的笔记");
		
		findViewById(R.id.common_head_right_btn).setVisibility(View.GONE);
		//列表
		 
		pelv = (PullToRefreshExpandableListView) findViewById(R.id.mynote_list);
		expand_ppt_list = pelv.getRefreshableView();
		mynoteitem_rl = (RelativeLayout) findViewById(R.id.mynoteitem_rl);
		errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
		errorBtn = (Button) findViewById(R.id.error_btn);
//		pelv.setMode(Mode.PULL_FROM_START);
		// 引入弹出窗配置文件  
		pop_layout = LayoutInflater.from(this).inflate(R.layout.popup_layout,null);
		btnShowNote = (Button) pop_layout.findViewById(R.id.btn_play);
		btnShowNote.setText("查看笔记");
		btnPlayVideo = (Button) pop_layout.findViewById(R.id.btn_delete);
		btnPlayVideo.setText("播放视频");
		btnPlayVideo.setVisibility(View.GONE);
		btnDelete = (Button) pop_layout.findViewById(R.id.btn_share);
		btnDelete.setText("删除笔记");
        
	}
	/**
	 * 
	 * @param direction	popwindow的方向
	 */
	private void createPopView (int direction) {
		if (direction == Constants.POP_NORMAL) {
			pop_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.popmenu1));
		} else {
			pop_layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.popmenu_reverse));
		}
		// 创建PopupWindow对象  
        pop = new PopupWindow(pop_layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);  
        // 需要设置一下此参数，点击外边可消失  
        pop.setBackgroundDrawable(new BitmapDrawable());  
        //设置点击窗口外边窗口消失  
        pop.setOutsideTouchable(true);  
        // 设置此参数获得焦点，否则无法点击  
        pop.setFocusable(true);
	}
	
	/**
	 * 设置弹出框的显示位置
	 * @param position
	 */
	private void showPopAtLocation(final MyNote note,ChildHolder holder,int[] position){
		
		int child_height = holder.my_note_txt_img.getHeight();
		
		int pop_height = Helper.dip2px(this, Constants.POP_ITEM_HEIGHT * 2); //pop的高度
		
        int y_position = position[1] + child_height;		//弹出框在屏幕上的位置
        if(y_position + pop_height >= screen_height){
        	y_position = position[1] - pop_height ;
        	createPopView(Constants.POP_REVERSE);
        } else {
        	createPopView(Constants.POP_NORMAL);
        }
		
        pop.showAtLocation(holder.my_note_txt_img, Gravity.TOP | Gravity.RIGHT, Helper.dip2px(this, 10), y_position);
        
        btnShowNote.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pop.dismiss();
				showNoteDetail(note);
			}
		});
        btnPlayVideo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pop.dismiss();
//				deleteLocalCourseware(video.url);	
				
			}
		});
		btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.e("pop", "---点击删除---");
				pop.dismiss();		//TODO 删除
				TmingHttp.asyncRequest(ServerApi.getDeleteNoteUrl(note.noteId, "0"), new HashMap<String, Object>(), new RequestCallback<String>() {

					@Override
					public String onReqestSuccess(String respones)
							throws Exception {
						return null;
					}

					@Override
					public void success(String respones) {
						requestMyNoteList(TYPE_REFRESH);
					}

					@Override
					public void exception(Exception exception) {
						
					}
					
				});
				
			}
		});
	}
	/**
	 * 删除本地课件
	 * @param
	 */
//	private void deleteLocalCourseware(String url){
//		operator.deleteByUrl(url);
//		
//	}
	
	
	private void showNoteDetail(MyNote note) {
		Intent it = new Intent(MyNoteActivity.this, NoteDetailActivity.class);
		it.putExtra("note", (Serializable)note);
		MyNoteActivity.this.startActivity(it);
	}
	
	/**
	 * 隐藏弹出框
	 */
	private void hidePopLayout(){
		pop_layout.setVisibility(View.GONE);
		
	}
	
	@Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		/*common_head_right_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LocalCoursewareActivity.this,ImportCoursewareActivity.class);
				startActivity(intent);
			}
		});*/

		pelv.setOnRefreshListener(new OnRefreshListener2<ExpandableListView>() {
			@Override
			public void onPullUpToRefresh(
					PullToRefreshBase<ExpandableListView> refreshView) {
				if (!isRefreshing) {
					isRefreshing = true ;
					++ pageNum ;
					requestMyNoteList(TYPE_LOAD_MORE);
				} else {
					pelv.onRefreshComplete();
				}
			}

			@Override
			public void onPullDownToRefresh(
					PullToRefreshBase<ExpandableListView> refreshView) {
				if (!isRefreshing) {
					isRefreshing = true ;
					pageNum = 1;
					requestMyNoteList(TYPE_REFRESH);
				} else {
					pelv.onRefreshComplete();
				}
				
			}
		});

		errorBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pageNum = 1;
				requestMyNoteList(TYPE_REFRESH);
			}
		});
	}

	/**
	 * 课程项点击监听
	 */
	class MyTitleClickListener implements OnClickListener{
		
		private int position;
		private boolean isExpanded;
		private ParentHolder holder ;
		
		private Drawable up_drawable = getResources().getDrawable(R.drawable.list_arrowright5);
		private Drawable down_drawable = getResources().getDrawable(R.drawable.list_arrowright4);
		
		public MyTitleClickListener(int position,boolean isExpanded,ParentHolder holder){
			this.position = position;
			this.isExpanded = isExpanded;
			this.holder = holder ;
		}
		
		@Override
		public void onClick(View v) {
			if(pop_layout.isShown()){
				hidePopLayout();
			}
			Log.e("click", "==hide==");
			
			if(isExpanded){
				holder.course_img.setImageDrawable(down_drawable);
				expand_ppt_list.collapseGroup(position);
			} else {
				holder.course_img.setImageDrawable(up_drawable);
				expand_ppt_list.expandGroup(position);
			}
		}
	}

	/**
	 * 子项点击能弹出对话框的监听
	 */
	class MyChildMoreClickListener implements OnClickListener{
		
		private ChildHolder holder ;
		private MyNote note ;
		
		public MyChildMoreClickListener(MyNote note,ChildHolder holder){
			this.note = note;
			this.holder = holder ;
		}
		
		@Override
		public void onClick(View v) {
			int[] location = new int[2];
			holder.my_note_txt_img.getLocationInWindow(location);
			if(!pop_layout.isShown()){
				showPopAtLocation(note,holder,location);
			} else {
				hidePopLayout();
			}
		}
		
	}
	
	/**
	 * 课件列表适配器
	 */
	private class MyNoteAdapter extends BaseExpandableListAdapter{
		private Context context;
		private LayoutInflater inflater ;
		
		public MyNoteAdapter(Context context){
			this.context = context;
			inflater = LayoutInflater.from(context);
		}
		
		@Override
		public int getGroupCount() {
			return parentList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return childList.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return parentList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(final int groupPosition, final boolean isExpanded,
				View convertView, ViewGroup parent) {
			ParentHolder holder = null;
			if(convertView == null){
				holder = new ParentHolder();
				//列表标题项
				convertView = inflater.inflate(R.layout.local_ppt_item_title, null);
				holder.course_item = (RelativeLayout) convertView.findViewById(R.id.local_ppt_item_title);
				holder.course_name_tv = (TextView) convertView.findViewById(R.id.local_ppt_item_tv);
				holder.course_img = (ImageView) convertView.findViewById(R.id.local_ppt_item_img);
				
				convertView.setTag(holder);
			} else {
				holder = (ParentHolder) convertView.getTag();
			}
			holder.course_name_tv.setText(parentList.get(groupPosition).course_name);
			holder.course_item.setOnClickListener(new MyTitleClickListener( groupPosition,isExpanded,holder)); 
			
			return convertView;
		}
		
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			ChildHolder holder ;
			if(convertView == null){
				holder = new ChildHolder();
				//列表项子项
				convertView = inflater.inflate(R.layout.my_note_txt_item,null);
				holder.my_note_txt_title_tv = (TextView) convertView.findViewById(R.id.my_note_txt_title_tv);
				holder.my_note_txt_video_tv = (TextView) convertView.findViewById(R.id.my_note_txt_video_tv);
				holder.my_note_txt_time_tv = (TextView) convertView.findViewById(R.id.my_note_txt_time_tv);
				holder.my_note_txt_img = (ImageView) convertView.findViewById(R.id.my_note_txt_img);
				holder.my_note_txt_item = (RelativeLayout) convertView.findViewById(R.id.my_note_txt_item);
				
				convertView.setTag(holder);
			} else {
				holder = (ChildHolder) convertView.getTag();
			}
			final MyNote note = childList.get(groupPosition).get(childPosition);
			
			holder.my_note_txt_title_tv.setText(note.noteContent);
			holder.my_note_txt_video_tv.setText(note.coursewareName);
			holder.my_note_txt_time_tv.setText("(" + note.videoPositionTime + ")");
			//点击弹出提示框
			holder.my_note_txt_img.setOnClickListener(new MyChildMoreClickListener(note, holder));
			//点击播放视频
			holder.my_note_txt_item.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					showNoteDetail(note);
				}
			});
			
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
	
	private class ChildHolder{
		private TextView my_note_txt_title_tv, my_note_txt_video_tv, my_note_txt_time_tv;
		private ImageView my_note_txt_img;
		private RelativeLayout my_note_txt_item;
	}
	
	private class ParentHolder{
		private RelativeLayout course_item;
		private TextView course_name_tv ;
		private ImageView course_img;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		/*if(pop_layout.isShown()){
			hidePopLayout();
		}*/
		return false;
	}
	
	private static class Note2 {
	    
	    private List<Course> parentList = new ArrayList<Course>(); //课程名称列表
	    private List<List<MyNote>> childList = new ArrayList<List<MyNote>>();   //课件列表
	    
	    private static Note2 itemJSONParsing(String result) {
	        Note2 note2 = new Note2();
	        try {
	            
	            JSONObject dataObj = new JSONObject(result).getJSONObject("res").getJSONObject("data");
	            if (!dataObj.isNull("list")) {
	                JSONArray list = dataObj.getJSONArray("list");
	                for (int i = 0; i < list.length(); i++) {
	                    
	                    JSONObject listObj = list.getJSONObject(i);
	                    
	                    String courseId = listObj.getString("course_id");
	                    
	                    if (!listObj.isNull("notes_list")) {
	                        Course course = new Course();
	                        course.course_id = courseId;
	                        course.course_name = listObj.getString("course_name");
	                        
	                        
	                        note2.parentList.add(course);     //只显示有笔记的课程
	                        
	                        JSONArray array = listObj.getJSONArray("notes_list");
	                        List<MyNote> notes = new ArrayList<MyNote>();
	                        for (int j = 0; j < array.length(); j++) {
	                            MyNote note = new MyNote();
	                            note.courseName = listObj.getString("course_name");
	                            note.coursewareName = array.getJSONObject(j).getString("courseware_name");
	                            note.courseId = courseId ;
	                            long playTime = array.getJSONObject(j).getLong("lecture_video_time");//接口单位为秒
	                            note.videoPositionTime = StringUtils.generateTime(playTime * 1000 );
	                            Log.e("playTime", note.videoPositionTime);
	                            
	                            note.noteContent = array.getJSONObject(j).getString("lecture_note");
	                            note.chapterId = array.getJSONObject(j).getString("lecture_chapter_id");
	                            note.noteId = array.getJSONObject(j).getString("lecture_note_id");
	                            note.noteAddTime = array.getJSONObject(j).getString("lecture_note_addtime");
	                            
	                            if (!notes.contains(note)) {
	                                notes.add(note);
	                            }
	                        }
	                        note2.childList.add(notes);
	                        
	                    }
	                }
	            }
	        } catch (JSONException e) {
	            e.printStackTrace();
	        }
	        return note2;
	    }
	    
	    
	}
}
