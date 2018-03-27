/**
 * 
 */
package com.nd.shapedexamproj.util;

/**
 * @ClassName: DateUtil
 * @author Abay Zhuang
 * @date 2013-3-11 下午6:05:05
 * 
 */

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
	private static final String TAG = "DateUtils";

	/**
	 * <p> 距离今天的绝对时间 </p>
	 * 
	 * @param time
	 * @return
	 */
	public static String toToday(long time) {

		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone(Constants.APP_TIMEZONE));
		calendar.setTimeInMillis(time);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		Calendar calendarCurr = Calendar.getInstance(TimeZone
				.getTimeZone(Constants.APP_TIMEZONE));
		int yearCurr = calendarCurr.get(Calendar.YEAR);
		int monthCurr = calendarCurr.get(Calendar.MONTH) + 1;
		int dayCurr = calendarCurr.get(Calendar.DAY_OF_YEAR);
		int hourCurr = calendarCurr.get(Calendar.HOUR_OF_DAY);
		int minuteCurr = calendarCurr.get(Calendar.MINUTE);

		Log.e(TAG,
				"getTime:"
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ")
								.format(time));
		Log.e(TAG,
				"currentTime:"
						+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ")
								.format(calendarCurr.getTime()));

		// 一天以内的部分
		String firstDayPart = "";
		if (hour >= 0 && hour <= 12) {
			firstDayPart = "早上";
		} else if (hour > 12 && hour < 18) {
			firstDayPart = "下午";
		} else {
			firstDayPart = "晚上";
		}
		firstDayPart += new SimpleDateFormat("HH:mm").format(time);

		/*
		 * 一天以内的部分 昨天 上午XX:XX ?月？日 上午XX:XX ?年?月？日 上午XX:XX
		 */

		String secondDayPart = "";

		if (yearCurr - year != 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 ");
			secondDayPart = sdf.format(time);
		} else {
			if (dayCurr - day > 1) {
				SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 ");
				secondDayPart = sdf.format(time);
			} else if (dayCurr - day == 1) {
				secondDayPart = "昨天 ";
			}

		}

		return secondDayPart + firstDayPart;

	}

	public static long getCurrentTime() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone(Constants.APP_TIMEZONE));
		return calendar.getTimeInMillis();
	}

	public static Date getCurrentDate() {
		Calendar calendar = Calendar.getInstance(TimeZone
				.getTimeZone(Constants.APP_TIMEZONE));
		return calendar.getTime();
	}

	public static String getDate() {
		String dateS = "";

		SimpleDateFormat dateformat1 = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		dateS = dateformat1.format(DateUtils.getCurrentTime());
		return dateS;
	}
	
	public static String getDateWithTime(long timeStamp) {
	    String dateS = "";

        SimpleDateFormat dateformat1 = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        dateS = dateformat1.format(timeStamp);
        return dateS;
	}
	
	public static long getTime(String dateStr) {
		long time = 0;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = format.parse(dateStr);
			time = date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return time;
	}
	
	/**
	 * 将秒数转换为时间
	 * @param seconds
	 * @param format yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String getDate(String seconds, String format) {
		String dateS = "";
		long secondsLong = 0l;
		if("".equals(format)){
			format = "yyyy-MM-dd HH:mm:ss";
		}
		
		if(!StringUtils.isEmpty(seconds)){
			secondsLong = Long.valueOf(seconds);
			SimpleDateFormat dateformat1 = new SimpleDateFormat(format);
			dateS = dateformat1.format(secondsLong*1000);
		}

		return dateS;
	}
	
	public static String getDate(long timeStamp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new Date(timeStamp));
		return date;
	}


    /**
     * 根据传入的秒数返回小时数+分钟数+秒数
     * @param seconds
     * @return
     */
    public static String getHMSStr(int seconds){
        String str="";
        int hours=seconds/3600;
        int min=(seconds-hours*3600)/60;
        int sec=seconds-hours*3600-min*60;
        str+=((hours>0)?(((hours<10)?"0"+hours:hours)+":"):"")+((min<10)?"0"+min:min)+":"+((sec<10)?"0"+sec:sec);
        return str;
    }
    /**
     * 根据毫秒数，返回0:0:00格式的时间
     * @param milliseconds
     * @return
     */
    public static String getTimeString(int milliseconds)
    {
        milliseconds/=1000;
        int hours=milliseconds/3600;
        int minutes=(milliseconds-hours*3600)/60;
        int seconds=milliseconds-hours*3600-minutes*60;
        if(hours!=0)
        {
            return String.format(Locale.CHINA,"%d:%d:%02d", hours,minutes,seconds);
        }
        return String.format(Locale.CHINA,"%d:%02d",minutes,seconds);
        
        
    }
}
