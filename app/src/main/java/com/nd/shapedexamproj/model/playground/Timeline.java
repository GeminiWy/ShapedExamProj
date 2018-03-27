/**
 *
 */
package com.nd.shapedexamproj.model.playground;

import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.util.JsonUtil;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * 动态对象
 *
 * @author Caiyx 2014-5-11 <br/>
 *         modified by Abay Zhuang 2014-6-4
 */
public class Timeline implements Serializable, IJsonInitable {

    private static final long serialVersionUID = 1L;
    /**
     * 动态布局类型
     */
    public static int LAYOUT_TYPE_FIRST = 0;
    /**
     * 动态布局类型，右边有向下箭头
     */
    public static int LAYOUT_TYPE_SECOND = 1;
    
    public static final int TYPE_ORIGINAL = 0,TYPE_COMMENT = 1,TYPE_TRANSMIT = 2 ,TYPE_REPLY_COMMENT = 3;
    private final static String TAG = "Timeline";
    // 动态类型，0-原创；1-评论；2-转发;3-回复评论
    private int type;
    private String content;
    private boolean top;
    private String timelineId;// 376,
    private long userId;//: 1100000403,
    private String flowerCount;//: null,
    private long commentCount;//: 0,
    private long commentedCount;//: 0,
    private long transferCount;//: 0,
    private long transferredCount;//: 0,
    private String postTime;//: "2014-05-19 18:57:07",
    private String referId;//: 0,
    private String username;//: "大乔改名字",
    private int userType;//用户类型  0、1教职工，2学生
    private String commentUserName;//如果type=3，此时表示被回复评论的用户名
    private PersonalInfo personalInfo;//用户信息
    private String photo;//: "http://www.fjou.tmc//avatar.php?uid=",
    private String originalTimelineId;//: 0,
    private long classId;//: 0,
    private long teachingPointId;//: 0,
    private int industryid;//: "10",
    private String sex;//: "0",
    private String age; // "32"
    private long totalSize;// 总数（评论）
    
    private List<TimelineImageInfo> images;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public String getTimelineId() {
        return timelineId;
    }

    public void setTimelineId(String timelineId) {
        this.timelineId = timelineId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getFlowerCount() {
        return flowerCount;
    }

    public void setFlowerCount(String flowerCount) {
        this.flowerCount = flowerCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(long commentCount) {
        this.commentCount = commentCount;
    }

    public long getCommentedCount() {
        return commentedCount;
    }

    public void setCommentedCount(long commentedCount) {
        this.commentedCount = commentedCount;
    }

    public long getTransferCount() {
        return transferCount;
    }

    public void setTransferCount(long transferCount) {
        this.transferCount = transferCount;
    }

    public long getTransferredCount() {
        return transferredCount;
    }

    public void setTransferredCount(long transferredCount) {
        this.transferredCount = transferredCount;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getReferId() {
        return referId;
    }

    public void setReferId(String referId) {
        this.referId = referId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public int getUserType() {
        return userType;
    }
    
    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getCommentUserName() {
        return commentUserName;
    }

    public void setCommentUserName(String commentUserName) {
        this.commentUserName = commentUserName;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getOriginalTimelineId() {
        return originalTimelineId;
    }

    public void setOriginalTimelineId(String originalTimelineId) {
        this.originalTimelineId = originalTimelineId;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public long getTeachingPointId() {
        return teachingPointId;
    }
    
    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }
    
    public void setTeachingPointId(long teachingPointId) {
        this.teachingPointId = teachingPointId;
    }

    public int getIndustryid() {
        return industryid;
    }

    public void setIndustryid(int industryid) {
        this.industryid = industryid;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public List<TimelineImageInfo> getImages() {
        return images;
    }

    public void setImages(List<TimelineImageInfo> images) {
        this.images = images;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    
    /**
     * 类的比较，通过timeline id 判断对象相等
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o instanceof Timeline) {
            Timeline other = (Timeline) o;
            if (other.getTimelineId() == this.getTimelineId())
                return true;
        }

        return false;
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObject) throws JSONException {
        userId = jsonObject.getInt("userId");
        timelineId = jsonObject.getString("timelineId");
        commentCount = jsonObject.getInt("commentCount");
        setType(jsonObject.getInt("type"));
        setTop(jsonObject.getBoolean("top"));
        setFlowerCount(jsonObject.getString("flowerCount"));
        setCommentedCount(jsonObject.getInt("commentedCount"));
        setTransferredCount(jsonObject.getInt("transferredCount"));
        setTransferCount(jsonObject.getInt("transferCount"));
        setClassId(jsonObject.getInt("classId"));
        setTeachingPointId(jsonObject.getLong("teachingPointId"));
        setReferId(jsonObject.getString("referId"));
        setIndustryid(Integer.parseInt(jsonObject.getString("industryid")));
        setSex(jsonObject.getString("sex"));
        setContent(jsonObject.getString("content"));
        setPostTime(jsonObject.getString("postTime"));
        setUsername(jsonObject.getString("username"));
        setUserType(jsonObject.getInt("userType"));
        setPhoto(jsonObject.getString("photo"));
        setAge(jsonObject.getString("age"));
        setOriginalTimelineId(jsonObject.getString("originalTimelineId"));
        List<TimelineImageInfo> imageInfos = JsonUtil.paraseJsonArray(jsonObject.getJSONArray("images"), TimelineImageInfo.class);
        setImages(imageInfos);

        // 兼容旧版本图片数据
        if (imageInfos.size() == 0) {
            parseImageInfoInOldVersion();
        }
    }

    private void parseImageInfoInOldVersion() {
        final List<String> imgUrlLst = UIHelper.parseImages(content);
        if (imgUrlLst.size() > 0) {
            images = new ArrayList<TimelineImageInfo>();
            for (String imageUrlString : imgUrlLst) {
                TimelineImageInfo timelineImageInfo = new TimelineImageInfo();
                timelineImageInfo.setUrlString(imageUrlString);
                images.add(timelineImageInfo);
            }
            content = UIHelper.filterImageContent(content);
        }
    }
}
