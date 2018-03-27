package com.nd.shapedexamproj.model.my;

import android.util.Log;

import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.IndustryUtil;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * 个人信息
 * 
 * @author Linlg
 * 
 *         Create on 2014-3-28
 */
public class PersonalInfo {
	
	private static final String TAG = "PersonalInfo";
	//性别 0男，1女
	public final static int SEX_MAN = 0, SEX_FEMALE = 1;
	public final static String SEX_MAN_STRING = "男", SEX_FEMALE_STRING = "女";
	public final static int PHONE_VISIABLE_TEACHER = 1,PHONE_VISIABLE_FRIENDS = 2,PHONE_VISIABLE_ALL = 3;
	
	//类型 0、1教职工，2学生
	public final static int USER_TYPE_TEACHER = 0, USER_TYPE_TEACHER_ = 1, USER_TYPE_STUDENT = 2;
	
	private String userId;
	public String kduserid;//学号（预留字段）
	public int usertype ;//类型 0、1教职工，2学生
	public int sex; // 性别
	public int age; // 年龄
	public String star;//星座
	public String photoUrl; // 照片地址
	public String userName; // 用户姓名
	public String className; // 用户姓名
	public int classid ;//班级id
	public String specialtyName ;//专业
	public String specialtyid;//学期专业id
	public String teachingpointid;//教学点id
	public String teachingpointName;//教学点名称
	public String userCardId; // 身份证号码
	public String brithday; // 生日
	public String city; // 所在城市
	public String explanation; // 个人签名
	public String hobby; // 兴趣
	public String primarySchool; // 小学
	public String juniormiddleSchool; // 初中
	public String seniormiddleSchool; // 高中
	public String university; // 大学
	public String profession; // 职业
	public String company; // 公司
	public String phone; // 电话
	public int phoneVisible; // 电话是否可见 :1-仅老师可见，2-好友可见，3-所有人可见
	public int fansCount; // 粉丝数量
	public int flowerCount ;//鲜花数量
	public int followCount; // 关注数量
	public int actionCount; // 动态数量
	public String industry; // 行业
	public int industryId;// 行业id
	public int timelineNum; // 动态数

	
	/**
	 * 个人信息Json解析
	 */
	public static PersonalInfo personalInfoJSONPasing(String result) {
		Log.d(TAG, result);
		PersonalInfo personalInfo = null;
		try {
			JSONObject personal = new JSONObject(result);
			int flag = personal.getInt("flag");
			if(Constants.SUCCESS_MSG == flag)
			{
				personalInfo = new PersonalInfo();
				JSONObject user = personal.getJSONObject("user");
				
				personalInfo.age = user.getInt("age");
				personalInfo.brithday = user.getString("birthday");
				personalInfo.city = user.getString("city");
				personalInfo.className = user.getString("className");
				personalInfo.classid = user.getInt("classid");
				personalInfo.company = user.getString("company");
				personalInfo.star = user.getString("constellation");
				personalInfo.explanation = user.getString("explanation");
				personalInfo.fansCount = user.getInt("fanscount");
				personalInfo.flowerCount = user.getInt("flowercount");
				personalInfo.followCount = user.getInt("followcount");
				personalInfo.hobby = user.getString("hobby");
				personalInfo.industryId = user.getInt("industryid");
				personalInfo.phone = user.getString("phone");
				personalInfo.juniormiddleSchool = user.getString("juniormiddleschool");
				personalInfo.phoneVisible = user.getInt("phonevisible");
				personalInfo.photoUrl = user.getString("photo");
				personalInfo.primarySchool = user.getString("primaryschool");
				personalInfo.profession = user.getString("profession");
				personalInfo.seniormiddleSchool = user.getString("seniormiddleschool");
				personalInfo.sex = user.getInt("sex");
				personalInfo.specialtyName = user.getString("specialtyName");
				personalInfo.specialtyid = user.getString("specialtyid");
				personalInfo.teachingpointid = user.getString("teachingpointid");
				personalInfo.teachingpointName = user.getString("teachingpointName");
				personalInfo.timelineNum = user.getInt("timelinecount");
				Log.e(TAG, "personalInfo.timelineNum := " + personalInfo.timelineNum);
				personalInfo.university = user.getString("university");
				personalInfo.userCardId = user.getString("usercardid");
				personalInfo.userId = user.getString("userid");
				personalInfo.userName = user.getString("username");
//				personalInfo.kduserid = user.getString("kduserid");
				personalInfo.usertype = user.getInt("usertype");
				
				String industrys = IndustryUtil.getIndustry(personalInfo.getIndustryId());
				if(!StringUtils.isEmpty(industrys))
				{
					personalInfo.industry = industrys.split("-")[1];
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return personalInfo;
	}
	
	/**
	 * 部分个人信息Json解析
	 * 仅仅更新：班级信息、教学点信息
	 */
	public static PersonalInfo personalSimpleInfoJSONPasing(String result) {
		Log.d(TAG, result);
		PersonalInfo personalInfo = null;
		try {
			JSONObject personal = new JSONObject(result);
			int flag = personal.getInt("flag");
			if(Constants.SUCCESS_MSG == flag)
			{
				personalInfo = new PersonalInfo();
				JSONObject user = personal.getJSONObject("user");

				personalInfo.classid = user.getInt("classid");
				//personalInfo.className = user.getString("className");
				personalInfo.teachingpointid = user.getString("teachingpointid");
				//personalInfo.teachingpointName = user.getString("teachingpointName");
								
				String industrys = IndustryUtil.getIndustry(personalInfo.getIndustryId());
				if(!StringUtils.isEmpty(industrys))
				{
					personalInfo.industry = industrys.split("-")[1];
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return personalInfo;
	}
	
	public String getSexString()
	{
		if( SEX_FEMALE == sex)
		{
			return SEX_FEMALE_STRING;
		}
		else
		{
			return SEX_MAN_STRING;
		}
	}
	
	public String getUserCoverImgUrl()
	{
		return getUserCoverImgUrl(userId);
	}
	
	public static String getUserCoverImgUrl(String id)
	{
		return Constants.HOST_UPLOAD + id +"/cover.jpg";
	}
	
	
	
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getStar() {
		return star;
	}

	public void setStar(String star) {
		this.star = star;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUserCardId() {
		return userCardId;
	}

	public void setUserCardId(String userCardId) {
		this.userCardId = userCardId;
	}

	public String getBrithday() {
		return brithday;
	}

	public void setBrithday(String brithday) {
		this.brithday = brithday;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getHobby() {
		return hobby;
	}

	public void setHobby(String hobby) {
		this.hobby = hobby;
	}

	public String getPrimarySchool() {
		return primarySchool;
	}

	public void setPrimarySchool(String primarySchool) {
		this.primarySchool = primarySchool;
	}

	public String getJuniormiddleSchool() {
		return juniormiddleSchool;
	}

	public void setJuniormiddleSchool(String juniormiddleSchool) {
		this.juniormiddleSchool = juniormiddleSchool;
	}

	public String getSeniormiddleSchool() {
		return seniormiddleSchool;
	}

	public void setSeniormiddleSchool(String seniormiddleSchool) {
		this.seniormiddleSchool = seniormiddleSchool;
	}

	public String getUniversity() {
		return university;
	}

	public void setUniversity(String university) {
		this.university = university;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getPhoneVisible() {
		return phoneVisible;
	}

	public void setPhoneVisible(int phoneVisible) {
		this.phoneVisible = phoneVisible;
	}

	public int getFansCount() {
		return fansCount;
	}

	public void setFansCount(int fansCount) {
		this.fansCount = fansCount;
	}

	public int getFollowCount() {
		return followCount;
	}

	public void setFollowCount(int followCount) {
		this.followCount = followCount;
	}

	public int getActionCount() {
		return actionCount;
	}
	
	public void setActionCount(int actionCount) {
		this.actionCount = actionCount;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public int getIndustryId() {
		return industryId;
	}

	public void setIndustryId(int industryId) {
		this.industryId = industryId;
	}

	public int getTimelineNum() {
		return timelineNum;
	}

	public void setTimelineNum(int timelineNum) {
		this.timelineNum = timelineNum;
	}

	public int getClassid() {
		return classid;
	}

	public void setClassid(int classid) {
		this.classid = classid;
	}

	public String getTeachingpointid() {
		return teachingpointid;
	}

	public void setTeachingpointid(String teachingpointid) {
		this.teachingpointid = teachingpointid;
	}

	@Override
	public String toString() {
		return "PersonalInfo [userId=" + userId + ", kduserid=" + kduserid
				+ ", usertype=" + usertype + ", sex=" + sex + ", age=" + age
				+ ", star=" + star + ", photoUrl=" + photoUrl + ", userName="
				+ userName + ", className=" + className + ", classid="
				+ classid + ", specialtyName=" + specialtyName
				+ ", specialtyid=" + specialtyid + ", teachingpointid="
				+ teachingpointid + ", userCardId=" + userCardId
				+ ", brithday=" + brithday + ", city=" + city
				+ ", explanation=" + explanation + ", hobby=" + hobby
				+ ", primarySchool=" + primarySchool + ", juniormiddleSchool="
				+ juniormiddleSchool + ", seniormiddleSchool="
				+ seniormiddleSchool + ", university=" + university
				+ ", profession=" + profession + ", company=" + company
				+ ", phone=" + phone + ", phoneVisible=" + phoneVisible
				+ ", fansCount=" + fansCount + ", flowerCount=" + flowerCount
				+ ", followCount=" + followCount + ", actionCount="
				+ actionCount + ", industry=" + industry + ", industryId="
				+ industryId + ", timelineNum=" + timelineNum + "]";
	}
	
	

	/**
	 * 更新用户部分信息
	 * 如：重新分班、更新教学点
	 * @param userId
	 */
	public static void updateUserInfo(String userId,RequestCallback<PersonalInfo> callBack){
		String url = Constants.GET_USER_URL;
		Map<String ,Object> params = new HashMap<String,Object>();
		params.put("userid", userId);
		
		TmingHttp.asyncRequest(url, params, callBack);
		
		/*new RequestCallback<PersonalInfo>() {

            @Override
            public PersonalInfo onReqestSuccess(String respones) throws Exception {
                return PersonalInfo.personalSimpleInfoJSONPasing(respones);
            }

            @Override
            public void success(PersonalInfo personalInfo) {
                Constants.CLASS_ID = String.valueOf(personalInfo.getClassid());
                Constants.TEACHING_POINT_ID = String.valueOf(personalInfo.getClassid());
            }

            @Override
            public void exception(Exception exception) {
                
            }           
        }*/
	}
	
	
}
