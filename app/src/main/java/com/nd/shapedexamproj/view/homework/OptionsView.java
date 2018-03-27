package com.nd.shapedexamproj.view.homework;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.view.AutoImageLoadTextView;

import java.util.*;

/**
 * 选择、判断题的选项列表视图
 *
 * Created by yusngying on 2015/1/25.
 */
public class OptionsView extends LinearLayout implements View.OnClickListener {

    private int mCurrentChoiceIndex = -1;
    private int[] mMultiChoice;
    private boolean mEditable;
    private OnChoiceChangeListener mListener;
    private OnMultiChoiceChangeListener mMultiListener;
    /**
     * 是否是多选
     */
    private boolean isMultiSelect;

    public OptionsView(Context context) {
        super(context);
        setOrientation(VERTICAL);
    }

    public OptionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
    }

    public void setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
    }


    public void setShowChoiceData(Map<String, String> choices) {
        if (choices == null || choices.size() == 0) {
            return;
        }
        Set<String> keys = choices.keySet();
        List<String> keysList = new ArrayList<String>(keys);
        Collections.sort(keysList);

        String[] keysAry = new String[keys.size()];
        String[] valueAry = new String[keys.size()];
        keysList.toArray(keysAry);
        for (int i = 0; i < keysAry.length; i++) {
            valueAry[i] = choices.get(keysAry[i]);
        }

        View[] choiceViews = createShowChoiceView(valueAry);
        int i = 0;
        for (String key : keysAry) {
            TextView preText = (TextView) choiceViews[i++].findViewById(R.id.options_view_choice_item_pre_tv);
            preText.setVisibility(VISIBLE);
            preText.setText(key + ".");
        }
    }

    public void setShowChoiceData(List<String> choices) {
        if (choices == null || choices.size() == 0) {
            return;
        }
        String[] listAry = new String[choices.size()];
        choices.toArray(listAry);
        createShowChoiceView(listAry);
    }

    public View[] createShowChoiceView(String[] choices) {
        removeAllViews();
        if (choices.length == 0) {
            return new View[]{};
        }
        View views[] = new View[choices.length];
        int i = 0;
        for (String choice : choices) {
            View choiceItem = View.inflate(getContext(), R.layout.options_view_choice_item, null);
            AutoImageLoadTextView textView = (AutoImageLoadTextView) choiceItem.findViewById(R.id.options_view_choice_item_tv);
            textView.setText(choice);
            choiceItem.setOnClickListener(this);
            choiceItem.setTag(choice);
            addView(choiceItem);
            views[i++] = choiceItem;

            if (isMultiSelect) {
                ImageView imageView = (ImageView) choiceItem.findViewById(R.id.options_view_choice_item_iv);
                imageView.setImageResource(R.drawable.common_multi_select_button_selector);
            }
        }
        return views;
    }

    public boolean isContainChoiceIndex(int[] choices, int choiceIndex) {
        if (choices == null) {
            return false;
        }
        for (int choice : choices) {
            if (choice == choiceIndex) {
                return true;
            }
        }
        return false;
    }

    public void setSelectedChoice(int[] choices) {
        mMultiChoice = choices;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View itemView = getChildAt(i);
            if (isContainChoiceIndex(choices, i)) {
                itemView.setSelected(true);
            } else {
                itemView.setSelected(false);
            }
        }
    }

    public void setSelectedChoice(int choiceIndex) {
        mCurrentChoiceIndex = choiceIndex;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View itemView = getChildAt(i);
            if (i == choiceIndex) {
                itemView.setSelected(true);
            } else {
                itemView.setSelected(false);
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mEditable = enabled;
    }

    public void setChoiceListener(OnChoiceChangeListener l) {
        mListener = l;
    }

    public void setMultiChoiceListener(OnMultiChoiceChangeListener l) {
        mMultiListener = l;
    }

    @Override
    public void onClick(View v) {
        if (!mEditable) {
            return;
        }
        if (isMultiSelect) {
            multiChoiceClick(v);
        } else {
            singleChoiceClick(v);
        }
    }

    private void multiChoiceClick(View v) {
        String choice = (String) v.getTag();
        boolean isSelected = !v.isSelected();
        v.setSelected(isSelected);

        int count = getChildCount();
        int choiceIndex = -1;
        for (int i = 0; i < count; i++) {
            if (getChildAt(i) == v) {
                choiceIndex = i;
            }
        }

        if (isSelected && !isContainChoiceIndex(mMultiChoice, choiceIndex)) {
            if (mMultiChoice == null) {
                mMultiChoice = new int[]{choiceIndex};
            } else {
                int[] newAry = new int[mMultiChoice.length + 1];
                for (int i = 0; i < newAry.length; i++) {
                    if (i < newAry.length - 1) {
                        newAry[i] = mMultiChoice[i];
                    } else {
                        newAry[i] = choiceIndex;
                    }
                }
                mMultiChoice = newAry;
            }
            if (mMultiListener != null) {
                mMultiListener.onOptionsViewChoiceChange(this, mMultiChoice);
            }
        } else if (!isSelected && isContainChoiceIndex(mMultiChoice, choiceIndex)) {
            if (mMultiChoice.length == 1) {
                mMultiChoice = null;
            } else {
                int[] newAry = new int[mMultiChoice.length - 1];
                int newAryIndex = 0;
                for (int i = 0; i < mMultiChoice.length; i++) {
                    if (mMultiChoice[i] != choiceIndex) {
                        newAry[newAryIndex++] = mMultiChoice[i];
                    }
                }
                mMultiChoice = newAry;
            }
            if (mMultiListener != null) {
                mMultiListener.onOptionsViewChoiceChange(this, mMultiChoice);
            }
        }
    }

    private void singleChoiceClick(View v) {
        int count = getChildCount();
        int choiceIndex = -1;
        for (int i = 0; i < count; i++) {
            if (getChildAt(i) == v) {
                choiceIndex = i;
            }
        }
        if (choiceIndex == mCurrentChoiceIndex) {
            return;
        }
        mCurrentChoiceIndex = choiceIndex;
        for (int i = 0; i < count; i++) {
            getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        if (mListener != null) {
            mListener.onOptionsViewChoiceChange(this, (String) v.getTag(), choiceIndex);
        }
    }


    public static interface OnChoiceChangeListener {
        public void onOptionsViewChoiceChange(OptionsView view, String choice, int index);
    }

    public static interface OnMultiChoiceChangeListener {
        public void onOptionsViewChoiceChange(OptionsView view, int[] indexs);
    }
}
