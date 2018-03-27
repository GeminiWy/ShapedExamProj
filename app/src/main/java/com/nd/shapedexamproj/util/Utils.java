package com.nd.shapedexamproj.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.EditText;

import com.nd.shapedexamproj.App;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Utils {

    private static final String TAG = "Utils";

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getUrlFileName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        return fileName;
    }

    /**
     * 获取当前学期
     * 
     * @return
     */
    public static String getCurrentTerm() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String str = "";
        if (month >= 3 && month <= 8) {
            str = "春季";
        } else {
            str = "秋季";
        }
        return "" + year + "年" + str;
    }

    /**
     * 打开浏览器
     * 
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            // ToastMessage(context, "无法浏览此网页", 500);
        }
    }
    
    // Get file name portion of URL.
    public static String convertUrlToPath(String url) {
        if (App.createDownloadPath() == null) {
            return null;
        }
        Log.e(TAG, App.createDownloadPath() + Helper.getMD5String(url.toString()));
        return App.createDownloadPath() + Helper.getMD5String(url.toString()) ;
    }
    
    public static int jsonParsing(String result) {
        Log.d(TAG, result);
        int flag = 0;
        try {
            JSONObject jobj = new JSONObject(result);
            flag = jobj.getInt("flag");
            if (flag != 1 && !jobj.isNull("msg")) {
                String msg = jobj.getString("msg");
            }
            Log.d(TAG, "=======");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return flag;
    }

    public static void addEditViewMaxLengthListener(final Context context, final EditText editText
            , final int maxLength) {
        addEditViewMaxLengthListener(context, editText, maxLength, "");
    }
    
    public static void addEditViewMaxLengthListener(final Context context, final EditText editText
            , final int maxLength, String errorStr) {
        class ErrorLengthFilter extends InputFilter.LengthFilter {

            protected String mErrorStr;
            protected int mMax;
            
            public ErrorLengthFilter(int max, String errorStr) {
                super(max);
                mMax = max;
                this.mErrorStr = errorStr;
            }
            
        }
        InputFilter maxLengthFilter = new ErrorLengthFilter(maxLength, errorStr) {
            
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                int keep = mMax - (dest.length() - (dend - dstart));

                if (keep >= end - start) {
                    return null;
                } else {
                    if (!StringUtils.isEmpty(mErrorStr)) {
                        UIHelper.ToastMessage(context, mErrorStr);
                    }
                    return "";
                }
            
            }
        };
        
        
        editText.setFilters(new InputFilter[] {maxLengthFilter});
    }

    public static String FormetFileSize(long fileS) {// 转换文件大小
        DecimalFormat df = new DecimalFormat("##.00");
        String fileSizeString = "";
        if (fileS <= 0) {// 因为缓存可能在后台下载，所以要<
            fileSizeString = "0.00B";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static boolean uploadFile(File file, String RequestURL, String name) {
        int time_out = 120 * 1000;
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String PREFIX = "--", LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(time_out);
            conn.setConnectTimeout(time_out);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"ringname\"" + LINE_END);
                sb.append(LINE_END);
                sb.append(name + LINE_END);
                sb.append(PREFIX + BOUNDARY + LINE_END);
                sb.append("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getParent()
                        + "\"" + LINE_END);
                sb.append("Content-Type: application/octet-stream; charset=UTF-8" + LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /**
                 * 获取响应码 200=成功 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                result = sb1.toString();
                int flag = 0;
                try {
                    flag = new JSONObject(result).getInt("flag");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (flag != 0) {
                    return true;
                }
            }
            return false;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * <p>获取系统信息，提供给友盟统计使用</P>
     *
     * @param context
     * @return
     */
    public static String getDeviceInfo(Context context) {
        try{
          JSONObject json = new JSONObject();
          android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
              .getSystemService(Context.TELEPHONY_SERVICE);
      
          String device_id = tm.getDeviceId();
          
          android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
              
          String mac = wifi.getConnectionInfo().getMacAddress();
          json.put("mac", mac);
          
          if( TextUtils.isEmpty(device_id) ){
            device_id = mac;
          }
          
          if( TextUtils.isEmpty(device_id) ){
            device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
          }
          
          json.put("device_id", device_id);
          
          return json.toString();
        }catch(Exception e){
          e.printStackTrace();
        }
      return null;
    }
    
    public static String encodeGetUrl(String url, Map<String, Object> params) {
        return url + "?" + encodeParameters(params);
    }
    
    /**
     * 将请求参数拼接，并对Value进行URLEncoder
     * @param httpParams 请求参数key-value
     * @return 拼接完成的参数字符串
     */
    public static String encodeParameters(Map<String, Object> httpParams) {
        if (httpParams == null || httpParams.size() == 0) return null;
        StringBuffer buf = new StringBuffer();
        Set<String> keys = httpParams.keySet();
        for (String key : keys) {
            try {
                buf.append(key).append("=").append(URLEncoder.encode(String.valueOf(httpParams.get(key)), "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException neverHappen) {}
        }
        return buf.substring(0, buf.length() - 1);
    }

    /**
     * 分数浮点转换为字符串
     * @param f
     * @return
     */
    public static String scoreFloat2String(float f) {
        if (f % 1 == 0) {
            return String.format("%d", (int) f);
        } else {
            return String.format("%.1f", f);
        }
    }

    private static final String[] chineseNumber = {"零", "一","二","三","四","五","六","七","八","九","十"};

    public static String numberToChinese(int i) {
        if (i == 0) {
            return chineseNumber[0];
        }
        StringBuffer res = new StringBuffer();
        if (i / 10 > 1) {
            res.append(chineseNumber[i / 10]);
        }
        if (i > 9) {
            res.append(chineseNumber[10]);
        }
        if (i % 10 > 0) {
            res.append(chineseNumber[i % 10]);
        }
        return res.toString();
    }
}
