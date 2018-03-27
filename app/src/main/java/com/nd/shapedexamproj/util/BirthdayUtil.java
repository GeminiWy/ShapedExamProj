package com.nd.shapedexamproj.util;

/**
 * 生日计算工具
 * 
 * @author wyl
 * @date 2014.04.01
 * 
 */
public class BirthdayUtil {
	private final static int[] dayArr = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };
	private final static String[] constellationArr = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座",
			"金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };

	/**
	 * 根据身份证号生成出生日期
	 * 
	 * @param cardID
	 * @return
	 */
	public static String getBirthday(String cardID) {
		String returnDate = null;
		StringBuffer tempStr = null;
		if (cardID != null && cardID.trim().length() > 0) {
			if (cardID.trim().length() == 15) {
				tempStr = new StringBuffer(cardID.substring(6, 12));
				tempStr.insert(4, '-');
				tempStr.insert(2, '-');
				tempStr.insert(0, "19");
			} else if (cardID.trim().length() == 18) {
				tempStr = new StringBuffer(cardID.substring(6, 14));
				tempStr.insert(6, '-');
				tempStr.insert(4, '-');
			}
		}
		if (tempStr != null && tempStr.toString().trim().length() > 0) {
			try {
				returnDate = tempStr.toString();
			} catch (Exception e) {
				System.out.println("输入的身份证错误，不能转换为相应的出生日期");
			}
		}
		return returnDate;
	}

	/**
	 * 根据生日计算星座
	 * 
	 * @param birthday
	 * @return
	 */
	public static String getConstellation(String birthday) {
		String constellation = "";
		try {
			int month = Integer.valueOf(birthday.substring(birthday.indexOf("-") + 1,
					birthday.lastIndexOf("-")));
			int day = Integer.valueOf(birthday.substring(birthday.lastIndexOf("-") + 1,
					birthday.length()));
			constellation = day < dayArr[month - 1] ? constellationArr[month - 1]
					: constellationArr[month];
		} catch (Exception e) {
			// TODO: handle exception
		}
		return constellation;
	}
}
