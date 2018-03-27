package com.nd.shapedexamproj.model.homework;

import android.content.Context;

import com.nd.shapedexamproj.view.homework.SubjectView;
import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 作业/考试 题目抽象对象
 * Created by yusongying on 2015/1/19.
 */
public abstract class Subject implements IJsonInitable, Comparable<Subject> {

    /**
     * 单选
     */
    public static final int SUBJECT_TYPE_SINGLE_CHOICE = 1;

    /**
     * 多选
     */
    public static final int SUBJECT_TYPE_MULTI_CHOICE = 2;

    /**
     * 判断
     */
    public static final int SUBJECT_TYPE_JUDGMENT = 3;

    /**
     * 填空
     */
    public static final int SUBJECT_TYPE_COMPLETION = 4;

    /**
     * 简答
     */
    public static final int SUBJECT_TYPE_SHORT_ANSWER = 5;

    /**
     * 综合
     */
    public static final int SUBJECT_TYPE_COMPLEX = 6;

    /**
     * 该题型的索引
     */
    protected int theSubjectTypeIndex;

    /**
     * 题目ID
     */
    protected String subjectId;

    /**
     * 题目
     */
    protected String subjectName;

    /**
     * 题目分数
     */
    protected float score = -1;

    /**
     * 获取题目的分数
     *
     * @return
     */
    public float getScore() {
        return score;
    }

    /**
     * 题目类型
     *
     * @return
     */
    public abstract int getType();

    /**
     * 获取题目类型名称
     * @return
     */
    public static String getTypeName(int type) {
        switch (type) {
            case SUBJECT_TYPE_SINGLE_CHOICE:
                return "单选题";
            case SUBJECT_TYPE_MULTI_CHOICE:
                return "多选题";
            case SUBJECT_TYPE_COMPLETION:
                return "填空题";
            case SUBJECT_TYPE_JUDGMENT:
                return "判断题";
            case SUBJECT_TYPE_SHORT_ANSWER:
                return "简答题";
            case Subject.SUBJECT_TYPE_COMPLEX:
                return "套题";
            default:
                return "";
        }
    }

    /**
     * 判断是否已经做完
     * @return
     */
    public abstract boolean hasDone();

    /**
     * 是否为客观题
     *
     * @return
     */
    public boolean isObjective() {
        switch (getType()) {
            case SUBJECT_TYPE_SINGLE_CHOICE:
            case SUBJECT_TYPE_MULTI_CHOICE:
            case SUBJECT_TYPE_JUDGMENT:
                return true;
        }
        return false;
    }

    /**
     * 判断客观题是否正确
     *
     * @return
     */
    public abstract boolean isRight();

    /**
     * 当前类型题目的索引
     *
     * @return
     */
    public int getTheSubjectTypeIndex() {
        return theSubjectTypeIndex;
    }

    /**
     * 设置当前类型题目的索引
     *
     * @return
     */
    public void setTheSubjectTypeIndex(int index) {
        theSubjectTypeIndex = index;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    /**
     * 获取用户答题数据，用于保存和提交用户答题数据
     *
     * @return
     */
    public abstract String getUserAnswerData();

    /**
     * 设置用户作答数据
     */
    public abstract void setUserAnswerData(String data);

    /**
     * 创建展示该题目的视图
     * @return
     */
    public abstract SubjectView createShowView(Context context);

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        subjectId = jsonObj.getString("question_id");
        subjectName = jsonObj.getString("work_name");
        if (jsonObj.has("sub_score")) {
            score = Float.parseFloat(jsonObj.getString("sub_score"));
        }
    }

    /**
     * 获取题目数据，用于保存题目
     *
     * @return
     */
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("work_name", subjectName);
        jsonObject.put("question_id", subjectId);
        jsonObject.put("sub_score", String.valueOf(score));
        return jsonObject;
    }

    /**
     * 根据服务端题目类型，实例化对象
     * @param workType
     * @return
     */
    public static Subject newSubjectWithWorkType(String workType) {
        if ("0".equals(workType)) {
            return new SingleChoiceSubject();
        } else if ("2".equals(workType)) {
            return new JudgmentSubject();
        } else if ("1".equals(workType)) {
            return new MultiChoiceSubject();
        } else if ("4".equals(workType)) {
            return new CompletionSubject();
        } else if ("5".equals(workType)) {
            return new ShortAnswerSubject();
        } else if ("g".equals(workType)) {
            return new ComplexSubject();
        }
        throw new IllegalArgumentException("no found workType=" + workType + " subject");
    }

    public static String getWorkType(int subjectType) {
        switch (subjectType) {
            case SUBJECT_TYPE_SINGLE_CHOICE:
                return "0";
            case SUBJECT_TYPE_MULTI_CHOICE:
                return "1";
            case SUBJECT_TYPE_JUDGMENT:
                return "2";
            case SUBJECT_TYPE_COMPLETION:
                return "4";
            case SUBJECT_TYPE_SHORT_ANSWER:
                return "5";
            case SUBJECT_TYPE_COMPLEX:
                return "g";
            default:
                return "0";
        }
    }

    /**
     * 根据题目类型，实例化对象
     *
     * @param type
     * @return
     */
    public static Subject newSubject(int type) {
        switch (type) {
            case SUBJECT_TYPE_SINGLE_CHOICE:
                return new SingleChoiceSubject();
            case SUBJECT_TYPE_MULTI_CHOICE:
                return new MultiChoiceSubject();
            case SUBJECT_TYPE_JUDGMENT:
                return new JudgmentSubject();
            case SUBJECT_TYPE_COMPLETION:
                return new CompletionSubject();
            case SUBJECT_TYPE_SHORT_ANSWER:
                return new ShortAnswerSubject();
            case SUBJECT_TYPE_COMPLEX:
                return new ComplexSubject();
            default:
                return new SingleChoiceSubject();
        }
    }

    @Override
    public int compareTo(Subject another) {
        return this.getType() - another.getType();
    }
}

