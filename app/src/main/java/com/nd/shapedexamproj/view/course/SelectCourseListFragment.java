package com.nd.shapedexamproj.view.course;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.PhpApiUtil;
import com.nd.shapedexamproj.util.SimpleAlertDialog;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;
import com.nd.shapedexamproj.view.homework.OptionsView;
import com.tming.common.adapter.CommonBaseAdapter;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingHttpRequestWithCacheTask;
import com.tming.common.util.Helper;
import com.tming.common.util.PhoneUtil;
import com.tming.common.view.RefreshableListView;
import com.tming.openuniversity.model.course.XKSelectClass;
import com.tming.openuniversity.model.course.XKSelectCourse;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/3/18.
 */
public class SelectCourseListFragment extends Fragment implements TmingCacheHttp.RequestWithCacheCallBackV2<List<XKSelectCourse>>, RefreshableListView.OnRefreshListener, View.OnClickListener {

    private SelectCourseListAdapter mAdpater;
    private int selectListType = 1;
    private int pageNo = 1;
    private View noDataView, errorView, loadingView;;
    private RefreshableListView mListView;
    private TmingHttpRequestWithCacheTask mTask;

    public static Fragment newInstance(int postion) {
        SelectCourseListFragment fragment = new SelectCourseListFragment();
        fragment.setSelectListType(postion == 0 ? 1 : 2);
        return fragment;
    }

    private void setSelectListType(int type) {
        selectListType = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_course_list_fragment, null);

        mListView = (RefreshableListView) view.findViewById(R.id.select_course_list_fragment_lv);
        mListView.setonRefreshListener(this);
        mAdpater = new SelectCourseListAdapter(getActivity());
        pageNo = 1;
        mListView.setAdapter(mAdpater);
        noDataView = view.findViewById(R.id.select_course_list_fragment_no_data_tv);
        errorView = view.findViewById(R.id.select_course_list_fragment_error_lay);
        loadingView = view.findViewById(R.id.select_course_list_fragment_loading_lay);
        view.findViewById(R.id.error_btn).setOnClickListener(this);

        requestData();
        return view;
    }

    private void requestData() {
        /*if (mTask == null) {
            mTask = TmingCacheHttp.getInstance().asyncRequestWithCache(ServerApi.XKSelectCourse.getSelectCourseListUrl(selectListType, pageNo++, 20), null, this);
        }*/
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("stu_id", App.getsStudentId());
        map.put("type",selectListType);
        map.put("page",pageNo++);
        map.put("size",20);
        PhpApiUtil.sendData(Constants.XK_COURSE_SELECT_LIST,map,this);
    }

    @Override
    public List<XKSelectCourse> parseData(String data) throws Exception {
        JSONObject jsonObject = new JSONObject(data);
        if (JsonUtil.checkXKPhpApiALLIsOK(jsonObject)) {
            return JsonUtil.paraseJsonArray(jsonObject.getJSONArray("list"), XKSelectCourse.class);
        }
        throw new RuntimeException("api error!");
    }

    @Override
    public void cacheDataRespone(List<XKSelectCourse> data) {
        if (pageNo == 2) {
            mAdpater.clear();
        }
        mAdpater.addItemCollection(data);
        mAdpater.notifyDataSetChanged();
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mListView.onRefreshComplete();
    }

    @Override
    public void requestNewDataRespone(List<XKSelectCourse> cacheRespones, List<XKSelectCourse> newRespones) {
        if (pageNo == 2) {
            mAdpater.clear();
            mAdpater.addItemCollection(newRespones);
        } else {
            mAdpater.replaceItem(cacheRespones, newRespones);
        }
        mAdpater.notifyDataSetChanged();
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mListView.onRefreshComplete();
    }

    @Override
    public void exception(Exception exception) {
        loadingView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        noDataView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFinishRequest() {
        if (mAdpater.getCount() == 0) {
            noDataView.setVisibility(View.VISIBLE);
        } else {
            noDataView.setVisibility(View.GONE);
        }
        mTask = null;
        mListView.onRefreshComplete();
    }

    @Override
    public void onRefresh() {
        pageNo = 1;
        requestData();
    }

    @Override
    public void onLoadMore() {
        requestData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_course_list_item_select_btn: {
                if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
                    Toast.makeText(getActivity(),"网络异常",Toast.LENGTH_SHORT).show();
                    return;
                }
                XKSelectCourse course = (XKSelectCourse) v.getTag();
                if (!course.isSelected()) {
                    new SelectCourseTask().execute(course);
                } else {
                    new UnSelectCourseTask().execute(course);
                }
            }
                break;
            case R.id.select_course_list_item_exam_scheme_btn: {
                XKSelectCourse course = (XKSelectCourse) v.getTag();
                new SchemeDialog(getActivity(), course).show();
            }
            break;
            case R.id.error_btn:
                pageNo = 1;
                requestData();
                break;
        }
    }

    private void showSelectClassDialog(final XKSelectCourse course, final List<XKSelectClass> classes) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new XKSelecClassDialog(getActivity(), course, classes).show();
            }
        });
    }

    private void showAlertDialog(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new XKAlertDialog(getActivity(), msg).show();
            }
        });
    }

    private class UnSelectCourseTask extends AsyncTask<Object, Void, Boolean> {

        private XKSelectCourse course;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingView.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            course.setSelected(!aBoolean);
            if (aBoolean) {
                mAdpater.notifyDataSetChanged();
            } else {
                Helper.ToastUtil(getActivity(), "取消选课失败，请重试");
            }
            loadingView.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            course = (XKSelectCourse) params[0];
            try {
                Map<String,Object> map = new HashMap<String, Object>();
                map.put("stu_id",App.getsStudentId());
                map.put("km_id",course.getId());

                JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_COURSE_CANCEL, map);
                return JsonUtil.checkXKPhpApiALLIsOK(jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private class SelectCourseTask extends AsyncTask<Object, Void, Boolean> {

        private XKSelectCourse course = null;
        private boolean showSelectClassDialog = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingView.setVisibility(View.VISIBLE);
        }

        private XKSelectClass getCouseClass(XKSelectCourse course) throws TmingHttpException, JSONException {
            //TmingResponse response = TmingHttp.tmingHttpRequest(ServerApi.XKSelectCourse.getCouseClassUrl(course.getId()), null);

            Map<String,Object> map = new HashMap<String, Object>();
            map.put("stu_id",App.getsStudentId());
            map.put("km_id",course.getId());
            JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_COURSE_GETCLASS,map);

            if (JsonUtil.checkXKPhpApiALLIsOK(jsonObject)) {
                List<XKSelectClass> classes = JsonUtil.paraseJsonArray(jsonObject.getJSONArray("list"), XKSelectClass.class);
                if (classes != null && classes.size() > 0) {
                    if (classes.size() == 1) {
                        // 只有一个班级，直接进入选课
                        return classes.get(0);
                    } else {
                        showSelectClassDialog(course, classes);
                    showSelectClassDialog = true;
                    }
                } else {
                    showAlertDialog("该课程没有可选择的班级!");
                }
            }
            return null;
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                course = (XKSelectCourse) params[0];
                XKSelectClass courseCls;
                if (params.length > 1) {
                    courseCls = (XKSelectClass) params[1];
                } else {
                    courseCls = getCouseClass(course);
                }
                if (courseCls != null) {
                    Map<String,Object> map = new HashMap<String, Object>();
                    map.put("stu_id",App.getsStudentId());
                    map.put("km_id",course.getId());
                    map.put("cls_id",courseCls.getId());

                    JSONObject jsonObject = PhpApiUtil.getJSONObject(Constants.XK_COURSE_SELECT,map);
                    return JsonUtil.checkXKPhpApiALLIsOK(jsonObject);
                }
            } catch (TmingHttpException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            course.setSelected(aBoolean);
            if (aBoolean) {
                mAdpater.notifyDataSetChanged();
            } else if (!showSelectClassDialog) {// 如果不是因为需要选班级失败
                Helper.ToastUtil(getActivity(), "选课失败，请重试");
            }
            loadingView.setVisibility(View.GONE);
        }
    }

    private class XKSelecClassDialog extends SimpleAlertDialog implements OptionsView.OnChoiceChangeListener {

        private List<XKSelectClass> classes;
        private XKSelectCourse course;
        private XKSelectClass selectedCls;

        public XKSelecClassDialog(Context context, XKSelectCourse course, List<XKSelectClass> classes) {
            super(context);
            this.classes = classes;
            this.course = course;
            setTitle("选择班级");
            ViewGroup parent = (ViewGroup) contentTextView.getParent();
            parent.removeView(contentTextView);
            ClassOptionsView classOptionsView = new ClassOptionsView(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            parent.addView(classOptionsView, 1, params);

            List<String> classNames = new ArrayList<String>();
            for (XKSelectClass xkSelectClass : classes) {
                classNames.add(xkSelectClass.getName());
            }

            classOptionsView.setShowChoiceData(classNames);
            classOptionsView.setChoiceListener(this);
        }


        @Override
        public void onPositiveButtonClick() {
            if (selectedCls != null) {
                new SelectCourseTask().execute(course, selectedCls);
            } else {
                Helper.ToastUtil(getActivity(), "请选择班级");
            }
        }

        @Override
        public void onOptionsViewChoiceChange(OptionsView view, String choice, int index) {
            selectedCls = classes.get(index);
        }
    }

    private class XKAlertDialog extends SimpleAlertDialog {

        public XKAlertDialog(Context context, String msg) {
            super(context);
            setTitle(msg);
            negativeButton.setVisibility(View.GONE);
        }

        @Override
        public void onPositiveButtonClick() {

        }
    }

    private class SchemeDialog extends SimpleAlertDialog {

        public SchemeDialog(Context context, XKSelectCourse course) {
            super(context);
            setTitle("形考方案");

            ScrollView scrollView = new ScrollView(context);

            AutoImageLoadTextView textView = new AutoImageLoadTextView(context);
            int paddingV = Helper.dip2px(context, 15);
            textView.setPadding(paddingV, Helper.dip2px(context, 5), paddingV, Helper.dip2px(context, 10));
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            //textView.setLineSpacing(Helper.dip2px(context, 3), 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            textView.setTextColor(0xFF757675);
            //textView.setMaxHeight(Helper.dip2px(context, 300));
            ViewGroup parent = (ViewGroup) contentTextView.getParent();
            parent.removeView(contentTextView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Helper.dip2px(context, 200));
            scrollView.addView(textView);
            parent.addView(scrollView, 1, params);

            contentTextView = textView;

            textView.setText(course.getScheme());
            negativeButton.setVisibility(View.GONE);
        }

        @Override
        public void onPositiveButtonClick() {
        }
    }


    private class SelectCourseListAdapter extends CommonBaseAdapter<XKSelectCourse> {

        public SelectCourseListAdapter(Context context) {
            super(context);
        }

        @Override
        public void setViewHolderData(BaseViewHolder holder, XKSelectCourse data) {
            ViewHolder viewHolder = (ViewHolder) holder;
            if (data.getCover() != null && data.getCover().length() > 0) {
                ImageCacheTool.getInstance().asyncLoadImage(viewHolder.imageView, data.getCover(), R.drawable.image_loading);
            }
            viewHolder.examSchemeBtn.setTag(data);
            viewHolder.selectImgBtn.setTag(data);
            viewHolder.selectImgBtn.setSelected(data.isSelected());
            viewHolder.nameTv.setText(data.getName());
            viewHolder.studyDayTv.setText(String.format("最短学习天数:%d\n建议学习天数:%d", data.getShortestDays(), data.getSuggestDays()));
        }

        @Override
        public View infateItemView(Context context) {
            View view = View.inflate(context, R.layout.select_course_list_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(R.id.select_course_list_item_iv);
            viewHolder.nameTv = (TextView) view.findViewById(R.id.select_course_list_item_name_tv);
            viewHolder.studyDayTv = (TextView) view.findViewById(R.id.select_course_list_item_study_time_tv);
            viewHolder.selectImgBtn = (ImageButton) view.findViewById(R.id.select_course_list_item_select_btn);
            viewHolder.examSchemeBtn = (Button) view.findViewById(R.id.select_course_list_item_exam_scheme_btn);
            viewHolder.examSchemeBtn.setOnClickListener(SelectCourseListFragment.this);
            viewHolder.selectImgBtn.setOnClickListener(SelectCourseListFragment.this);
            view.setTag(viewHolder);
            return view;
        }

        private class ViewHolder extends CommonBaseAdapter.BaseViewHolder {
            TextView nameTv, studyDayTv;
            ImageView imageView;
            ImageButton selectImgBtn;
            Button examSchemeBtn;
        }
    }


}
