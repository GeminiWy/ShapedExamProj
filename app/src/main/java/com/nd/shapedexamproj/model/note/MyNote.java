package com.nd.shapedexamproj.model.note;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的笔记model
 * 
 * @author Linlg
 * 
 */
public class MyNote implements Serializable {
	public String courseName = "";
	public String courseId = "0";
	/**
	 * 课件名称
	 */
	public String coursewareName = "";
	/**
	 * 视频播放到的时间点(秒)
	 */
	public String videoPositionTime ;
	/**
	 * 章节id（课件id）
	 */
	public String chapterId = "0";
	/**
	 * 笔记id
	 */
	public String noteId = "0";
	/**
	 * 笔记标题
	 */
	public String noteName = "";
	/**
	 * 笔记的添加时间
	 */
	public String noteAddTime = "";
	public String noteContent = "";
	public static List<String> types = new ArrayList<String>();
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MyNote) ) {
			return false ;
		} 
		MyNote myNote = (MyNote) o;
		if (myNote.noteId.equals(this.noteId)) {
			return true ;
		} else {
			return false ;
		}
 		
	}
	
	/*public static List<List<MyNote>> itemJSONParsing(String result) {
		MyNote.types = new ArrayList<String>();
		List<List<MyNote>> allNotes = new ArrayList<List<MyNote>>();
		try {
			JSONArray list = new JSONObject(result).getJSONObject("res")
					.getJSONObject("data").getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				MyNote.types.add(list.getJSONObject(i).getString("course_name"));
				
				List<MyNote> notes = new ArrayList<MyNote>();
				JSONArray array = list.getJSONObject(i).getJSONArray("notes_list");
				for (int j = 0; j < array.length(); j++) {
					MyNote note = new MyNote();
					note.courseware = array.getJSONObject(j).getString("courseware_name");
					note.videoPosition = array.getJSONObject(j).getLong("lecture_video_time");
					note.noteContent = array.getJSONObject(j).getString("lecture_note");
					note.videoId = array.getJSONObject(j).getString("lecture_chapter_id");
					notes.add(note);
				}
				allNotes.add(notes);
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return allNotes;
	}*/
}
