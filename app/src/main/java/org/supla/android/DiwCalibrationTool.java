package org.supla.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.supla.android.lib.SuplaConst;

import java.util.Timer;
import java.util.TimerTask;

public class DiwCalibrationTool extends DimmerCalibrationTool
        implements SuplaRangeCalibrationWheel.OnChangeListener {

    public final static int DIW_CMD_ENTER_CFG_MODE = 0x1;
    public final static int DIW_CMD_CONFIGURATION_REPORT = 0x1;
    public final static int DIW_CMD_EXIT_CFG_MODE = 0x3;
    public final static int DIW_CMD_SET_MINIMUM = 0x4;
    public final static int DIW_CMD_SET_MAXIMUM = 0x5;
    public final static int DIW_CMD_SET_LEDCONFIG = 0x6;

    private final SuplaRangeCalibrationWheel calibrationWheel;
    private final DiwCfgParameters cfgParameters;
    private final TextView tvSTMFirmwareVersion;

    public DiwCalibrationTool(ChannelDetailRGBW detailRGB) {
        super(detailRGB);

        calibrationWheel = getMainView().findViewById(R.id.diwCfgCalibrationWheel);
        calibrationWheel.setOnChangeListener(this);
        cfgParameters = new DiwCfgParameters();

        setImgViews(R.id.diwCfgLedImgOn, R.id.diwCfgLedImgOff, R.id.diwCfgLedImgAlwaysOff);
        setButtons(R.id.diwBtnOK, 0, R.id.diwBtnInfo);

        tvSTMFirmwareVersion = getMainView().findViewById(R.id.diwCfgSTMFirmwareVersion);
    }

    @Override
    protected void onSuperuserOnAuthorizarionSuccess()  {
        Trace.d("calcfg", "onSuperuserOnAuthorizarionSuccess");
        calCfgRequest(DIW_CMD_ENTER_CFG_MODE);
    }

    @Override
    protected void onCalCfgResult(int Command, int Result, byte[] Data) {
        Trace.d("calcfgResult", Integer.toString(Command)+","+Integer.toString(Result));
    }

    private void setLedConfig() {
        setLedConfig(cfgParameters);
    }


    @Override
    protected void displayCfgParameters() {
        setLedConfig();
        /*
        calibrationWheel.setRightEdge(cfgParameters.getRightEdge());
        calibrationWheel.setLeftEdge(cfgParameters.getLeftEdge());
        calibrationWheel.setMinMax(cfgParameters.getMinimum(), cfgParameters.getMaximum());
        tvSTMFirmwareVersion.setText(cfgParameters.getSTMFirmwareVersion());
         */
    }

    @Override
    protected void showInformationDialog() {
    }

    @Override
    protected void doRestore() {
    }

    @Override
    protected void saveChanges() {
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (cfgParameters.getLedConfig() != null) {
            int ledConfig = viewToLedConfig(v);
            if (ledConfig != VLCfgParameters.LED_UNKNOWN) {
                setLedConfig(ledConfig);
                //calCfgRequest(VL_CALCFG_MSG_SET_LED_CONFIG, (byte) (ledConfig & 0xFF), null);
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
                    /*
            case VL_MSG_SET_MINIMUM:
                calCfgRequest(cmd, null,
                        (short) calibrationWheel.getMinimum());
                break;
            case VL_MSG_SET_MAXIMUM:
                calCfgRequest(cmd, null,
                        (short) calibrationWheel.getMaximum());
                break;
                     */
            default:
                super.calCfgRequest(cmd);
                break;
        }

    }

    @Override
    public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
       // calCfgDelayedRequest(minimum ? VL_MSG_SET_MINIMUM : VL_MSG_SET_MAXIMUM);
    }

    @Override
    public void onBoostChanged(SuplaRangeCalibrationWheel calibrationWheel) {
    }

    @Override
    protected void onHide() {
        if (isConfigStarted()) {
            byte[] data = new byte[1];
           // getDetailRGB().deviceCalCfgRequest(VL_MSG_CONFIG_COMPLETE, 0, data, true);
        }
    }
}
