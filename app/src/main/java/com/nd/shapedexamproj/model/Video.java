package com.nd.shapedexamproj.model;
/**
 * 视频信息类
 * @author zll
 * create in 2014-3-14
 */
public class Video {
    private final  int DOWNLOADING = 1;
    private final int DOWNLOADED = 0;
    private final int NOT_DOWNLOADED = 2;
    private final int DOWNLOAD_WAITING = 3;
    
	public String video_id = "0";		//课件id
	public String user_id = "0";
	public String video_name ;
	
	public String desc ;
	/**
	 * 视频播放地址
	 */
	public String video_url ;
	/**
	 * 视频播放进度,百分比
	 */
	public double percent = 0;
	/**
	 * 本地路径(没有文件后缀名)
	 */
	public String video_path;
	/**
	 * 所属的课程id
	 */
	public String course_id = "0";
	/**
	 * 所属的课程名
	 */
	public String course_name;
	/**
	 * 下载状态
	 */
	public int downloadState = NOT_DOWNLOADED;
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Video)){
			return true ;
		}
		Video video = (Video) o;
		if (video.video_id.equals(this.video_id)){
			return true;
		} else {
			return false;
		}
		
	}
	
}
