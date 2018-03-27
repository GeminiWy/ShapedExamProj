/**
 * 
 */
package com.nd.shapedexamproj.fragment;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.TeacherClasses;
import com.nd.shapedexamproj.model.TeacherCourse;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Helper;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.Mode;
import com.tming.common.view.support.pulltorefresh.PullToRefreshBase.OnRefreshListener2;
import com.tming.common.view.support.pulltorefresh.PullToRefreshScrollView;

import java.util.HashMap;

/**
 * 老师权限 课堂是可见
 * 
 * @version 1.0.0
 * @author Abay Zhuang <br/>
 *         Create at 2014-5-21
 */
public class TeacherCourseFragment extends BaseFlowFragment {
	private static final String TAG = TeacherCourseFragment.class
			.getSimpleName();
	private ListView mCourseLst;
	private ListView mClassesLst;
	private TeacherCourse mTeacherCourse;
	private TeacherClasses mTeacherClasses;
	private TeacherCourseAdapter mTeacherAdapter;
	private TeacherClassesAdapter mTeacherClassesAdapter;
	private LinearLayout mDetailLl;
	private LinearLayout mEmptyLl;
	private LinearLayout mCourseLl;
	private LinearLayout mClassesLl;
	private PullToRefreshScrollView timelineScrollView;// 上拉加载更多 下拉刷新
	private int pageNum = 1;
	private int pageSize = 50;
	
	public void initData() {

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("userid", App.getUserId());
		map.put("pageNum", pageNum);
		map.put("pageSize", pageSize);
		TmingHttp.asyncRequest(Constants.TEACHER_CLASSES, map,
				new TmingHttp.RequestCallback<TeacherClasses>() {

					@Override
					public TeacherClasses onReqestSuccess(String respones)
							throws Exception {
						TeacherClasses.Result result = TeacherClasses
								.getObject(respones);
						if (result.flag == 1)
							return result.res.data;
						return null;
					}

					@Override
					public void success(TeacherClasses respones) {
						if (respones != null) {
							Log.e(TAG, "success " + respones.getTotal());
							mTeacherClasses = respones;
							mTeacherClassesAdapter
									.setTeacherClasses(mTeacherClasses);
							App.sTeacherClassesNum = respones.getTotal();
						}
						updateView();
					}

					@Override
					public void exception(Exception exception) {
						Helper.ToastUtil(getBaseActivity(), getResources().getString(R.string.net_error));
						updateView();
					}
				});

		TmingHttp.asyncRequest(Constants.TEACHER_COURSES, map,
				new TmingHttp.RequestCallback<TeacherCourse>() {

					@Override
					public TeacherCourse onReqestSuccess(String respones)
							throws Exception {
						TeacherCourse.Result result = TeacherCourse
								.getObject(respones);
						if (result.flag == 1)
							return result.res.data;
						else
							return null;

					}

					@Override
					public void success(TeacherCourse respones) {
						if (respones != null) {
							Log.e(TAG, "success " + respones.getTotal());
							mTeacherCourse = respones;
							mTeacherAdapter.setTeacherCourse(mTeacherCourse);
						}
						updateView();
					}

					@Override
					public void exception(Exception exception) {
						Helper.ToastUtil(getBaseActivity(), "网络错误");
						updateView();
					}
				});

	}

	/**
	 * 
	 * 更新显示内容，有些内容不显示
	 */
	public void updateView() {
	    // 停止刷新图标
        timelineScrollView.onRefreshComplete();
		if ((mTeacherClasses == null || mTeacherClasses.getTotal() == 0)
				&& (mTeacherCourse == null || mTeacherCourse.getTotal() == 0)) {
			mEmptyLl.setVisibility(View.VISIBLE);
			mDetailLl.setVisibility(View.GONE);
			return;
		}
		mEmptyLl.setVisibility(View.GONE);
		mDetailLl.setVisibility(View.VISIBLE);
		if (mTeacherClasses != null && mTeacherClasses.getTotal() > 0) {
			setListViewHeightBasedOnChildren(mClassesLst);
			mClassesLl.setVisibility(View.VISIBLE);
		} else {
			mClassesLl.setVisibility(View.GONE);
		}

		if (mTeacherCourse != null && mTeacherCourse.getTotal() > 0) {
			mCourseLl.setVisibility(View.VISIBLE);
//			// 动态显示 ，最多显示4个item
//			if (mTeacherClasses != null && mTeacherClasses.getTotal() > 0) {
//
//				int totalHeight = getBaseActivity().getResources()
//						.getDimensionPixelSize(
//								R.dimen.teacher_course_listview_item_height) * 4;
//				int maxHeight = totalHeight
//						+ (mCourseLst.getDividerHeight() * 3);
//				ViewGroup.LayoutParams params = mCourseLst.getLayoutParams();
//				int lstHeight = params.height;
//
//				params.height = lstHeight > maxHeight ? maxHeight : lstHeight;
//				mCourseLl.setLayoutParams(params);
//			}
			setListViewHeightBasedOnChildren(mCourseLst);
		} else {
			mCourseLl.setVisibility(View.GONE);
		}

	}

	public void initComponent(View v) {

		// 获取下拉刷新，上拉加载控件
		timelineScrollView = (PullToRefreshScrollView) v.findViewById(R.id.teacher_scroll_view);
		timelineScrollView.setMode(Mode.PULL_FROM_START);
		
//		mContentRl = (RelativeLayout) LayoutInflater.from(getBaseActivity())
//				.inflate(R.layout.course_4_teacher_content, null);
		mDetailLl = (LinearLayout) v.findViewById(R.id.teacher_course_detail_ll);
		mEmptyLl = (LinearLayout) v.findViewById(R.id.teacher_empty_ll);
		mCourseLl = (LinearLayout) v.findViewById(R.id.teacher_course_ll);
		mClassesLl = (LinearLayout) v.findViewById(R.id.teacher_classes_ll);

		mCourseLst = (ListView) v.findViewById(R.id.teacher_course_lstv);
		mClassesLst = (ListView) v.findViewById(R.id.teacher_classes_lstv);

		mTeacherAdapter = new TeacherCourseAdapter(getBaseActivity(),
				mTeacherCourse);
		mCourseLst.setAdapter(mTeacherAdapter);

		mTeacherClassesAdapter = new TeacherClassesAdapter(getBaseActivity(),
				mTeacherClasses);
		mClassesLst.setAdapter(mTeacherClassesAdapter);

	}

	public void initListener() {
		mCourseLst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position > mTeacherAdapter.getList().size() || position < 0)
					return;
				TeacherCourse.CourseInfo info = mTeacherAdapter.getList().get(position);
				UIHelper.showCourseDetail(getBaseActivity(),
						info.getCourse_id(), info.getName());
			}
		});

		mClassesLst.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position > mTeacherClassesAdapter.getList().size()
						|| position < 0)
					return;
				TeacherClasses.ClassInfo cInfo = mTeacherClasses.getList().get(position);
				// UIHelper.showContact(getBaseActivity());
				UIHelper.showClassContactActivity(getBaseActivity(),
						cInfo.getCls_id());
			}
		});

		timelineScrollView.setOnRefreshListener(new OnRefreshListener2<ScrollView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                initData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                
            }
		    
		});
	}

	@Override
	public int initResource() {
		return R.layout.course_4_teacher;
	}

	/**
	 * @param listView
	 */
	private void setListViewHeightBasedOnChildren(ListView listView) {

		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
	}

}
