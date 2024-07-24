package org.supla.android;

import android.util.Log;

public class Trace {
  public static final int NONE = 0;
  public static final int ERRORS_ONLY = 1;
  public static final int ERRORS_WARNINGS = 2;
  public static final int ERRORS_WARNINGS_INFO = 3;
  public static final int ERRORS_WARNINGS_INFO_DEBUG = 4;

  public static void e(String tag, String msg) {
    if (loggingLevel() >= ERRORS_ONLY) Log.e(tag, msg);
  }

  public static void e(String tag, String msg, Throwable e) {
    if (loggingLevel() >= ERRORS_ONLY) Log.e(tag, msg, e);
  }

  public static void w(String tag, String msg) {
    if (loggingLevel() >= ERRORS_WARNINGS) Log.w(tag, msg);
  }

  public static void w(String tag, String msg, Throwable throwable) {
    if (loggingLevel() >= ERRORS_WARNINGS) Log.w(tag, msg, throwable);
  }

  public static void i(String tag, String msg) {
    if (loggingLevel() >= ERRORS_WARNINGS_INFO) Log.i(tag, msg);
  }

  public static void d(String tag, String msg) {
    if (loggingLevel() >= ERRORS_WARNINGS_INFO_DEBUG) Log.d(tag, msg);
  }

  public static void d(String tag, String msg, Throwable throwable) {
    if (loggingLevel() >= ERRORS_WARNINGS_INFO_DEBUG) Log.d(tag, msg, throwable);
  }

  private static int loggingLevel() {
    if (BuildConfig.DEBUG) {
      return ERRORS_WARNINGS_INFO_DEBUG;
    } else {
      return ERRORS_WARNINGS;
    }
  }
}
