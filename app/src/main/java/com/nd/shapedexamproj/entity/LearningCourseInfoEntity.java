package com.nd.shapedexamproj.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p> 在修课程实体类 </p>
 * <p> Created by xuwenzhuo  on 2014/10/24.</p>
 */
public class LearningCourseInfoEntity {

    public String mName;
    public String mTimes;
    public String mCredit;
    public String mCourseId;
    public double mPercent;
    public String mPhoto;
    public PlayRecord mPlayRecord;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LearningCourseInfoEntity)) {
            return false;
        }
        LearningCourseInfoEntity coursing = (LearningCourseInfoEntity) o;
        if (coursing.mCourseId.equals(this.mCourseId)) {
            return true;
        } else {
            return false;
        }
    }

    public void setPlayRecord(JSONObject jsonObj){
        mPlayRecord=new PlayRecord(jsonObj);
    }

    public static class PlayRecord{
        public String mChapterId="";
        public String mChapterName="";
        public String mLesson="";
        public String mTime="0";

       public PlayRecord(JSONObject jsonObj){
            try{
                if(null==jsonObj){
                    mChapterId="";
                    mChapterName="未开始";
                    mLesson="";
                    mTime="0";

                } else{
                    mChapterId=jsonObj.getString("chapter_id");
                    mChapterName=jsonObj.getString("chapter_name");
                    mLesson=jsonObj.getString("lesson");
                    mTime=jsonObj.getString("time");
                }

            } catch (JSONException exception){

            }
       }
    }
}
