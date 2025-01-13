package org.supla.android;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.res.ResourcesCompat;
import org.supla.android.lib.SuplaConst;

public class DiwCalibrationTool extends DimmerCalibrationTool
    implements SuplaRangeCalibrationWheel.OnChangeListener {

  public static final int DIW_CMD_ENTER_CFG_MODE = 0x1;
  public static final int DIW_CMD_CONFIGURATION_REPORT = 0x2;
  public static final int DIW_CMD_CONFIG_COMPLETE = 0x3;
  public static final int DIW_CMD_SET_MINIMUM = 0x4;
  public static final int DIW_CMD_SET_MAXIMUM = 0x5;
  public static final int DIW_CMD_SET_LEDCONFIG = 0x6;
  public static final int DIW_CMD_SET_INPUT_BEHAVIOR = 0x7;
  public static final int DIW_CMD_SET_INPUT_MODE = 0x8;
  public static final int DIW_CMD_SET_INPUT_BI_MODE = 0x9;

  private final SuplaRangeCalibrationWheel calibrationWheel;
  private final DiwCfgParameters cfgParameters;
  private final TextView tvSTMFirmwareVersion;
  private final Button btnInputMonostable;
  private final Button btnInputBistable;
  private final AppCompatImageView imgInputOption;

  public DiwCalibrationTool(ChannelDetailRGBW detailRGB) {
    super(detailRGB);

    calibrationWheel = getMainView().findViewById(R.id.diwCfgCalibrationWheel);
    calibrationWheel.setMaximumValue(500);
    calibrationWheel.setOnChangeListener(this);
    cfgParameters = new DiwCfgParameters();

    btnInputMonostable = getMainView().findViewById(R.id.diwCfgInMonostable);
    btnInputMonostable.setOnClickListener(this);

    btnInputBistable = getMainView().findViewById(R.id.diwCfgInBistable);
    btnInputBistable.setOnClickListener(this);

    imgInputOption = getMainView().findViewById(R.id.diwCfgInOption);
    imgInputOption.setClickable(true);
    imgInputOption.setOnClickListener(this);

    setImgViews(R.id.diwCfgLedImgOn, R.id.diwCfgLedImgOff, R.id.diwCfgLedImgAlwaysOff);
    setButtons(R.id.diwBtnOK, 0, R.id.diwBtnInfo);

    tvSTMFirmwareVersion = getMainView().findViewById(R.id.diwCfgSTMFirmwareVersion);
  }

  @Override
  protected void onSuperuserOnAuthorizarionSuccess() {
    Trace.d("calcfg", "onSuperuserOnAuthorizarionSuccess");
    calCfgRequest(DIW_CMD_ENTER_CFG_MODE);
  }

  @Override
  protected void onCalCfgResult(int Command, int Result, byte[] Data) {
    if (Result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      return;
    }

    switch (Command) {
      case DIW_CMD_ENTER_CFG_MODE:
        setConfigStarted();
        break;
      case DIW_CMD_CONFIGURATION_REPORT:
        cfgParameters.setParams(Data);
        displayCfgParameters(false);
        break;
    }
  }

  private void setLedConfig() {
    setLedConfig(cfgParameters);
  }

  @Override
  protected void displayCfgParameters() {
    setLedConfig();
    setInputMode();
    calibrationWheel.setMinMax(cfgParameters.getMinimum(), cfgParameters.getMaximum());
    tvSTMFirmwareVersion.setText(cfgParameters.getStmFirmwareVersion());
  }

  @Override
  protected void showInformationDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    ViewGroup viewGroup = getDetailRGB().findViewById(android.R.id.content);
    View dialogView =
        LayoutInflater.from(getContext()).inflate(R.layout.diw_config_info, viewGroup, false);
    builder.setView(dialogView);
    final AlertDialog alertDialog = builder.create();

    dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> alertDialog.dismiss());

    Typeface quicksand = SuplaApp.getApp().getTypefaceQuicksandRegular();
    Typeface opensansbold = SuplaApp.getApp().getTypefaceOpenSansBold();
    Typeface opensans = SuplaApp.getApp().getTypefaceOpenSansRegular();

    ((TextView) dialogView.findViewById(R.id.tvInfoTitle)).setTypeface(quicksand);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt1)).setTypeface(opensansbold);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt2)).setTypeface(opensans);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt3)).setTypeface(opensansbold);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt4)).setTypeface(opensans);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt5)).setTypeface(opensans);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt6)).setTypeface(opensansbold);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt7)).setTypeface(opensans);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt8)).setTypeface(opensans);
    ((TextView) dialogView.findViewById(R.id.tvInfoTxt9)).setTypeface(opensans);

    alertDialog.show();
  }

  @Override
  protected void doRestore() {}

  private void calCfgConfigComplete(boolean save) {
    byte[] data = new byte[1];
    data[0] = (byte) (save ? 1 : 0);
    calCfgRequest(DIW_CMD_CONFIG_COMPLETE, 0, data, true);
  }

  @Override
  protected void saveChanges() {
    calCfgConfigComplete(true);
  }

  private int viewToInputMode(View btn) {
    int mode = DiwCfgParameters.INPUT_MODE_UNKNOWN;
    if (btn == btnInputMonostable) {
      mode = DiwCfgParameters.INPUT_MODE_MONOSTABLE;
    } else if (btn == btnInputBistable) {
      mode = DiwCfgParameters.INPUT_MODE_BISTABLE;
    }
    return mode;
  }

  private Button inputModeToBtn(int mode) {
    switch (mode) {
      case DiwCfgParameters.INPUT_MODE_MONOSTABLE:
        return btnInputMonostable;
      case DiwCfgParameters.INPUT_MODE_BISTABLE:
        return btnInputBistable;
    }
    return null;
  }

  protected void setInputOptionBtnApparance(boolean selected, int resIdNormal, int resIdSelected) {
    int resId = selected ? R.drawable.rounded_option_sel_btn : R.drawable.rounded_option_normal_btn;
    imgInputOption.setBackground(ResourcesCompat.getDrawable(getResources(), resId, null));
    imgInputOption.setImageResource(selected ? resIdSelected : resIdNormal);
  }

  private static class InputOptionTag {
    private final int inputMode;
    private final boolean selected;

    InputOptionTag(int inputMode, boolean selected) {
      this.inputMode = inputMode;
      this.selected = selected;
    }

    public int getInputMode() {
      return inputMode;
    }

    public boolean isSelected() {
      return selected;
    }
  }

  private void setInputOptionSelected(int mode, boolean selected) {
    imgInputOption.setTag(new InputOptionTag(mode, selected));

    if (mode == DiwCfgParameters.INPUT_MODE_MONOSTABLE) {
      setInputOptionBtnApparance(selected, R.drawable.infinity, R.drawable.infinitywhite);
    } else {
      setInputOptionBtnApparance(selected, R.drawable.p100, R.drawable.p100white);
    }
  }

  private void setInputMode(int mode, int behavior, int bi_mode) {
    setBtnApparance(btnInputMonostable, 0, Color.BLACK);
    setBtnApparance(btnInputBistable, 0, Color.BLACK);

    if (mode == DiwCfgParameters.INPUT_MODE_MONOSTABLE) {
      setInputOptionSelected(mode, behavior == DiwCfgParameters.BEHAVIOR_LOOP);
    } else {
      setInputOptionSelected(mode, bi_mode == DiwCfgParameters.BISTABLE_MODE_100P);
    }

    setBtnApparance(inputModeToBtn(mode), R.drawable.rounded_sel_btn, Color.WHITE);
  }

  private void setInputMode(int mode) {
    setInputMode(mode, cfgParameters.getInputBehavior(), cfgParameters.getInputBiMode());
  }

  private void setInputMode() {
    setInputMode(
        cfgParameters.getInputMode(),
        cfgParameters.getInputBehavior(),
        cfgParameters.getInputBiMode());
  }

  private byte optionToByteValue() {
    if (imgInputOption.getTag() != null) {
      InputOptionTag tag = (InputOptionTag) imgInputOption.getTag();
      if (tag.getInputMode() == DiwCfgParameters.INPUT_MODE_MONOSTABLE) {
        return tag.isSelected()
            ? (byte) DiwCfgParameters.BEHAVIOR_LOOP
            : (byte) DiwCfgParameters.BEHAVIOR_NORMAL;
      } else {
        return tag.isSelected()
            ? (byte) DiwCfgParameters.BISTABLE_MODE_100P
            : (byte) DiwCfgParameters.BISTABLE_MODE_RESTORE;
      }
    }

    return -1;
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);

    if (cfgParameters.getLedConfig() != null) {
      int ledConfig = viewToLedConfig(v);
      if (ledConfig != VLCfgParameters.LED_UNKNOWN) {
        setLedConfig(ledConfig);
        calCfgRequest(DIW_CMD_SET_LEDCONFIG, (byte) (ledConfig & 0xFF), null);
      }
    }

    int mode = viewToInputMode(v);
    if (mode != DiwCfgParameters.INPUT_MODE_UNKNOWN) {
      setInputMode(mode);
      calCfgRequest(DIW_CMD_SET_INPUT_MODE, (byte) (mode & 0xFF), null);
    }

    if (v == imgInputOption && imgInputOption.getTag() != null) {
      InputOptionTag tag = (InputOptionTag) imgInputOption.getTag();
      setInputOptionSelected(tag.getInputMode(), !tag.isSelected());

      if (tag.getInputMode() == DiwCfgParameters.INPUT_MODE_BISTABLE) {
        calCfgRequest(DIW_CMD_SET_INPUT_BI_MODE, (byte) (optionToByteValue() & 0xFF), null);
      } else {
        calCfgRequest(DIW_CMD_SET_INPUT_BEHAVIOR, (byte) (optionToByteValue() & 0xFF), null);
      }
    }
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.diw_calibration;
  }

  @Override
  protected void calCfgRequest(int cmd) {

    switch (cmd) {
      case DIW_CMD_SET_MINIMUM:
        calCfgRequest(cmd, null, (short) calibrationWheel.getMinimum());
        break;
      case DIW_CMD_SET_MAXIMUM:
        calCfgRequest(cmd, null, (short) calibrationWheel.getMaximum());
        break;
      default:
        super.calCfgRequest(cmd);
        break;
    }
  }

  @Override
  public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
    calCfgDelayedRequest(minimum ? DIW_CMD_SET_MINIMUM : DIW_CMD_SET_MAXIMUM);
  }

  @Override
  public void onBoostChanged(SuplaRangeCalibrationWheel calibrationWheel) {}

  @Override
  protected void onHide() {
    if (isConfigStarted()) {
      calCfgConfigComplete(false);
    }
  }
}
