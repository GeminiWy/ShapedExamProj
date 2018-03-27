package com.nd.shapedexamproj.view.major;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.nd.shapedexamproj.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 专业详情-专业规则
 * @author wyl
 * create in 2014.03.18
 */
public class MajorRuleList extends RelativeLayout{
	
	private Context context;
	private ListView listView;
	private JSONArray ruleJsonArray;
	private View view;
	
	public MajorRuleList(Context context,JSONArray ruleJsonArray) {
		super(context);
		this.context = context;
		this.ruleJsonArray = ruleJsonArray;
		initComponent();
	}

	private void initComponent(){
		view = LayoutInflater.from(context).inflate(R.layout.major_rule_list, this);
		listView = (ListView)findViewById(R.id.lv_major_list);

		try {
			List<HashMap<String,Object>> ruleList = new ArrayList<HashMap<String,Object>>();
			HashMap<String,Object> ruleItemMap = new HashMap<String,Object>();
			ruleItemMap.put("name", getResources().getString(R.string.major_rule_name));
			ruleItemMap.put("graduate_credit", getResources().getString(R.string.major_rule_graduate_credit));
			ruleItemMap.put("educational_credit", getResources().getString(R.string.major_rule_educational_credit));
			ruleList.add(ruleItemMap);
			
			int size = ruleJsonArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject ruleItemJson = ruleJsonArray.getJSONObject(i);
				
				ruleItemMap = new HashMap<String,Object>();
				ruleItemMap.put("name", ruleItemJson.get("name"));
				ruleItemMap.put("graduate_credit", ruleItemJson.getJSONArray("credit").get(0));
				ruleItemMap.put("educational_credit", ruleItemJson.getJSONArray("credit").get(1));
				
				ruleList.add(ruleItemMap);
			}
			//创建SimpleAdapter适配器将数据绑定到item显示控件上  
			SimpleAdapter adapter = new SimpleAdapter (context, ruleList, R.layout.major_rule_item,
					new String[]{"name", "graduate_credit", "educational_credit"}, new int[]{R.id.name, R.id.graduate_credit, R.id.educational_credit});  
	        //实现列表的显示  
		    listView.setAdapter(adapter);  
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
