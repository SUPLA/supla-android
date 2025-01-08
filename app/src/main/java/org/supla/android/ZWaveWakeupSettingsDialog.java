package org.supla.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.ZWaveWakeUpSettings;

public class ZWaveWakeupSettingsDialog
    implements DialogInterface.OnDismissListener,
        View.OnClickListener,
        NumberPicker.OnValueChangeListener {
  private final Activity activity;
  private int ChannelID;
  private final AlertDialog dialog;
  private final LinearLayout llMain;
  private final NumberPicker npHours;
  private final NumberPicker npMinutes;
  private final NumberPicker npSeconds;
  private final TextView tvError;
  private final ProgressBar progressBar1;
  private final ProgressBar progressBar2;
  private Timer timeoutTimer;
  private Timer delayTimer;
  private final Button btnOK;
  private ZWaveWakeUpSettings settings;

  ZWaveWakeupSettingsDialog(Activity activity) {
    this.activity = activity;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    LayoutInflater inflater =
        (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.zwave_wake_up_configuration, null);

    llMain = v.findViewById(R.id.llMain);
    npHours = v.findViewById(R.id.npHours);
    npMinutes = v.findViewById(R.id.npMinutes);
    npSeconds = v.findViewById(R.id.npSeconds);
    tvError = v.findViewById(R.id.tvError);
    progressBar1 = v.findViewById(R.id.progressBar1);
    progressBar2 = v.findViewById(R.id.progressBar2);
    btnOK = v.findViewById(R.id.btnOK);

    btnOK.setOnClickListener(this);
    v.findViewById(R.id.btnClose).setOnClickListener(this);
    v.findViewById(R.id.btnCancel).setOnClickListener(this);

    builder.setView(v);
    dialog = builder.create();
    dialog.setOnDismissListener(this);

    npSeconds.setOnValueChangedListener(this);
    npMinutes.setOnValueChangedListener(this);
    npHours.setOnValueChangedListener(this);
  }

  private void stopTimeoutTimer() {
    if (timeoutTimer != null) {
      timeoutTimer.cancel();
      timeoutTimer = null;
    }
  }

  private void showError(String text) {
    tvError.setText(text);

    progressBar1.setVisibility(View.GONE);
    progressBar2.setVisibility(View.GONE);
    tvError.setVisibility(View.VISIBLE);

    if (llMain.getVisibility() == View.VISIBLE) {
      btnOK.setEnabled(isValueValid());
      btnOK.setVisibility(View.VISIBLE);
    }
  }

  private void startTimeoutTimer() {
    stopTimeoutTimer();

    timeoutTimer = new Timer();
    timeoutTimer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            activity.runOnUiThread(
                new Runnable() {
                  public void run() {
                    if (timeoutTimer != null) {
                      stopTimeoutTimer();
                      showError(
                          activity
                              .getResources()
                              .getString(R.string.zwave_waiting_for_resposne_timeout));
                    }
                  }
                });
          }
        },
        5000,
        1000);
  }

  public void show(int ChannelID) {
    this.ChannelID = ChannelID;

    if (dialog != null) {

      llMain.setVisibility(View.GONE);
      tvError.setVisibility(View.INVISIBLE);
      progressBar1.setVisibility(View.GONE);
      progressBar2.setVisibility(View.VISIBLE);
      startTimeoutTimer();

      dialog.show();

      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.zwaveGetWakeUpSettings(ChannelID);
      }
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    stopTimeoutTimer();
  }

  @Override
  public void onClick(View v) {
    if (v == btnOK) {
      valueCorrection();
      btnOK.setVisibility(View.GONE);
      progressBar1.setVisibility(View.VISIBLE);
      startTimeoutTimer();
      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.zwaveSetWakeUpTime(ChannelID, pickersToSeconds());
      }
    } else {
      dialog.dismiss();
    }
  }

  public boolean isVisible() {
    return dialog != null && dialog.isShowing();
  }

  public void onWakeUpSettingsReport(int result, ZWaveWakeUpSettings settings) {

    stopTimeoutTimer();

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE && settings != null) {
      this.settings = settings;
      progressBar1.setVisibility(View.GONE);
      progressBar2.setVisibility(View.GONE);
      llMain.setVisibility(View.VISIBLE);

      npSeconds.setMinValue(0);
      npSeconds.setMaxValue(59);

      npMinutes.setMinValue(0);
      npMinutes.setMaxValue(59);

      npHours.setMinValue(0);
      npHours.setMaxValue(settings.getMaximum() / 3600);

      valueToPickers();

    } else {
      showError(
          activity
              .getResources()
              .getString(
                  R.string.zwave_unexpected_answer,
                  settings == null ? "NULL" : Integer.toString(result)));
    }
  }

  public void onZwaveSetWakeUpTimeResult(int result) {
    if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      dialog.dismiss();
    } else {
      showError(
          activity
              .getResources()
              .getString(R.string.zwave_unexpected_answer, Integer.toString(result)));
    }
  }

  private void valueToPickers() {
    npHours.setValue(settings.getValue() / 3600);
    npMinutes.setValue(settings.getValue() % 3600 / 60);
    npSeconds.setValue(settings.getValue() % 3600 % 60);
  }

  private int pickersToSeconds() {
    return npSeconds.getValue() + npMinutes.getValue() * 60 + npHours.getValue() * 3600;
  }

  private boolean isValueValid() {
    int value = pickersToSeconds();

    if (value < settings.getMinimum() || value > settings.getMaximum()) {
      return false;
    }

    if (settings.getStep() > 0) {
      int v = settings.getMinimum();
      while (v <= settings.getMaximum()) {
        if (v == value) {
          return true;
        }
        v += settings.getStep();
      }
      return false;
    }

    return true;
  }

  private void valueCorrection() {
    int value = pickersToSeconds();

    if (value < settings.getMinimum()) {
      value = settings.getMinimum();
    } else if (value > settings.getMaximum()) {
      value = settings.getMaximum();
    }

    if (settings.getStep() > 0) {
      value -= settings.getMinimum();
      int n = value / settings.getStep();
      if ((float) value / settings.getStep() % 1 >= 0.50f) {
        n += 1;
      }
      value = settings.getMinimum() + n * settings.getStep();
    }

    if (value != pickersToSeconds()) {
      settings.setValue(value);
      valueToPickers();
    }

    btnOK.setEnabled(isValueValid());
  }

  private void valueCorrectionWithDelay(int delayMsec) {

    if (delayTimer != null) {
      delayTimer.cancel();
      delayTimer = null;
    }

    if (delayMsec > 0) {
      delayTimer = new Timer();
      delayTimer.scheduleAtFixedRate(
          new TimerTask() {
            @Override
            public void run() {
              activity.runOnUiThread(
                  new Runnable() {
                    public void run() {
                      if (delayTimer != null) {
                        delayTimer.cancel();
                        delayTimer = null;

                        valueCorrection();
                      }
                    }
                  });
            }
          },
          delayMsec,
          1000);
    } else {
      valueCorrection();
    }
  }

  @Override
  public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
    btnOK.setEnabled(isValueValid());
    valueCorrectionWithDelay(1500);
  }
}
