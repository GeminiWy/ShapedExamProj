package com.nd.shapedexamproj.model.note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoteInfo {
	public String noteId;
	public long noteTime;
	public long totalTime;
	public String noteUrl;

	public static List<NoteInfo> JSONParsing(String result) {
		List<NoteInfo> noteInfos = new ArrayList<NoteInfo>();
		try {
			JSONArray list = new JSONObject(result).getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				NoteInfo noteInfo = new NoteInfo();
				noteInfo.totalTime = list.getJSONObject(i).getLong("audiotime");
				noteInfo.noteTime = list.getJSONObject(i).getLong("anchortime");
				noteInfo.noteUrl = list.getJSONObject(i).getString("noteurl");
				noteInfos.add(noteInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return noteInfos;
	}
}
