package com.nd.shapedexamproj.model.xkhomework;

import android.widget.Toast;

import com.nd.shapedexamproj.App;
import com.tming.common.util.AESencrypt;
import com.tming.common.util.Helper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * 热身考试（接口没有id，需要从外部设置id）
 * Created by zll on 2015/3/19.
 */
public class XKWarmup extends XKHomework {

    @Override
    public void initWithJsonObject(JSONObject jsonObj) throws JSONException {
        if (jsonObj.getInt("_c") != 0) {
            final String errorMsg = jsonObj.getString("_m");
            App.getAppHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(App.getAppContext(),errorMsg,Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        JSONArray jsonArray = jsonObj.getJSONArray("list");
        int len = jsonArray.length();
        for (int i = 0; i < len; i ++) {
            JSONObject subjectJsonObj = jsonArray.getJSONObject(i);
            String serviceType =subjectJsonObj.getString("kind");
            String typeName = subjectJsonObj.getString("name");
            int typeNum = subjectJsonObj.getInt("num");
            int typeTotalScore = subjectJsonObj.getInt("score");
            JSONArray rowsArr = subjectJsonObj.getJSONArray("rows");
            for (int j = 0; j < rowsArr.length(); j ++) {
                JSONObject rowsObj = rowsArr.getJSONObject(j);
                XKSubject subject = XKSubject.newSubjectWithWorkType(serviceType);
                subject.initWithJsonObject(rowsObj);
                subjects.add(subject);
            }

        }
        hasSort = false;
    }

    /**
     * 读取作业信息
     *
     * @param homeworkId
     * @return
     */
    public static XKWarmup readHomework(String homeworkId) {
        InputStream is = null;
        try {
            XKWarmup homework = new XKWarmup();
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

                XKSubject subject = XKSubject.newSubject(type);
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

}
