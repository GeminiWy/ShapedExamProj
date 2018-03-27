package com.nd.shapedexamproj.model.playground;

import android.util.Log;

import com.nd.shapedexamproj.util.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommentLastTime implements Serializable {

	private static final String TAG = "CommentLastTime";
	private int targetId;
	private String operateTime;// 最后时间
	private int userId;// 用户ID
	private int targetUserId;
	private int operateType;

	public static List<CommentLastTime> commentLastTimeJSONPasing(String result) {
		Log.d(TAG, result);
		List<CommentLastTime> commentLastTimeList = new ArrayList<CommentLastTime>();
//		commentLastTimeList.clear();

		try {
			JSONObject commentLastTimeObj = new JSONObject(result);
			
			int flag = commentLastTimeObj.getInt("flag");
			if (Constants.SUCCESS_MSG == flag) {
				if(!commentLastTimeObj.isNull("data")){
					JSONArray dataAry = commentLastTimeObj.getJSONArray("data");
					if (null != dataAry && dataAry.length() > 0) {
						for (int i = 0; i < dataAry.length(); i++) {
							CommentLastTime commentLastTime = new CommentLastTime();
							JSONObject item = dataAry.getJSONObject(i);
							commentLastTime.setTargetId(item.getInt("targetId"));
							commentLastTime.setOperateTime(item
									.getString("operateTime"));
							commentLastTime.setUserId(item.getInt("userId"));
							commentLastTime.setTargetUserId(item
									.getInt("targetUserId"));
							commentLastTime.setOperateType(item
									.getInt("operateType"));
							commentLastTimeList.add(commentLastTime);

						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return commentLastTimeList;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public String getOperateTime() {
		return operateTime;
	}

	public void setOperateTime(String operateTime) {
		this.operateTime = operateTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(int targetUserId) {
		this.targetUserId = targetUserId;
	}

	public int getOperateType() {
		return operateType;
	}

	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}

}
