package com.nd.shapedexamproj.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.fragment.BaseFragment;
import com.nd.shapedexamproj.view.IconPageIndicator;
import com.nd.shapedexamproj.view.IconPagerAdapter;
import com.tming.common.BaseFragmentActivity;
import com.tming.common.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName: SplashActivity
 * @Title:
 * @Description:新手引导页
 * @Author:XueWenJian
 * @Since:2014年5月15日21:01:22
 * @Version:1.0
 */
public class GuideActivity extends BaseFragmentActivity{

	private final static String TAG = "GuideActivity";
	
	private ViewPager guideVP;
	private IconPageIndicator guidePI;
	
	private Context mContext;
	private IconAdapter mAdapter;
	private List<Fragment> mListViews;
	
	private int mFlag;
	
	
	 protected void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	        setContentView(R.layout.guide_activity);
	        guideVP = (ViewPager)findViewById(R.id.guide_vp);
			guidePI = (IconPageIndicator)findViewById(R.id.guide_indicator);
	
			initData();
			addListener();
	 }

	public void initData() {
		
		Log.e(TAG, "initData");
		mContext = this;
		mFlag = getIntent().getIntExtra("flag", 0);
		mListViews = new ArrayList<Fragment>();
		mAdapter = new IconAdapter(mContext,mListViews,getSupportFragmentManager());
		
		GuideFragment fragment01 = new GuideFragment();
		fragment01.setFlagView(GuideFragment.FLAG_VIEW_01);
		mListViews.add(fragment01);
		
		GuideFragment fragment02 = new GuideFragment();
		fragment02.setFlagView(GuideFragment.FLAG_VIEW_02);
		mListViews.add(fragment02);
		
		GuideFragment fragment03 = new GuideFragment();
		fragment03.setFlagView(GuideFragment.FLAG_VIEW_03);
		mListViews.add(fragment03);
		
		GuideFragment fragment04 = new GuideFragment();
		fragment04.setFlagView(GuideFragment.FLAG_VIEW_04);
		mListViews.add(fragment04);
		
		guideVP.setAdapter(mAdapter);
		guidePI.setViewPager(guideVP);
	}
	
	public void addListener()
	{
		guidePI.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				Log.e(TAG, "onPageSelected:"+arg0);

				if(mListViews.size()-1 == arg0)
				{
					guidePI.setVisibility(View.INVISIBLE);
				}
				else
				{
					guidePI.setVisibility(View.VISIBLE);
				}
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				Log.e(TAG, "onPageScrollStateChanged:"+arg0);
			}
		});
	}


	
	class IconAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

		private List<Fragment> mListViews;
		private Context mContext;
		
		protected final int[] ICONS = new int[] {
			R.drawable.indicator_guide_selector,
        };

		public IconAdapter(Context context, List<Fragment> listViews,
				FragmentManager fragmentManager) {
			super(fragmentManager);
			mContext = context;
			mListViews = listViews;
		}

		@Override
		public int getIconResId(int index) {
			return ICONS[index % ICONS.length];
		}

		@Override
		public Fragment getItem(int arg0) {
			return (Fragment)mListViews.get(arg0);
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}
	}
	
	@SuppressLint("ValidFragment")
	class GuideFragment extends BaseFragment {
		
		private Context mContext;
		
		private LinearLayout mGuideTextLL;
		private ImageView mGuideIV;
		private TextView mGuideTitleTV;
		private ImageView mGuideLineIV;
		private TextView mGuideContentTV;
		//private LinearLayout mGuideToMainLL;
		private ImageButton mGuideToMainIMGBTN;
		
		protected final static int FLAG_VIEW_01 = 1, FLAG_VIEW_02 = 2, FLAG_VIEW_03 = 3, FLAG_VIEW_04 = 4;
		
		private int mFlagView;
		
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	    }

	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    	
	    	mContext=getActivity();
			View mView = inflater.inflate(R.layout.guide_view, null);
			mGuideTitleTV = (TextView)mView.findViewById(R.id.guide_titile_tv);
			mGuideContentTV = (TextView)mView.findViewById(R.id.guide_content_tv);
			mGuideIV = (ImageView)mView.findViewById(R.id.guide_iv);
			mGuideTextLL = (LinearLayout)mView.findViewById(R.id.guide_text_ll);
			mGuideLineIV = (ImageView)mView.findViewById(R.id.guide_line_iv);
			//mGuideToMainLL = (LinearLayout)mView.findViewById(R.id.guide_tomain_ll);
			mGuideToMainIMGBTN = (ImageButton)mView.findViewById(R.id.guide_tomain_imgbtn);

			
			if(FLAG_VIEW_01 == mFlagView)
			{
				mGuideIV.setBackgroundResource(R.drawable.new_guide_a);
				mGuideTitleTV.setText(R.string.guide01_title);
				mGuideLineIV.setBackgroundResource(R.drawable.new_guide_linea);
				mGuideContentTV.setText(R.string.guide01_content);
			}
			else if(FLAG_VIEW_02 == mFlagView)
			{
				mGuideIV.setBackgroundResource(R.drawable.new_guide_b);
				mGuideTitleTV.setText(R.string.guide02_title);
				mGuideContentTV.setText(R.string.guide02_content);
				mGuideLineIV.setBackgroundResource(R.drawable.new_guide_lineb);
				
			}
			else if(FLAG_VIEW_03 == mFlagView)
			{
				mGuideIV.setBackgroundResource(R.drawable.new_guide_c);
				mGuideTitleTV.setText(R.string.guide03_title);
				mGuideContentTV.setText(R.string.guide03_content);
				mGuideLineIV.setBackgroundResource(R.drawable.new_guide_linec);
				
			}
			else if(FLAG_VIEW_04 == mFlagView)
			{
				mGuideIV.setBackgroundResource(R.drawable.new_guide_d);
				mGuideTitleTV.setText(R.string.guide04_title);
				mGuideContentTV.setText(R.string.guide04_content);
				mGuideLineIV.setBackgroundResource(R.drawable.new_guide_lined);
				mGuideToMainIMGBTN.setVisibility(View.VISIBLE);
				
			}

			mGuideToMainIMGBTN.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					if(0 == mFlag)
					{
						//进入到主页面
						Intent intent =  new Intent(getApplication(),HomeActivity.class);
					    startActivity(intent);
						GuideActivity.this.finish();
					}
					else
					{
						//直接关闭引导页
						GuideActivity.this.finish();
					}
					
				}
			});
			
			
			return mView;
	    }
	    
	    public void setFlagView(int flagView)
	    {
	    	mFlagView = flagView;
	    }
	    
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.push_pic_right_in, R.anim.push_pic_right_out);
	}
}
