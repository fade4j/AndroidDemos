package com.miles.atsomebody;

/**
 * @author: SpaceCowboy
 * @date: 2019/3/13
 * @description:
 */

import com.miles.atsomebody.view.EditTextWithAt;

import java.io.Serializable;

/**
 * Created by jiangtao on 2018/12/24.
 * Description:
 */

public class UserAt implements Serializable {
    private String userId;
    private String nickName;
    private int startIndex;// 昵称在字符串中开始位置

    public UserAt() {
    }

    public UserAt(String jid, String remarkNick) {
        userId = jid;
        nickName = remarkNick;
    }

    public UserAt(String jid, String remarkNick, int start) {
        userId = jid;
        nickName = remarkNick;
        startIndex = start;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickName;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEnd() {
        int end = startIndex + getAtContent(getNickname()).length();
        return end;
    }

    /**
     * 目标文字处于start和end之间
     *
     * @param start
     * @param end
     * @return
     */
    public boolean isWrapped(int start, int end) {
        if (start == end) {
            return false;
        }
        return startIndex >= start && getEnd() <= end;
    }

    /**
     * 目标文字部分处于start和end之间
     *
     * @param start
     * @param end
     * @return
     */
    public boolean isWrappedBy(int start, int end) {
        return (start > startIndex && start < getEnd()) || (end > startIndex && end < getEnd());
    }

    /**
     * 光标处于文字之间
     *
     * @param start
     * @param end
     * @return
     */
    public boolean contains(int start, int end) {
        return startIndex <= start && getEnd() >= end;
    }

    /**
     * 就近放置光标
     *
     * @param value
     * @return
     */
    public int getAnchorPosition(int value) {
        if (value == startIndex || value == getEnd()) {
            return value;
        }
        if ((value - startIndex) - (getEnd() - value) >= 0) {
            return getEnd();
        } else {
            return startIndex;
        }
    }

    public void changeOffset(int offset) {
        startIndex += offset;
    }

    public boolean isEqual(int start, int end) {
        return (startIndex == start && getEnd() == end) || (startIndex == end && getEnd() == start);
    }

    public static String getAtContent(String remarkAndNick) {
        return EditTextWithAt.AT.concat(remarkAndNick).concat(" ");
    }

    public static String getAtContentWithOutAt(String remarkAndNick) {
        return remarkAndNick.concat(" ");
    }
}
