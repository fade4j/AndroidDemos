package com.miles.atsomebody.view;

import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import com.miles.atsomebody.UserAt;

/**
 * Created by jiangtao on 2019/01/17.
 * Description:
 */

public class HackInputConnection extends InputConnectionWrapper {
    private EditTextWithAt mEditText;
    //private boolean mIsSelected;

    public HackInputConnection(InputConnection target, boolean mutable, EditTextWithAt editText) {
        super(target, mutable);
        this.mEditText = editText;
    }

    @Override
    public boolean sendKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
            int selectionStart = mEditText.getSelectionStart();
            int selectionEnd = mEditText.getSelectionEnd();
            UserAt closestRange = mEditText.getRangeOfClosestMentionString(selectionStart, selectionEnd);
            if (closestRange == null) {
                mEditText.setIsSelected(false);
                return super.sendKeyEvent(event);
            }
            //if mention string has been selected or the cursor is at the beginning of mention string, just use default action(delete)
            if (mEditText.getIsSelected() || selectionStart == closestRange.getStartIndex()) {
                mEditText.setIsSelected(false);
                return super.sendKeyEvent(event);
            } else {
                //select the mention string
                mEditText.setIsSelected(true);
                mEditText.onSelectedUserChanged(closestRange);
                mEditText.setSetSelection(true);
                setSelection(closestRange.getStartIndex(), closestRange.getEnd());
            }
            super.sendKeyEvent(event);
            return true;
        }
        return super.sendKeyEvent(event);
    }

    @Override
    public boolean deleteSurroundingText(int beforeLength, int afterLength) {
        if (beforeLength == 1 && afterLength == 0) {
            return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
        }
        return super.deleteSurroundingText(beforeLength, afterLength);
    }
}
