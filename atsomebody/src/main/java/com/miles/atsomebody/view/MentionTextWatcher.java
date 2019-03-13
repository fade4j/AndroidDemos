package com.miles.atsomebody.view;

import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;

import com.miles.atsomebody.UserAt;
import com.miles.atsomebody.Utils;
import com.miles.atsomebody.interfaces.OnAtInputListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by jiangtao on 2019/01/17.
 * Description:
 */

public class MentionTextWatcher implements TextWatcher {
    private OnAtInputListener mOnAtListener;
    private EditTextWithAt mEditText;
    private TextWatcher mOuterWatcher;

    public MentionTextWatcher(EditTextWithAt editText) {
        mEditText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (mEditText.isInit()) {
            return;
        }
        Editable editable = mEditText.getText();
        int offset = after - count;
        int end = start + count;

        if (start != end && !mEditText.getAtList().isEmpty()) {
            ForegroundColorSpan[] spans = editable.getSpans(0, s.length(), ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                editable.removeSpan(span);
            }
        }

        //清理arraylist中上面已经清理掉的range
        //将end之后的span往后挪offset个位置
        Iterator iterator = mEditText.getAtList().iterator();
        while (iterator.hasNext()) {
            UserAt range = (UserAt) iterator.next();
            if (range.isWrapped(start, end)) {
                iterator.remove();
                continue;
            }

            if (range.getStartIndex() >= start) {
                range.changeOffset(offset);
            }
        }
        if (mOuterWatcher != null) {
            mOuterWatcher.beforeTextChanged(s, start, count, after);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mEditText.isInit()) {
            if (mOuterWatcher != null) {
                mOuterWatcher.onTextChanged(s, start, before, count);
            }
            return;
        }
        if (!TextUtils.isEmpty(s)) {
            if (count == 1) {
                char mentionChar = s.toString().charAt(start);
                if (EditTextWithAt.AT == String.valueOf(mentionChar) && mOnAtListener != null) {
                    mOnAtListener.onAtCharacterInput();
                }
            } else {
                Editable editable = mEditText.getText();
                ForegroundColorSpan[] spans = editable.getSpans(0, s.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    editable.removeSpan(span);
                }
                if (!mEditText.getAtList().isEmpty()) {
                    int startI;
                    int endI;
                    ArrayList<UserAt> users = new ArrayList<>();
                    for (UserAt user : mEditText.getAtList()) {
                        startI = user.getStartIndex() > 0 ? user.getStartIndex() : 0;
                        endI = user.getEnd() < s.length() ? user.getEnd() : s.length();
                        endI = endI > 0 ? endI : 0;

                        CharSequence sequence = s.subSequence(startI, endI);
                        if (UserAt.getAtContent(user.getNickname()).equals(sequence.toString())) {
                            editable.setSpan(mEditText.getColorSpan(), startI, endI, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            users.add(user);
                        }
                    }
                    if (!Utils.isListEmpty(users)) {
                        mEditText.getAtList().removeAll(users);
                    }
                }
            }
        }

        if (mOuterWatcher != null) {
            mOuterWatcher.onTextChanged(s, start, before, count);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mOuterWatcher != null) {
            mOuterWatcher.afterTextChanged(s);
        }
    }

    public void setOnAtListener(OnAtInputListener atListener) {
        this.mOnAtListener = atListener;
    }

    public void setOuterWatcher(TextWatcher watcher) {
        mOuterWatcher = watcher;
    }
}
