package io.ipoli.android.app.utils;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/31/16.
 */
public class StringUtils {

    public static String cut(String text, int startIdx, int endIdx) {
        return text.substring(0, startIdx) + (endIdx + 1 >= text.length() ? "" : text.substring(endIdx + 1));
    }

    public static String cutLength(String text, int startIdx, int lenToCut) {
        return text.substring(0, startIdx) + (startIdx + lenToCut >= text.length() ? "" : text.substring(startIdx + lenToCut));
    }
}
