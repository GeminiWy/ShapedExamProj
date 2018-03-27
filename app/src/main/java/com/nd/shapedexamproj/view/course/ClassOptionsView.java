package com.nd.shapedexamproj.view.course;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.homework.OptionsView;

/**
 * Created by yusongying on 2015/3/19.
 */
public class ClassOptionsView extends OptionsView {

    public ClassOptionsView(Context context) {
        super(context);
        setEnabled(true);
    }

    public ClassOptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEnabled(true);
    }

    public View[] createShowChoiceView(String[] choices) {
        removeAllViews();
        if (choices.length == 0) {
            return new View[]{};
        }
        View views[] = new View[choices.length];
        int i = 0;
        for (String choice : choices) {
            View choiceItem = View.inflate(getContext(), R.layout.select_couse_class_item, null);
            TextView textView = (TextView) choiceItem.findViewById(R.id.select_couse_class_item_tv);
            textView.setText(choice);
            choiceItem.setOnClickListener(this);
            choiceItem.setTag(choice);
            addView(choiceItem);
            views[i++] = choiceItem;
        }
        return views;
    }
}
