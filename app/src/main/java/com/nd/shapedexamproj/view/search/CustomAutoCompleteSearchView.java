package com.nd.shapedexamproj.view.search;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;

/**
 * 自定义的搜索选择对话框
 * @author Xiezz
 * @creation 2014-05-26
 */
public class CustomAutoCompleteSearchView extends LinearLayout{
	private static final String TAG = "CustomAutoCompleteSearchView";
		 	
	public EditText mContentEd;
	public ListView listView;
    private ListAdapter mAdapter;

	private String mContent = "";                //编辑框内容
	private Filter mFilter;
	private onValueSelectedListener valueSelectedListener;
	private OnClickListener onClickListener; 
	private static ImageView clearIv;
	public Button goBackBtn;
	private View mCustomAutoCompleteView;        //自定义布局
	public ImageView mSearchBtnIv, mDeleteBtnIv; //类型搜索框	 
	
	//用来保持搜索类型
	private SharedPreferences mSreachTypeSp; 
	private Editor mEditor; 
	private Context mContext;
	private boolean mKeyWordsChanged=false;     //关键字是否发生变化
    public boolean mIsKeyWordVaild;             //判定关键字是否有效，用于PlaygroundSearchActivity判定

	public CustomAutoCompleteSearchView(Context context) {
		super(context);
		initView(context);
	}
	
	public CustomAutoCompleteSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}
	  	 
	/**
	 * 初始化布局
	 * */ 
	private void initView(Context context){
		this.mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		mCustomAutoCompleteView = inflater.inflate(R.layout.custom_auto_complete_search_view, null);
		mContentEd = (EditText) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_tv);
		mContentEd.setHint(R.string.playground_search_hint);
		mContentEd.setTextSize(15.0f);
 		 		
		goBackBtn = (Button) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_confirm_btn);				  
		 
		listView = (ListView) mCustomAutoCompleteView.findViewById(R.id.custom_auto_complete_listview); 
					
		mSearchBtnIv = (ImageView)mCustomAutoCompleteView.findViewById(R.id.search_btn_iv);	
		mDeleteBtnIv = (ImageView) mCustomAutoCompleteView.findViewById(R.id.del_btn_iv);
		addView(mCustomAutoCompleteView); 
		
		initData();
		addListener();
	}
	 
	/**
	 * 初始化数据
	 * */
	private void initData(){
		//获取SharedPreferences对象      
		mSreachTypeSp = mContext.getSharedPreferences(Constants.SHARE_PREFERENCES_SEARCH_TYPE_FILE_NAME, Context.MODE_PRIVATE);
		mEditor = mSreachTypeSp.edit();
	    
	    //保存上一次搜索类型
		int type = mSreachTypeSp.getInt(Constants.SHARE_PREFERENCES_SEARCH_TYPE_DATA, Constants.SEARCH_RESULT_TYPE_USER);
		setContenEdHint(type); 
	}
	
	private void addListener() {  
		mContentEd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {   
				if(mContentEd.getEditableText().toString().length() > 0){
					listView.setVisibility(View.VISIBLE);  
					if(mFilter != null){
						mFilter.filter(mContentEd.getText());
					} 	 
				}else{
					listView.setVisibility(View.GONE); 
				} 
			}
		});
		
		mDeleteBtnIv.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mContentEd.setText("");
                mDeleteBtnIv.setVisibility(View.GONE);
                
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
		
		/*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {//移到SearchAllResultActivity执行
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Object selectedItem = mAdapter.getItem(position);
				CharSequence charSequence = mFilter.convertResultToString(selectedItem);
				mContentEd.setText(charSequence);
				//设置光标显示位置
				mContentEd.setSelection(charSequence.toString().length());
				listView.setVisibility(View.GONE); 
			}
		});*/  
	}

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
		    String input = TextUtils.isEmpty(s) ? null : s.toString().trim();
		    
		    if (!TextUtils.isEmpty(input)) {
		        mDeleteBtnIv.setVisibility(View.VISIBLE);
		        goBackBtn.setText(getResources().getString(R.string.search_btn));
                mIsKeyWordVaild=true;
		    } else {
		        mDeleteBtnIv.setVisibility(View.GONE);
		        goBackBtn.setText(getResources().getString(R.string.search_cancel));
                mIsKeyWordVaild=false;
		    } 
		    mKeyWordsChanged=true;
            //触发text变化操作，用于PlaygroundSearchActivity的控制操作，暂时屏蔽
            //mContext.sendBroadcast(new Intent(PlaygroundSearchActivity.BRODCAST_SEARCHTEXT_CHANGED));
		}

		@Override
		public void afterTextChanged(Editable s) {
			if(mFilter != null){
				mFilter.filter(mContentEd.getText());
			}	
			 //时时动态的跟新适配器中的数据
			if(s.toString().length() > 0){ 
				mContent = s.toString();
			}
		}
	}
	 	 	 
	
	/**
	 * 获取编辑框的搜索内容
	 * */
	public String getSearchContent(){
		this.mContent = mContentEd.getEditableText().toString();
		return this.mContent;
	}

    public boolean ismKeyWordsChanged() {
        return mKeyWordsChanged;
    }

    public void setmKeyWordsChanged(boolean mKeyWordsChanged) {
        this.mKeyWordsChanged = mKeyWordsChanged;
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
	 * 设置编辑框默认显示字符串
	 * @param type 搜索类型
	 * */
	private void setContenEdHint(int type){
		switch (type) {
		case Constants.SEARCH_RESULT_TYPE_USER:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_hint_default));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_SPECIALTY:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_hint_default));					
			} 
			break;
		case Constants.SEARCH_RESULT_TYPE_COURSE:
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_hint_default));					
			} 
			break;
		default:  
			if(TextUtils.isEmpty(mContentEd.getEditableText().toString()) ){
				mContentEd.setHint(getResources().getString(R.string.search_hint_default));					
			} 
			break;  
		}
	}

	/**
	 * 设置默认显示字符串
	 * */
	public void setHint(String string) {
		mContentEd.setHint(string);			
	}
}
