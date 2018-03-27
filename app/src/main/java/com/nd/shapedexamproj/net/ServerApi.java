package com.nd.shapedexamproj.net;

import android.text.TextUtils;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.AuthorityManager;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.util.Log;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerApi {

	private static String encodeGetUrlString(String urlString, Map<String, Object> params) {
		if (params != null && params.size() > 0) {
			StringBuffer buffer = new StringBuffer();
			Set<String> paramKeys = params.keySet();
			for (String key : paramKeys) {
				buffer.append(key)
						.append("=")
						.append(urlEncodeWithUTF8(String.valueOf(params.get(key))))
						.append("&");
			}
			return urlString + "?" + buffer.substring(0, buffer.length() - 1);
		}
		Log.e("ServerApi", urlString);
		return urlString;
	}

	public static String getURL(Map<String, Object> params, String method) {
		return encodeGetUrlString(Constants.HOST + method, params);
	}
	
	/**
	 * 提问列表
	 */
	public static String getQuestionList(int pageSize, int page,
			String courseId, String userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageNum", page);
		params.put("pageSize", pageSize);
		params.put("courseid", courseId);
		params.put("userid", userId);
		return getURL(params, "question/list.html");
	}

    public static String getMicroBlogURL(Map<String, Object> params, String method) {
        return encodeGetUrlString(Constants.MICROBLOG_HOST + method, params);
    }
	/**
	 * 笔记列表
	 */
	public static String getNoteList(String coursewareId, int pageNum,
			int pageSize) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageNum", pageNum);
		params.put("pageSize", pageSize);
		params.put("coursewareId", coursewareId);
		params.put("userId", App.getUserId());
		return getURL(params, "note/getNoteList.html");
	}
	
	/**
	 * 我的提问列表
	 */
	public static String getMyQuestionList(String courseId, int page, int pageSize){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageNum", page);
		params.put("pageSize", pageSize);
		params.put("courseid", courseId);
		params.put("userid", App.getUserId());
		return getURL(params, "question/mylist.html");
	}
	
	/**
	 * 文本笔记列表
	 */
	public static String getTextNoteList(int pageNum, int pageSize){
        return String.format(Constants.HOST + "note/list.html?userid=%s&pageSize=%d&pageNum=%d", App.getUserId(), pageSize, pageNum);
	}
	
	/**
	 * 笔记上传
	 */
	public static String getUploadNoteUrl(String courseId, String coursewareId, long anchorTime, long audioTime){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", App.getUserId());
		params.put("courseId", courseId);
		params.put("coursewareId", coursewareId);
		params.put("anchorTime", anchorTime);
		params.put("audioTime", audioTime);
		return getURL(params, "note/addNote.html");
	}
	
	/**
	 * 头像上传
	 */
	public static String getUploadPhotoUrl(String file){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", App.getUserId());
		params.put("userpic", file);
		return getURL(params, "user/editPic.html");
	}
	
	/**
	 * 删除笔记
	 */
	public static String getDeleteNoteUrl(String noteId, String type){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("noteId", noteId);
		params.put("note_type", "0");
		return getURL(params, "note/delNote.html");
	}
	/**
	 * 获取关注列表(包括被关注，关注)
	 */
	public static String getFriendsList(int pageNum,int pageSize,int type) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", App.getUserId());
		params.put("type", type);
		params.put("pageno", pageNum);
		params.put("pagesize", pageSize);
		return getMicroBlogURL(params, "userrelation/list.do");
		
	}
	
	/**
	 * 查看所有学习资源列表
	 */
	public static String getCheckAllStudyResourceList(int page) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pageNum", page); 
		params.put("pageSize", Constants.PAGESIZE);
		return getURL(params, "course/categorys.html");
	}
	
	/**
	 * 搜索课程列表
	 * @param pageNum 页数
	 * @param pageSize 每页请求的个数
	 * @param key 搜索的关键字
	 */
	public static String getSearchCourseList(String pageNum, String pageSize, String key) {		 
		if(TextUtils.isEmpty(key)){
		    key = "";
		}
		
		Map<String, Object> params = new HashMap<String, Object>(); 
		
		params.put("pageNum", pageNum);
		params.put("pageSize", pageSize);
		params.put("keyword", key);
		return getURL(params, "search/course.html");
	}
	
	/**
	 * 搜索专业列表
	 * @param pageNum 页数
	 * @param pageSize 每页请求的个数
	 * @param key 搜索的关键字
	 */
	public static String getSearchSpecialtyList(String pageNum, String pageSize, String key) {		 
		if(TextUtils.isEmpty(key)){
		    key = "";
		}
		
		Map<String, Object> params = new HashMap<String, Object>(); 
		
		params.put("pageNum", pageNum);
		params.put("pageSize", pageSize);
		params.put("keyword", key);
		return getURL(params, "search/pro.html"); 	 
	}
	
	/**
	 * 搜索用户列表
	 * @param pageNum 页数
	 * @param pageSize 每页请求的个数
	 * @param key 搜索的关键字
	 */
	public static String getSearchUserInfoList(String pageNum, String pageSize, String key) {		 
		if(TextUtils.isEmpty(key)){
		    key = "";
		}
		
		Map<String, Object> params = new HashMap<String, Object>(); 
		
		params.put("pageNum", pageNum);
		params.put("pageSize", pageSize);
		params.put("keyword", key);
		return getURL(params, "search/user.html"); 	  	 
	}

    /**
     * 发布帖子
     * @param content 帖子内容
     * @return
     */
    public static String getPublicTimelineUrl(String content, String imageIdsString) {
		return Constants.WEIBO_PUBLISH
				+ "?userid=" + urlEncodeWithUTF8(App.getUserId())
				+ "&content=" + urlEncodeWithUTF8(content)
				+ "&classid=" + urlEncodeWithUTF8(App.sClassId)
				+ "&imageIds=" + urlEncodeWithUTF8(imageIdsString)
				+ "&teachingpointid=" + urlEncodeWithUTF8(App.sTeachingPointId);
    }

    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return
     */
    public static String getUserInfoUrl(String userId) {
        return Constants.GET_USER_URL + "?userid=" + userId;
    }
    /**
     * <p>获取课件详情</P>
     *
     * @param coursewareid
     * @return
     */
    public static String getCoursewareDetailUrl(String coursewareid) {
        Map<String, Object> params = new HashMap<String, Object>(); 
        
        params.put("coursewareid", coursewareid);
        params.put("userid", App.getUserId());
        return getURL(params, "courseware/info.html");
    }

	/**
	 * 学生的在修课程情况
	 *
	 * @param userId
	 * @param courseId
	 * @return
	 */
	public static String getStudentCourseInfo(String userId, String courseId) {
		return String.format(Constants.HOST + "student/courseinfo.html?userid=%s&courseid=%s", urlEncodeWithUTF8(userId), urlEncodeWithUTF8(courseId));
	}

	/**
	 * 学生的作业列表过期时间排序(NO.64)
	 * @param userId 用户UID”, 必填
	 * @param isFinished ”undone”为未完成”，选填
	 * @param courseId 课程ID，选填，不填为请求该同学作业，填写则请求该同学该课程下作业
	 * @param isOrderASC 排序，选填，指定为”asc”为顺序，不指定或其它则倒序
	 * @param pageNum 当前页码”,
	 * @param pageSize 每页总数”
	 * @return
	 */
	public static String getExamList(String userId, boolean isFinished, String courseId, boolean isOrderASC, int pageNum, int pageSize) {
		StringBuffer buffer = new StringBuffer(String.format(Constants.HOST + "student/examOrderByTimeEnd?userid=%s&pageNum=%d&pageSize=%d", urlEncodeWithUTF8(userId), pageNum, pageSize));
		if (!isFinished) {
			buffer.append("&type=undone");
		}
		if (courseId != null) {
			buffer.append("&course_id=").append(urlEncodeWithUTF8(courseId));
		}
		if (isOrderASC) {
			buffer.append("&oreder=asc");
		}
		return buffer.toString();
	}

	public static class Timeline {

		public static String getMyTimelineListUrl(String userId, int pageNo, int pageSize) {
			return String.format(Constants.LEIWEIBO_HOST + "timeline/getmylist.do?userid=%s&pageno=%d&pagesize=%d&v=2", urlEncodeWithUTF8(userId), pageNo, pageSize);
		}

		public static String getNewTimelineListUrl(String userId, String type, String typeid, int pageNo, int pageSize) {
			if (AuthorityManager.getInstance().isInnerAuthority()) {
				return String.format(Constants.LEIWEIBO_HOST + "timeline/getnewlist.do?userid=%s&type=2&typeid=%s&pageno=%d&pagesize=%d&v=2", urlEncodeWithUTF8(userId), typeid, pageNo, pageSize);
			} else {
				return String.format(Constants.LEIWEIBO_HOST + "timeline/getnewlist.do?userid=%s&type=%s&typeid=%s&pageno=%d&pagesize=%d&v=2", urlEncodeWithUTF8(userId), type, typeid, pageNo, pageSize);
			}
		}

		/**
		 * 搜索动态列表
		 * @param typeValue 类型
		 * @param pageNo 页数
		 * @param pageSize 每页请求的个数
		 * @param key 搜索的关键字
		 */
		public static String getSearchTimelineListUrl(String typeValue, int pageNo, int pageSize, String key) {
			if(TextUtils.isEmpty(key)){
				key = "";
			}
			return String.format(Constants.LEIWEIBO_HOST + "timeline/searchnewlist.do?type=%s&pageno=%d&pagesize=%d&v=2&searchKey=%s", typeValue, pageNo, pageSize, urlEncodeWithUTF8(key));
		}
		/**
	     * <p>获取动态</P>
	     * @param timelineId
	     * @return
	     */
	    public static String getTimeline(String timelineId) {
	        if(TextUtils.isEmpty(timelineId)){
	            timelineId = "";
            }
	        return String.format(Constants.LEIWEIBO_HOST + "timeline/gettimeline.do?timelineid=%s&v=2",timelineId);
	    }
	}

	public static class Homework {

		/**
		 * 获取作业列表
		 *
		 * @param userId
		 * @param workId
		 * @return
		 */
		public static String getWorkList(String userId, String workId) {
			return String.format(Constants.HOST + "homework/workList.html?workid=%s&userid=%s&pageNum=1&pageSize=1000", workId, userId);
		}

        /**
         * 错题集
         * @param courseId
         * @param userId
         * @return
         */
        public static String getWronSubjectCollect(String userId, String courseId) {
            return String.format(Constants.HOST + "errquestion/errInfo.html?userid=%s&pageSize=1000&course_id=%s&pageNum=1", userId, courseId);
        }

		/**
		 * 保存已做过的作业到服务端
		 * @param workId
		 * @param costTime 单位秒
		 * @param userAnswers 用户的答案：{‘题目ID’:’答案’，‘题目ID’:’答案’}
		 * @return
		 */
		public static String sentWork(String workId,long costTime,JSONObject userAnswers) {
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("userid",App.getUserId());
			params.put("workid",workId);
			params.put("cost_time",costTime);
			params.put("workanswers",userAnswers);
			return encodeGetUrlString(Constants.SENT_QUESTION,params);
		}

		/**
		 * 提交作业
		 * @param workId
		 * @return
		 */
		public static String submitWork(String workId) {
			Map<String,Object> params = new HashMap<String, Object>();
			params.put("userid",App.getUserId());
			params.put("workid",workId);
			return encodeGetUrlString(Constants.SUBMIT_QUESTION,params);
		}

	}

	private static String urlEncodeWithUTF8(String string) {
		try {
			return URLEncoder.encode(string, "Utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return string;
	}
}
