package com.nd.shapedexamproj;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;

/**
 * 
 * 权限管理, 判读
 * 单例模式
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-22
 */
public class AuthorityManager {

	private static AuthorityManager intance;
	private AlertDialog dialog;
	
	public static AuthorityManager getInstance(){
		if (intance == null)
			intance = new AuthorityManager();
		return intance;
	}

	private AuthorityManager(){
	}
	
	/** 
	 * 判断是否学生权利
	 * @return
	 */
	public boolean isStudentAuthority(){
		return App.getUserType() == Constants.USER_TYPE_STUDENT;
	}
	
	/**
	 * 判断是否教师权利
	 * @return
	 */
	public boolean isTeacherAuthority(){
		return App.getUserType() == Constants.USER_TYPE_TEACHER;
	}
	
	
	/**
	 * 判断是否游客
	 * @return
	 */
	public boolean isInnerAuthority(){
		return App.getUserType() == Constants.USER_TYPE_INNER;
	}
	
    public void showInnerDialog(final Context context) {
        Builder builder = new Builder(context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.inner_playground_dialog_layout, null);
        // 初始化Dialog中的控件
        Button negative = (Button) view.findViewById(R.id.negative_button);// 评论
        Button positive = (Button) view.findViewById(R.id.positive_button);// 查看个人信息
        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        negative.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        positive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UIHelper.showLogin(context);
                dialog.dismiss();
            }
        });

    }
		
}
