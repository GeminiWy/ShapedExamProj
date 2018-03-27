package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nd.shapedexamproj.model.homework.Subject;
import com.nd.shapedexamproj.util.Utils;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

import java.lang.ref.WeakReference;

/**
 * 题目显示视图抽象对象
 * Created by yusongying on 2015/1/19.
 */
public abstract class SubjectView extends LinearLayout {

    protected Subject subject;
    protected AutoImageLoadTextView titleView;
    protected WeakReference<INextable> nextWeakReference;

    public SubjectView(Context context) {
        super(context);
    }

    public SubjectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        onInit();
    }

    /**
     * 初始化界面相关
     */
    protected abstract void onInit();

    /**
     * 当前显示题目类型
     *
     * @return
     */
    public abstract int getShowSubjectType();

    /**
     * 设置显示的题目
     * @param subject
     */
    public void setShowSubject(Subject subject) {
        this.subject = subject;
        StringBuffer stringBuffer = new StringBuffer(subject.getTheSubjectTypeIndex() + "."
                + subject.getSubjectName());
        if (subject.getScore() >= 0) {
            stringBuffer.append(" <font color=\"#888888\">(" + Utils.scoreFloat2String(subject.getScore()) + "分)</font>");
        }
        titleView.setText(stringBuffer.toString());
    }

    /**
     * 按照综合题题目格式显示题目
     *
     */
    public void showCompexSubjectType() {
        StringBuffer stringBuffer = new StringBuffer(subject.getTheSubjectTypeIndex()
                + ").【" + Subject.getTypeName(subject.getType()) + "】"
                + subject.getSubjectName());
        if (subject.getScore() >= 0) {
            stringBuffer.append(" <font color=\"#888888\">(" + Utils.scoreFloat2String(subject.getScore()) + "分)</font>");
        }
        titleView.setText(stringBuffer.toString());
    }

    /**
     * 设置是否可编辑
     *
     * @param editable
     */
    public abstract void setEditable(boolean editable);

    /**
     * 下一题接口
     * @param nextable
     */
    public void setShowNext(INextable nextable) {
        nextWeakReference = new WeakReference<INextable>(nextable);
    }

    /**
     * 获取显示的题目
     * @return
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * 把滚动视图移除
     * 在综合题中，最外层已经存在滚动条,如果不移除，输入框WrapContent无效，无法自动适应内容大小
     */
    public void removeScrollView() {
        ScrollView scrollView = (ScrollView) getChildAt(0);
        View view = scrollView.getChildAt(0);
        scrollView.removeView(view);
        removeView(scrollView);

        addView(view);
    }
}
