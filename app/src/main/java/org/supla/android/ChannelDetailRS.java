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

import static org.supla.android.lib.SuplaConst.SUPLA_CTR_ROLLER_SHUTTER_CLOSE;
import static org.supla.android.lib.SuplaConst.SUPLA_CTR_ROLLER_SHUTTER_OPEN;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.db.Channel;
import org.supla.android.db.ChannelBase;
import org.supla.android.db.ChannelGroup;
import org.supla.android.lib.RollerShutterValue;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.DetailLayout;

public class ChannelDetailRS extends DetailLayout
    implements SuplaRollerShutter.OnTouchListener,
        View.OnTouchListener,
        SuplaRoofWindowController.OnClosingPercentageChangeListener,
        View.OnClickListener,
        SuperuserAuthorizationDialog.OnAuthorizarionResultListener {

  private LinearLayout llRollerShutter;
  private SuplaRollerShutter rollerShutter;
  private SuplaRoofWindowController roofWindow;
  private SuplaChannelStatus status;
  private TextView tvPercentCaption;
  private TextView tvPercent;
  private Button btnUp;
  private Button btnDown;
  private Button btnStop;
  private Button btnOpen;
  private Button btnClose;
  private Button btnRecalibrate;
  private TextView rsTvPressTime;
  private SuplaWarningIcon warningIcon;
  private Timer delayTimer1;
  private SuperuserAuthorizationDialog authDialog;
  private long btnUpDownTouchedAt;
  private boolean showOpening;
  private TextView percentageCaption;

  public ChannelDetailRS(Context context) {
    super(context);
  }

  public ChannelDetailRS(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  protected void init() {

    super.init();

    percentageCaption = findViewById(R.id.rsDetailPercentCaption);
    readShowOpeningValue();
    updatePercentageCaption();

    llRollerShutter = findViewById(R.id.llRS);

    rollerShutter = findViewById(R.id.rs1);
    rollerShutter.setMarkerColor(getResources().getColor(R.color.detail_rs_marker));
    rollerShutter.setOnPercentTouchListener(this);

    roofWindow = findViewById(R.id.rw1);
    roofWindow.setOnClosingPercentageChangeListener(this);

    status = findViewById(R.id.rsstatus);
    status.setOnlineColor(getResources().getColor(R.color.channel_dot_on));
    status.setOfflineColor(getResources().getColor(R.color.channel_dot_off));

    btnUp = findViewById(R.id.rsBtnUp);
    btnDown = findViewById(R.id.rsBtnDown);
    btnStop = findViewById(R.id.rsBtnStop);
    btnOpen = findViewById(R.id.rsBtnOpen);
    btnClose = findViewById(R.id.rsBtnClose);
    btnRecalibrate = findViewById(R.id.rsBtnRecalibrate);
    rsTvPressTime = findViewById(R.id.rsTvPressTime);
    warningIcon = findViewById(R.id.rsWarningIcon);

    btnUp.setOnTouchListener(this);
    btnDown.setOnTouchListener(this);
    btnStop.setOnTouchListener(this);
    btnOpen.setOnTouchListener(this);
    btnClose.setOnTouchListener(this);
    btnRecalibrate.setOnClickListener(this);

    Typeface type = SuplaApp.getApp().getTypefaceOpenSansBold();

    tvPercentCaption = findViewById(R.id.rsDetailPercentCaption);
    tvPercentCaption.setTypeface(type);

    tvPercent = findViewById(R.id.rsDetailPercent);
    tvPercent.setTypeface(type);

    delayTimer1 = null;
  }

  @Override
  public View inflateContentView() {
    return inflateLayout(R.layout.detail_rs);
  }

  private void setRollerShutterVisible(ChannelBase channelBase) {
    if (channelBase.getFunc() == SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW) {
      llRollerShutter.setVisibility(GONE);
      roofWindow.setVisibility(VISIBLE);
    } else {
      roofWindow.setVisibility(GONE);
      llRollerShutter.setVisibility(VISIBLE);
    }
  }

  private void OnChannelDataChanged(boolean withoutDelay) {

    if (delayTimer1 != null) {
      delayTimer1.cancel();
      delayTimer1 = null;
    }

    btnRecalibrate.setVisibility(INVISIBLE);
    rsTvPressTime.setVisibility(INVISIBLE);

    if (!isGroup()) {
      status.setVisibility(View.GONE);
      Channel channel = (Channel) getChannelFromDatabase();
      setRollerShutterVisible(channel);
      warningIcon.setChannel(channel);

      RollerShutterValue rsValue = channel.getValue().getRollerShutterValue();

      rollerShutter.setMarkers(null);
      rollerShutter.setPercent(rsValue.getClosingPercentage());
      rollerShutter.setBottomPosition(rsValue.getBottomPosition());
      roofWindow.setMarkers(null);
      roofWindow.setClosingPercentage(rsValue.getClosingPercentage());

      if (rsValue.getClosingPercentage() < 0) {
        tvPercent.setText(R.string.calibration);
        rsTvPressTime.setVisibility(VISIBLE);
      } else {
        tvPercent.setText(
            Integer.toString((int) mappedPercentage(rsValue.getClosingPercentage())) + "%");
      }

      if ((channel.getFlags() & SuplaConst.SUPLA_CHANNEL_FLAG_CALCFG_RECALIBRATE) > 0) {
        btnRecalibrate.setVisibility(VISIBLE);
      }

    } else {
      status.setVisibility(View.VISIBLE);
      warningIcon.setChannel(null);

      ChannelGroup cgroup = (ChannelGroup) getChannelFromDatabase();
      setRollerShutterVisible(cgroup);

      rollerShutter.setPercent(0);
      rollerShutter.setBottomPosition(0);
      roofWindow.setClosingPercentage(0);
      status.setPercent(cgroup.getOnLinePercent());

      ArrayList<Float> positions = cgroup.getRollerShutterPositions();

      int percent = -1;

      for (int a = 0; a < positions.size(); a++) {
        int p = positions.get(a).intValue();
        if (p < 0) {
          positions.remove(a);
          a--;
        } else if (percent == -1) {
          percent = p;
        } else if (percent != -2 && percent != p) {
          percent = -2;
        }
      }

      if (percent >= 0) {

        rollerShutter.setMarkers(positions);
        roofWindow.setMarkers(positions);
        tvPercent.setText("---");

        delayTimer1 = new Timer();
        delayTimer1.schedule(
            new DelayTask(percent) {
              @Override
              public void run() {
                if (getContext() instanceof Activity) {
                  ((Activity) getContext())
                      .runOnUiThread(
                          new Runnable() {

                            @Override
                            public void run() {
                              rollerShutter.setMarkers(null);
                              rollerShutter.setPercent(percent);
                              roofWindow.setMarkers(null);
                              roofWindow.setClosingPercentage(percent);
                              tvPercent.setText(Integer.toString(mappedPercentage(percent)) + "%");
                            }
                          });
                }
              }
            },
            withoutDelay ? 0 : 2000);

      } else if (percent == -1) {
        // All of RS wait for calibration
        rollerShutter.setPercent(0);
        rollerShutter.setMarkers(null);
        roofWindow.setClosingPercentage(0);
        roofWindow.setMarkers(null);
        tvPercent.setText(R.string.calibration);
      } else {
        rollerShutter.setPercent(0);
        rollerShutter.setMarkers(positions);
        roofWindow.setClosingPercentage(0);
        roofWindow.setMarkers(positions);
        tvPercent.setText("---");
      }
    }
  }

  @Override
  public void OnChannelDataChanged() {
    OnChannelDataChanged(false);
  }

  public void setData(ChannelBase channel) {
    super.setData(channel);
    OnChannelDataChanged(true);
  }

  public void onPercentChanged(float percent) {
    SuplaClient client = SuplaApp.getApp().getSuplaClient();

    if (client == null || !isDetailVisible()) return;

    client.open(getRemoteId(), isGroup(), (int) (10 + percent));
    OnChannelDataChanged();
  }

  @Override
  public void onPercentChanged(SuplaRollerShutter rs, float percent) {
    onPercentChanged(percent);
  }

  public void onPercentChangeing(float percent) {
    tvPercent.setText(Integer.toString(mappedPercentage((int) percent)) + "%");
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onPercentChangeing(SuplaRollerShutter rs, float percent) {
    onPercentChangeing(percent);
  }

  @Override
  public void onClosingPercentageChanged(
      SuplaRoofWindowController controller, float closingPercentage) {
    onPercentChanged(closingPercentage);
  }

  @Override
  public void onClosingPercentageChangeing(
      SuplaRoofWindowController controller, float closingPercentage) {
    onPercentChangeing(closingPercentage);
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {

    int action = event.getAction();

    switch (action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        break;

      default:
        return false;
    }

    SuplaClient client = SuplaApp.getApp().getSuplaClient();

    if (client == null) return false;

    if (v == btnUp || v == btnDown) {
      if (action == MotionEvent.ACTION_DOWN) {
        btnUpDownTouchedAt = System.currentTimeMillis();
      } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
        if (btnUpDownTouchedAt > 0) {
          String time =
              String.format("%.2fs", (System.currentTimeMillis() - btnUpDownTouchedAt) / 1000f);
          rsTvPressTime.setText(time);
        } else {
          rsTvPressTime.setText("");
        }
        btnUpDownTouchedAt = 0;
      }
    }

    if (v == btnUp) {

      client.open(getRemoteId(), isGroup(), action == MotionEvent.ACTION_DOWN ? 2 : 0);

    } else if (v == btnDown) {

      client.open(getRemoteId(), isGroup(), action == MotionEvent.ACTION_DOWN ? 1 : 0);

    } else if (v == btnStop) {

      if (action == MotionEvent.ACTION_DOWN) client.open(getRemoteId(), isGroup(), 0);
      else return false;

    } else if (v == btnOpen) {

      if (action == MotionEvent.ACTION_DOWN)
        client.open(getRemoteId(), isGroup(), SUPLA_CTR_ROLLER_SHUTTER_OPEN);
      else return false;

    } else if (v == btnClose) {

      if (action == MotionEvent.ACTION_DOWN)
        client.open(getRemoteId(), isGroup(), SUPLA_CTR_ROLLER_SHUTTER_CLOSE);
      else return false;
    }

    SuplaApp.Vibrate(getContext());

    return true;
  }

  @Override
  public void onClick(View view) {
    if (view == btnRecalibrate) {
      authDialog = new SuperuserAuthorizationDialog(getContext());
      authDialog.setObject(btnRecalibrate);
      authDialog.setOnAuthorizarionResultListener(this);
      authDialog.showIfNeeded();
    }
  }

  @Override
  public void onSuperuserOnAuthorizarionResult(
      SuperuserAuthorizationDialog authDialog, boolean Success, int Code) {
    if (Success && authDialog.getObject() == btnRecalibrate) {

      authDialog.close();

      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      builder.setMessage(R.string.recalibration_question);

      builder.setPositiveButton(
          R.string.yes,
          (dialog, id) -> {
            SuplaClient client = getClient();
            if (client != null) {
              client.deviceCalCfgRequest(
                  getRemoteId(), false, SuplaConst.SUPLA_CALCFG_CMD_RECALIBRATE, 0, null);
            }
          });

      builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

      AlertDialog alert = builder.create();
      alert.show();
    }
    this.authDialog = null;
  }

  @Override
  public void authorizationCanceled() {
    authDialog = null;
  }

  private class DelayTask extends TimerTask {
    int percent;

    public DelayTask(int percent) {
      this.percent = percent;
    }

    @Override
    public void run() {
      // You can do anything you want with param
    }
  }

  @Override
  public void onDetailShow() {
    super.onDetailShow();
    rsTvPressTime.setText("");

    boolean prevShowOpening = showOpening;
    readShowOpeningValue();

    if (showOpening != prevShowOpening) {
      updatePercentageCaption();
      OnChannelDataChanged(true);
    }
  }

  private int mappedPercentage(int v) {
    return showOpening ? 100 - v : v;
  }

  private void updatePercentageCaption() {
    percentageCaption.setText(
        showOpening ? R.string.rs_percent_caption_open : R.string.rs_percent_caption);
  }

  private void readShowOpeningValue() {
    Preferences prefs = new Preferences(getContext());
    showOpening = prefs.isShowOpeningPercent();
  }
}
