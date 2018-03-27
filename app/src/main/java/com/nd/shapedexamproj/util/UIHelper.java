package com.nd.shapedexamproj.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.activity.ErrorActivity;
import com.nd.shapedexamproj.activity.ImagePagerActivity;
import com.nd.shapedexamproj.activity.LoginActivity;
import com.nd.shapedexamproj.activity.MainActivity;
import com.nd.shapedexamproj.activity.PersonalActivity;
import com.nd.shapedexamproj.activity.course.ClassContactActivity;
import com.nd.shapedexamproj.activity.course.CoachDiscListActivity;
import com.nd.shapedexamproj.activity.course.CoachNewTopicActivity;
import com.nd.shapedexamproj.activity.course.CoachReplyTopicActivty;
import com.nd.shapedexamproj.activity.course.CoachTopicDetailActivity;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.activity.course.CourseListActivity;
import com.nd.shapedexamproj.activity.course.CourseNewQuestionActivity;
import com.nd.shapedexamproj.activity.course.CourseQuestionDetailActivity;
import com.nd.shapedexamproj.activity.course.CourseQuestionListActivity;
import com.nd.shapedexamproj.activity.homework.DoingHomeworkActivity;
import com.nd.shapedexamproj.activity.homework.DoingWrongHomeworkActivity;
import com.nd.shapedexamproj.activity.homework.FinishedHomeworkActivity;
import com.nd.shapedexamproj.activity.homework.FinishedHomeworkDetailActivity;
import com.nd.shapedexamproj.activity.homework.HomeworkDetailActivity;
import com.nd.shapedexamproj.activity.homework.HomeworkListActivityV2;
import com.nd.shapedexamproj.activity.im.ChatActivity;
import com.nd.shapedexamproj.activity.im.ChatGroupActivity;
import com.nd.shapedexamproj.activity.im.ContactPersonAddActivity;
import com.nd.shapedexamproj.activity.im.GroupListActivity;
import com.nd.shapedexamproj.activity.im.IMRoomChatSettingActivity;
import com.nd.shapedexamproj.activity.im.IMSingleChatSettingActivity;
import com.nd.shapedexamproj.activity.im.NewFriendsActivity;
import com.nd.shapedexamproj.activity.im.PersonSearchActicity;
import com.nd.shapedexamproj.activity.inner.Registration;
import com.nd.shapedexamproj.activity.inner.RegistrationResult;
import com.nd.shapedexamproj.activity.inner.RegistrationSecond;
import com.nd.shapedexamproj.activity.major.MajorDetailActivity;
import com.nd.shapedexamproj.activity.my.EditPersonalActivity;
import com.nd.shapedexamproj.activity.my.MyQuestionsActivity;
import com.nd.shapedexamproj.activity.my.ShowPersonalActivity;
import com.nd.shapedexamproj.activity.plaza.PlaygroundRelatemeActivity;
import com.nd.shapedexamproj.activity.plaza.PlaygroundSendCommentActivity;
import com.nd.shapedexamproj.activity.plaza.TimelineActivityV2;
import com.nd.shapedexamproj.activity.remind.MyRemindActivity;
import com.nd.shapedexamproj.activity.search.PlaygroundSearchActivity;
import com.nd.shapedexamproj.im.model.CommunicationItemInfo;
import com.nd.shapedexamproj.model.download.DownloadInfo;
import com.nd.shapedexamproj.model.my.MyQuestion;
import com.nd.shapedexamproj.model.playground.Timeline;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.tming.common.util.Helper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 统一管理跳转和显示
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-21
 */
public class UIHelper {
	private static final String TAG = UIHelper.class.getSimpleName();

	/** 表情图片匹配 */
	private static Pattern facePattern = Pattern
			.compile("\\[{1}([0-9]\\d*)\\]{1}");
	
	private static Pattern imagePattern = Pattern.compile("\\[img\\s.*?src=\"([^\"]*)", Pattern.CASE_INSENSITIVE);
	

	/** 全局web样式 */
	// 链接样式文件，代码块高亮的处理
	public final static String linkCss = "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script>"
			+ "<script type=\"text/javascript\" src=\"file:///android_asset/brush.js\"></script>"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\">"
			+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\">"
			+ "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>";
	public final static String WEB_STYLE = linkCss + "<style>* {font-size:14px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;overflow: auto;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

	
	
	
	/**
	 * 跳转到主界面
	 *@param context
	 */
	public static void showMain(Context context){
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);
	}
	
	
	/**
	 * 跳转登陆界面
	 *@param context
	 */
	public static  void showLogin(Context context){
		Intent intent = new Intent(context, LoginActivity.class);
		context.startActivity(intent);
	}
	
	/**
	 * 
	 *跳转课程详情页
	 *@param context
	 *@param courseId
	 *@param courseName
	 */
	public static void showCourseDetail(Context context, String courseId, String courseName){
		Intent it = new Intent(context, CourseDetailActivity.class);
		it.putExtra("course_id", courseId);
		it.putExtra("course_name", courseName);	
		context.startActivity(it);
	}
	/**
     * 
     *在修课程跳转课程详情页
     *@param context
     *@param courseId
     *@param courseName
     */
    public static void showCourseDetail(Context context, String courseId, String courseName,String videoId){
        Intent it = new Intent(context, CourseDetailActivity.class);
        it.putExtra("local", 3);
        it.putExtra("course_id", courseId);
        it.putExtra("course_name", courseName); 
        it.putExtra("video_id", videoId);
        context.startActivity(it);
    }
	
	/**
	 * <p>跳转到课程详情页</P>
	 *
	 * @param context
	 * @param downloadInfo
	 */
	public static void showCourseDetail(Context context,DownloadInfo downloadInfo) {
	    Intent it = new Intent(context, CourseDetailActivity.class);
	    it.putExtra("local", 1);
        it.putExtra("course_id", downloadInfo.courseId);
        it.putExtra("course_name", downloadInfo.coursecatename);
        it.putExtra("video_id", downloadInfo.videoId);
        it.putExtra("video_name", downloadInfo.title);
        it.putExtra("video_url", downloadInfo.url);
        context.startActivity(it);
	}
	
	/**
	 * 跳转专业详情页
	 *@param context
	 *@param courseId
	 *@param courseName
	 * */
	public static void showMajorDetailActivity(Context context, String courseId, String courseName){
		Intent it = new Intent(context, MajorDetailActivity.class);
		it.putExtra("course_id", courseId);
		it.putExtra("course_name", courseName);
		it.putExtra("comefrom", "search_model");
	
		context.startActivity(it);
	}
	
	/**
	 * 返回到主界面
	 */
	public static void backMain(Context context) {
		Intent intent = new Intent();
		intent.setAction("backToMain");
		Helper.sendLocalBroadCast(context, intent);
	}
	
	/**
	 * 
	 * 在线报名第一步骤
	 *@param context
	 */
	public static void showOnlineRegisterFirst(Context context){
		Intent it = new Intent(context, Registration.class);
		context.startActivity(it);
	}
	
	/**
	 * 
	 * 在线报名第二步骤
	 *@param context
	 */
	public static void showOnlineRegisterSecond(Context context){
		Intent it = new Intent(context, RegistrationSecond.class);
		context.startActivity(it);
	}
	
	/**
	 * 
	 * 在线报名第三步骤
	 *@param context
	 */
	public static void showOnlineRegisterThird(Context context){
		Intent it = new Intent(context, RegistrationResult.class);
		context.startActivity(it);
	}

    /**
     *
     * 查看他人信息页
     * @param context
     * @param userId 用户ID
     */
    public static void showFriendInfoActivity(Context context,String userId)
    {
        Intent it = new Intent(context, PersonalActivity.class);
        it.putExtra("friendid", userId);
        it.putExtra("bottomVisiable",true);
        context.startActivity(it);
    }

    /**
     * <p>查看他人信息，并设定底部功能按钮是否可见</p>
     * @param context
     * @param userId
     * @param bottomBarVisiable 底部功能按钮是否可见
     */
    public static void showFriendInfoActivity(Context context,String userId,boolean bottomBarVisiable)
    {
        Intent it = new Intent(context, PersonalActivity.class);
        it.putExtra("friendid", userId);
        it.putExtra("bottomVisiable",bottomBarVisiable);
        context.startActivity(it);
    }

    /**
     * <p>查看他人信息页</P>
     * @param context
     * @param itemInfo
     */
    public static void showFriendInfoActivity(Context context,CommunicationItemInfo itemInfo) {
        String toUserid = "";
        if (App.getUserId().equals(itemInfo.userid)) {
           toUserid = itemInfo.followId;
        } else {
           toUserid = itemInfo.userid;
        }
        showFriendInfoActivity(context, toUserid);
    }

    /**
	 *  编辑个人信息
	 * @param activity
	 */
	public static void showEditPersonalActivity(Activity activity)
	{
		Intent it = new Intent(activity, EditPersonalActivity.class);
		activity.startActivityForResult(it, 0);
	}
	
	/**
	 * 个人信息展示
	 * @param context
	 */
	public static void showPersonalActivity(Context context)
	{
		Intent it = new Intent(context, ShowPersonalActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(it);
	}
	
	/**
	 * 跳转到作业列表页
	 */
	public static void showHomeworkListActivity(Context context,String courseid) {
	    Intent intent = new Intent(context, HomeworkListActivityV2.class);
	    if (courseid != null) {
	        intent.putExtra("course_id", courseid);
	    }
	    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
	}

	/**
	 * 跳转到错题列表页
	 * @param context
	 */
	public static void showErrorActivity(Context context) {
		Intent intent = new Intent(context, ErrorActivity.class);
		context.startActivity(intent);
	}

	/**
	 * <p>跳转到已完成课程列表</P>
	 * @param context
	 * @param courseId
	 */
	public static void showFinishedHomework(Context context,String courseId) {
	    Intent intent = new Intent();
	    if (courseId != null) {
    	    intent.setClass(context, FinishedHomeworkDetailActivity.class);
            intent.putExtra("courseId", courseId);
	    } else {
	        intent.setClass(context, FinishedHomeworkActivity.class);
	    }
        context.startActivity(intent);
	}
	
	/**
	 * <p>跳转到作业详情页</P>
	 * @param context
	 * @param highestScore 最高分,值为-1表示未批改
	 */
	public static void showHomeworkDetailActivity(Context context,String homeworkId,String homeworkName,float highestScore) {
	    Intent intent = new Intent(context, HomeworkDetailActivity.class);
        intent.putExtra("homework_id", homeworkId);
        intent.putExtra("homework_name", homeworkName);
        intent.putExtra("highestScore", highestScore);
        context.startActivity(intent);
	}
	
	/**
	 * 课堂-答疑-发起提问/我的-我的提问-发起提问
	 * @param activity
	 * @param courseId
	 * @param courseName
	 * @param chapterId(可为空)
	 * @param chapterName(可为空)
	 */
	public static void showCourseNewQuestionActivity(Activity activity, String courseId, String courseName, String chapterId, String chapterName)
	{
		Intent it = new Intent(activity, CourseNewQuestionActivity.class);
		it.putExtra("courseId", courseId);
		it.putExtra("courseName", courseName);
		it.putExtra("chapterId", chapterId);
		it.putExtra("chapterName", chapterName);
		activity.startActivityForResult(it, 0);
	}
	
	/**
	 * 课堂-答疑-发起提问/我的-我的提问-发起提问
	 * @param context
	 * @param courseId
	 * @param courseName
	 * @param chapterId(可为空)
	 * @param chapterName(可为空)
	 */
	public static void showCourseNewQuestionActivity(Context context, String courseId, String courseName, String chapterId, String chapterName)
	{
		Intent it = new Intent(context, CourseNewQuestionActivity.class);
		it.putExtra("courseId", courseId);
		it.putExtra("courseName", courseName);
		it.putExtra("chapterId", chapterId);
		it.putExtra("chapterName", chapterName);
		context.startActivity(it);
	}
	
	/**
	 * 课堂-辅导讨论区
	 * @param context
	 * @param courseId
	 * @param courseName
	 */
	public static void showCoachDiscListActivity(Context context, String courseId, String courseName)
	{
		Intent it = new Intent(context, CoachDiscListActivity.class);
		it.putExtra("courseId", courseId);
		it.putExtra("courseName", courseName);
		context.startActivity(it);
	}
	
	/**
	 * 课堂-辅导讨论区-帖子详情
	 * @param context
	 * @param courseId
	 * @param topicId
	 */
	public static void showCoachTopicDetailActivity(Context context, String courseId, String topicId)
	{
		Intent it = new Intent(context, CoachTopicDetailActivity.class);
		it.putExtra("courseId", courseId);
		it.putExtra("topicId", topicId);
		context.startActivity(it);
	}
	
	/**
	 * 课堂-辅导讨论区-新提问
	 * @param activity
	 * @param courseId
	 */
	public static void showCoachNewTopicActivity(Activity activity, String courseId)
	{
		Intent it = new Intent(activity, CoachNewTopicActivity.class);
		it.putExtra("courseId", courseId);
		activity.startActivityForResult(it, 0);
	}
		
	/**
	 * 课堂-辅导讨论区-帖子详情-回复帖子
	 * @param activity
	 * @param courseId
	 * @param topicId
	 */
	public static void showCoachReplyTopicActivty(Activity activity, String courseId, String topicId)
	{
		Intent it = new Intent(activity, CoachReplyTopicActivty.class);
		it.putExtra("courseId", courseId);
		it.putExtra("topicId", topicId);
		activity.startActivityForResult(it, 0);
	}
	
	/**
	 * 我的-我的提问-提问详情
	 * @param context
	 * @param myQuestion
	 */
	public static void showCourseQuestionDetailActivty(Context context, MyQuestion myQuestion)
	{
		Intent intent = new Intent(context, CourseQuestionDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("question", myQuestion);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 我的-我的提问-提问详情
	 * @param activity
	 * @param myQuestion
	 */
	public static void showCourseQuestionDetailActivty(Activity activity, MyQuestion myQuestion)
	{
		Intent intent = new Intent(activity, CourseQuestionDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("question", myQuestion);
		intent.putExtras(bundle);
		activity.startActivityForResult(intent, 0);
	}
	
	/**
	 * 我的-我的提问-我的提问列表
	 * @param context 
	 */
	public static void showMyQuestionsActivity(Context context)
	{
		Intent intent = new Intent(context, MyQuestionsActivity.class);
		context.startActivity(intent);
	}
	
	/**
	 * 课程-课程答疑列表
	 * @param context 
	 */
	public static void showCourseQuestionListActivity(Context context, String courseId, String courseName)
	{
		Intent intent = new Intent(context, CourseQuestionListActivity.class);
		intent.putExtra("courseId", courseId);
		intent.putExtra("courseName", courseName);
		context.startActivity(intent);
	}
	
	/**
	 * 
	  * @Title: showContactPersonAddActivity
	  * @Description: IM-添加好友界面
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
	public static void showContactPersonAddActivity(Context context){
		Intent intent = new Intent(context, ContactPersonAddActivity.class);
		context.startActivity(intent);
	}

    /**
     * @Title: showNewFriendsActivity
     * @Description: 消息模块 ，新的朋友Activity
     * @param context
     */
    public static void showNewFriendsActivity(Context context){
        Intent intent=new Intent(context, NewFriendsActivity.class);
        context.startActivity(intent);
    }

	
	/**
	 * 
	  * @Title: showPersonSearchActivity
	  * @Description: IM-搜索好友界面
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
	public static void showPersonSearchActivity(Context context)
	{
		Intent intent = new Intent(context, PersonSearchActicity.class);
		context.startActivity(intent);
	}
	

	/**
	 * 
	  * @Title: showPersonSearchActivity
	  * @Description: IM-单聊界面
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
	public static void showChatActivity(Context context, String toUserid, String toUserName)
	{
		Intent intent = new Intent(context, ChatActivity.class);
		intent.putExtra("toUserid", toUserid);
		intent.putExtra("toUserName", toUserName);
		context.startActivity(intent);
	}
	/**
	 * <p>IM单聊界面</P>
	 * @param context
	 * @param itemInfo
	 */
	public static void showChatActivity(Context context, CommunicationItemInfo itemInfo) {
	    String toUserid = "";
        if (App.getUserId().equals(itemInfo.userid)) {
           toUserid = itemInfo.followId;
        } else {
           toUserid = itemInfo.userid;
        }
        String toUserName = itemInfo.getName();
        showChatActivity(context, toUserid, toUserName);
	}
	
	/**
	 * 
	  * @Title: showPersonSearchActivity
	  * @Description: IM-群聊界面
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
    public static void showChatGroupActivity(Context context, String roomJid, String roomName)
	{
		Intent intent = new Intent(context, ChatGroupActivity.class);
		intent.putExtra("roomJid", roomJid);
		intent.putExtra("roomName", roomName);
		context.startActivity(intent);
	}
	
	/**
	 * 
	  * @Title: showGroupListActivity
	  * @Description: IM-群组列表
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
	public static void showGroupListActivity(Context context)
	{
		Intent intent = new Intent(context, GroupListActivity.class);
		context.startActivity(intent);
	}
	
	/**
	 * 
	  * @Title: showContactPersonAddActivity
	  * @Description: IM-我的提醒界面
	  * @param @param context
	  * @return void    返回类型
	  * @throws
	 */
	public static void showMyRemindActivity(Context context)
	{
		Intent intent = new Intent(context, MyRemindActivity.class);
		context.startActivity(intent);
	}

    /**
     * <p>跳转到search activity，默认的tab index为：0</p>
     * @param context
     */
    public static void showSearchModelActivity(Context context)
    {
        //Intent intent = new Intent(context, SearchAllResultActivity.class);//之前的操场搜索模块
        Intent intent=new Intent(context, PlaygroundSearchActivity.class);
        intent.putExtra("tabIndex",0);
        context.startActivity(intent);
    }

    /**
     * showSearchModelActivity 转入搜索Activity
     * @param context
     * @param tabIndex   初始化的tab标签，默认为0
     */
	public static void showSearchModelActivity(Context context,int tabIndex)
	{
		//搜索下拉列表搜索入口
        //Intent intent = new Intent(context, SearchActivity.class);
		//搜索全部搜索入口
		//Intent intent = new Intent(context, SearchAllResultActivity.class);//之前的操场搜索模块
		Intent intent=new Intent(context, PlaygroundSearchActivity.class);
        intent.putExtra("tabIndex",tabIndex);
        context.startActivity(intent);
	}
	
	/**
	 * 
	  * @Title: showIMSingleChatSettingActivity
	  * @Description: IM-聊天设置界面-单聊
	  * @param @param context
	  * @param @param oppId
	  * @param @param oppName
	  * @param @param oppImgUrl
	  * @return void    返回类型
	  * @throws
	 */
	public static void showIMSingleChatSettingActivity(Activity activity, String oppId, String oppName, String oppImgUrl)
	{
		
		Intent intent = new Intent(activity, IMSingleChatSettingActivity.class);
		intent.putExtra("oppId", oppId);
		intent.putExtra("oppName", oppName);
		intent.putExtra("oppImgUrl", oppImgUrl);
		activity.startActivityForResult(intent, 0);
	}
	
	/**
	 * 
	  * @Title: showIMRoomChatSettingActivity
	  * @Description: IM-聊天设置界面-聊天室
	  * @param @param context
	  * @param @param roomId
	  * @param @param roomName
	  * @param @param number
	  * @return void    返回类型
	  * @throws
	 */
	public static void showIMRoomChatSettingActivity(Activity activity, String roomId, String roomName, int number)
	{
		
		Intent intent = new Intent(activity, IMRoomChatSettingActivity.class);
		intent.putExtra("oppId", roomId);
		intent.putExtra("roomName", roomName);
		intent.putExtra("number", number);
		activity.startActivityForResult(intent, 0);
	}
	
	
	/**
	 * 倒转到班级通讯录
	 * @param context
	 * @param classId
	 */
	public static void showClassContactActivity(Context context, String classId){
		Intent intent = new Intent(context, ClassContactActivity.class);
		intent.putExtra(ClassContactActivity.ARG_CLASS_ID, classId);
		context.startActivity(intent);
	}

    /**
     * <p>跳转到课程列表activity中</p>
     * @param context
     */
    public static void showAllCourseListActivity(Context context,String comeFrom){
        Intent intent = new Intent(context, CourseListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("comefrom", comeFrom);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

//	public static void showCourseDetail(Context context, String courseId, String courseName){
//		Intent it = new Intent(context,
//				CourseDetailActivity.class);
//		it.putExtra("course_id", courseId);
//		it.putExtra("course_name", courseName);
//		context.startActivity(it);
//	}
	
	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}
	
	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}
	
	/**
	 * 将[12]之类的字符串替换为表情
	 * 
	 * @param context
	 * @param content
	 */
	public static SpannableStringBuilder parseFaceByText(Context context,
			String content) {
		SpannableStringBuilder builder = new SpannableStringBuilder(content);
		Matcher matcher = facePattern.matcher(content);
		while (matcher.find()) {
			// 使用正则表达式找出其中的数字
			int position = StringUtils.toInt(matcher.group(1));
			int resId = 0;
			try {
				if (position > 65 && position < 102)
					position = position - 1;
				else if (position > 102)
					position = position - 2;
				resId = InputBottomBar.mImageIds[position];
				Drawable d = context.getResources().getDrawable(resId);
				d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				builder.setSpan(span, matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} catch (Exception e) {
			}
		}
		return builder;
	}
	
	/**
	 * 关闭输入法窗口
	 * */
	public static void closeInputWindow(Context context, View view){
		 InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	/**
	 * 将[12]之类的字符串替换为表情
	 * 
	 * @param context
	 * @param builder
	 */
	public static SpannableStringBuilder parseFaceByText(Context context,
			SpannableStringBuilder builder) {
		//SpannableStringBuilder builder = new SpannableStringBuilder(content);
		Matcher matcher = facePattern.matcher(builder);
		while (matcher.find()) {
			// 使用正则表达式找出其中的数字
			int position = StringUtils.toInt(matcher.group(1));
			int resId = 0;
			try {
				if (position > 65 && position < 102)
					position = position - 1;
				else if (position > 102)
					position = position - 2;
				//resId = GridViewFaceAdapter.getImageIds()[position];
				Drawable d = context.getResources().getDrawable(resId);
				//d.setBounds(0, 0, (int)mContentET.getTextSize(), (int)mContentET.getTextSize());// 设置表情图片的显示大小
				d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
				ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
				builder.setSpan(span, matcher.start(), matcher.end(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} catch (Exception e) {
			}
		}
		return builder;
	}
	
	
	/**
	 * 
	 * 获取Image 地址
	 * @param content
	 */
	
	public static String parseImage(String content){
		//Matcher matcher  = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(content);
		Matcher matcher = imagePattern.matcher(content);
		while(matcher.find()){
        	String url = matcher.group(1);
        	Log.e(TAG, " image url : " + url);
        	
        	return url;
        }
		
		return null;
	}
	
	/**
	 * 获取Image 地址列表
	 * @param content
	 * @return
	 */
	public static List<String> parseImages(String content){
		List<String> list  = new ArrayList<String>();
		Matcher matcher = imagePattern.matcher(content);
		while(matcher.find()){
        	String url = matcher.group(1);
        	if (!url.startsWith("http")) {
        	    url = "http://" + url;
            }
        	Log.e(TAG, " image url : " + url);
        	list.add(url);
        }
		return list;
	}
	
	/**
     * 获取本地当前时间
	 * @return
	 */
	public static  String getCurrentTime() {
		return new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA)
				.format(new Date());
	} 
	
	private static final String DEFAULT_INNER_SEARCH_RECORD_SP_FILE_NAME = "inner_user";
	/**
     * 获取当前用户搜索记录配置文件名
	 * @return
	 */
	public static String getCurrentUserNameSPName(Context context) {
		/**
		 * 如果是游客就统一默认一个文件
		 * */
		if (AuthorityManager.getInstance().isInnerAuthority()) {
			return DEFAULT_INNER_SEARCH_RECORD_SP_FILE_NAME;
		}
		SharedPreferences loginRecordSp;
		loginRecordSp = context.getSharedPreferences(Constants.SHARE_PREFERENCES_USERNAME_FILE_NAME, Context.MODE_PRIVATE);
		String userName  = loginRecordSp.getString(Constants.SHARE_PREFERENCES_LAST_LOGIN_USERNAME_DATA, "");
		if(TextUtils.isEmpty(userName)){
			userName = "";
		} 
		String fileName = userName + Constants.SHARE_PREFERENCES_SEARCH_TYPE_FILE_NAME; 
		return fileName;
	}  
	
	/**
	 * 跳转到动态正文界面
	 * @param context
	 * @param timeline 动态对象
	 */
	public static void showTimelineActivity(Context context, Timeline timeline) {
		Intent intent = new Intent(context, TimelineActivityV2.class);
		intent.putExtra("user_timeline", timeline);// 动态内容		
		context.startActivity(intent);
	}
	/**
     * 跳转到动态正文界面
     * @param context
     * @param timeline 动态对象
     */
    public static void showTimelineActivity(Context context, Timeline timeline,String userName) {
        Intent intent = new Intent(context, TimelineActivityV2.class);
        intent.putExtra("user_timeline", timeline);// 动态内容      
        intent.putExtra("user_name", userName);//回复框中的用户名
        context.startActivity(intent);
    }
	
	/**
	 * 跳转到与我相关的动态界面
	 * @param context
	 * @param lastOperateTime
	 */
	public static void showRelativeActivity(Context context, String lastOperateTime) {
	    Intent msgIntent = new Intent();
        msgIntent.setClass(App.getAppContext(), PlaygroundRelatemeActivity.class);
        msgIntent.putExtra("operateTime", lastOperateTime);
        context.startActivity(msgIntent);
	}
	
	/**
	 * 跳转到发评论界面
	 * @param context
	 * @param timeline 动态对象
	 * @param sendType 发送的类型 
	 */
	public static void showSendCommentActivity(Context context, Timeline timeline, int sendType, int refreshlistType) {
		Intent intent = new Intent(context, PlaygroundSendCommentActivity.class);
		intent.putExtra("send_type", sendType);
		intent.putExtra("userName", timeline.getUsername());
		intent.putExtra("timelineid",timeline.getTimelineId()); 
		intent.putExtra("classid", Long.toString(timeline.getClassId())); 
		intent.putExtra("teachingpointid", Long.toString(timeline.getTeachingPointId())); 
		intent.putExtra("originaltimelineid", timeline.getTimelineId() + ""); 
		intent.putExtra("refreshlisttype", refreshlistType);
		intent.putExtra("comeformrefresh", "search_resfresh"); 
		context.startActivity(intent);
	}
	/**
	 * <p>浏览图片</P>
	 * @param context
	 * @param position
	 * @param urls
	 */
	public static void showImageBrower(Context context, int position, String[] urls) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        // 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
        intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
        context.startActivity(intent);
    }
	
	/**
	 * 过滤包含有图片地址和表情的动态内容。
	 * @param content 要过滤的文本
	 * @return
	 */
	public static String filterImageAndFaceContent(String content) {
		String filter = ""; 
 
		filter = filterImageContent(content);
		filter = filterFaceContent(filter); 	 
	
		return filter;
	}
	
	/**
	 * 过滤包含有图片地址内容。
	 * @param content 要过滤的文本
	 * @return
	 */
	public static String filterImageContent(String content) {
		String filter = ""; 
//		filter = content.replaceAll("[<](/)?img[^>]*[>]", "");
		filter = content.replaceAll("\\[img\\s*src=.*?]", "");
		
		return filter;
	}
	
	/**
	 * 过滤包含有表情的动态内容。
	 * @param content 要过滤的文本
	 * @return
	 */
	public static String filterFaceContent(String content) {
		String filter = ""; 
		/* *
		 * 过滤[某*某]字符串
		 * */  		   
	  	filter = content.replaceAll("[\\[][^\\[\\]]+[\\]]", ""); //可行正则表达式
		
//		filter = content.replaceAll("\\[.*?\\]",""); 	  //可行正则表达式 
		return filter;
	}

	/**
	 * 做作业界面
	 *
	 * @param context
	 * @param homeworkId 作业ID
	 * @param continueDoing 是否继续做作业
	 */
	public static void showDoingHomeworkActivity(Context context, String homeworkId, boolean continueDoing) {
		Intent intent = new Intent(context, DoingHomeworkActivity.class);
		intent.putExtra("workId", homeworkId);
		intent.putExtra("continueDoing", continueDoing);
		context.startActivity(intent);
	}

    /**
     * 错题集
     * @param context
     * @param courseId 课程ID
     * @param courseName 课程名
     */
    public static void showDoingWrongHomeworkActivity(Context context, String courseId, String courseName) {
        Intent intent = new Intent(context, DoingWrongHomeworkActivity.class);
        intent.putExtra("courseId", courseId);
        intent.putExtra("courseName", courseName);
        context.startActivity(intent);
    }
}
