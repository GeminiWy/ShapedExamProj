package com.nd.shapedexamproj.view.xkhomework;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.shapedexam.DoingTaskActivity;
import com.nd.shapedexamproj.activity.shapedexam.DoingWarmupActivity;
import com.nd.shapedexamproj.model.xkhomework.XKHomework;
import com.nd.shapedexamproj.model.xkhomework.XKSubject;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import java.util.List;

/**
 * 做作业进度
 * Created by yusongying on 2015/1/19.
 */
public class XKDoingProgressFragment extends Fragment implements View.OnClickListener{

    private TextView mTitleTv;
    private ListView mListView;
    private XKHomework mHomework;
    private ProgressAdapter mAdapter = new ProgressAdapter();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doing_homework_progress_fragment, null);
        View headView = inflater.inflate(R.layout.doing_homework_progress_list_head, null);
        mTitleTv = (TextView) headView.findViewById(R.id.doing_homework_progress_list_head_title_tv);
        mListView = (ListView) view.findViewById(R.id.doing_homework_progress_frame_content_lv);
        mListView.addHeaderView(headView);
        View emptyFootView = new View(getActivity());
        emptyFootView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, Helper.dip2px(getActivity(), 48)));
        mListView.addFooterView(emptyFootView);

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        if (mHomework != null) {
            mTitleTv.setText(mHomework.getDoingProfile());
        }

        return view;
    }

    public void setSubjects(XKHomework homework) {
        mHomework = homework;

        if (mTitleTv != null) {
            mTitleTv.setText(mHomework.getDoingProfile());
            mAdapter.notifyDataSetChanged();
        }
    }

    private static final int LINE_MAX_COUNT = 5;

    @Override
    public void onClick(View v) {
        XKSubject subject = (XKSubject) v.getTag();
        if (subject != null) {
            FragmentActivity fragmentActivity = getActivity();
            if (fragmentActivity instanceof DoingTaskActivity) {
                DoingTaskActivity activity = (DoingTaskActivity) getActivity();
                activity.redoSubject(subject);
            } else if (fragmentActivity instanceof DoingWarmupActivity) {
                DoingWarmupActivity activity = (DoingWarmupActivity) getActivity();
                activity.redoSubject(subject);
            }

        }
    }

    private class ProgressAdapter extends BaseAdapter {

        private int mCount = -1;

        @Override
        public int getCount() {
            if (mCount != -1) {
                return mCount;
            }
            if (mHomework != null && mHomework.getSubjectSize() > 0) {
                int count = 0;
                List<Integer> subjectTypes = mHomework.getSubjectTypes();
                for (Integer type : subjectTypes) {
                    int subjectTypeCount = mHomework.getSubjectSizeInType(type);
                    count += subjectTypeCount / LINE_MAX_COUNT;
                    if (subjectTypeCount % LINE_MAX_COUNT != 0) {
                        count++;
                    }
                }
                mCount = count;
            } else {
                mCount = 0;
            }
            return mCount;
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
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.doing_homework_progress_list_item, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.titleTv = (TextView) convertView.findViewById(R.id.doing_homework_progress_list_item_title_tv);
                viewHolder.subTitleTv = (TextView) convertView.findViewById(R.id.doing_homework_progress_list_item_sub_title_tv);
                ViewGroup subjectViewGroup = (ViewGroup) convertView.findViewById(R.id.doing_homework_progress_list_item_subject_lay);
                viewHolder.subjectTvs = new TextView[subjectViewGroup.getChildCount()];
                for (int i = 0; i < viewHolder.subjectTvs.length; i++) {
                    viewHolder.subjectTvs[i] = (TextView) subjectViewGroup.getChildAt(i);
                    viewHolder.subjectTvs[i].setOnClickListener(XKDoingProgressFragment.this);
                }
                convertView.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            int firstSubjectIndex = getLineFirstSubjectIndex(position);
            XKSubject firstSubject = mHomework.getSubjectAt(firstSubjectIndex);
            int pageIndex = mHomework.getPageIndexOf(firstSubject);
            XKSubject pageSub = mHomework.getSubjectAtPageIndex(pageIndex);

            int firstSubjectType = firstSubject.getType();
            if (pageSub != firstSubject) {
                firstSubjectType = XKSubject.SUBJECT_TYPE_COMPLEX;
            }

            // 标题显示
            if (checkNeedSowTitle(firstSubjectIndex, firstSubjectType)) {
                viewHolder.titleTv.setVisibility(View.VISIBLE);
                viewHolder.titleTv.setText(mHomework.getSubjectTitle(pageIndex));
            } else {
                viewHolder.titleTv.setVisibility(View.GONE);

            }
            // 副标题
            showSubTitle(viewHolder, firstSubjectIndex);
            for (int i = 0; i < LINE_MAX_COUNT; i++) {
                if (i + firstSubjectIndex < mHomework.getSubjectSize()) {
                    if (checkCanShowContinue(firstSubjectIndex + i, position)) {
                        XKSubject subject = mHomework.getSubjectAt(firstSubjectIndex + i);
                        viewHolder.subjectTvs[i].setVisibility(View.VISIBLE);
                        viewHolder.subjectTvs[i].setText(String.valueOf(subject.getTheSubjectTypeIndex()));
                        viewHolder.subjectTvs[i].setSelected(subject.hasDone());
                        viewHolder.subjectTvs[i].setTag(subject);
                        continue;
                    }
                }
                viewHolder.subjectTvs[i].setVisibility(View.INVISIBLE);
            }
            return convertView;
        }

        private void showSubTitle(ViewHolder viewHolder, int firstSubjectIndex) {
            int firstSubjectType = getSubjectTypeAtIndex(firstSubjectIndex);
            if (firstSubjectType / 100 == XKSubject.SUBJECT_TYPE_COMPLEX) {
                int lastType = getSubjectTypeAtIndex(firstSubjectIndex - 1);
                if (lastType != firstSubjectType) {
                    viewHolder.subTitleTv.setVisibility(View.VISIBLE);
                    viewHolder.subTitleTv.setText("题" + Utils.numberToChinese(firstSubjectType % 100));
                    return;
                }
            }
            viewHolder.subTitleTv.setVisibility(View.GONE);
        }

        /**
         * 检查是否需要显示标题
         * @param firstSubjectIndex
         * @param firstSubjectType
         * @return
         */
        private boolean checkNeedSowTitle(int firstSubjectIndex, int firstSubjectType) {
            if (firstSubjectIndex == 0) {
                return true;
            }
            // 综合题类型需要特殊处理
            if (firstSubjectType == XKSubject.SUBJECT_TYPE_COMPLEX) {
                firstSubjectType = getSubjectTypeAtIndex(firstSubjectIndex);
                if (firstSubjectType % 100 > 1) { // 只有综合题第一题需要显示大标题
                    return false;
                }
            }
            int lastSubjectType = getSubjectTypeAtIndex(firstSubjectIndex - 1);
            return lastSubjectType != firstSubjectType;
        }

        /**
         *  判断是否能继续显示在本行，如果类型相同就显示
         *  如果是相同题型或者是综合题就继续显示
         * @param postion
         * @return
         */
        private boolean checkCanShowContinue(int index, int postion) {
            int lastLineNumber = getLineNumberAtIndex(index);
            return lastLineNumber == postion;
        }

        private int getSubjectTypeAtIndex(int index) {
            List<Integer> subjectTypes = mHomework.getSubjectTypes();
            int tempIndex = 0;
            for (Integer type : subjectTypes) {
                tempIndex += mHomework.getSubjectSizeInType(type);
                if (tempIndex > index) {
                    return type;
                }
            }
            return -1;
        }

        private int getLineNumberAtIndex(int index) {
            List<Integer> subjectTypes = mHomework.getSubjectTypes();
            int lineNumber = 0;
            int tempIndex = 0;
            for (Integer type : subjectTypes) {
                int typeCount = mHomework.getSubjectSizeInType(type);
                while (typeCount > 0) {
                    if (typeCount >= LINE_MAX_COUNT) {
                        tempIndex += LINE_MAX_COUNT;
                        typeCount -= LINE_MAX_COUNT;
                    } else {
                        tempIndex += typeCount;
                        typeCount = 0;
                    }
                    if (tempIndex - 1>= index) {
                        return lineNumber;
                    }
                    lineNumber++;
                }
            }
            return -1;
        }

        /**
         * 获取一行的第一个元素在列表中的索引
         * @param position
         * @return
         */
        private int getLineFirstSubjectIndex(int position) {
            if (position == 6 || position == 5) {
                Log.d("test", "");
            }
            int positionTemp = 0;
            int firstSubjectIndex = 0;
            List<Integer> subjectTypes = mHomework.getSubjectTypes();
            for (Integer type : subjectTypes) {
                int typeCount = mHomework.getSubjectSizeInType(type);
                while (typeCount > 0) {
                    if (positionTemp >= position) {
                        return firstSubjectIndex;
                    }
                    if (typeCount > LINE_MAX_COUNT) {
                        firstSubjectIndex += LINE_MAX_COUNT;
                        typeCount -= LINE_MAX_COUNT;
                    } else {
                        firstSubjectIndex += typeCount;
                        typeCount = 0;
                    }
                    positionTemp++;
                }
            }
            return 0;
        }
    }

    private class ViewHolder {
        TextView titleTv;
        TextView subTitleTv;
        TextView[] subjectTvs;
    }

}
