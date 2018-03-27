package com.nd.shapedexamproj.model.xkhomework;

import android.content.Context;

import com.nd.shapedexamproj.view.xkhomework.XKSubjectView;
import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zll on 2015/3/11.
 */
public abstract class XKSubject implements IJsonInitable, Comparable<XKSubject> {

    /**
     * 单选
     */
    public static final int SUBJECT_TYPE_SINGLE_CHOICE = 0;

    /**
     * 多选
     */
    public static final int SUBJECT_TYPE_MULTI_CHOICE = 1;

    /**
     * 判断
     */
    public static final int SUBJECT_TYPE_JUDGMENT = 2;

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
    //-----------------------------------------------
    /**
     * 题目ID
     */
    protected String subjectId;

    /**
     * 题目
     */
    protected String subjectName;
    //protected String subjectContent;

    /**
     *难易程度- 0=容易，1=一般，2=较难 3难
     */
    protected int lv;

    /**
     * 正确答案
     */
    protected String rightAnswer;

    /**
     * 学生答案
     */
    protected String userAnswer;
    /**
     * 是否正确- 1=正确 0=错误
     */
    protected int isRight;

    /**
     * 题目分数
     */
    protected float score = -1;

    private boolean isEditable = true;

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

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
    public abstract XKSubjectView createShowView(Context context);

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        subjectId = jsonObj.getString("id");
        subjectName = jsonObj.getString("content");//
        score = jsonObj.getInt("score");
        //lv = jsonObj.getInt("lv");

        if (jsonObj.has("answer")) {
            rightAnswer = jsonObj.getString("answer");
        }
        if (jsonObj.has("stu_answer")) {
            userAnswer = jsonObj.getString("stu_answer");
        }
        if (jsonObj.has("is_right")) {
            isRight = jsonObj.getInt("is_right");
        }

    }

    /**
     * 获取题目数据，用于保存题目
     *
     * @return
     */
    public JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", subjectName);
        jsonObject.put("id", subjectId);
        jsonObject.put("score", String.valueOf(score));
        return jsonObject;
    }

    /**
     * 根据服务端题目类型，实例化对象
     * @param workType
     * @return
     */
    public static XKSubject newSubjectWithWorkType(String workType) {
        if ("0".equals(workType)) {
            return new XKSingleChoiceSubject();
        } else if ("2".equals(workType)) {
            return new XKJudgmentSubject();
        } else if ("1".equals(workType)) {
            return new XKMultiChoiceSubject();
        } else if ("4".equals(workType)) {
            return new XKCompletionSubject();
        } else if ("5".equals(workType)) {
            return new XKShortAnswerSubject();
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
    public static XKSubject newSubject(int type) {
        switch (type) {
            case SUBJECT_TYPE_SINGLE_CHOICE:
                return new XKSingleChoiceSubject();
            case SUBJECT_TYPE_MULTI_CHOICE:
                return new XKMultiChoiceSubject();
            case SUBJECT_TYPE_JUDGMENT:
                return new XKJudgmentSubject();
            case SUBJECT_TYPE_COMPLETION:
                return new XKCompletionSubject();
            case SUBJECT_TYPE_SHORT_ANSWER:
                return new XKShortAnswerSubject();
            default:
                return new XKSingleChoiceSubject();
        }
    }

    @Override
    public int compareTo(XKSubject another) {
        return this.getType() - another.getType();
    }
}
