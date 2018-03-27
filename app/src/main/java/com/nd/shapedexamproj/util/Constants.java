package com.nd.shapedexamproj.util;


import com.nd.shapedexamproj.App;

/**
 * 存放常量的类
 * @author zll
 * create in 2014-3-11
 */
public class Constants {
	public static String APP_TIMEZONE = "GMT+08:00";
	/**
	 * 是否外网，true是外网，false是内网
	 */
	public static boolean IS_OUTER_NET = false;
	public static String SHARE_PREFERENCES_NAME = "open_university_config";
	/**
	 * 登录用户名
	 * */
	public static final String SHARE_PREFERENCES_USERNAME_FILE_NAME = "username_preferences";
	public static final String SHARE_PREFERENCES_USERNAME_DATA = "username";
	public static final String SHARE_PREFERENCES_LAST_LOGIN_USERNAME_DATA = "last_login_username"; 
	
	/**
	 * 搜索模块 搜索记录配置文件 文件名，开头以用户名来做前缀
	 * */
	public static final String SHARE_PREFERENCES_SEARCH_FILE_NAME = "search_preferences";
	public static final String SHARE_PREFERENCES_HISTORY_DATA = "search_history";
	
	/**
	 * 搜索模块 搜索类型配置文件, 
	 * */
	public static final String SHARE_PREFERENCES_SEARCH_TYPE_FILE_NAME = "custom_search_type_preferences";
	public static final String SHARE_PREFERENCES_SEARCH_TYPE_DATA = "search_type_key";
	
	/**
	 * 搜索类型,请求不同的搜索数据。1对应用户，2对应专业搜索，3对应课程搜索，4对应动态， 5 对应搜索全部，其它值目前无效
	 * */ 
	public static final int SEARCH_RESULT_TYPE_USER = 1;
	public static final int SEARCH_RESULT_TYPE_SPECIALTY = 2;
	public static final int SEARCH_RESULT_TYPE_COURSE = 3;
	public static final int SEARCH_RESULT_TYPE_PUB = 4;
	public static final int SEARCH_RESULT_TYPE_ALL = 5;

	/**
	 * 成功信息
	 */
	public static final int SUCCESS_MSG = 1;
	public static final String SUCCESS_MICROBLOG_MSG = "成功";
	/**
	 * 刷新列表
	 */
	public static final String REFRESH_LIST = "refresh_list";
	/**
	 * 关注类型，0-user关注follow；1-双方为好友；2-follow关注user(-1-没有关系，不是从服务器端返回)
	 */
	public static final int PERSON_RELATION_EMPTY = -1, PERSON_RELATION_FOLLOW = 0,PERSON_RELATION_FRIEND = 1,PERSON_RELATION_FOLLOWED = 2;
	
	/**
	 * 关注列表
	 */
	public static final int FOLLOW = 0;
	/**
	 * 粉丝列表
	 */
	public static final int FANS = 2;
	/**
	 * 动态回复参数
	 */
	public static final int REPLY_TIMELINE = 1,REPLY_COMMENT = 3,TRANSMIT = 2;
	
	public final static int USER_TYPE_INNER = -1;
	public final static int USER_TYPE_TEACHER = 0;
	public final static int USER_TYPE_STUDENT = 1;
	
	public static final int POP_NORMAL = 0;
	public static final int POP_REVERSE = 1;
	public static final int POP_ITEM_HEIGHT = 40;
	
	public static final String QUESTION_ID = "question_id";     //题目id
	public static final String HOMEWORK_ID = "homework_id" ;	//作业id
	public static final String COURSE_ID = "course_id";		    //课程id
	public static final String COURSE_NAME = "course_name" ; 	//课程名称
	public static final String MAJOR_ID = "major_id";		    //专业id
	public static final String VIDEO_ACTION = "com.kaida.video" ; //视频广播
	public static final String COURSE_DETAIL_ACTION = "com.kaida.coursedetail";
	public static final String COURSE_DETAIL_FOCUS_CURR_VIDEO = "com.kaida.focus.curr.video";
//	public static String DOWNLOAD_PATH = App.getDownload_path();
	public static final String COURSEWARE = "courseware" ;
	public static final String COURSEWARE_PATH = App.createDownloadPath()  ;
	public static final int PAGESIZE = 20;
	public static final String IS_NOLONGER_NOTIFY = "isNoLongerNotify";//是否不再弹出更新对话框
	/**
	 * 绑定的手机号
	 */
	public static String PHONE_NUM = "binding_phone_number";   
	
	//--------------------引导页----------------------
	public static  String IS_FIRST_LAUNCH_APP = "is_first_launch_app_"; // 是否第一次启动该版本App，is_first_launch_app_(版本)
	//------------------------------------------------

    //--------------------课程----------------------
    public static boolean IS_LEFTMENU_VISIABLE=true;//左抽屉是否打开,默认为打开
	//--------------------操场-----------------------
	public static final String PREFERENCES_NAME = "save_last_time";
	
	//--------------------设置页面----------------------
	public static boolean HAS_NEW_VERSION = false ;   //是否有新版本
	
	public static  String IS_NOTIFY_WITHOUT_WIFI = "is_notify_without_wifi"; 	// 是否在非wifi网络下开启提醒，默认打开
	public static  String IS_NOTIFICATION_OPEN = "is_notification_open";  //是否打开通知栏提示，默认打开
	
	public static  String IS_REMIND_OPEN ="is_remind_open" ; //是否打开提醒，默认打开
	public static  String IS_ANNOUNCEMENT_OPEN = "is_announcement_open"; 	//是否打开公告，默认打开
	public static  String IS_GROUPCHAT_OPEN = "is_group_chat_open" ;	//是否打开群聊通知，默认打开
	public static  String IS_PERSONALCHAT_OPEN = "is_personal_chat_open";  //是否打开私聊通知，默认打开
	public static String IS_PERSONALCHATACTIVITY_OPEN = "is_personal_chat_activity_open";//是否打开了单聊页面
	public static String IS_GROUPCHATACTIVITY_OPEN = "is_group_chat_activity_open";//是否打开了群聊页面
	
	//------------------------------------------
	
	//public static List<Activity> activity_list = new ArrayList<Activity>();
	/**
	 * 字体大小
	 */
	public static final int FONT_SIZE_SMALL = 13;
	public static final int FONT_SIZE_MIDLE = 15;
	public static final int FONT_SIZE_LARGE = 18;
	
	
	public static int downloadNotificationId = 1;
	
	//用户头像地址
	public static final String USER_PHOTO_URL = IS_OUTER_NET ? "http://new.fjou.cn/avatar.php?uid=" : "http://www.fjou.tmc/avatar.php?uid=";//内网
	//服务器地址
	public static final String HOST_UPLOAD = IS_OUTER_NET ? "http://api.service.fjou.cn/upload" : "http://192.168.33.62:8088/upload/"; //开发大学Java移动端正式机接口地址 //192.168.33.62
	public static final String HOST = IS_OUTER_NET ? "http://api.service.fjou.cn/api/" : "http://192.168.33.62:8088/api/";	           //开发大学Java移动端正式机接口地址 172.25.75.53:8080
	public static final String LEIWEIBO_HOST = IS_OUTER_NET ? "http://weibo.service.fjou.cn/" : "http://192.168.33.62:8086/";	       //开放大学类微博正式机接口地址
	public static final String MICROBLOG_HOST = IS_OUTER_NET ? "http://weibo.service.fjou.cn/" : "http://192.168.33.62:8086/";         //开放大学类微博正式机接口地址

//	public static String HOST_DEBUG = "http://api.service.fjou.cn/api/";//正式机地址
	public static final String HOST_DEBUG = IS_OUTER_NET ? "http://api.service.fjou.cn/api/" : "http://192.168.33.62:8088/api/";//added by Caiyx on 2014-09-02
	
	
	/**
	 * 上传图片
	 */
	public final static String IMAGE_UPLOAD = MICROBLOG_HOST + "timeline/uploadimageV2.do" ;
	/**
	 * 发布动态
	 */
	public final static String WEIBO_PUBLISH = MICROBLOG_HOST + "timeline/post.do";
	/**
	 * 提交评论
	 */
	public static String COMMENT_PUBLISH = MICROBLOG_HOST + "timeline/comment.do";
	
	
	/**
	 * teacher
	 */
	public final static String TEACHER_COURSES = HOST+"teacher/courses.html";
	
	public final static String TEACHER_CLASSES = HOST+"teacher/classes.html";
	
	/**
	 * 检查更新的接口
	 */
	public static String VERSION_CHECK = HOST + "version/checkout.html" ; 
	/**
	 * 作业列表
	 */
	public static String HOMEWORK_URL = HOST + "student/exams.html";
	/**
	 * 第二版作业列表接口
	 */
	public static String HOMEWORK_URL_V2 = HOST + "student/examOrderByTimeEnd";
	/**
	 * 作业详情页
	 */
	public static String HOMEWORK_DETAIL = HOST + "homework/workInfo.html" ;
	/**
	 * 题目列表
	 */
	public static String QUESTION_LIST = HOST + "homework/workList.html" ;
	/**
	 * 交卷
	 */
	public static String SUBMIT_QUESTION = HOST + "homework/submitWork.html";
	/**
	 * 交到历史记录
	 */
	public static String SENT_QUESTION = HOST + "homework/sentWork.html";
	/**
	 * 课程列表
	 */
	public static String COURSE_URL = HOST + "course/list.html";
	/**
	 * 课程分类列表
	 */
	public static String COURSECATEGORY_URL = HOST + "course/categorys.html";
	/**
	 * 专业列表
	 */
	public static String PROFRESSIONAL_URL = HOST + "specialty/list.html";
	/**
	 * 专业详情
	 */
	public static String PROFRESSIONAL_INFO_URL = HOST + "specialty/info.html";
	/**
	 * 课程详情页--课程简介
	 */
	public static String COURSE_DESC = HOST + "course/info.html";
	/**
	 * 课程详情页--课件列表
	 */
	public static String COURSEWARE_LIST = HOST + "courseware/list.html";
 	/**
 	 * 学生在修课程进度
 	 */
	/*public static String COURSE_PROGRESS = HOST + "student/courseinfo.html" ;*/
	/**
	 * 返回服务器当前时间节点
	 */
	public static String CURRENT_TIME_IN_MILLIS = HOST + "open/getTime.html";
	/**
	 * 学生已看过的课件列表
	 */
	public static String HAVE_SEEN_COURSEWARE_LIST = HOST + "student/coursewares.html";
	/**
	 * 课程详情页首个视频的信息，和学生的在修课程进度
	 */
	public static String COURSE_DETAIL_URL = HOST + "course/combinInfo.html" ;
	/**
	 * 登录
	 */
	public static String LOGIN_URL = HOST + "user/login.html";
	/**
	 * 学生课程统计
	 */
	public static String COURSESTATIS_URL = HOST + "student/coursestatis.html";
	/**
	 * 添加好友，关注好友
	 */
	public static String ADD_FRIENDS_URL = MICROBLOG_HOST + "userrelation/add.do" ;
	/**
	 * 获取关注列表(包括被关注，关注，互相关注)
	 */
	public static String GET_FRIENDS_LIST = MICROBLOG_HOST + "userrelation/list.do";
	/**
	 * 获取用户信息
	 */
	public static String GET_USER_URL = HOST + "user/getUser.html" ;
	/**
	 * 修改用户信息
	 */
	public static String UPDATE_USER_URL = HOST + "user/modUser.html" ;
	
	/**
	 * 获取同班级用户信息
	 */
	public static String GET_USER_LIST_URL = HOST + "user/getUserList.html" ; 
	/**
     * 删除我的状态
     */
    public static String WEIBO_DELETE = MICROBLOG_HOST + "timeline/delete.do" ;
	/**
	 * 删除好友
	 */
	public static String DELETE_FRIENDS_URL = MICROBLOG_HOST + "userrelation/delete.do" ;
	/**
	 * 修改好友
	 */
	public static String MODIFY_FRIENDS_URL = HOST + "userfriends/modUserfriends.html" ;
	/**
	 * 获取关注状态
	 */
	public static String GET_FRIEDN_STATE = MICROBLOG_HOST + "userrelation/query.do" ;
	/**
	 * 送鲜花
	 */
	public static String SEND_FLOWER = HOST + "flowerrelation/giveFlowerrelation.html";
	/**
	 * 获取剩余鲜花数
	 */
	public static String GET_REST_FLOWER = HOST + "flowerrelation/getFlowerCount.html";
	/**
	 * 我的提问
	 */
	public static String MY_QUESTIONS = HOST + "question/mylist.html";
	/**
	 * 我的笔记
	 */
	public static String MY_NOTES = HOST + "notes/list.html";
	/**
	 * 删除笔记
	 */
	public static String DEL_NOTES = HOST + "note/delNote.html";
	/**
	 * 成绩查询页
	 */
	public static String MY_SCORE = HOST + "courseware/scoreList.html";
	/**
	 * 我的学期列表
	 */
	public static String MY_TERM_LIST = HOST + "step/list.html";
	/**
	 * 提交意见反馈
	 */
	public static String COMMIT_FEEDBACK = HOST + "/feedback/addFeedback.html";
	
	//类微博接口
	/**
	 * 获取自己的动态列表
	 */
	public static String TIMELINE_GETMYLIST = LEIWEIBO_HOST + "timeline/getmylist.do?userid=%s&pageno=%d&pagesize=%d";
	
	/**
	 * 辅导讨论接口-讨论列表
	 */
	public static String DISCUSSION_GET_TOPICLIST = HOST + "discussion/list.html";
	/**
	 * 辅导讨论接口-帖子详情
	 */
	public static String DISCUSSION_GET_TOPICDETAIL = HOST + "discussion/replylist.html";
	/**
	 * 辅导讨论接口-发布帖子
	 */
	public static String DISCUSSION_NEW_TOPIC = HOST + "discussion/coachPost.html";
	/**
	 * 辅导讨论接口-回复帖子
	 */
	public static String DISCUSSION_REPLAY_TOPIC = HOST + "discussion/replyPost.html";
	/**
	 * 课堂-答疑-发起提问/我的-我的提问-发起提问
	 */
	public static String COURSE_NEW_QUESTION = HOST + "question/add.html";
	
	/**
	 * 教学点 列表
	 */
	public final static String TEACHING_POINT = HOST + "/teachingpoint/list.html";
	
	/**
	 * 用户搜索详情
	 */
	public static final String SEARCH_USER = HOST + "/search/user.html";

	
	/**
	 * 考试提醒
	 */
	public static final String EXAM_REMIND_LAST = HOST + "/exam/remindLast.html";
	public static final String EXAM_REMIND_LIST = HOST + "/exam/remindList.html";
	public static final String EXAM_LIST = HOST + "/exam/list.html";
	public static final String EXAM_REMIND_DEL = HOST + "/exam/remindDel.html";
	
	
	public static int leftInnerType = 0;//0-所有课程；1-非学历精品课程；2-课程分类下的课程；3-专业列表
	public static String leftInnerId = "0";//进入的ID
	public static String leftInnerName = "";//进入的分类名称



	
	/**
	 * 发布评论返回值   从发布评论界面到动态正文界面   
	 */
	public static final int SEND_COMMENT_SUCCESS_CEOD = 101;//获取到发表评论成功后返回到界面刷新评论列表
	public static final int SEND_COMMENT_SUCCESS_BACK_CEOD = 102;
	
	/**
	 * 发布评论返回广播值或者删除评论返回广播值  从动态正文界面到大厅界面  或者  从发布评论界面到大厅界面
	 */
	public static final String SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION = "com.tming.openuniversity.fragment.refreshlist";

    /**
     * 消息操作
     */
    public static final int USER_RELATEDSTAT_CHANGED=1;
    public static final int USER_RELATEDSTAT_NOTCHANGE=0;

	public static final int PULL_DOWN_TO_REFRESH = 1;
	public static final int PULL_UP_TO_REFRESH = 2;
	/************************** 形考 PHP API 接口常量 ***************************/
	public static final String XK_PHP_API = IS_OUTER_NET ? "http://kw.91open.com/api.php" : "http://192.168.19.196:68/api.php";//测试机地址
	public static final String XK_PHP_API_V = "v1";
	public static final long PHP_API_TIME_DIF = -20000;//192.168.33.62时差
	public static final long PHP_API_TIME_MOVE = 1000*60*10;

	public static String XK_USER_LOGIN = "mobile.user.login";
	public static String XK_TASK_INFO = "mobile.exam.taskInfo";
	public static String XK_TASK_LIST = "mobile.exam.taskList";
	public static String XK_ANSWER_LIST = "mobile.exam.answerList";
	public static String XK_ANSWER_INFO = "mobile.exam.answerInfo";
	public static String XK_GET_SUBJECTS = "mobile.exam.getSubjects";
	public static String XK_SAVE_SUBJECTS = "mobile.exam.saveSubjects";
	public static String XK_RESTART = "mobile.exam.restart";
	public static String XK_UNDO = "mobile.exam.undo";
	public static String XK_REJECT_DETAIL = "mobile.exam.returnReason";

	public static String XK_WARM_UP_SUBJECTS = "mobile.warmup.getSubjects";
	public static String XK_WARM_UP_INFO = "mobile.warmup.info";
	public static String XK_WARM_UP_RESTART = "mobile.warmup.restart";
	public static String XK_WARM_UP_SAVE_SUBJECTS = "mobile.warmup.saveSubjects";

	public static String XK_STU_COURSES = "mobile.stu.myCourse";
	public static String XK_STU_INFO = "mobile.stu.info";

	public static String XK_COURSE_SELECT_LIST = "mobile.course.selectList";
	public static String XK_COURSE_SELECT = "mobile.course.select";
	public static String XK_COURSE_CANCEL = "mobile.course.cancel";
	public static String XK_COURSE_GETCLASS = "mobile.course.getCls";

	/**
	 * 系统公告
	 */
	public static final String ANNOUNCEMENT_LAST = "mobile.announcement.getNew";
	public static final String ANNOUNCEMENT_INFO = "/announcement/info.html";
	public static final String ANNOUNCEMENT_LIST = "mobile.announcement.getList";

	public static int PHP_SUCCESS_CODE = 0;
}


