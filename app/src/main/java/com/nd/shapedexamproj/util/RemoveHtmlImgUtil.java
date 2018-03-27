package com.nd.shapedexamproj.util;

import android.text.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 移除HTML标签中的IMG获取到照片URL
 * 
 * @author Xujs
 * 
 */
public class RemoveHtmlImgUtil {

	private final String TAG = "RemoveHtmlImgUtil";
	private final static String IMAGE_TAG = "<[img|IMG]+[^>]*src=\"(http:\\/\\/[^\"]+\\.[jpg|jpeg|bmp|gif|png|JPG|JPEG|BMP|GIF|PNG]+\\b)\"[^>]*/*>";
	private final static String HTML_IMAGE_TAG = "<img src=";
	public static List<String> getImageUrl(String content) {
		ArrayList<String[]> data = new ArrayList<String[]>();
		List<String> imageUrlList = new ArrayList<String>();
		Pattern patternImage = Pattern.compile(IMAGE_TAG);

		Matcher matcher = patternImage.matcher(content);
		int pend = 0;
		while (matcher.find()) {
			String preContentTxt = content.substring(pend, matcher.start());
			String imagePath = matcher.group(1);
			pend = matcher.end();
			preContentTxt = fixText(preContentTxt);
			data.add(new String[] { imagePath,
					Html.fromHtml(preContentTxt).toString() });
		}
		if (pend < content.length() && data.size() > 0) {
			int size = data.size();
			String[] lastData = data.get(size - 1);
			String text = fixText(content.substring(pend, content.length())
					.toString());
			lastData[1] += Html.fromHtml(text);
		}

		for (String[] imagesUrlStr : data) {
			imageUrlList.add(imagesUrlStr[0]);
		}

		return imageUrlList;
	}

	protected static String fixText(String text) {
		if (text != null) {
			text = text.trim().replaceAll("\\\\+", "");
			text = text.replace("&amp;", "&");
		}
		return text;

	}

	/**
	 * 获取评论内容  去除IMG 包含内容和表情
	 * 适用于包含<img src=标题的内容
	 * @param content
	 * @return
	 */
	public static String getContentValue(String mycontent) {
		String contentValue = "";
		int countValueNum = -1;
		if(mycontent.contains(HTML_IMAGE_TAG)){
			countValueNum = mycontent.indexOf(HTML_IMAGE_TAG);
			contentValue = mycontent.substring(0, countValueNum);
			System.out.println("xxxx   countValueNum:" + countValueNum
					+ "    contentValue:" + contentValue);
		}
		return contentValue;

	}
}
