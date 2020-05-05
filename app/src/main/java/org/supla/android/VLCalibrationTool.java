package org.supla.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.supla.android.lib.SuplaClientMsg;
import org.supla.android.lib.SuplaConst;

import java.util.Timer;
import java.util.TimerTask;

public class VLCalibrationTool implements View.OnClickListener, SuplaRangeCalibrationWheel.OnChangeListener, SuperuserAuthorizationDialog.OnAuthorizarionResultListener {
    private ChannelDetailRGB detailRGB;
    private Button btnOK;
    private Button btnRestore;
    private Button btnCancel;
    private Button btnDmAuto;
    private Button btnDm1;
    private Button btnDm2;
    private Button btnDm3;
    private Button btnBoostAuto;
    private Button btnBoostYes;
    private Button btnBoostNo;
    private Button btnOpRange;
    private Button btnBoost;
    private SuplaRangeCalibrationWheel calibrationWheel;
    private RelativeLayout mainView;
    private SuperuserAuthorizationDialog authDialog;
    private long uiRefreshLockTime = 0;
    private boolean configStarted = false;
    private VLCfgParameters cfgParameters;
    private int mColorDisabled;

    private final static int VL_MSG_RESTORE_DEFAULTS = 0x4E;
    private final static int VL_MSG_CONFIGURATION_MODE = 0x44;
    private final static int VL_MSG_CONFIGURATION_ACK = 0x45;
    private final static int VL_MSG_CONFIGURATION_QUERY = 0x15;
    private final static int VL_MSG_CONFIGURATION_REPORT = 0x51;
    private final static int VL_MSG_CONFIG_COMPLETE = 0x46;
    private final static int VL_MSG_SET_MODE = 0x58;
    private final static int VL_MSG_SET_MINIMUM = 0x59;
    private final static int VL_MSG_SET_MAXIMUM = 0x5A;
    private final static int VL_MSG_SET_BOOST = 0x5B;
    private final static int VL_MSG_SET_BOOST_LEVEL = 0x5C;
    private final static int VL_MSG_SET_CHILD_LOCK = 0x18;

    private final static int UI_REFRESH_LOCK_TIME = 2000;
    private final static int MIN_SEND_DELAY_TIME = 500;
    private final static int DISPLAY_DELAY_TIME = 1000;

    private Timer delayTimer1 = null;
    private Timer delayTimer2 = null;

    private long lastCalCfgTime = 0;
    private Handler _sc_msg_handler = null;

    class DisplayDelayedTask extends TimerTask {
        private int msg;

        DisplayDelayedTask(int msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            if (detailRGB != null && detailRGB.getContext() instanceof Activity) {
                ((Activity) detailRGB.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        calCfgDelayed(msg);
                    }
                });
            }
        }
    }

    public VLCalibrationTool(ChannelDetailRGB detailRGB) {

        this.detailRGB = detailRGB;
        mainView = (RelativeLayout)detailRGB.inflateLayout(R.layout.vl_calibration);
        mainView.setVisibility(View.GONE);
        detailRGB.addView(mainView);

        btnOK = getBtn(R.id.vlBtnOK);
        btnRestore = getBtn(R.id.vlBtnRestore);
        btnCancel = getBtn(R.id.vlBtnCancel);
        btnDmAuto = getBtn(R.id.vlCfgDmAuto);
        btnDm1 = getBtn(R.id.vlCfgDm1);
        btnDm2 = getBtn(R.id.vlCfgDm2);
        btnDm3 = getBtn(R.id.vlCfgDm3);

        btnBoostAuto = getBtn(R.id.vlCfgBoostAuto);
        btnBoostYes = getBtn(R.id.vlCfgBoostYes);
        btnBoostNo = getBtn(R.id.vlCfgBoostNo);
        btnOpRange = getBtn(R.id.vlCfgOpRange);
        btnBoost = getBtn(R.id.vlCfgBoost);
        calibrationWheel = mainView.findViewById(R.id.vlCfgCalibrationWheel);
        calibrationWheel.setOnChangeListener(this);
        cfgParameters = new VLCfgParameters();

        mColorDisabled =
                detailRGB.getContext().getResources().getColor(R.color.vl_btn_disabled);
    }

    private void registerMessageHandler() {
        if ( _sc_msg_handler != null)
            return;

        _sc_msg_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                SuplaClientMsg _msg = (SuplaClientMsg)msg.obj;
                if (_msg != null
                        && _msg.getType() == SuplaClientMsg.onCalCfgResult
                        && detailRGB != null
                        && detailRGB.isDetailVisible()
                        && _msg.getChannelId() == detailRGB.getRemoteId()) {
                    onCalCfgResult(_msg.getCommand(), _msg.getResult(), _msg.getData());
                }
            }
        };

        SuplaApp.getApp().addMsgReceiver(_sc_msg_handler);
    }

    private void unregisterMessageHandler() {
        if ( _sc_msg_handler != null ) {
            SuplaApp.getApp().removeMsgReceiver(_sc_msg_handler);
            _sc_msg_handler = null;
        }
    }

    @Override
    public void onSuperuserOnAuthorizarionResult(SuperuserAuthorizationDialog dialog,
                                                 boolean Success, int Code) {
        if (Success) {
            registerMessageHandler();
            calCfgRequest(VL_MSG_CONFIGURATION_MODE);
        } else {
            unregisterMessageHandler();
        }
    }

    @Override
    public void authorizationCanceled() {
        unregisterMessageHandler();
    }

    private void onCalCfgResult(int Command, int Result, byte[] Data) {
        switch (Command) {
            case VL_MSG_CONFIGURATION_ACK:
                if (Result == SuplaConst.SUPLA_RESULTCODE_TRUE && authDialog != null) {
                    authDialog.close();
                    authDialog = null;

                    displayCfgParameters(true);
                    detailRGB.getContentView().setVisibility(View.GONE);
                    mainView.setVisibility(View.VISIBLE);
                    configStarted = true;
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

    public View getMainView() {
        return getMainView();
    }

    private Button getBtn(int resid) {
        Button btn = mainView.findViewById(resid);
        btn.setOnClickListener(this);
        return btn;
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
        return mode == VLCfgParameters.MODE_UNKNOWN || cfgParameters.isModeDisabled(mode) ?
                VLCfgParameters.MODE_UNKNOWN : mode;
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

        return boost == VLCfgParameters.BOOST_UNKNOWN || cfgParameters.isBoostDisabled(boost) ?
                VLCfgParameters.BOOST_UNKNOWN : boost;
    }

    private void setBtnApparance(Button btn, int resid, int textColor) {
        btn.setBackgroundResource(resid);
        btn.setTextColor(textColor);
    }


    private void setMode(int mode) {

        setBtnApparance(btnDmAuto, R.drawable.vl_left_btn_off,
                cfgParameters.isModeDisabled(VLCfgParameters.MODE_AUTO)
                        ? mColorDisabled : Color.BLACK);

        setBtnApparance(btnDm1, R.drawable.vl_middle_btn_off,
                cfgParameters.isModeDisabled(VLCfgParameters.MODE_1)
                        ? mColorDisabled : Color.BLACK);

        setBtnApparance(btnDm2, R.drawable.vl_middle_btn_off,
                cfgParameters.isModeDisabled(VLCfgParameters.MODE_2)
                        ? mColorDisabled : Color.BLACK);

        setBtnApparance(btnDm3, R.drawable.vl_right_btn_off,
                cfgParameters.isModeDisabled(VLCfgParameters.MODE_3)
                        ? mColorDisabled : Color.BLACK);

        if (!cfgParameters.isModeDisabled(mode)) {
            switch (mode) {
                case VLCfgParameters.MODE_AUTO:
                    setBtnApparance(btnDmAuto, R.drawable.vl_left_btn_on, Color.WHITE);
                    break;
                case VLCfgParameters.MODE_1:
                    setBtnApparance(btnDm1, R.drawable.vl_middle_btn_on, Color.WHITE);
                    break;
                case VLCfgParameters.MODE_2:
                    setBtnApparance(btnDm2, R.drawable.vl_middle_btn_on, Color.WHITE);
                    break;
                case VLCfgParameters.MODE_3:
                    setBtnApparance(btnDm3, R.drawable.vl_right_btn_on, Color.WHITE);
                    break;
            }
        }
    }

    private void setMode() {
        setMode(cfgParameters.getMode());
    }

    private void setBoost(int boost) {
        setBtnApparance(btnBoostAuto, R.drawable.vl_left_btn_off,
                cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_AUTO)
                        ? mColorDisabled : Color.BLACK);

        setBtnApparance(btnBoostYes, R.drawable.vl_middle_btn_off,
                cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_YES)
                        ? mColorDisabled : Color.BLACK);

        setBtnApparance(btnBoostNo, R.drawable.vl_right_btn_off,
                cfgParameters.isBoostDisabled(VLCfgParameters.BOOST_NO)
                        ? mColorDisabled : Color.BLACK);

        if (!cfgParameters.isBoostDisabled(boost)) {
            switch (boost) {
                case VLCfgParameters.BOOST_AUTO:
                    setBtnApparance(btnBoostAuto, R.drawable.vl_left_btn_on, Color.WHITE);
                    break;
                case VLCfgParameters.BOOST_YES:
                    setBtnApparance(btnBoostYes, R.drawable.vl_middle_btn_on, Color.WHITE);
                    break;
                case VLCfgParameters.BOOST_NO:
                    setBtnApparance(btnBoostNo, R.drawable.vl_right_btn_on, Color.WHITE);
                    break;
            }

            if (boost == VLCfgParameters.BOOST_YES) {
                btnBoost.setVisibility(View.VISIBLE);
                return;
            }
        }

        btnBoost.setVisibility(View.INVISIBLE);
        displayOpRange(true);
    }

    private void setBoost() {
        setBoost(cfgParameters.getBoost());
    }

    private void displayOpRange(boolean display) {
        if (display) {
            setBtnApparance(btnOpRange, R.drawable.vl_tab_on, Color.WHITE);
            setBtnApparance(btnBoost, R.drawable.vl_tab_off, Color.BLACK);
            calibrationWheel.setBoostVisible(false);
        } else {
            setBtnApparance(btnOpRange, R.drawable.vl_tab_off, Color.BLACK);
            setBtnApparance(btnBoost, R.drawable.vl_tab_on, Color.WHITE);
            calibrationWheel.setBoostVisible(true);
        }
    }

    private void displayCfgParameters(boolean force) {
        if (delayTimer2!=null) {
            delayTimer2.cancel();
            delayTimer2 = null;
        }

        if (force || System.currentTimeMillis() - lastCalCfgTime >= DISPLAY_DELAY_TIME) {
            setMode();
            setBoost();
            calibrationWheel.setRightEdge(cfgParameters.getRightEdge());
            calibrationWheel.setLeftEdge(cfgParameters.getLeftEdge());
            calibrationWheel.setMinMax(cfgParameters.getMinimum(), cfgParameters.getMaximum());
            calibrationWheel.setBoostLevel(cfgParameters.getBoostLevel());
        } else {

            long delayTime = 1;

            if (System.currentTimeMillis() - lastCalCfgTime < DISPLAY_DELAY_TIME)
                delayTime = DISPLAY_DELAY_TIME - (System.currentTimeMillis() - lastCalCfgTime) + 1;

            delayTimer2 = new Timer();

            if (delayTime < 1) {
                delayTime = 1;
            }

            delayTimer2.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (detailRGB != null && detailRGB.getContext() instanceof Activity) {
                        ((Activity) detailRGB.getContext()).runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                displayCfgParameters(false);
                            }
                        });
                    }
                }
            }, delayTime, 1000);
        }
    }

    private void LockUIrefresh() {
        uiRefreshLockTime = System.currentTimeMillis()+UI_REFRESH_LOCK_TIME;
    }

    private void calCfgRequest(int cmd, Byte bdata, Short sdata) {
        if (detailRGB ==null) {
            return;
        }

        LockUIrefresh();
        lastCalCfgTime = System.currentTimeMillis();

        if (bdata != null) {
            detailRGB.deviceCalCfgRequest(cmd, bdata);
        } else if (sdata != null) {
            detailRGB.deviceCalCfgRequest(cmd, sdata);
        } else {
            detailRGB.deviceCalCfgRequest(cmd);
        }
    }

    private void calCfgRequest(int cmd) {
        calCfgRequest(cmd, null, null);
    }

    private void showRestoreConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(detailRGB.getContext());
        builder.setMessage(R.string.restore_question);

        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        calCfgDelayed(VL_MSG_RESTORE_DEFAULTS);
                    }
                });

        builder.setNeutralButton(R.string.no,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onClick(View v) {

        if (v == btnOK) {
            configStarted = false;
            calCfgRequest(VL_MSG_CONFIG_COMPLETE, (byte) 1, null);
            Hide();
            return;
        } else if (v == btnRestore) {
            showRestoreConfirmDialog();
        } else if (v == btnCancel) {
            Hide();
            return;
        }

        int mode = viewToMode(v);
        if (mode != VLCfgParameters.MODE_UNKNOWN) {
            setMode(mode);
            calCfgRequest(VL_MSG_SET_MODE, (byte)(mode & 0xFF), null);
        }

        int boost = viewToBoost(v);
        if (boost != VLCfgParameters.BOOST_UNKNOWN) {
            setBoost(boost);
            calCfgRequest(VL_MSG_SET_BOOST, (byte)(boost & 0xFF), null);

            if (boost == VLCfgParameters.BOOST_YES) {
                onBoostChanged(calibrationWheel);
            }
        }

        if (v == btnOpRange) {
            displayOpRange(true);
        } else if (v == btnBoost) {
            displayOpRange(false);
        }
    }

    public void Show() {
        if (authDialog!=null) {
            authDialog.close();
            authDialog = null;
        }

        authDialog =
                new SuperuserAuthorizationDialog(detailRGB.getContext());
        authDialog.setOnAuthorizarionResultListener(this);
        authDialog.show();
    }

    public void Hide() {

        unregisterMessageHandler();
        if (mainView.getVisibility() == View.VISIBLE) {
            mainView.setVisibility(View.GONE);
            detailRGB.getContentView().setVisibility(View.VISIBLE);

            if (configStarted) {
                configStarted = false;
                byte[] data = new byte[1];
                detailRGB.deviceCalCfgRequest(VL_MSG_CONFIG_COMPLETE, 0, data, true);
            }
        }

    }

    public boolean isVisible() {
        return mainView.getVisibility() == View.VISIBLE;
    }

    private void calCfgDelayed(int msg) {
        if (delayTimer1!=null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }

        if (System.currentTimeMillis() - lastCalCfgTime >= MIN_SEND_DELAY_TIME) {
            switch (msg) {
                case VL_MSG_SET_MINIMUM:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getMinimum()));
                    break;
                case VL_MSG_SET_MAXIMUM:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getMaximum()));
                    break;
                case VL_MSG_SET_BOOST_LEVEL:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getBoostLevel()));
                    break;
                default:
                    calCfgRequest(msg);
                    break;
            }
        } else {

            long delayTime = 1;

            if (System.currentTimeMillis() - lastCalCfgTime < MIN_SEND_DELAY_TIME)
                delayTime = MIN_SEND_DELAY_TIME - (System.currentTimeMillis() - lastCalCfgTime) + 1;

            delayTimer1 = new Timer();

            if (delayTime < 1) {
                delayTime = 1;
            }

            delayTimer1.schedule(new DisplayDelayedTask(msg), delayTime, 1000);
        }
        
    }

    @Override
    public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
        LockUIrefresh();
        calCfgDelayed(minimum ? VL_MSG_SET_MINIMUM : VL_MSG_SET_MAXIMUM);
    }

    @Override
    public void onBoostChanged(SuplaRangeCalibrationWheel calibrationWheel) {
        LockUIrefresh();
        calCfgDelayed(VL_MSG_SET_BOOST_LEVEL);
    }
}
