package com.nd.shapedexamproj.model;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 统一管理 在修课程
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-6-18
 * 修改者，修改日期，修改内容。
 */
public class CoursingManager {
	private static final String TAG = CoursingManager.class.getSimpleName();
	/**
	 * 线程安全
	 */
	private List<Coursing> mLst = new Vector<Coursing>();
	
	private static CoursingManager mInstance;
	public static CoursingManager getInstance(){
		if (mInstance == null)
			mInstance = new CoursingManager();
		return mInstance;
	}
	
	private CoursingManager(){
		if (mLst.size() == 0)
			loadData();
	}
	
	public void loadData(){
		requestCoursingData();
	}
	
	
    public void clear(){
    	mLst.clear();
    }
    
    public int size(){
    	return mLst.size();
    }
	
	/**
	 * 
	 * 该课程是否在修
	 * @param c
	 * @return
	 */
	public boolean isExist(Coursing c){
		return mLst.contains(c);
	}
	/**
	 * 该课程是否在修
	 * @param courseid
	 * @return
	 */
	public boolean isExist(String courseid){
		if (courseid  == null)
			return false;
		
		for (Coursing c: mLst){
	        if (c.getCourse_id().equals(courseid))
	        	return true;
		}
		
		return false;
	}
	
	
	
	/*
	 * 异步加载数据
	 */
	private void requestCoursingData() {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", App.getUserId());
		params.put("pageNum", 1);
		params.put("pageSize", 200);
		TmingHttp.asyncRequest(Constants.HOST + "student/courses.html", params, new TmingHttp.RequestCallback<Coursing.Result>() {

			@Override
			public Coursing.Result onReqestSuccess(String respones) throws Exception {
				return Coursing.parseJson(respones);
			}

			@Override
			public void success(Coursing.Result respones) {
				if (respones.flag == 1 && respones.res != null) {
					if (respones.res.data == null || respones.res.data.list == null) {
						return;
					}
					List<Coursing> list = respones.res.data.list;
					if (list == null)
						return;
					Log.e(TAG, "list : " + list.size());
					for (Coursing c : list) {
						if (!mLst.contains(c))
							mLst.add(c);
					}
				}
			}

			@Override
			public void exception(Exception exception) {
				UIHelper.ToastMessage(App.getAppContext(), "网络异常");

			}
		});
	}
}
