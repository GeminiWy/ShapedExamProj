package com.nd.shapedexamproj.activity.search;
  
/**
 * @Description:课程搜索课程资源实体类
 * @author xiezz
 * @date 2014-05-29 
 * */
public class CourseInfo { 
	private String courseId; //课程ID
	private String courseName; //课程名称
	private String credit; //学费
	private String lecturer; //讲师
	private int stuNum; //学生数
	private String img; //课程图片 

	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getCredit() {
		return credit;
	}

	public void setCredit(String credit) {
		this.credit = credit;
	}

	public String getLecturer() {
		return lecturer;
	}

	public void setLecturer(String lecturer) {
		this.lecturer = lecturer;
	}

	public int getStuNum() {
		return stuNum;
	}

	public void setStuNum(int stuNum) {
		this.stuNum = stuNum;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

    public CourseInfo (){

    }

    public CourseInfo (CourseInfo courseInfo){
        setCourseId(courseInfo.getCourseId());
        setCourseName(courseInfo.getCourseName());
        setCredit(courseInfo.getCredit());
        setLecturer(courseInfo.getLecturer());
        setStuNum(courseInfo.getStuNum());
        setImg(courseInfo.getImg());
    }
}
