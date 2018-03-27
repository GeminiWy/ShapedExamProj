package com.nd.shapedexamproj.model;

import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 课件详情
 * @author Linlg
 *
 */
public class CourseDetail implements Parcelable {
	public String name;
	public String desc;
	public String url;
	/**
	 * 资源ID
	 */
	public String fs_id;
	
	/**
	 * 统计ID
	 */
	public String stat_id;
	
	/**
	 * 最后观看到的点（单位秒）
	 */
	public int last_point;
	
	public static CourseDetail JSONPasring(String result){
		CourseDetail detail = new CourseDetail();
		try {
			JSONObject object = new JSONObject(result);
			JSONObject resObj = object.getJSONObject("res");
			if (!resObj.isNull("data") ) {
				JSONObject dataObj = resObj.getJSONObject("data");
			
				detail.fs_id = dataObj.getJSONObject("info").getString("fs_id");
				if (dataObj.getJSONObject("info").getString("last_point").equals("")) {
				    detail.last_point = 0;
				} else {
				    detail.last_point = dataObj.getJSONObject("info").getInt("last_point");
				}
				detail.stat_id = dataObj.getJSONObject("info").getString("stat_id");
				detail.name = dataObj.getJSONObject("info").getString("name");
				detail.desc = dataObj.getJSONObject("info").getString("desc");
				detail.url = dataObj.getJSONObject("info").getString("url");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return detail;
	}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(url);
        dest.writeString(fs_id);
        dest.writeString(stat_id);
        dest.writeInt(last_point);
    }
	
    public static final Creator<CourseDetail> CREATOR = new Creator<CourseDetail>() {

        @Override
        public CourseDetail createFromParcel(Parcel source) {
            CourseDetail courseDetail = new CourseDetail();
            courseDetail.name = source.readString();
            courseDetail.desc = source.readString();
            courseDetail.url = source.readString();
            courseDetail.fs_id = source.readString();
            courseDetail.stat_id = source.readString();
            courseDetail.last_point = source.readInt();
            
            return courseDetail;
        }

        @Override
        public CourseDetail[] newArray(int size) {
            return new CourseDetail[size];
        }
        
    };
    
}
