package com.ecjtu.sharebox.util;

/**
 * Created by Ethan_Xiang on 2017/12/7.
 */

public class CodeUtil {
    /**
     * return current java file name and code line number
     *
     * @return String
     */
    public static String getLineInfo() {
        StackTraceElement ste = new Throwable().getStackTrace()[0];
        return (ste.getFileName() + ": Line " + ste.getLineNumber());
    }

    /**
     * return current java file name and code line name
     *
     * @return String
     */
    public static String getLineInfo(StackTraceElement ste) {
        return (ste.getFileName() + ": Line " + (ste.getLineNumber()));
    }
}
