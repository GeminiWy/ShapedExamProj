package com.nd.shapedexamproj.util;

import android.content.Context;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.model.my.IndustryInfo;

/**
 * 
 * 行业
 * 
 * @author Linlg
 * editor: xuewenjian
 *         Create on 2014-4-3
 */
public class IndustryUtil {
	
	/**
	 * 行业对应的ID
	 */
	public final static int INDUSTRY_ID_OTHERS = 0, INDUSTRY_ID_IT = 1, INDUSTRY_ID_EMPLOYMENT = 2, INDUSTRY_ID_BUSINESS = 3, INDUSTRY_ID_FINANCE = 4
			, INDUSTRY_ID_CULTURE = 5, INDUSTRY_ID_ART = 6, INDUSTRY_ID_MEDICAL = 7, INDUSTRY_ID_LAW = 8, INDUSTRY_ID_EDUCATION = 9
			, INDUSTRY_ID_GOVERNMENT = 10, INDUSTRY_ID_STUDENT = 11;
	
	/**
	 * 行业名称
	 */
	public final static String INDUSTRY_OTHERS ="其他", INDUSTRY_IT = "IT", INDUSTRY_EMPLOYMENT = "工业", INDUSTRY_BUSINESS = "商业", INDUSTRY_FINANCE = "金融"
			, INDUSTRY_CULTURE = "文化", INDUSTRY_ART = "艺术", INDUSTRY_MEDICAL = "医疗", INDUSTRY_LAW = "法律", INDUSTRY_EDUCATION = "教育"
			, INDUSTRY_GOVERNMENT = "政府", INDUSTRY_STUDENT = "学生";
	
	/**
	 * 子行业名称
	 */
	public final static String INDUSTRY_SUB_OTHERS ="其他", INDUSTRY_SUB_IT = "计算机/互联网/通信", INDUSTRY_SUB_EMPLOYMENT = "生产/工艺/制造", INDUSTRY_SUB_BUSINESS = "商业/服务业/个体经营", INDUSTRY_SUB_FINANCE = "金融/银行/投资/保险"
			, INDUSTRY_SUB_CULTURE = "文化/广告/传媒", INDUSTRY_SUB_ART = "娱乐/艺术/表演", INDUSTRY_SUB_MEDICAL = "医疗/护理/制药", INDUSTRY_SUB_LAW = "律师/法务", INDUSTRY_SUB_EDUCATION = "教育/培训"
			, INDUSTRY_SUB_GOVERNMENT = "公务员/事业单位", INDUSTRY_SUB_STUDENT = "学生";
	
	/**
	 * 行业 简称
	 */
	public final static String INDUSTRY_SHORT_OTHERS = "其", INDUSTRY_SHORT_IT = "IT", INDUSTRY_SHORT_EMPLOYMENT = "工", INDUSTRY_SHORT_BUSINESS = "商", INDUSTRY_SHORT_FINANCE = "金"
			, INDUSTRY_SHORT_CULTURE = "文", INDUSTRY_SHORT_ART = "艺", INDUSTRY_SHORT_MEDICAL = "医", INDUSTRY_SHORT_LAW = "法", INDUSTRY_SHORT_EDUCATION = "教"
			, INDUSTRY_SHORT_GOVERNMENT = "政", INDUSTRY_SHORT_STUDENT = "学";
	
	
	public static String getIndustry(int id) {
		String industry = "";
		switch (id) {
		case INDUSTRY_ID_OTHERS:
			industry = getIndustry(INDUSTRY_OTHERS,INDUSTRY_SUB_OTHERS);
			break;
		case INDUSTRY_ID_IT:
			industry = getIndustry(INDUSTRY_IT,INDUSTRY_SUB_IT);
			break;
		case INDUSTRY_ID_EMPLOYMENT:
			industry = getIndustry(INDUSTRY_EMPLOYMENT,INDUSTRY_SUB_EMPLOYMENT);
			break;
		case INDUSTRY_ID_BUSINESS:
			industry = getIndustry(INDUSTRY_BUSINESS,INDUSTRY_SUB_BUSINESS);
			break;
		case INDUSTRY_ID_FINANCE:
			industry = getIndustry(INDUSTRY_FINANCE,INDUSTRY_SUB_FINANCE);
			break;
		case INDUSTRY_ID_CULTURE:
			industry = getIndustry(INDUSTRY_CULTURE,INDUSTRY_SUB_CULTURE);
			break;
		case INDUSTRY_ID_ART:
			industry = getIndustry(INDUSTRY_ART,INDUSTRY_SUB_ART);
			break;
		case INDUSTRY_ID_MEDICAL:
			industry = getIndustry(INDUSTRY_MEDICAL,INDUSTRY_SUB_MEDICAL);
			break;
		case INDUSTRY_ID_LAW:
			industry = getIndustry(INDUSTRY_LAW,INDUSTRY_SUB_LAW);
			break;
		case INDUSTRY_ID_EDUCATION:
			industry = getIndustry(INDUSTRY_EDUCATION,INDUSTRY_SUB_EDUCATION);
			break;
		case INDUSTRY_ID_GOVERNMENT:
			industry = getIndustry(INDUSTRY_GOVERNMENT,INDUSTRY_SUB_GOVERNMENT);
			break;
		case INDUSTRY_ID_STUDENT:
			industry = getIndustry(INDUSTRY_IT,INDUSTRY_SUB_STUDENT);
			break;
		default:
			break;
		}
		return industry;
	}

	private static String getIndustry(String industry,String industrySub)
	{
		return industry + "-"+ industrySub;
	}
	
	
	public static String getIndustrySub(int id) {
		String industry = "";
		switch (id) {
		case INDUSTRY_ID_OTHERS:
			industry = INDUSTRY_SUB_OTHERS;
			break;
		case INDUSTRY_ID_IT:
			industry = INDUSTRY_SUB_IT;
			break;
		case INDUSTRY_ID_EMPLOYMENT:
			industry = INDUSTRY_SUB_EMPLOYMENT;
			break;
		case INDUSTRY_ID_BUSINESS:
			industry = INDUSTRY_SUB_BUSINESS;
			break;
		case INDUSTRY_ID_FINANCE:
			industry = INDUSTRY_SUB_FINANCE;
			break;
		case INDUSTRY_ID_CULTURE:
			industry = INDUSTRY_SUB_CULTURE;
			break;
		case INDUSTRY_ID_ART:
			industry = INDUSTRY_SUB_ART;
			break;
		case INDUSTRY_ID_MEDICAL:
			industry = INDUSTRY_SUB_MEDICAL;
			break;
		case INDUSTRY_ID_LAW:
			industry = INDUSTRY_SUB_LAW;
			break;
		case INDUSTRY_ID_EDUCATION:
			industry = INDUSTRY_SUB_EDUCATION;
			break;
		case INDUSTRY_ID_GOVERNMENT:
			industry = INDUSTRY_SUB_GOVERNMENT;
			break;
		case INDUSTRY_ID_STUDENT:
			industry = INDUSTRY_SUB_STUDENT;
			break;
		default:
			break;
		}
		return industry;
	}
	
	
	/**
	 * 工具方法
	 * 由industryId提取行业简写及颜色值
	 * 
	 * @author Lincj
	 * 
	 *         Create on 2014-5-23
	 */
	public static IndustryInfo getIndustryInfo(Context context, int industryId) {

		IndustryInfo industryInfo = new IndustryInfo();//包含行业简写及颜色值
		industryInfo.setIndustryId(industryId);
		switch (industryId) {
		case INDUSTRY_ID_OTHERS:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_OTHERS);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_other));

			break;
		case INDUSTRY_ID_IT:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_IT);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_it));

			break;
		case INDUSTRY_ID_EMPLOYMENT:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_EMPLOYMENT);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_gong));

			break;
		case INDUSTRY_ID_BUSINESS:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_BUSINESS);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_shang));

			break;
		case INDUSTRY_ID_FINANCE:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_FINANCE);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_jin));

			break;
		case INDUSTRY_ID_CULTURE:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_CULTURE);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_wen));

			break;
		case INDUSTRY_ID_ART:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_ART);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_yishu));

			break;
		case INDUSTRY_ID_MEDICAL:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_MEDICAL);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_doctor));

			break;
		case INDUSTRY_ID_LAW:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_LAW);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_fa));

			break;
		case INDUSTRY_ID_EDUCATION:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_EDUCATION);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_jiao));

			break;
		case INDUSTRY_ID_GOVERNMENT:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_GOVERNMENT);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_zheng));

			break;
		case INDUSTRY_ID_STUDENT:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_STUDENT);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_xue));

			break;

		default:
			industryInfo.setAbbreviationIndustry(INDUSTRY_SHORT_OTHERS);
			industryInfo.setColor(context.getResources().getColor(
					R.color.industry_other));
			break;

		}
		return industryInfo;
	}

}
