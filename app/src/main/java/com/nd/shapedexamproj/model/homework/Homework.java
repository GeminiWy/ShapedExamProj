package com.nd.shapedexamproj.model.homework;

import android.text.Html;
import android.text.Spanned;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.util.AESencrypt;
import com.tming.common.util.FileUtil;
import com.tming.common.util.Helper;
import com.tming.common.util.PhoneUtil;
import com.tming.openuniversity.model.IJsonInitable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yusongying on 2015/1/20.
 */
public class Homework implements IJsonInitable {

    /**
     * 加密秘钥
     */
    private static final String AES_SEED = "homework";

    /**
     * 用户答题数据文件后缀名
     */
    private static final String FILE_SUFFIX_USER_ANSWER = ".a";

    /**
     * 题目保存文件后缀名
     */
    private static final String FILE_SUFFIX_SUBJECTS = ".s";

    /**
     * 各类型题目的分数总和
     */
    private HashMap<Integer, Float> mSubjectTypeScore;

    /**
     * 各类型题目的数量
     */
    private HashMap<Integer, Integer> mSubjectTypeCount;

    /**
     * 各类型的题目
     */
    private List<Integer> mSubjectTypes;

    /**
     * 题目列表
     */
    protected List<Subject> subjects;

    /**
     * 是否已经排序
     */
    protected boolean hasSort = false;

    private long costTimeMillis = 0;

    /**
     * 作业限制时间，单位秒
     */
    private long limitTime;
    /**
     * 上次检查时间是否用尽的时间
     */
    private long lastCheckTimeMillis;

    /**
     * 题目数量
     */
    private int subjectsSize;

    /**
     * 作业ID
     */
    private String homeworkId;

    public Homework() {
        subjects = new ArrayList<Subject>();
    }

    public void setHomeworkId(String homeworkId) {
        this.homeworkId = homeworkId;
    }

    public String getHomeworkId() {
        return homeworkId;
    }

    private static File getSaveDir() {
        String sdcardPath = PhoneUtil.getSDPath();
        if (sdcardPath != null) {
            return new File(sdcardPath + "/data/" + App.getAppContext().getPackageName() + "/homework/");
        } else {
            try {
                return FileUtil.getTempDir();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*
    来电精灵 （仅支持电信天翼用户）
        项目简介:通过状态彩铃告知来电者状态的软件，状态彩铃随意设，好友状态及时查，用彩铃直播你我的心情，跟朋友分享你我的状态
        平台：Android
        开发工具：Eclipse Java
        主要技术：网络请求、图片加载、文件下载、音乐播放、自定义控件、XML/JSON
        职责：负责客户端所有功能开发

    Get5 Store 海外手机应用市场
        项目简介:海量高质量App、游戏和主题下载市场
        平台：Android
        开发工具：Eclipse Java
        主要技术：网络请求、图片加载、文件下载、Service
        职责：负责App详情页面和下载模块的设计和开发

    GameLife 海外游戏资讯阅读器
        项目简介：捕捉游戏新闻热点，分享精彩内容。可订阅多个游戏门户网站新闻。并支持离线阅读
        平台：Android、iOS
        开发工具：Eclipse Java
        主要技术：网络请求、图片加载、文件下载、图文展示、Sqlite、widget组件
        职责：负责Android端项目整体结构设计，新闻详情页面和离线阅读开发

    CMS
        项目简介：承载公司所有资讯类站点的后台管理
        平台：Web
        开发工具：Eclipse Java
        主要技术：Servlets、Hibernate、Struts2、Oracle、MySql、HTML/XML/Javascript/Css的编写及Jquery、ajax等等Web相关技术
        职责：负责新闻发布模块优化、内容推送分享、新闻图片编辑器、服务端文件管理系统、关键词系统等

    开放大学
        项目简介：为学员提供一个集学习、作业、答疑、即时通讯、类微博展示等功能于一体的手机应用
        平台：Web、Android、iOS
        开发工具：IntelliJ IDEA 14.0.3，Java
        主要技术：xmpp、sqlite、Volley、Gson、Vitamio
        职责：作为开放大学产品移动端负责人，负责移动端代码开发架构、设计和缺陷重构。

     */
    /**
     * 保存用户作答信息
     */
    public void saveUserAnswer() {
        OutputStream os = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("costtime", costTimeMillis);
            for (Subject subject : subjects) {
                if (subject.hasDone()) {
                    jsonObject.put(subject.getSubjectId(), subject.getUserAnswerData());
                }
            }
            String saveString = AESencrypt.encrypt(AES_SEED, jsonObject.toString());

            File file = new File(getSaveDir(), Helper.getMD5String(homeworkId + "_" + App.getUserId()) + FILE_SUFFIX_USER_ANSWER);
            FileUtil.makeParentDirIfNeed(file);

            os = new GZIPOutputStream(new FileOutputStream(file));
            os.write(saveString.getBytes("UTF-8"));
            os.flush();
            os.close();
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 保存作业题目信息
     */
    public void saveHomework() {
        OutputStream os = null;
        try {
            JSONArray jsonArray = new JSONArray();
            for (Subject subject : subjects) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", subject.getType());
                jsonObject.put("data", subject.toJsonObject());
                jsonArray.put(jsonObject);
            }
            String saveString = AESencrypt.encrypt(AES_SEED, jsonArray.toString());

            File file = new File(getSaveDir(), Helper.getMD5String(homeworkId) + FILE_SUFFIX_SUBJECTS);
            FileUtil.makeParentDirIfNeed(file);

            os = new GZIPOutputStream(new FileOutputStream(file));
            os.write(saveString.getBytes("UTF-8"));
            os.flush();
            os.close();
            os = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取用户作答信息
     */
    public void readUserAnswer() {
        InputStream is = null;
        try {
            File file = new File(getSaveDir(), Helper.getMD5String(homeworkId + "_" + App.getUserId()) + FILE_SUFFIX_USER_ANSWER);

            // 没有作业
            if (!file.exists()) {
                return;
            }

            is = new GZIPInputStream(new FileInputStream(file));
            ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                byteArrayOs.write(buf, 0, len);
            }
            is.close();
            is = null;

            String aesString = new String(byteArrayOs.toByteArray(), "UTF-8");
            JSONObject jsonObject = new JSONObject(AESencrypt.decrypt(AES_SEED, aesString));

            costTimeMillis = jsonObject.getLong("costtime");
            for (Subject subject : subjects) {
                if (jsonObject.has(subject.getSubjectId())) {
                    String userAnswerData = jsonObject.getString(subject.getSubjectId());
                    subject.setUserAnswerData(userAnswerData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断用户是否已经作答,并未提交
     *
     * @param homeworkId
     * @return
     */
    public static boolean checkUserHasDoHomework(String homeworkId) {
        File file = new File(getSaveDir(), Helper.getMD5String(homeworkId + "_" + App.getUserId()) + FILE_SUFFIX_USER_ANSWER);
        return file.exists();
    }

    /**
     * 清除用户作答信息
     */
    public static void clearUserAnswer(String homeworkId) {
        File file = new File(getSaveDir(), Helper.getMD5String(homeworkId + "_" + App.getUserId()) + FILE_SUFFIX_USER_ANSWER);
        file.delete();
    }

    /**
     * 读取作业信息
     *
     * @param homeworkId
     * @return
     */
    public static Homework readHomework(String homeworkId) {
        InputStream is = null;
        try {
            Homework homework = new Homework();
            homework.homeworkId = homeworkId;

            File file = new File(getSaveDir(), Helper.getMD5String(homeworkId) + FILE_SUFFIX_SUBJECTS);
            if (!file.exists()) {
                return null;
            }

            is = new GZIPInputStream(new FileInputStream(file));
            ByteArrayOutputStream byteArrayOs = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                byteArrayOs.write(buf, 0, len);
            }
            is.close();
            is = null;

            String aesString = new String(byteArrayOs.toByteArray(), "UTF-8");
            JSONArray jsonArray = new JSONArray(AESencrypt.decrypt(AES_SEED, aesString));

            int size = jsonArray.length();
            for (int i = 0; i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int type = jsonObject.getInt("type");
                JSONObject data = jsonObject.getJSONObject("data");

                Subject subject = Subject.newSubject(type);
                subject.initWithJsonObject(data);
                homework.subjects.add(subject);
            }
            return homework;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取各题目类型个数
     * @return
     */
    /*public Map<Integer, Integer> getSubjectTypeCount() {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        return mSubjectTypeCount;
    }*/
    public List<Integer> getSubjectTypes() {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        return mSubjectTypes;
    }


    public int getSubjectSizeInType(int type) {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        Integer count = mSubjectTypeCount.get(type);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 做作业显示的页数
     *
     * @return
     */
    public int getPageSize() {
        return subjects.size();
    }

    /**
     * 获取当前页需要显示的题目
     *
     * @param page
     * @return
     */
    public Subject getSubjectAtPageIndex(int page) {
        return subjects.get(page);
    }

    /**
     * 根据题目索引获取题目
     *
     * @param index
     * @return
     */
    public Subject getSubjectAt(int index) {
        int tempIndex = 0;
        for (Subject subject : subjects) {
            if (subject instanceof ComplexSubject) {
                List<Subject> subs = ((ComplexSubject) subject).getSubjects();
                if (tempIndex + subs.size() > index) {
                    return subs.get(index - tempIndex);
                } else {
                    tempIndex += subs.size();
                }
            } else {
                if (tempIndex == index) {
                    return subject;
                }
                tempIndex++;
            }
        }
        return null;
    }

    /**
     * 未做作业的第一题
     *
     * @return
     */
    public int getFirstUndoSubjectIndex() {
        for (int i = 0; i < subjects.size(); i++) {
            Subject subject = subjects.get(i);
            if (subject instanceof ComplexSubject) {
                List<Subject> subs = ((ComplexSubject) subject).getSubjects();
                for (Subject subSub : subs) {
                    if (!subSub.hasDone()) {
                        return i;
                    }
                }
            } else {
                if (!subject.hasDone()) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 题目总数，综合题包含多个题目
     *
     * @return
     */
    public int getSubjectSize() {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        return subjectsSize;
    }

    /**
     * 题目索引
     * @param subject
     * @return
     */
    public int indexOf(Subject subject) {
        int index = 0;
        for (Subject tempSub : subjects) {
            if (tempSub == subject) {
                return index;
            }
            if (tempSub.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                ComplexSubject complexSubject = (ComplexSubject) tempSub;
                for (Subject tempSub2 : complexSubject.getSubjects()) {
                    if (tempSub2 == subject) {
                        return index;
                    }
                    index++;
                }
            } else {
                index++;
            }
        }
        return 0;
    }

    /**
     * 获取显示题目页码
     *
     * @param subject
     * @return
     */
    public int getPageIndexOf(Subject subject) {
        int index = subjects.indexOf(subject);
        if (index > -1) {
            return index;
        } else {
            for (Subject subject1 : subjects) {
                if (subject1 instanceof ComplexSubject) {
                    ComplexSubject complexSubject = (ComplexSubject) subject1;
                    index = complexSubject.getSubjects().indexOf(subject);
                    if (index > -1) {
                        return subjects.indexOf(complexSubject);
                    }
                }
            }
        }
        return -1;
    }

    public void addSubject(Subject subject) {
        subjects.add(subject);
        hasSort = false;
    }

    /**
     * 时间是否用尽
     *
     * @return true 还有时间，否则时间到
     */
    public boolean isOutOfTime() {
        long currentTime = System.currentTimeMillis();
        if (lastCheckTimeMillis > 0) {
            // 计算本次消耗时间
            long usedTime = currentTime - lastCheckTimeMillis;
            if (usedTime > 0) {
                costTimeMillis += usedTime;
            }
        }
        lastCheckTimeMillis = currentTime;
        // 已经消耗完
        if (limitTime > 0 && limitTime * 1000 < costTimeMillis) {
            return true;
        }
        return false;
    }

    public long getLimitTime() {
        return limitTime;
    }

    public long getCostTimeMillis() {
        return costTimeMillis;
    }

    /**
     * 设置 计时开始时间为当前时间
     */
    public void setCheckTimeToCurrent() {
        this.lastCheckTimeMillis = System.currentTimeMillis();
    }

    public String getShowRemainingTime() {
        if (limitTime == 0) {
            return "";
        }
        long remainingTimeSecond = limitTime - costTimeMillis / 1000;
        if (remainingTimeSecond < 0) {
            remainingTimeSecond = 0;
        }
        return String.format("%02d:%02d:%02d", remainingTimeSecond / 3600, remainingTimeSecond % 3600 / 60, remainingTimeSecond % 3600 % 60);
    }

    private void sortEveryTypeSubjectScoreAndCount() {
        // 对题目进行排序
        Collections.sort(subjects);

        HashMap<Integer, Float> subjectTypeScore = new HashMap<Integer, Float>();
        HashMap<Integer, Integer> subjectTypeCount = new HashMap<Integer, Integer>();
        ArrayList<Integer> types = new ArrayList<Integer>();
        int theSubjectIndex = 0;
        int lastSubjectType = 0;
        int subjectCount = 0;
        for (Subject subject : subjects) {

            if (lastSubjectType != subject.getType()) {
                theSubjectIndex = 1;
            } else {
                theSubjectIndex++;
            }
            subject.setTheSubjectTypeIndex(theSubjectIndex);
            lastSubjectType = subject.getType();

            // 题型分数统计、题型数量统计
            Float score = subjectTypeScore.get(subject.getType());
            Integer count = subjectTypeCount.get(subject.getType());
            subjectTypeScore.put(subject.getType(), (score != null ? score.floatValue() : 0f) + subject.getScore());

            // 计算题目数量
            if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                ComplexSubject complexSubject = (ComplexSubject) subject;
                List<Subject> complexSubList = complexSubject.getSubjects();
                subjectCount += complexSubList.size();
                int type = subject.getType() * 100 + subject.getTheSubjectTypeIndex(); // 综合题类型 为600+
                subjectTypeCount.put(type, complexSubject.getSubjects().size());
                types.add(type);

                // 对综合题内部题目进行排序
                Collections.sort(complexSubList);
                int theInnerSubjectIndex = 1;
                for (Subject innerSub : complexSubList) {
                    innerSub.setTheSubjectTypeIndex(theInnerSubjectIndex++);
                }
            } else {
                if (!types.contains(subject.getType())) {
                    types.add(subject.getType());
                }
                subjectTypeCount.put(subject.getType(), (count != null ? count.intValue() : 0) + 1);
                subjectCount++;
            }
        }
        Collections.sort(types);
        mSubjectTypes = types;
        this.subjectsSize = subjectCount;
        mSubjectTypeCount = subjectTypeCount;
        mSubjectTypeScore = subjectTypeScore;
        hasSort = true;
    }

    public String getSubjectTitle(int position) {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        // 找到该题型索引
        Subject subject = subjects.get(position);
        int type = subject.getType();
        if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
            type = type * 100 + 1;
        }
        int index = mSubjectTypes.indexOf(type) + 1;

        StringBuffer titleString = new StringBuffer(Utils.numberToChinese(index) + "、");
        titleString.append(Subject.getTypeName(subject.getType()));

        Float totalScoreFloat = mSubjectTypeScore.get(subject.getType());
        Integer totalCountInteger = mSubjectTypeCount.get(subject.getType());
        float totalScore = totalScoreFloat != null ? totalScoreFloat.floatValue() : 0;
        int totalCount = totalCountInteger != null ? totalCountInteger.intValue() : 0;

        if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
            // 对综合题总数特殊处理
            for (Integer subType : mSubjectTypes) {
                if (subType / 100 == Subject.SUBJECT_TYPE_COMPLEX) {
                    Integer countInteger = mSubjectTypeCount.get(subType);
                    if (countInteger != null) {
                        totalCount += countInteger;
                    }
                    Float scoreFloat = mSubjectTypeScore.get(subType);
                    if (scoreFloat != null) {
                        totalScore += scoreFloat;
                    }
                }
            }
        }
        if (totalScore >= 0) {
            if (totalScore % 1 == 0) {
                titleString.append(String.format("(%d题,%d分)", totalCount, (int) totalScore));
            } else {
                titleString.append(String.format("(%d题,%.1f分)", totalCount, totalScore));
            }
        } else {
            titleString.append(String.format("(%d题)", totalCount));
        }
        return titleString.toString();
    }

    /**
     * 获取用户作业的答题情况
     * @return
     */
    public JSONObject getUserAnswer() {
        JSONObject userAnswerObj = new JSONObject();
        for (Subject subject : subjects) {

            String subjectId = subject.getSubjectId();
            String subjectAnswer = subject.getUserAnswerData();
            try {
                if (subject instanceof ComplexSubject) {
                    JSONObject jsonObject = new JSONObject(subjectAnswer);
                    Iterator<String> keys = jsonObject.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        userAnswerObj.put(key, jsonObject.getString(key));
                    }
                }
                userAnswerObj.put(subjectId,subjectAnswer);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return userAnswerObj;
    }

    /**
     * 获取未做过的题目数
     * @return
     */
    public int getSubjectSizeOfNotDone() {
        int hasDoneCount = 0;//已做的题目
        int subjectCount = 0;//全部的题目数
        for (Subject subject : subjects) {

            // 计算题目数量
            if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                ComplexSubject complexSubject = (ComplexSubject) subject;
                List<Subject> complexSubList = complexSubject.getSubjects();
                subjectCount += complexSubList.size();

            } else {
                subjectCount++;
            }
            if (subject.hasDone()) {
                if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                    ComplexSubject complexSubject = (ComplexSubject) subject;
                    List<Subject> subList = complexSubject.getSubjects();
                    for (Subject sub : subList) {
                        if (sub.hasDone()) {
                            hasDoneCount++;
                        }
                    }
                } else {
                    hasDoneCount++;
                }
            }
        }
        return subjectCount - hasDoneCount;
    }

    /**
     * 获取客观题分数
     * @return
     */
    public float getObjectiveScore() {
        float objectiveScore = 0;
        for (Subject subject : subjects) {
            if (subject.hasDone()) {
                if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                    ComplexSubject complexSubject = (ComplexSubject) subject;
                    List<Subject> subList = complexSubject.getSubjects();
                    for (Subject sub : subList) {
                        if (sub.isObjective() && sub.isRight()) {
                            objectiveScore += sub.getScore();
                        }
                    }
                } else {
                    if (subject.isObjective() && subject.isRight()) {
                        objectiveScore += subject.getScore();
                    }
                }
            }
        }
        return objectiveScore;
    }

    /**
     * 获取做作业的概况
     * @param needObjectiveScore
     * @return
     */
    public String getDoingProfileString(boolean needObjectiveScore) {
        if (!hasSort) {
            sortEveryTypeSubjectScoreAndCount();
        }
        int hasDoneCount = 0;
        float objectiveScore = 0;
        for (Subject subject : subjects) {
            if (subject.hasDone()) {
                if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                    ComplexSubject complexSubject = (ComplexSubject) subject;
                    List<Subject> subList = complexSubject.getSubjects();
                    for (Subject sub : subList) {
                        if (sub.hasDone()) {
                            hasDoneCount++;
                        }
                        if (needObjectiveScore && sub.isObjective() && sub.isRight()) {
                            objectiveScore += sub.getScore();
                        }
                    }
                } else {
                    hasDoneCount++;
                    if (needObjectiveScore && subject.isObjective() && subject.isRight()) {
                        objectiveScore += subject.getScore();
                    }
                }
            }
        }
        if (needObjectiveScore) {
            return String.format("总计%d题，已答%d题，未答%d题。\r\n目前客观题得分:%s分。",
                    subjectsSize, hasDoneCount, subjectsSize - hasDoneCount,
                    Utils.scoreFloat2String(objectiveScore));
        } else {
            return String.format("总计%d题，已答%d题，未答%d题",
                    subjectsSize, hasDoneCount, subjectsSize - hasDoneCount);
        }
    }

    public Spanned getDoingProfile() {
        int hasDoneCount = 0;
        for (Subject subject : subjects) {
            if (subject.hasDone()) {
                if (subject.getType() == Subject.SUBJECT_TYPE_COMPLEX) {
                    ComplexSubject complexSubject = (ComplexSubject) subject;
                    List<Subject> subList = complexSubject.getSubjects();
                    for (Subject sub : subList) {
                        if (sub.hasDone()) {
                            hasDoneCount++;
                        }
                    }
                } else {
                    hasDoneCount++;
                }
            }
        }
        return Html.fromHtml(String.format("总计 %d 题，已回答 <font color=\"#22BE8A\">%d</font> 题，未答 <font color=\"#FBA728\">%d</font> 题",
                subjectsSize, hasDoneCount, subjectsSize - hasDoneCount));
    }

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        String timeLimitString = jsonObj.getString("time_dis");
        limitTime = Integer.parseInt(timeLimitString) * 60;
        JSONArray jsonArray = jsonObj.getJSONArray("list");
        int len = jsonArray.length();
        for (int i = 0; i < len; i++) {
            JSONObject subjectJsonObj = jsonArray.getJSONObject(i);
            String serviceType =subjectJsonObj.getString("work_type");
            Subject subject = Subject.newSubjectWithWorkType(serviceType);
            subject.initWithJsonObject(subjectJsonObj);
            subjects.add(subject);
        }
        hasSort = false;
    }
}
