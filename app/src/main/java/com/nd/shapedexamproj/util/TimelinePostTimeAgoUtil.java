/**
 * 
 */
package com.nd.shapedexamproj.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Caiyx 2014-5-12
 */
public class TimelinePostTimeAgoUtil {
	/**
	 * 
	 * @param dateTime
	 *            "年-月-日 时:分:秒"格式的时间字符串
	 * @return <=60分钟，返回xx分前<br/>
	 *         >60分钟 且 <=24小时，返回xx小时前<br/>
	 *         >24小时 且 <=48小时，返回昨天<br/>
	 *         >48小时 且 <=365天，返回月-日<br/>
	 *         >365天，返回年-月-日<br/>
     *
	 */
	public static String TimelinePostTimeAgo(String dateTime) {
	    if (StringUtils.isEmpty(dateTime)) {
	        return "";
	    }
		String interval = null;

		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date d1 = (Date) sd.parse(dateTime, pos);

		// 用现在距离1970年的时间间隔new
		// Date().getTime()减去以前的时间距离1970年的时间间隔d1.getTime()得出的就是以前的时间与现在时间的时间间隔
		long time = new Date().getTime() - d1.getTime();// 得出的时间间隔是毫秒

		if (time / 1000 < 10 && time / 1000 >= 0) {
			// 如果时间间隔小于10秒则显示“刚刚”time/10得出的时间间隔的单位是秒
			interval = "刚刚";

		} else if (time / 1000 < 60 && time / 1000 > 0) {
			// 如果时间间隔小于60秒则显示多少秒前
			int se = (int) ((time % 60000) / 1000);
			interval = se + "秒前";

		} else if (time / 60000 < 60 && time / 60000 > 0) {
			// 如果时间间隔小于60分钟则显示多少分钟前
			int m = (int) ((time % 3600000) / 60000);// 得出的时间间隔的单位是分钟
			interval = m + "分钟前";

		} else if (time / 3600000 <= 24 && time / 3600000 >= 0) {
			// 如果时间间隔小于24小时则显示多少小时前
			int h = (int) (time / 3600000);// 得出的时间间隔的单位是小时
			interval = h + "小时前";

		} else if (time / 3600000 > 24 && time / 3600000 <= 48) {
			// 如果时间间隔大于24小时且小于等于48小时
			interval = "昨天";
		} else if (time / 3600000 > 48 && time / (3600000 * 24) <= 364) {
			// 如果时间间隔大于48小时且小于等于365天
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
			interval = sdf.format(d1);
		} else {
			// 大于365天，则显示日期，不显示具体时间
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			interval = sdf.format(d1);
		}
		return interval;
	}
	
	public static void main(String[] args) {

		System.out.println(TimelinePostTimeAgoUtil.TimelinePostTimeAgo("2014-05-12 13:46"));

    }
}
