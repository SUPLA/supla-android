package org.supla.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class PreloaderPopup {
  private final Activity activity;
  private final AlertDialog dialog;
  private final TextView textView;
  private String text;
  private Timer timer;
  private int pos;

  PreloaderPopup(Activity activity) {
    this.activity = activity;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    LayoutInflater inflater =
        (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.preloaderpopup, null);

    textView = v.findViewById(R.id.tvText);

    builder.setView(v);
    dialog = builder.create();
    dialog.setCancelable(false);
  }

  public void show() {
    if (dialog != null && !dialog.isShowing()) {
      dialog.show();
    }
    setAnimDotTimerEnabled(true);
  }

  public void close() {
    if (dialog != null) {
      dialog.dismiss();
    }
    setAnimDotTimerEnabled(false);
  }

  private void setAnimDotTimerEnabled(boolean enabled) {

    if (dialog == null || !dialog.isShowing()) {
      enabled = false;
    }

    if (enabled) {
      if (timer == null) {
        timer = new Timer();

        timer.schedule(
            new TimerTask() {
              @Override
              public void run() {
                activity.runOnUiThread(
                    new Runnable() {
                      @Override
                      public void run() {
                        if (timer != null) {
                          updateText();
                          pos++;
                          if (pos > 4) {
                            pos = 0;
                          }
                        }
                      }
                    });
              }
            },
            0,
            200);
      }
    } else if (timer != null) {
      timer.cancel();
      timer = null;
    }
  }

  private void updateText() {
    SpannableString ss = new SpannableString(text + " ....");
    int start = text.length() + 1 + pos;
    ss.setSpan(
        new ForegroundColorSpan(Color.WHITE),
        start,
        start + 4 - pos,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    textView.setText(ss);
  }

  public void setText(String text) {
    this.text = text;
    updateText();
  }
}
