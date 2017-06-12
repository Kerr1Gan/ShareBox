package org.ecjtu.channellibrary.wifidirect.util;

import android.util.Log;

public class ALog {
	private static final String TAG = "ALog";
	
	public static final boolean DEBUG = true;

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void d(String tag, String msg) {
    	if (DEBUG) {
    		Log.d(TAG, tag + " " + msg);
    	}
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void e(String tag, String msg) {
    	if (DEBUG) {
    		Log.e(TAG, tag + " " + msg);
    	}
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void i(String tag, String msg) {
    	if (DEBUG) {
    		Log.i(TAG, tag + " " + msg);
    	}
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void v(String tag, String msg) {
    	if (DEBUG) {
    		Log.v(TAG, tag + " " + msg);
    	}
    }
    
    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void w(String tag, String msg) {
    	if (DEBUG) {
    		Log.w(TAG, tag + " " + msg);
    	}
    }
}
