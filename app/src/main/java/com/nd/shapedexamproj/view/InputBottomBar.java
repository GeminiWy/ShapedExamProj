package com.nd.shapedexamproj.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.fragment.ChatFacesFragment;
import com.nd.shapedexamproj.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * @ClassName: InputBottomBar
 * @Title:
 * @Description:输入框底部工具（表情）
 * @Author:XueWenJian
 * @Since:2014年5月22日16:37:12
 * @Version:1.0
 */
public class InputBottomBar {
	private final static String TAG = "InputBottomBar";
	public static final int PAGE_SIZE = 21;
	/** 表情图片匹配 */
	private static Pattern facePattern = Pattern.compile("\\[{1}(\u5fae\u7b11|\u6487\u5634|\u8272|\u53d1\u5446|\u5f97\u610f|\u6d41\u6cea|\u5bb3\u7f9e|\u95ed\u5634|\u7761|\u5927\u54ed|\u5c34\u5c2c|\u53d1\u6012|\u8c03\u76ae|\u5472\u7259|\u60ca\u8bb6|\u96be\u8fc7|\u9177|\u51b7\u6c57|\u6293\u72c2|\u5410|\u5077\u7b11|\u53ef\u7231|\u767d\u773c|\u50b2\u6162|\u9965\u997f|\u56f0|\u60ca\u6050|\u6d41\u6c57|\u61a8\u7b11|\u5927\u5175|\u594b\u6597|\u5492\u9a82|\u7591\u95ee|\u5618|\u6655|\u8870|\u6572\u6253|\u518d\u89c1|\u64e6\u6c57|\u9f13\u638c|\u7cd7\u5927\u4e86|\u574f\u7b11|\u5de6\u54fc\u54fc|\u53f3\u54fc\u54fc|\u54c8\u6b20|\u9119\u89c6|\u59d4\u5c48|\u5feb\u54ed\u4e86|\u9634\u9669|\u4eb2\u4eb2|\u5413|\u53ef\u601c|\u9AB7\u9AC5)\\]{1}");
	public static String[] faceCodeArray = {"微笑","撇嘴","色","发呆","得意","流泪","害羞","闭嘴","睡","大哭","尴尬","发怒","调皮","呲牙","惊讶","难过","酷","冷汗","抓狂","吐","删除",
	    "偷笑","可爱","白眼","傲慢","饥饿","困","惊恐","流汗","憨笑","大兵","奋斗","咒骂","疑问","嘘","晕","衰","敲打","再见","擦汗","鼓掌","删除",
	    "糗大了","坏笑","左哼哼","右哼哼","哈欠","鄙视","委屈","快哭了","阴险","亲亲","吓","可怜","骷髅","删除"};
	// 定义整型数组 即图片源
    public static int[] mImageIds = new int[] {
        R.drawable.smiley_stand_wx, R.drawable.smiley_stand_pz, R.drawable.smiley_stand_s, R.drawable.smiley_stand_fd,
        R.drawable.smiley_stand_dy, R.drawable.smiley_stand_ll, R.drawable.smiley_stand_hx, R.drawable.smiley_stand_bz,
        R.drawable.smiley_stand_shui, R.drawable.smiley_stand_dk, R.drawable.smiley_stand_gg, R.drawable.smiley_stand_fn,
        R.drawable.smiley_stand_tp, R.drawable.smiley_stand_cy, R.drawable.smiley_stand_jy, R.drawable.smiley_stand_ng,
        R.drawable.smiley_stand_kuk, R.drawable.smiley_stand_lengh, R.drawable.smiley_stand_zk, R.drawable.smiley_stand_tu,R.drawable.smily_delete_icon,
        
        R.drawable.smiley_stand_tx, R.drawable.smiley_stand_ka, R.drawable.smiley_stand_by, R.drawable.smiley_stand_am,
        R.drawable.smiley_stand_jie, R.drawable.smiley_stand_kun, R.drawable.smiley_stand_jk, R.drawable.smiley_stand_lh,
        R.drawable.smiley_stand_hunx, R.drawable.smiley_stand_db, R.drawable.smiley_stand_fendou, R.drawable.smiley_stand_zhm,
        R.drawable.smiley_stand_yiw,R.drawable.smiley_stand_xu, R.drawable.smiley_stand_yun, R.drawable.smiley_stand_shuai,
        R.drawable.smiley_stand_qiao,R.drawable.smiley_stand_zj, R.drawable.smiley_stand_ch, R.drawable.smiley_stand_gz,R.drawable.smily_delete_icon,
        
        R.drawable.smiley_stand_qd,R.drawable.smiley_stand_huaix,R.drawable.smiley_stand_zhh, R.drawable.smiley_stand_yhh, 
        R.drawable.smiley_stand_hq, R.drawable.smiley_stand_bs,R.drawable.smiley_stand_wq, R.drawable.smiley_stand_kk,
        R.drawable.smiley_stand_yx,R.drawable.smiley_stand_qq, R.drawable.smiley_stand_xia, R.drawable.smiley_stand_kel, 
        R.drawable.smiley_stand_zhd,R.drawable.smily_delete_icon};
	
	public static int faceCodeSize;
	protected Context mContext;
	
	protected View mParentView;
	
	protected ViewPager viewPager;
	protected RelativeLayout tweetPubViewpagerRl;
	protected FragmentManager fragmentManager;
    private List<Fragment> fragmentList;
    private List<ImageView> dotsList;
    protected ChatFacesPagerAdapter pagerAdapter;
    protected PageChangeListener pageChangeListener;
	
	private EditText mContentET;
	protected InputMethodManager mInputMethodManager;
	private Holder mHolder;
	private RelativeLayout mBottombarLL;
	protected LinearLayout smilyPageLL;
//	private GridView mFaceGridView;
	protected GridViewFaceAdapter mGVFaceAdapter;
	protected int mMaxLength = 140;
	/**
	 * ture 表示已经显示了表情，false表示显示的是输入法，在edittext点击的时候，判断ture，就把表情隐藏 
	 * */
	
	public InputBottomBar(Context context,FragmentManager fragmentManager, View parentView,EditText contentET,int maxLength)
	{
		mContext = context;
		this.fragmentManager = fragmentManager;
		mParentView = parentView;
		mContentET = contentET;
		mMaxLength = maxLength;
		initView();
		addListener();
	}
	/**
	 * 带输入框的布局时使用
	 * @param context
	 * @param fragmentManager
	 * @param parentView
	 * @param maxLength
	 */
	public InputBottomBar(Context context,FragmentManager fragmentManager, View parentView, int maxLength)
    {
        mContext = context;
        this.fragmentManager = fragmentManager;
        mParentView = parentView;
        mContentET = (EditText) mParentView.findViewById(R.id.chat_text_et);
        mMaxLength = maxLength;
        initView();
        addListener();
    }
	
	public void initView()
	{ 
	    // 软键盘管理类
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(mContext.INPUT_METHOD_SERVICE);    
		mHolder = new Holder();
		mBottombarLL = (RelativeLayout) mParentView.findViewById(R.id.common_bottombar);
		smilyPageLL = (LinearLayout) mParentView.findViewById(R.id.smily_page_ll);
		viewPager = (ViewPager) mParentView.findViewById(R.id.tweet_pub_viewpager);
		tweetPubViewpagerRl = (RelativeLayout) mParentView.findViewById(R.id.tweet_pub_viewpager_rl);
		mHolder.mFace = (ImageView) mParentView.findViewById(R.id.tweet_pub_footbar_face);		
		
		fragmentList = new ArrayList<Fragment>();
		pagerAdapter = new ChatFacesPagerAdapter(fragmentManager, fragmentList);
		viewPager.setAdapter(pagerAdapter);
		pageChangeListener = new PageChangeListener();
		viewPager.setOnPageChangeListener(pageChangeListener);
		/*initGridView(); */  
		initViewPager();
		initCircleImages();
	}
	
	public class Holder{
		public ImageView mFace;
	}
	
	private void initViewPager() {
	    int size = mImageIds.length;
        if(size > 0){
            int temp = mImageIds.length / PAGE_SIZE;
            int rest = mImageIds.length % PAGE_SIZE;
            int page = temp;
            if (rest > 0) {
                page = temp + 1 ;
            }
            for (int i = 0; i < page; i ++) {
                ChatFacesFragment fragment = new ChatFacesFragment(mContentET, mMaxLength);
                fragment.setFacesImgList(mImageIds);
                fragment.setCurrentPageNum(i);
                fragmentList.add(fragment);
            }
            pagerAdapter.notifyDataSetChanged();
        }
	}
	/**
	 * <p>初始化表情下的页码指示符</P>
	 */
	private void initCircleImages(){
	    if (smilyPageLL != null) {
	        smilyPageLL.removeAllViews();
	    }
	    dotsList = new ArrayList<ImageView>();
	    for (int i = 0;i < fragmentList.size();i ++) {
	        ImageView imageView = new ImageView(mContext);
	        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	        if (i == 0) {
	            imageView.setImageResource(R.drawable.smily_page_on);
	        } else {
	            imageView.setImageResource(R.drawable.smily_page);
	        }
	        
	        dotsList.add(imageView);
	        smilyPageLL.addView(imageView, params);
	    }
	}
	
	public void addListener()
	{ 
		mHolder.mFace.setOnClickListener(faceClickListener);
		
		String errContentMaxLength = mContext.getResources().getString(R.string.coach_content_max_length);
        errContentMaxLength = String.format(errContentMaxLength, mMaxLength);
        Utils.addEditViewMaxLengthListener(mContext, mContentET, mMaxLength, errContentMaxLength);
	}
	
	private class ChatFacesPagerAdapter extends FragmentStatePagerAdapter  {//不使用FragmentPagerAdapter
        
        private List<Fragment> fragmentList;
        
        
        public ChatFacesPagerAdapter(FragmentManager fm, List<Fragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
            /*notifyDataSetChanged();*/
        }


        @Override
        public Fragment getItem(int arg0) {
            return fragmentList.get(arg0);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
        
        /**
         * viewpager中解决彻底性动态删除Fragment，需返回PagerAdapter.POSITION_NONE
         */
        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }        
    }
    
    private class PageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            
        }

        @Override
        public void onPageSelected(int position) {
            int size = dotsList.size();
            if (size > 0) {
                // 其他页面 (上一个页面 灰色)
                for (ImageView dotImage : dotsList) {
                    dotImage.setImageResource(R.drawable.smily_page);
                }
                // 当前页面
                dotsList.get(position % size).setImageResource(
                        R.drawable.smily_page_on);
            }
        }
    }
	
	
	private View.OnClickListener faceClickListener = new View.OnClickListener() {
		public void onClick(View v) { 
			showOrHideIMM();
		}
	};
	
	private void showOrHideIMM() { 
		if (mHolder.mFace.getTag() == null) { 
			// 隐藏软键盘
			mInputMethodManager.hideSoftInputFromWindow(mContentET.getWindowToken(), 0);   
			// 显示表情
			showFace();
		} else { 
			// 显示软键盘
			mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			// 隐藏表情
			hideFace();
		}
	}
	
	private void showFace() { 
		mHolder.mFace.setImageResource(R.drawable.widget_bar_keyboard);
		mHolder.mFace.setTag(1);
		App.getAppHandler().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                tweetPubViewpagerRl.setVisibility(View.VISIBLE);
            }
        }, 150);
		
	}
	
	public void hideFace() { 
		mHolder.mFace.setImageResource(R.drawable.widget_bar_face);
		mHolder.mFace.setTag(null);
		tweetPubViewpagerRl.setVisibility(View.GONE);
	}
	/**
	 * <p>隐藏键盘和表情</P>
	 */
	public void hideFaceAndKeyboard() {
	    if (hasShowFace()) {
	        hideFace();
	    }
	    mInputMethodManager.hideSoftInputFromWindow(mContentET.getWindowToken(), 0);
	}
	
	/**
	 * 判断是否显示表情，true表示显示，false表示没有。
	 * */
	public boolean hasShowFace(){
		return mHolder.mFace.getTag() == null ? false : true;
	}
	/**
	 * 设置可见
	 * @param visibility
	 */
	public void setVisiable(final int visibility)
	{
		mBottombarLL.setVisibility(visibility);
	}
	
	/**
	 * 返回输入管理者
	 */
	public InputMethodManager getInputMethodManager()
	{
		return mInputMethodManager;
	}
	/**
	 * 半角转为全角
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
	    char[] c = input.toCharArray();
	    for (int i = 0; i< c.length; i++) {
	        if (c[i] == 12288) {
	          c[i] = (char) 32;
	          continue;
	        }if (c[i]> 65280&& c[i]< 65375)
	           c[i] = (char) (c[i] - 65248);
	        }
	    return new String(c);
	 }
	
	/**
	 * 将[微笑]之类的字符串替换为表情
	 * 
	 * @param context
	 * @param content
	 * @param textSize 设置表情图片的显示大小
	 * @return
	 */
	public static SpannableStringBuilder parseFaceByText(Context context,
			String content, int textSize) {
	    content = ToDBC(content);
		SpannableStringBuilder builder = new SpannableStringBuilder(content);
		Matcher matcher = facePattern.matcher(content);
		while (matcher.find()) {
			// 使用正则表达式找出其中的表情字符
			int position = getPosition(matcher.group(1));
			int resId = 0;
			try {
				resId = mImageIds[position];
				Drawable d = context.getResources().getDrawable(resId);
				int divider = textSize/4;//水平间距
				int verticalSpac = textSize/6;//竖直间距
				d.setBounds(verticalSpac, divider, textSize + verticalSpac, textSize + divider);// 设置表情图片的显示大小
				
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
				builder.setSpan(span, matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
		return builder;
	}
	
	/**
	 * 将[微笑]之类的字符串替换为表情
	 * 
	 * @param context
	 * @param builder
	 * @return
	 */
	public static SpannableStringBuilder parseFaceByText(Context context,
			SpannableStringBuilder builder) {
		Matcher matcher = facePattern.matcher(builder);
		while (matcher.find()) {
			
			// 使用正则表达式找出其中的表情字符
			int position = getPosition(matcher.group(1));
			int resId = 0;
			try {
				resId = mImageIds[position];
				Drawable d = context.getResources().getDrawable(resId);
				d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				builder.setSpan(span, matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder;
	}
	
	/**
     * 将[微笑]之类的字符串替换为表情
     * 
     * @param context
     * @param content
     * @param textSize 设置表情图片的显示大小
     * @return
     */
    public static SpannableStringBuilder parseFaceByText(Context context,
            String content, int textSize, int textColor) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Matcher matcher = facePattern.matcher(content);
        while (matcher.find()) {
            // 使用正则表达式找出其中的表情字符
            int position = getPosition(matcher.group(1));
            int resId = 0;
            try {
                resId = mImageIds[position];
                Drawable d = context.getResources().getDrawable(resId);
                int divider = textSize/6;//水平间距
                int verticalSpac = textSize/6;//竖直间距
                d.setBounds(verticalSpac, divider, textSize + verticalSpac, textSize + divider);// 设置表情图片的显示大小
                
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
                builder.setSpan(span, matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int start = content.indexOf("@");
        int end = content.indexOf(":") == -1 ? 0 : content.indexOf(":");
        if (start == 0 && end != 0) {
            builder.setSpan(new ForegroundColorSpan(textColor), start + 1, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        return builder;
    }
	
	public Holder getmHolder() {
		return mHolder;
	}

	public void setmHolder(Holder mHolder) {
		this.mHolder = mHolder;
	}
	
	/**
	 * 获取表情位置
	 * @param faceCode
	 * @return
	 */
	public static int getPosition(String faceCode){
		int position = 0;
		faceCodeSize = faceCodeArray.length;
		for (int i = 0; i < faceCodeSize; i++) {
			if(faceCodeArray[i].equals(faceCode)){
				position = i;
				break;
			}
		}
		return position;
	}
	
	/**
	 * 获取表情字符
	 * @param position 
	 * @param currentPageNum 当前第几页，从0开始
	 * @return
	 */
	public static String getFaceCode(int position,int currentPageNum){
		String faceCode = faceCodeArray[0];
		if((position + 1) > PAGE_SIZE){
			position = 0;
		} else {
			
		}
		faceCode = faceCodeArray[position + currentPageNum * PAGE_SIZE];
		return faceCode;
	}
}
