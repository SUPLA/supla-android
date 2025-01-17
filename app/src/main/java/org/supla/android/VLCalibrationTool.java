package org.supla.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.lib.SuplaConst;

public class VLCalibrationTool extends DimmerCalibrationTool
    implements SuplaRangeCalibrationWheel.OnChangeListener {
  private static final int VL_MSG_RESTORE_DEFAULTS = 0x4E;
  private static final int VL_MSG_CONFIGURATION_MODE = 0x44;
  private static final int VL_MSG_CONFIGURATION_ACK = 0x45;
  private static final int VL_MSG_CONFIGURATION_QUERY = 0x15;
  private static final int VL_MSG_CONFIGURATION_REPORT = 0x51;
  private static final int VL_MSG_CONFIG_COMPLETE = 0x46;
  private static final int VL_MSG_SET_MODE = 0x58;
  private static final int VL_MSG_SET_MINIMUM = 0x59;
  private static final int VL_MSG_SET_MAXIMUM = 0x5A;
  private static final int VL_MSG_SET_BOOST = 0x5B;
  private static final int VL_MSG_SET_BOOST_LEVEL = 0x5C;
  private static final int VL_MSG_SET_CHILD_LOCK = 0x18;
  private static final int VL_CALCFG_MSG_SET_LED_CONFIG = 0x01FF;
  private static final int UI_REFRESH_LOCK_TIME = 2000;

  private final Button btnDmAuto;
  private final Button btnDm1;
  private final Button btnDm2;
  private final Button btnDm3;
  private final Button btnBoostAuto;
  private final Button btnBoostYes;
  private final Button btnBoostNo;
  private final Button btnOpRange;
  private final Button btnBoost;
  private final SuplaRangeCalibrationWheel calibrationWheel;
  private final VLCfgParameters cfgParameters;
  private final int mColorDisabled;
  private boolean restoringDefaults;
  private Timer startConfigurationRetryTimer;
  private final TextView tvPicFirmwareVersion;

  public VLCalibrationTool(ChannelDetailRGBW detailRGB) {
    super(detailRGB);

    btnDmAuto = findBtnViewById(R.id.vlCfgDmAuto);
    btnDm1 = findBtnViewById(R.id.vlCfgDm1);
    btnDm2 = findBtnViewById(R.id.vlCfgDm2);
    btnDm3 = findBtnViewById(R.id.vlCfgDm3);

    btnBoostAuto = findBtnViewById(R.id.vlCfgBoostAuto);
    btnBoostYes = findBtnViewById(R.id.vlCfgBoostYes);
    btnBoostNo = findBtnViewById(R.id.vlCfgBoostNo);
    btnOpRange = findBtnViewById(R.id.vlCfgOpRange);
    btnBoost = findBtnViewById(R.id.vlCfgBoost);
    calibrationWheel = getMainView().findViewById(R.id.vlCfgCalibrationWheel);
    calibrationWheel.setOnChangeListener(this);
    cfgParameters = new VLCfgParameters();

    mColorDisabled = detailRGB.getContext().getResources().getColor(R.color.vl_btn_disabled);

    btnDmAuto.setVisibility(View.GONE);
    btnBoostAuto.setVisibility(View.GONE);

    setImgViews(R.id.vlCfgLedImgOn, R.id.vlCfgLedImgOff, R.id.vlCfgLedImgAlwaysOff);
    setButtons(R.id.vlBtnOK, R.id.vlBtnRestore, R.id.vlBtnInfo);

    tvPicFirmwareVersion = getMainView().findViewById(R.id.vlCfgPicFirmwareVersion);
  }

  @Override
  protected void onSuperuserOnAuthorizarionSuccess() {
    calCfgRequest(VL_MSG_CONFIGURATION_MODE);
  }

  private void stopConfigurationRetryTimer() {
    if (startConfigurationRetryTimer != null) {
      startConfigurationRetryTimer.cancel();
      startConfigurationRetryTimer = null;
    }
  }

  private void startConfigurationAgainWithRetry() {

    stopConfigurationRetryTimer();

    startConfigurationRetryTimer = new Timer();

    startConfigurationRetryTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            getActivity()
                .runOnUiThread(
                    () -> {
                      if (startConfigurationRetryTimer != null) {
                        calCfgRequest(VL_MSG_CONFIGURATION_MODE);
                      }
                    });
          }
        },
        5000,
        5000);
  }

  @Override
  protected void onCalCfgResult(int Command, int Result, byte[] Data) {
    switch (Command) {
      case VL_MSG_CONFIGURATION_ACK:
        if (Result == SuplaConst.SUPLA_RESULTCODE_TRUE && !restoringDefaults) {

          setConfigStarted();
          stopConfigurationRetryTimer();

        } else if (restoringDefaults) {
          restoringDefaults = false;
          startConfigurationAgainWithRetry();
        }
        break;
      case VL_MSG_CONFIGURATION_REPORT:
        if (Result == SuplaConst.SUPLA_RESULTCODE_TRUE) {
          cfgParameters.setParams(Data);
          displayCfgParameters(false);
        }
        break;
    }
  }

  private int viewToMode(View btn) {
    int mode = VLCfgParameters.MODE_UNKNOWN;
    if (btn == btnDmAuto) {
      mode = VLCfgParameters.MODE_AUTO;
    } else if (btn == btnDm1) {
      mode = VLCfgParameters.MODE_1;
    } else if (btn == btnDm2) {
      mode = VLCfgParameters.MODE_2;
    } else if (btn == btnDm3) {
      mode = VLCfgParameters.MODE_3;
    }
    return mode == VLCfgParameters.MODE_UNKNOWN || cfgParameters.isModeDisabled(mode)
        ? VLCfgParameters.MODE_UNKNOWN
        : mode;
  }

  private int viewToBoost(View btn) {
    int boost = VLCfgParameters.BOOST_UNKNOWN;

    if (btn == btnBoostAuto) {
      boost = VLCfgParameters.BOOST_AUTO;
    } else if (btn == btnBoostYes) {
      boost = VLCfgParameters.BOOST_YES;
    } else if (btn == btnBoostNo) {
      boost = VLCfgParameters.BOOST_NO;
    }

    return boost == VLCfgParameters.BOOST_UNKNOWN || cfgParameters.isBoostDisabled(boost)
        ? VLCfgParameters.BOOST_UNKNOWN
        : boost;
  }

  private Button modeToBtn(int mode) {
    switch (mode) {
      case VLCfgParameters.MODE_AUTO:
        return btnDmAuto;
      case VLCfgParameters.MODE_1:
        return btnDm1;
      case VLCfgParameters.MODE_2:
        return btnDm2;
      case VLCfgParameters.MODE_3:
        return btnDm3;
    }
    return null;
  }

  private Button boostToBtn(int boost) {
    switch (boost) {
      case VLCfgParameters.BOOST_AUTO:
        return btnBoostAuto;
      case VLCfgParameters.BOOST_YES:
        return btnBoostYes;
      case VLCfgParameters.BOOST_NO:
        return btnBoostNo;
    }
    return null;
  }

  private void setMode(int mode) {
    int inactiveColor = ResourcesCompat.getColor(getResources(), R.color.on_background, null);
    setBtnApparance(
        btnDmAuto,
        0,
        cfgParameters.isModeDisabled(VLCfgParameters.MODE_AUTO) ? mColorDisabled : inactiveColor);

    setBtnApparance(
        btnDm1,
        0,
        cfgParameters.isModeDisabled(VLCfgParameters.MODE_1) ? mColorDisabled : inactiveColor);

    setBtnApparance(
        btnDm2,
        0,
        cfgParameters.isModeDisabled(VLCfgParameters.MODE_2) ? mColorDisabled : inactiveColor);

    setBtnApparance(
        btnDm3,
        0,
        cfgParameters.isModeDisabled(VLCfgParameters.MODE_3) ? mColorDisabled : inactiveColor);

    int activeColor = ResourcesCompat.getColor(getResources(), R.color.on_primary, null);
    setBtnApparance(modeToBtn(mode), R.drawable.rounded_sel_btn, activeColor);
  }

  private void setMode() {
    setMode(cfgParameters.getMode());
  }

  private void setBoost(int boost) {
    int inactiveColor = ResourcesCompat.getColor(getResources(), R.color.on_background, null);
    setBtnApparance(
        btnBoostAuto,
        0,
        cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_AUTO) ? mColorDisabled : inactiveColor);

    setBtnApparance(
        btnBoostYes,
        0,
        cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_YES) ? mColorDisabled : inactiveColor);

    setBtnApparance(
        btnBoostNo,
        0,
        cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_NO) ? mColorDisabled : inactiveColor);

    int activeColor = ResourcesCompat.getColor(getResources(), R.color.on_primary, null);
    setBtnApparance(boostToBtn(boost), R.drawable.rounded_sel_btn, activeColor);

    if (boost == VLCfgParameters.BOOST_YES) {
      btnBoost.setVisibility(View.VISIBLE);
      return;
    }

    btnBoost.setVisibility(View.INVISIBLE);
    displayOpRange(true);
  }

  private void setBoost() {
    setBoost(cfgParameters.getBoost());
  }

  private void setLedConfig() {
    setLedConfig(cfgParameters);
  }

  private void displayOpRange(boolean display) {
    int color = ResourcesCompat.getColor(getResources(), R.color.on_background, null);
    if (display) {
      setBtnApparance(btnOpRange, R.drawable.vl_tab, color);
      setBtnApparance(btnBoost, 0, color);
      calibrationWheel.setBoostVisible(false);
    } else {
      setBtnApparance(btnOpRange, 0, color);
      setBtnApparance(btnBoost, R.drawable.vl_tab, color);
      calibrationWheel.setBoostVisible(true);
    }
  }

  @Override
  protected void displayCfgParameters() {
    setMode();
    setBoost();
    setLedConfig();
    calibrationWheel.setRightEdge(cfgParameters.getRightEdge());
    calibrationWheel.setLeftEdge(cfgParameters.getLeftEdge());
    calibrationWheel.setMinMax(cfgParameters.getMinimum(), cfgParameters.getMaximum());
    calibrationWheel.setBoostLevel(cfgParameters.getBoostLevel());
    tvPicFirmwareVersion.setText(cfgParameters.getPicFirmwareVersion());
  }

  @Override
  protected void showInformationDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    ViewGroup viewGroup = getDetailRGB().findViewById(android.R.id.content);
    View dialogView =
        LayoutInflater.from(getContext()).inflate(R.layout.vl_dimmer_config_info, viewGroup, false);
    builder.setView(dialogView);
    final AlertDialog alertDialog = builder.create();

    dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> alertDialog.dismiss());

    dialogView
        .findViewById(R.id.btnUrl)
        .setOnClickListener(
            v -> {
              alertDialog.dismiss();
              Intent browserIntent =
                  new Intent(Intent.ACTION_VIEW, Uri.parse(((Button) v).getText().toString()));
              getContext().startActivity(browserIntent);
            });

    alertDialog.show();
  }

  @Override
  protected void doRestore() {
    restoringDefaults = true;
    startConfigurationAgainWithRetry();

    calCfgDelayedRequest(VL_MSG_RESTORE_DEFAULTS);
  }

  private void calCfgConfigComplete(boolean save) {
    byte[] data = new byte[1];
    data[0] = (byte) (save ? 1 : 0);
    calCfgRequest(VL_MSG_CONFIG_COMPLETE, 0, data, true);
  }

  @Override
  protected void saveChanges() {
    calCfgConfigComplete(true);
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);

    int mode = viewToMode(v);
    if (mode != VLCfgParameters.MODE_UNKNOWN) {
      setMode(mode);
      calCfgRequest(VL_MSG_SET_MODE, (byte) (mode & 0xFF), null);

      showPreloaderWithText(R.string.mode_change_in_progress);
      startConfigurationAgainWithRetry();
    }

    int boost = viewToBoost(v);
    if (boost != VLCfgParameters.BOOST_UNKNOWN) {
      setBoost(boost);
      calCfgRequest(VL_MSG_SET_BOOST, (byte) (boost & 0xFF), null);

      if (boost == VLCfgParameters.BOOST_YES) {
        onBoostChanged(calibrationWheel);
        displayOpRange(false);
      }
    }

    if (cfgParameters.getLedConfig() != null) {
      int ledConfig = viewToLedConfig(v);
      if (ledConfig != VLCfgParameters.LED_UNKNOWN) {
        setLedConfig(ledConfig);
        calCfgRequest(VL_CALCFG_MSG_SET_LED_CONFIG, (byte) (ledConfig & 0xFF), null);
      }
    }

    if (v == btnOpRange) {
      displayOpRange(true);
    } else if (v == btnBoost) {
      displayOpRange(false);
    }
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.vl_calibration;
  }

  @Override
  protected void calCfgRequest(int cmd) {
    switch (cmd) {
      case VL_MSG_SET_MINIMUM:
        calCfgRequest(cmd, null, (short) calibrationWheel.getMinimum());
        break;
      case VL_MSG_SET_MAXIMUM:
        calCfgRequest(cmd, null, (short) calibrationWheel.getMaximum());
        break;
      case VL_MSG_SET_BOOST_LEVEL:
        calCfgRequest(cmd, null, (short) calibrationWheel.getBoostLevel());
        break;
      default:
        super.calCfgRequest(cmd);
        break;
    }
  }

  @Override
  public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
    calCfgDelayedRequest(minimum ? VL_MSG_SET_MINIMUM : VL_MSG_SET_MAXIMUM);
  }

  @Override
  public void onBoostChanged(SuplaRangeCalibrationWheel calibrationWheel) {
    calCfgDelayedRequest(VL_MSG_SET_BOOST_LEVEL);
  }

  @Override
  protected void onHide() {
    stopConfigurationRetryTimer();

    if (isConfigStarted()) {
      calCfgConfigComplete(false);
    }
  }
}
