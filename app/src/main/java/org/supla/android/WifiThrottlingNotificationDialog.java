package org.supla.android;

/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;

public class WifiThrottlingNotificationDialog implements DialogInterface.OnCancelListener {

  private Activity activity;
  private AlertDialog alertDialog;
  private long secondsLeft;
  private Timer timer;
  private TextView tvTime;
  private OnDialogResultListener onDialogResultListener;

  public WifiThrottlingNotificationDialog(Activity activity) {
    this.activity = activity;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.wifithrottling, null);
    builder.setView(view);

    tvTime = view.findViewById(R.id.tvTime);

    alertDialog = builder.create();
    alertDialog.setOnCancelListener(this);
  }

  public interface OnDialogResultListener {
    void onWifiThrottlingDialogCancel(WifiThrottlingNotificationDialog dialog);

    void onWifiThrottlingDialogFinish(WifiThrottlingNotificationDialog dialog);
  }

  public void close() {
    alertDialog.dismiss();
  }

  public void show() {

    secondsLeft = 120 - SuplaApp.getSecondsSinceLastWiFiScan();
    final WifiThrottlingNotificationDialog dialog = this;

    timer = new Timer();
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            activity.runOnUiThread(
                new Runnable() {
                  public void run() {

                    tvTime.setText(String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60));

                    secondsLeft--;
                    if (secondsLeft < 0) {
                      if (timer != null) {
                        timer.cancel();
                        timer = null;

                        dialog.close();

                        if (onDialogResultListener != null) {
                          onDialogResultListener.onWifiThrottlingDialogFinish(dialog);
                        }
                      }
                    }
                  }
                });
          }
        },
        0,
        1000);

    alertDialog.show();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    if (onDialogResultListener != null) {
      onDialogResultListener.onWifiThrottlingDialogCancel(this);
    }
  }

  public void setOnDialogResultListener(OnDialogResultListener onDialogResultListener) {
    this.onDialogResultListener = onDialogResultListener;
  }
}
;
