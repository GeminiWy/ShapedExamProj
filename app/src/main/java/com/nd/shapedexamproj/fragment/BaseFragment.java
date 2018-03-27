package com.nd.shapedexamproj.fragment;


import android.app.Activity;
import android.support.v4.app.Fragment;
import com.tming.common.util.Log;
import com.umeng.analytics.MobclickAgent;
/**
 * 
 * 基本功能 定义 
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-22
 */
public  class BaseFragment extends Fragment{
	
	/**
	 * 
	 * 获取Activity 对象统一该方法
	 *<p>使用getActivity() 会出现空指针错误</p>
	 *@return Activity 
	 */
	public Activity getBaseActivity(){
		return mActivity;
	}
	
	private Activity mActivity;
	
	
	@Override
	public void onAttach(Activity activity) {
		mActivity = activity;
		super.onAttach(activity);
	}
	@Override
    public void onResume() {
        super.onResume();
        Log.e("BaseFragment", getClass().getSimpleName());
        MobclickAgent.onPageStart(getClass().getSimpleName()); //友盟统计页面
    }
    
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getSimpleName()); //友盟统计页面
    }
}
