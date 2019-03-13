package com.miles.atsomebody;

import java.util.List;

/**
 * @author: SpaceCowboy
 * @date: 2019/3/13
 * @description:
 */
public class Utils {
    public static boolean isListEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public static boolean isTextEmpty(String text) {
        return text == null || text.length() == 0;
    }
}
