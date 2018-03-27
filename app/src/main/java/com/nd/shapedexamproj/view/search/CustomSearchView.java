package com.nd.shapedexamproj.view.search;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;

/**
 * 自定义的搜索选择对话框(暂时没用)
 * @author Xiezz
 * @creation 2014-05-26
 */
public class CustomSearchView extends LinearLayout{
	private static final String TAG = "CustomSearchView";
		 	
	public EditText mContentEd;
	public ListView listView;
	public View tempView; //占位置用
	/**
	 * 编辑框内容
	 * */
	private String mContent = ""; 
	// ListView底部View  
//	public View clearAllRecordView; 
//  public TextView clearAllBtn; //清楚所有按钮 
	
	private ListAdapter mAdapter;
	private Filter mFilter;
	private onValueSelectedListener valueSelectedListener;
	private OnClickListener onClickListener; 
	private static ImageView clearIv;
	public Button goBackBtn;
	private View mCustomAutoCompleteView; //自定义布局
	
	private ImageView mTypeIv; //类型搜索框
	/**
	 * 搜索类型下拉框选项
	 * */
	private Button mAllTypeBtn; //全部搜索
	private Button mUserTypeBtn; //用户搜索
	private Button mSpecialtyTypeBtn; //专业搜索
	private Button mCourseTypeBtn; //课程搜索
	private Button mPubTypeBtn; //发动态
	private PopupWindow mPopWindow; 
	private View mPopView;
	
	//用来保持搜索类型
	private SharedPreferences mSreachTypeSp; 
	private Editor mEditor;
//	private int[] mWidthHeight; //屏幕宽高
	private Context mContext;
	
	public CustomSearchView(Context context) {
		super(context);
		initView(context);
	}
	
	public CustomSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	  	 
//	public CustomSearchView(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle); 
//		initView(context);
//	}

	private void initView(Context context){
		this.mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		mCustomAutoCompleteView = inflater.inflate(R.layout.custom_search_view, null);
		mContentEd = (EditText) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_tv);
		mContentEd.setHint(R.string.search_hint_default);
		mContentEd.setTextSize(16.0f);
 		
		mContentEd.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {  
				if(getSearchContent().length() < 1){
					if(mFilter != null){
						mFilter.filter(mContentEd.getText());
					} 			  
					listView.setVisibility(View.VISIBLE);
					tempView.setVisibility(View.GONE);	
				}else{
					listView.setVisibility(View.GONE); 
				} 
			}
		});
		
//		mContentEd.setOnFocusChangeListener(new OnFocusChangeListener() {
//			
//			@Override
//			public void onFocusChange(View v, boolean hasFocus) { 
//				if(hasFocus && (getSearchContent().length() < 1) ){
//					mContentEd.performClick();
//				}
//			}
//		});
		mContentEd.addTextChangedListener(new MyWatcher());
		
		goBackBtn = (Button) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_confirm_btn);
				
//		goBackBtn.setOnClickListener(new MyClickListener());
//		setGoBackLParam();
		
		clearIv = (ImageView) mCustomAutoCompleteView.findViewById(R.id.clear_iv); 
		clearIv.setOnClickListener(new MyClickListener());  
		
		tempView = (View) mCustomAutoCompleteView.findViewById(R.id.custom_temp_view);
		listView = (ListView) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_listview);
//		// 实例化listView底部布局   
//		clearAllRecordView = inflater.inflate(R.layout.clear_all_search_record, null);
//		clearAllBtn = (TextView) clearAllRecordView.findViewById(R.id.clear_all_record_tv);
//	    // 加上底部View，注意要放在setAdapter方法前   
//		listView.addFooterView(clearAllRecordView);
 	
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Object selectedItem = mAdapter.getItem(position);
				CharSequence charSequence = mFilter.convertResultToString(selectedItem);
				mContentEd.setText(charSequence);
				//设置光标显示位置
				mContentEd.setSelection(charSequence.toString().length());
				listView.setVisibility(View.GONE);
				tempView.setVisibility(View.VISIBLE);
			}
		});  
				
		mTypeIv = (ImageView)mCustomAutoCompleteView.findViewById(R.id.type_btn_iv);
		mTypeIv.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) { 
				int[] location = new int[2];
				v.getLocationInWindow(location);
				showPopAtLocation(location);
			}
		});
		 
		//获取屏幕宽高
//		mWidthHeight = getDisplayWidthHeight((Activity)context);
		mPopView = LayoutInflater.from(context).inflate(R.layout.search_type_popup_layout,null);
		mAllTypeBtn = (Button) mPopView.findViewById(R.id.all_search_type_btn); 
		mUserTypeBtn = (Button) mPopView.findViewById(R.id.user_search_type_btn);
		mSpecialtyTypeBtn = (Button) mPopView.findViewById(R.id.specialty_search_type_btn);
		mCourseTypeBtn = (Button) mPopView.findViewById(R.id.course_search_type_btn);
		mPubTypeBtn = (Button) mPopView.findViewById(R.id.pub_search_type_btn);		
		
		mAllTypeBtn.setOnClickListener(new SearchTypeOnClickListener());
		mUserTypeBtn.setOnClickListener(new SearchTypeOnClickListener());
		mSpecialtyTypeBtn.setOnClickListener(new SearchTypeOnClickListener());
		mCourseTypeBtn.setOnClickListener(new SearchTypeOnClickListener()); 
		mPubTypeBtn.setOnClickListener(new SearchTypeOnClickListener());
		
		addView(mCustomAutoCompleteView); 
		
		//获取SharedPreferences对象      
		mSreachTypeSp = context.getSharedPreferences(Constants.SHARE_PREFERENCES_SEARCH_TYPE_FILE_NAME, Context.MODE_PRIVATE);
		mEditor = mSreachTypeSp.edit();
	    
	    //保存上一次搜索类型
		int type = mSreachTypeSp.getInt(Constants.SHARE_PREFERENCES_SEARCH_TYPE_DATA, Constants.SEARCH_RESULT_TYPE_USER);
		setContenEdHint(type); 
	}
	
//	/**
//	 * 设置返回按钮的参数
//	 * */
//	private void setGoBackLParam(){
//		int width = DisplayUtil.sp2px(mContext, 76);
//		int height = DisplayUtil.sp2px(mContext, 60);
//		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//				DisplayUtil.px2dip(mContext, width),
//				DisplayUtil.px2dip(mContext, height)); // , 1是可选写的 
//		//设置相对父类的偏移量
//		lp.setMargins(5, 5, 3, 5);
//		goBackBtn.setTextSize(DisplayUtil.px2sp(mContext, 21));
//		goBackBtn.setLayoutParams(lp);
//	}
	
//	public static int[] getDisplayWidthHeight(Activity act) {
//		WindowManager wm = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
//		int height = wm.getDefaultDisplay().getHeight()- act.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
//		int width = wm.getDefaultDisplay().getWidth();
//		return new int[] { width, height };
//	} 

	public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
		mAdapter = adapter;
		listView.setAdapter(adapter);
		mFilter = adapter.getFilter();
	}

	private class MyWatcher implements TextWatcher{

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,	int after) { 
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,int count) {
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(mFilter != null){
				mFilter.filter(mContentEd.getText());
			}
			 //时时动态的跟新适配器中的数据
			if(s.toString().length() > 0){
				goBackBtn.setText(R.string.search_btn);
				listView.setVisibility(View.GONE);
				tempView.setVisibility(View.VISIBLE);
				mContent = s.toString();
			} else{
				goBackBtn.setText(R.string.search_cancel);
				listView.setVisibility(View.VISIBLE);
				tempView.setVisibility(View.GONE);
			}
		}
	}
	
	private class MyClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.custom_auto_complete_confirm_btn:
//				//确定选择
//				if(valueSelectedListener != null){
//					valueSelectedListener.onValueSelected(contengEd.getText().toString());
//				}
			 
				break;
			case R.id.clear_iv:
				//清除文本框文字
				mContentEd.setText("");
				break;
			default:
				break;
			}			
		}		
	}	 
	 	 
	
	/**
	 * 获取编辑框的搜索内容
	 * */
	public String getSearchContent(){
		return this.mContent;
	}
	
	/**
	 * 设置确定按钮点击事件的监听器
	 * @param listener
	 */
	public void setSearchOnClickListener(OnClickListener listener){
		this.onClickListener = listener;
	}
	
	/**
	 * 设置确定选择项的监听器
	 * @param listener
	 */
	public void setOnValueSelectedListener(onValueSelectedListener listener){
		this.valueSelectedListener = listener;
	}
	 	
	/**
	 * 当选项被确定选择时触发的监听器
	 * @author king
	 * @creation 2013-8-26
	 */
	public interface onValueSelectedListener{
		public void onValueSelected(String selectedValue);
	}
		
	/**
	 * 显示类型搜索对话框
	 * */
	private void showPopAtLocation(int[] position){		 
		int popWidth = mPopView.getWidth() == 0 ? 40 : mPopView.getWidth();
		Log.e("popwidth", ""+popWidth); 
		
		// 创建PopupWindow对象  
		mPopWindow = new PopupWindow(mPopView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false);   
		/**
		 * TODO 需要提过图像，优化比例
		 * */
//		mPopWindow = new PopupWindow(mPopView, 145, LayoutParams.WRAP_CONTENT, false);   
        // 需要设置一下此参数，点击外边可消失  
		mPopWindow.setBackgroundDrawable(new BitmapDrawable());  
        //设置点击窗口外边窗口消失  
		mPopWindow.setOutsideTouchable(true);  
        // 设置此参数获得焦点，否则无法点击  
		mPopWindow.setFocusable(true);
        
		mPopWindow.showAtLocation(mPopView, Gravity.CENTER|Gravity.TOP|Gravity.LEFT, 5, position[1] + 40);//在屏幕居中，无偏移        
	}
	
	/**
	 * 关闭搜索类型弹出框
	 * */
	private void closePopWindow(){
		if(mPopWindow.isShowing() && (mPopWindow != null)){
			mPopWindow.dismiss();
		}
	}
	 
	/**
	 * 搜索类型点击事件
	 * */
	private class SearchTypeOnClickListener implements OnClickListener{
		@Override
		public void onClick(View v) { 
			
			int type = Constants.SEARCH_RESULT_TYPE_USER;
			switch(v.getId()){		
			case R.id.all_search_type_btn:
				type = Constants.SEARCH_RESULT_TYPE_ALL;  
				break;	
			case R.id.user_search_type_btn:
				type = Constants.SEARCH_RESULT_TYPE_USER;  
				break;			
			case R.id.specialty_search_type_btn:
				type = Constants.SEARCH_RESULT_TYPE_SPECIALTY;  
				break;			
			case R.id.course_search_type_btn:
				type = Constants.SEARCH_RESULT_TYPE_COURSE;  
				break;
			case R.id.pub_search_type_btn:
				type = Constants.SEARCH_RESULT_TYPE_PUB;  
				break;
			default:
				type = Constants.SEARCH_RESULT_TYPE_USER; 
				break;
			}
			setContenEdHint(type);
			saveSearchTypeData(type);
			
			closePopWindow();				 		
		}		
	} 
	
	/**
	 * 保持搜索类型数据
	 * @param type 搜索类型
	 * */
	private void saveSearchTypeData(int type){
		//存入数据 			
		mEditor.putInt(Constants.SHARE_PREFERENCES_SEARCH_TYPE_DATA, type); 
		mEditor.commit();
	}
	
	/**
	 * 设置编辑框默认显示字符串
	 * @param type 搜索类型
	 * */
	private void setContenEdHint(int type){
		switch (type) {
		case Constants.SEARCH_RESULT_TYPE_USER:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_by_user));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_SPECIALTY:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_by_specialty));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_COURSE:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_by_course));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_PUB:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString())){
				mContentEd.setHint(getResources().getString(R.string.search_by_pub));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_ALL:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_by_all));					
			} 
			break;
		default:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_by_user));					
			} 
			break;
		}
	}
}
