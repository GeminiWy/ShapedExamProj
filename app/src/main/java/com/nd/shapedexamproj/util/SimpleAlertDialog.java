package com.nd.shapedexamproj.util;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nd.shapedexamproj.R;

/**
 * 简单的弹出窗口
 * Created by Administrator on 2015/1/20.
 */
public abstract class SimpleAlertDialog implements View.OnClickListener {

    protected TextView titleTextView;
    protected TextView contentTextView;
    protected Button positiveButton, negativeButton;
    protected AlertDialog dialog;


    public SimpleAlertDialog(Context context) {
        View view = View.inflate(context, R.layout.common_simple_dialog, null);

        titleTextView = (TextView) view.findViewById(R.id.common_simple_dialog_title_tv);
        contentTextView = (TextView) view.findViewById(R.id.common_simple_dialog_content_tv);
        positiveButton = (Button) view.findViewById(R.id.common_simple_dialog_positive_btn);
        negativeButton = (Button) view.findViewById(R.id.common_simple_dialog_negative_btn);

        negativeButton.setOnClickListener(this);
        positiveButton.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
        dialog = builder.create();
    }

    public SimpleAlertDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public SimpleAlertDialog setTitle(CharSequence title) {
        titleTextView.setText(title);
        return this;
    }

    public SimpleAlertDialog setContent(CharSequence content) {
        contentTextView.setText(content);
        return this;
    }

    public SimpleAlertDialog setPositiveButton(CharSequence text) {
        positiveButton.setText(text);
        return this;
    }

    public SimpleAlertDialog setNegativeButton(CharSequence text) {
        negativeButton.setText(text);
        return this;
    }

    public SimpleAlertDialog setNegativeButtonGone() {
        negativeButton.setVisibility(View.GONE);
        return this;
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.common_simple_dialog_negative_btn:
                onNegativeButtonClick();
                break;
            case R.id.common_simple_dialog_positive_btn:
                onPositiveButtonClick();
                break;
        }
        dialog.dismiss();
    }

    public abstract void onPositiveButtonClick();

    public void onNegativeButtonClick() {
    }
}
