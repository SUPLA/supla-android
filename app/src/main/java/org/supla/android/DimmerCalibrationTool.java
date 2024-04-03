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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.lib.SuplaClientMessageHandler;
import org.supla.android.lib.SuplaClientMsg;

public abstract class DimmerCalibrationTool
    implements View.OnClickListener,
        SuperuserAuthorizationDialog.OnAuthorizarionResultListener,
        SuplaClientMessageHandler.OnSuplaClientMessageListener {

  private static final int DISPLAY_DELAY_TIME = 1000;
  private static final int MIN_SEND_DELAY_TIME = 500;

  private PreloaderPopup preloaderPopup;
  private final ChannelDetailRGBW detailRGB;
  private final RelativeLayout mainView;
  private Button btnOK;
  private Button btnRestore;
  private Button btnInfo;
  private long configStartedAtTime = 0;
  private boolean mSuperuserAuthorizationStarted;
  private SuperuserAuthorizationDialog authDialog;
  private AppCompatImageView imgLedOn;
  private AppCompatImageView imgLedOff;
  private AppCompatImageView imgLedAlwaysOff;
  private long lastCalCfgTime = 0;
  private Timer delayTimer1 = null;
  private Timer delayTimer2 = null;
  private boolean settingsChanged;

  public DimmerCalibrationTool(ChannelDetailRGBW detailRGB) {
    if (detailRGB == null || !(detailRGB.getContext() instanceof ContextWrapper)) {
      throw new IllegalArgumentException("The detailRGB pattern is invalid");
    }

    this.detailRGB = detailRGB;
    mainView = (RelativeLayout) detailRGB.inflateLayout(getLayoutResId());
    mainView.setVisibility(View.GONE);
    detailRGB.addView(mainView);
  }

  @Override
  public void onSuperuserOnAuthorizarionResult(
      SuperuserAuthorizationDialog dialog, boolean Success, int Code) {
    SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this);
    mSuperuserAuthorizationStarted = false;

    if (Success) {
      SuplaClientMessageHandler.getGlobalInstance().registerMessageListener(this);
      onSuperuserOnAuthorizarionSuccess();
    }
  }

  @Override
  public void authorizationCanceled() {
    mSuperuserAuthorizationStarted = false;
    SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this);
  }

  protected void setImgViews(int imgOnResId, int imgOffResId, int imgAlwaysOffResId) {
    imgLedOn = getMainView().findViewById(imgOnResId);
    imgLedOn.setClickable(true);
    imgLedOn.setOnClickListener(this);

    imgLedOff = getMainView().findViewById(imgOffResId);
    imgLedOff.setClickable(true);
    imgLedOff.setOnClickListener(this);

    imgLedAlwaysOff = getMainView().findViewById(imgAlwaysOffResId);
    imgLedAlwaysOff.setClickable(true);
    imgLedAlwaysOff.setOnClickListener(this);
  }

  protected void setButtons(int btnOkResId, int btnRestoreResId, int btnInfoResId) {
    btnOK = btnOkResId == 0 ? null : findBtnViewById(btnOkResId);
    btnRestore = btnRestoreResId == 0 ? null : findBtnViewById(btnRestoreResId);
    btnInfo = btnInfoResId == 0 ? null : findBtnViewById(btnInfoResId);
  }

  protected void setSettingsChanged(boolean changed) {
    settingsChanged = changed;
    Drawable d = getResources().getDrawable(changed ? R.drawable.btnok : R.drawable.btnokdisabled);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      btnOK.setBackground(d);
    } else {
      btnOK.setBackgroundDrawable(d);
    }
  }

  protected RelativeLayout getMainView() {
    return mainView;
  }

  protected ChannelDetailRGBW getDetailRGB() {
    return detailRGB;
  }

  protected View getDetailContentView() {
    return detailRGB == null ? null : detailRGB.getContentView();
  }

  protected Resources getResources() {
    return detailRGB == null ? null : detailRGB.getResources();
  }

  protected boolean isDetailVisible() {
    return detailRGB != null && detailRGB.isDetailVisible();
  }

  protected int getRemoteId() {
    return detailRGB == null ? 0 : detailRGB.getRemoteId();
  }

  protected Context getContext() {
    return detailRGB == null ? null : detailRGB.getContext();
  }

  protected Activity getActivity() {
    return detailRGB == null || !(detailRGB.getContext() instanceof ContextWrapper)
        ? null
        : (Activity) ((ContextWrapper) detailRGB.getContext()).getBaseContext();
  }

  private void setConfigStarted(boolean started) {
    configStartedAtTime = started ? System.currentTimeMillis() : 0;
    NavigationActivity activity = NavigationActivity.getCurrentNavigationActivity();
    if (activity != null) {
      activity.showBackButton();
    }

    authDialogClose();

    displayCfgParameters(true);
    getDetailContentView().setVisibility(View.GONE);
    getMainView().setVisibility(View.VISIBLE);

    if (started) {
      closePreloaderPopup();
      setSettingsChanged(false);
    }
  }

  protected void setConfigStarted() {
    setConfigStarted(true);
  }

  protected void closePreloaderPopup() {
    if (preloaderPopup != null) {
      preloaderPopup.close();
      preloaderPopup = null;
    }
  }

  protected void showPreloaderWithText(int resId) {

    if (preloaderPopup == null) {
      preloaderPopup =
          new PreloaderPopup((Activity) ((ContextWrapper) detailRGB.getContext()).getBaseContext());
    }

    preloaderPopup.setText(detailRGB.getContext().getResources().getString(resId));
    preloaderPopup.show();
  }

  protected void authDialogClose() {
    if (authDialog != null) {
      authDialog.close();
      authDialog = null;
    }
  }

  protected Button findBtnViewById(int resid) {
    Button btn = getMainView().findViewById(resid);
    if (btn != null) {
      btn.setOnClickListener(this);
    }
    return btn;
  }

  protected void setBtnApparance(Button btn, int resid, int textColor) {
    if (btn == null) {
      return;
    }

    Drawable d = resid == 0 ? null : ResourcesCompat.getDrawable(getResources(), resid, null);
    btn.setBackground(d);
    btn.setTextColor(textColor);
  }

  protected void setLedBtnApparance(AppCompatImageView img, boolean selected, int resIdNormal) {
    if (img == null) {
      return;
    }

    Drawable bg =
        ResourcesCompat.getDrawable(
            getResources(),
            selected ? R.drawable.rounded_led_sel_btn : R.drawable.rounded_led_normal_btn,
            null);

    img.setBackground(bg);

    img.setImageResource(resIdNormal);
    img.setImageTintList(
        ColorStateList.valueOf(
            ResourcesCompat.getColor(
                getResources(), selected ? R.color.on_primary : R.color.on_background, null)));
  }

  protected void setLedConfig(int ledConfig) {
    setLedBtnApparance(
        imgLedOn, ledConfig == DeviceCfgParameters.LED_ON_WHEN_CONNECTED, R.drawable.ledon);

    setLedBtnApparance(
        imgLedOff, ledConfig == DeviceCfgParameters.LED_OFF_WHEN_CONNECTED, R.drawable.ledoff);

    setLedBtnApparance(
        imgLedAlwaysOff, ledConfig == DeviceCfgParameters.LED_ALWAYS_OFF, R.drawable.ledalwaysoff);
  }

  protected void setLedConfig(DeviceCfgParameters params) {
    setLedConfig(
        params.getLedConfig() != null ? params.getLedConfig() : DeviceCfgParameters.LED_UNKNOWN);
  }

  protected void calCfgRequest(int cmd, int dataType, byte[] data, boolean force) {
    if (detailRGB != null) {
      detailRGB.deviceCalCfgRequest(cmd, dataType, data, force);
      setSettingsChanged(true);
    }
  }

  protected void calCfgRequest(int cmd, Byte bdata, Short sdata) {
    if (detailRGB == null) {
      return;
    }

    lastCalCfgTime = System.currentTimeMillis();

    if (bdata != null) {
      detailRGB.deviceCalCfgRequest(cmd, bdata);
    } else if (sdata != null) {
      detailRGB.deviceCalCfgRequest(cmd, sdata);
    } else {
      detailRGB.deviceCalCfgRequest(cmd);
    }

    setSettingsChanged(true);
  }

  protected void calCfgRequest(int cmd) {
    calCfgRequest(cmd, null, null);
  }

  protected void calCfgDelayedRequest(int msg) {
    if (delayTimer1 != null) {
      delayTimer1.cancel();
      delayTimer1 = null;
    }

    if (System.currentTimeMillis() - lastCalCfgTime >= MIN_SEND_DELAY_TIME) {
      calCfgRequest(msg);
    } else {

      long delayTime = 1;

      if (System.currentTimeMillis() - lastCalCfgTime < MIN_SEND_DELAY_TIME) {
        delayTime = MIN_SEND_DELAY_TIME - (System.currentTimeMillis() - lastCalCfgTime) + 1;
      }

      delayTimer1 = new Timer();

      if (delayTime < 1) {
        delayTime = 1;
      }

      delayTimer1.schedule(new DimmerCalibrationTool.DisplayDelayedTask(msg), delayTime, 1000);
    }
  }

  protected void displayCfgParameters(boolean force) {
    if (delayTimer2 != null) {
      delayTimer2.cancel();
      delayTimer2 = null;
    }

    if (force || System.currentTimeMillis() - lastCalCfgTime >= DISPLAY_DELAY_TIME) {
      displayCfgParameters();
    } else {

      long delayTime = 1;

      if (System.currentTimeMillis() - lastCalCfgTime < DISPLAY_DELAY_TIME) {
        delayTime = DISPLAY_DELAY_TIME - (System.currentTimeMillis() - lastCalCfgTime) + 1;
      }

      delayTimer2 = new Timer();

      if (delayTime < 1) {
        delayTime = 1;
      }

      delayTimer2.schedule(
          new TimerTask() {
            @Override
            public void run() {
              getActivity().runOnUiThread(() -> displayCfgParameters(false));
            }
          },
          delayTime,
          1000);
    }
  }

  public boolean isVisible() {
    return getMainView().getVisibility() == View.VISIBLE;
  }

  public void Show() {
    if (authDialog != null) {
      authDialog.close();
      authDialog = null;
    }
    mSuperuserAuthorizationStarted = true;
    authDialog = new SuperuserAuthorizationDialog(getContext());
    authDialog.setOnAuthorizarionResultListener(this);
    authDialog.showIfNeeded();
  }

  public void Hide() {
    onHide();
    closePreloaderPopup();
    SuplaClientMessageHandler.getGlobalInstance().unregisterMessageListener(this);

    if (getMainView().getVisibility() == View.VISIBLE) {
      getMainView().setVisibility(View.GONE);
      getDetailContentView().setVisibility(View.VISIBLE);
    }

    if (configStartedAtTime > 0) {
      configStartedAtTime = 0;
    }
  }

  public boolean isExitUnlocked() {
    return preloaderPopup == null
        && (!mSuperuserAuthorizationStarted || authDialog == null || !authDialog.isShowing())
        && (configStartedAtTime == 0 || System.currentTimeMillis() - configStartedAtTime > 15000);
  }

  public boolean isConfigStarted() {
    return configStartedAtTime != 0;
  }

  protected int viewToLedConfig(View btn) {
    int ledConfig = VLCfgParameters.LED_UNKNOWN;

    if (btn == imgLedOn) {
      ledConfig = VLCfgParameters.LED_ON_WHEN_CONNECTED;
    } else if (btn == imgLedOff) {
      ledConfig = VLCfgParameters.LED_OFF_WHEN_CONNECTED;
    } else if (btn == imgLedAlwaysOff) {
      ledConfig = VLCfgParameters.LED_ALWAYS_OFF;
    }

    return ledConfig;
  }

  public boolean onBackPressed() {

    if (!settingsChanged) {
      Hide();
      return false;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMessage(R.string.save_without_saving);

    builder.setPositiveButton(R.string.yes, (dialog, id) -> Hide());

    builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();

    return false;
  }

  private void showRestoreConfirmDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder.setMessage(R.string.restore_question);

    builder.setPositiveButton(
        R.string.yes,
        (dialog, id) -> {
          showPreloaderWithText(R.string.restoring_default_settings);
          doRestore();
          setSettingsChanged(false);
        });

    builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();
  }

  public void onClick(View v) {
    if (v == btnOK) {

      if (!settingsChanged) {
        Hide();
        return;
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
      builder.setMessage(R.string.do_you_want_to_save);

      builder.setPositiveButton(
          R.string.yes,
          (dialog, id) -> {
            setConfigStarted(false);
            saveChanges();
            Hide();
          });

      builder.setNegativeButton(
          R.string.no,
          (dialog, id) -> {
            Hide();
            dialog.cancel();
          });

      builder.setNeutralButton(android.R.string.cancel, (dialog, id) -> dialog.cancel());

      AlertDialog alert = builder.create();
      alert.show();
    } else if (v == btnRestore) {
      showRestoreConfirmDialog();
    } else if (v == btnInfo) {
      showInformationDialog();
    }
  }

  @Override
  public void onSuplaClientMessageReceived(SuplaClientMsg msg) {
    if (msg != null
        && msg.getType() == SuplaClientMsg.onCalCfgResult
        && isDetailVisible()
        && msg.getChannelId() == getRemoteId()) {
      onCalCfgResult(msg.getCommand(), msg.getResult(), msg.getData());
    }
  }

  private class DisplayDelayedTask extends TimerTask {

    private final int msg;

    DisplayDelayedTask(int msg) {
      this.msg = msg;
    }

    @Override
    public void run() {
      getActivity().runOnUiThread(() -> calCfgDelayedRequest(msg));
    }
  }

  protected abstract int getLayoutResId();

  protected abstract void showInformationDialog();

  protected abstract void displayCfgParameters();

  protected abstract void doRestore();

  protected abstract void saveChanges();

  protected abstract void onHide();

  protected abstract void onSuperuserOnAuthorizarionSuccess();

  protected abstract void onCalCfgResult(int Command, int Result, byte[] Data);
}
