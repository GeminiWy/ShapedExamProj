package com.nd.shapedexamproj.util;

import android.content.Context;
import android.content.Intent;

import com.nd.shapedexamproj.activity.shapedexam.AnswerRecordDetailActivity;
import com.nd.shapedexamproj.activity.shapedexam.DoingTaskActivity;
import com.nd.shapedexamproj.activity.shapedexam.DoingWarmupActivity;
import com.nd.shapedexamproj.activity.shapedexam.ShapeExamAnswerHistory;
import com.nd.shapedexamproj.activity.shapedexam.ShapeExamDetailActivity;
import com.nd.shapedexamproj.activity.shapedexam.ShapeExamTaskList;
import com.nd.shapedexamproj.activity.shapedexam.WarmupExamActivity;

/**
 * Created by zll on 2015/3/3.
 */
public class XKUIHelper {

    /**
     * 打开形考任务列表
     * @param context
     * @param courseId 课程id
     * @param stepId    学期id
     */
    public static void showTaskList(Context context,String courseId,String courseName,String stepId) {
        Intent intent = new Intent(context, ShapeExamTaskList.class);
        intent.putExtra("courseId",courseId);
        intent.putExtra("courseName",courseName);
        if (stepId != null) {
            intent.putExtra("stepId",stepId);
        }
        context.startActivity(intent);
    }

    /**
     * 打开任务详情
     * @param context
     * @param taskId 任务id
     * @param taskName 任务名称
     */
    public static void showTaskDetailInfo(Context context,String taskId,String taskName) {
        Intent intent = new Intent(context, ShapeExamDetailActivity.class);
        intent.putExtra("taskId",taskId);
        intent.putExtra("taskName",taskName);
        context.startActivity(intent);
    }

    /**
     *历史记录列表
     * @param context
     * @param taskId 任务id
     * @param taskName 任务名称
     */
    public static void showAnswerList(Context context,String taskId,String taskName) {
        Intent intent = new Intent(context, ShapeExamAnswerHistory.class);
        intent.putExtra("taskId",taskId);
        intent.putExtra("taskName",taskName);
        context.startActivity(intent);
    }

    /**
     * 打开历史记录详情页
     * @param context
     * @param answerRecordId
     * @param answerRecordName
     */
    public static void showAnswerRecordDetail(Context context,String taskId,String answerRecordId,String answerRecordName) {
        Intent intent = new Intent(context, AnswerRecordDetailActivity.class);
        intent.putExtra("taskId",taskId);
        intent.putExtra("answerRecordId",answerRecordId);
        intent.putExtra("answerRecordName",answerRecordName);
        context.startActivity(intent);
    }

    /**
     * 打开热身考试页
     * @param context
     * @param kmId 课程id
     * @param warmupName
     */
    public static void showWarmupExamActivity(Context context,String kmId,String warmupName) {
        Intent intent = new Intent(context,WarmupExamActivity.class);
        intent.putExtra("kmId",kmId);
        intent.putExtra("warmupName",warmupName);
        context.startActivity(intent);
    }

    /**
     * 热身考试
     * @param context
     * @param warmupName
     * @param warmupId
     * @param status
     * @param isEditable
     */
    public static void showDoingWarmupActicity(Context context,String warmupName,String warmupId,int status,boolean isEditable) {
        Intent intent = new Intent(context, DoingWarmupActivity.class);
        intent.putExtra("warmupName",warmupName);
        intent.putExtra("warmupId",warmupId);
        intent.putExtra("status",status);
        intent.putExtra("isEditable",isEditable);
        context.startActivity(intent);
    }

    /**
     * 答题页面
     * @param context
     * @param answerId  答题记录ID - 非必须，查看或是继续答题的时候需要
     * @param workId 形考任务ID
     * @param status    1=继续答题，2=已提交，3=正在评阅，4=已经退回，5=已经评阅, 6=已经过期
     */
    public static void showDoingTaskActivity(Context context,String taskName,String answerId,String workId,int status) {
        Intent intent = new Intent(context, DoingTaskActivity.class);
        intent.putExtra("taskName",taskName);
        intent.putExtra("answerId",answerId);
        intent.putExtra("workId",workId);
        intent.putExtra("status",status);
        context.startActivity(intent);
    }


}
