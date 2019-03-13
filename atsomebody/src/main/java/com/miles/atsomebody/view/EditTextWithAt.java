package com.miles.atsomebody.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.StyleableRes;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.miles.atsomebody.R;
import com.miles.atsomebody.UserAt;
import com.miles.atsomebody.Utils;
import com.miles.atsomebody.interfaces.OnAtInputListener;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jiangtao on 2019/01/17.
 * Description:
 */

public class EditTextWithAt extends AppCompatEditText {
    private static final int DEFAULT_TEXT_COLOR_ID = R.color.color_4990E2;
    public static final String AT = "@";
    private MentionTextWatcher mInnerWatcher;
    private List<UserAt> mAtList;
    private int mAtTextColor;
    private UserAt mLastSelectedUser;
    private boolean mIsSelected;
    private boolean isSetSelection;
    private boolean isAtEnable = false;

    public EditTextWithAt(Context context) {
        this(context, null);
    }

    public EditTextWithAt(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EditTextWithAt);
            mAtTextColor = typedArray.getInt(R.styleable.EditTextWithAt_atTextColor, DEFAULT_TEXT_COLOR_ID);
            isAtEnable = typedArray.getBoolean(R.styleable.EditTextWithAt_atEnable, isAtEnable);
            typedArray.recycle();
        }
    }

    public void atUser(String id, String name) {
        Editable editable = getText();
        int start = getSelectionStart();
        String tempName = UserAt.getAtContent(name);
        int index = start;
        if (start != 0) {
            String s = editable.toString().toCharArray()[start - 1] + "";
            if (AT.equals(s)) {
                tempName = UserAt.getAtContentWithOutAt(name);
                index--;
            }
        }

        int end = start + tempName.length();
        editable.insert(start, tempName);
        editable.setSpan(getColorSpan(), index, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mAtList.add(new UserAt(id, name, index));
    }

    private boolean isInit = false;

    public boolean isInit() {
        return isInit;
    }

    public void setAtText(String text, List<UserAt> list) {
        if (Utils.isTextEmpty(text)) {
            return;
        }
        isInit = true;
        setText(text);
        if (Utils.isListEmpty(list)) {
            isInit = false;
            return;
        }
        mAtList.addAll(list);
        Editable editable = getText();
        String tempName;
        for (UserAt user : mAtList) {
            tempName = UserAt.getAtContent(user.getNickname());
            String substring = text.substring(user.getStartIndex(), user.getEnd());
            if (tempName.equals(substring)) {
                editable.setSpan(getColorSpan(), user.getStartIndex(), user.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        isInit = false;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new HackInputConnection(super.onCreateInputConnection(outAttrs), true, this);
    }

    public void onSelectedUserChanged(UserAt userAt) {
        mLastSelectedUser = userAt;
    }

    public void setSetSelection(boolean setSelection) {
        isSetSelection = setSelection;
    }

    public void setIsSelected(boolean mIsSelected) {
        this.mIsSelected = mIsSelected;
    }

    public boolean getIsSelected() {
        return mIsSelected;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {// selStart<=selEnd
        super.onSelectionChanged(selStart, selEnd);
        if (isSetSelection) {
            isSetSelection = false;
            return;
        }
        //avoid infinite recursion after calling setSelection()
        if (mLastSelectedUser != null && mLastSelectedUser.isEqual(selStart, selEnd)) {
            return;
        }

        //if user cancel a selection of mention string, reset the state of 'mIsSelected'
        UserAt closestRange = getRangeOfClosestMentionString(selStart, selEnd);
        if (closestRange != null && closestRange.getEnd() == selEnd) {
            mIsSelected = false;
        }

        UserAt nearbyRange = getRangeOfNearbyMentionString(selStart, selEnd);
        //if there is no mention string nearby the cursor, just skip
        if (nearbyRange == null) {
            return;
        }

        //forbid cursor located in the mention string.
        if (selStart == selEnd) {
            setSelection(nearbyRange.getAnchorPosition(selStart));
        } else {
            if (selEnd < nearbyRange.getEnd()) {
                setSelection(selStart, nearbyRange.getEnd());
            }
            if (selStart > nearbyRange.getStartIndex()) {
                setSelection(nearbyRange.getStartIndex(), selEnd);
            }
        }
    }

    public ForegroundColorSpan getColorSpan() {
        return new ForegroundColorSpan(mAtTextColor);
    }

    private UserAt getRangeOfNearbyMentionString(int selStart, int selEnd) {
        if (mAtList == null) {
            return null;
        }
        for (UserAt range : mAtList) {
            if (range.isWrappedBy(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    public UserAt getRangeOfClosestMentionString(int selStart, int selEnd) {
        if (mAtList == null) {
            return null;
        }
        for (UserAt range : mAtList) {
            if (range.contains(selStart, selEnd)) {
                return range;
            }
        }
        return null;
    }

    public List<UserAt> getAtList() {
        return mAtList;
    }

    public void clearList() {
        mAtList.clear();
    }

    public int getColor(Context context, int id) {
        return context.getResources().getColor(id);
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public class Builder {
        private boolean isAtEnable = true;
        private TextWatcher mOuterWatcher;
        private OnAtInputListener mAtListener;
        private int mAtTextColor = DEFAULT_TEXT_COLOR_ID;

        private Builder() {
        }

        public Builder setAtEnable(boolean atEnable) {
            isAtEnable = atEnable;
            return this;
        }

        public Builder setTextWatcher(TextWatcher outerWatcher) {
            this.mOuterWatcher = outerWatcher;
            return this;
        }

        public Builder setOnAtListener(OnAtInputListener atListener) {
            this.mAtListener = atListener;
            return this;
        }

        public Builder setAtTextColor(int atTextColor) {
            this.mAtTextColor = atTextColor;
            return this;
        }

        public void apply() {
            EditTextWithAt.this.isAtEnable = isAtEnable;
            EditTextWithAt.this.mAtTextColor = mAtTextColor;
            if (isAtEnable) {
                mAtList = new LinkedList<>();
                mInnerWatcher = new MentionTextWatcher(EditTextWithAt.this);
                mInnerWatcher.setOuterWatcher(mOuterWatcher);
                mInnerWatcher.setOnAtListener(mAtListener);
                addTextChangedListener(mInnerWatcher);
            } else {
                addTextChangedListener(mOuterWatcher);
            }
        }
    }
}
