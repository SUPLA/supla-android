package org.supla.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class VLCalibrationTool implements View.OnClickListener, SuplaRangeCalibrationWheel.OnChangeListener {
    private ChannelDetailRGB detailRGB;
    private Button btnOK;
    private Button btnDmAuto;
    private Button btnDm1;
    private Button btnDm2;
    private Button btnDm3;
    private Button btnDriveAuto;
    private Button btnDriveYes;
    private Button btnDriveNo;
    private Button btnOpRange;
    private Button btnDrive;
    private SuplaRangeCalibrationWheel calibrationWheel;
    private RelativeLayout mainView;
    private long uiRefreshLockTime = 0;

    private final static int MODE_UNKNOWN = -1;
    private final static int MODE_AUTO = 0;
    private final static int MODE_1 = 1;
    private final static int MODE_2 = 2;
    private final static int MODE_3 = 3;

    private final static int DRIVE_UNKNOWN = -1;
    private final static int DRIVE_AUTO = 0;
    private final static int DRIVE_YES = 1;
    private final static int DRIVE_NO = 2;

    private final static int VL_MSG_CONFIGURATION_MODE = 0x44;
    private final static int VL_MSG_CONFIGURATION_ACK = 0x45;
    private final static int VL_MSG_CONFIG_COMPLETE = 0x46;
    private final static int VL_MSG_SET_MODE = 0x58;
    private final static int VL_MSG_SET_MINIMUM = 0x59;
    private final static int VL_MSG_SET_MAXIMUM = 0x5A;
    private final static int VL_MSG_SET_DRIVE = 0x5B;
    private final static int VL_MSG_SET_DRIVE_LEVEL = 0x5C;
    private final static int VL_MSG_SET_CHILD_LOCK = 0x18;

    private final static int UI_REFRESH_LOCK_TIME = 2000;
    private final static int MIN_DELAY_TIME = 500;

    private Timer delayTimer1 = null;
    private long lastCalCfgTime = 0;

    class DelayedTask extends TimerTask {
        private int msg;

        DelayedTask(int msg) {
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
        btnDmAuto = getBtn(R.id.vlCfgDmAuto);
        btnDm1 = getBtn(R.id.vlCfgDm1);
        btnDm2 = getBtn(R.id.vlCfgDm2);
        btnDm3 = getBtn(R.id.vlCfgDm3);

        btnDriveAuto = getBtn(R.id.vlCfgDriveAuto);
        btnDriveYes = getBtn(R.id.vlCfgDriveYes);
        btnDriveNo = getBtn(R.id.vlCfgDriveNo);
        btnOpRange = getBtn(R.id.vlCfgOpRange);
        btnDrive = getBtn(R.id.vlCfgDrive);
        calibrationWheel = mainView.findViewById(R.id.vlCfgCalibrationWheel);
        calibrationWheel.setOnChangeListener(this);

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
        if (btn == btnDmAuto) {
            return MODE_AUTO;
        } else if (btn == btnDm1) {
            return MODE_1;
        } else if (btn == btnDm2) {
            return MODE_2;
        } else if (btn == btnDm3) {
            return MODE_3;
        }
        return MODE_UNKNOWN;
    }

    private int viewToDrive(View btn) {
        if (btn == btnDriveAuto) {
            return DRIVE_AUTO;
        } else if (btn == btnDriveYes) {
            return DRIVE_YES;
        } else if (btn == btnDriveNo) {
            return DRIVE_NO;
        }

        return DRIVE_UNKNOWN;
    }

    private void setBtnApparance(Button btn, int resid, int textColor) {
        btn.setBackgroundResource(resid);
        btn.setTextColor(textColor);
    }

    private void setMode(int mode) {
        setBtnApparance(btnDmAuto, R.drawable.vl_left_btn_off, Color.BLACK);
        setBtnApparance(btnDm1, R.drawable.vl_middle_btn_off, Color.BLACK);
        setBtnApparance(btnDm2, R.drawable.vl_middle_btn_off, Color.BLACK);
        setBtnApparance(btnDm3, R.drawable.vl_right_btn_off, Color.BLACK);

        switch (mode) {
            case MODE_AUTO:
                setBtnApparance(btnDmAuto, R.drawable.vl_left_btn_on, Color.WHITE);
                break;
            case MODE_1:
                setBtnApparance(btnDm1, R.drawable.vl_middle_btn_on, Color.WHITE);
                break;
            case MODE_2:
                setBtnApparance(btnDm2, R.drawable.vl_middle_btn_on, Color.WHITE);
                break;
            case MODE_3:
                setBtnApparance(btnDm3, R.drawable.vl_right_btn_on, Color.WHITE);
                break;
        }
    }

    private void setDrive(int drive) {
        setBtnApparance(btnDriveAuto, R.drawable.vl_left_btn_off, Color.BLACK);
        setBtnApparance(btnDriveYes, R.drawable.vl_middle_btn_off, Color.BLACK);
        setBtnApparance(btnDriveNo, R.drawable.vl_right_btn_off, Color.BLACK);

        switch (drive) {
            case DRIVE_AUTO:
                setBtnApparance(btnDriveAuto, R.drawable.vl_left_btn_on, Color.WHITE);
                break;
            case DRIVE_YES:
                setBtnApparance(btnDriveYes, R.drawable.vl_middle_btn_on, Color.WHITE);
                break;
            case DRIVE_NO:
                setBtnApparance(btnDriveNo, R.drawable.vl_right_btn_on, Color.WHITE);
                break;
        }

        if (drive == DRIVE_YES) {
            btnDrive.setVisibility(View.VISIBLE);
        } else {
            btnDrive.setVisibility(View.INVISIBLE);
            displayOpRange(true);
        }
    }

    private void displayOpRange(boolean display) {
        if (display) {
            setBtnApparance(btnOpRange, R.drawable.vl_tab_on, Color.WHITE);
            setBtnApparance(btnDrive, R.drawable.vl_tab_off, Color.BLACK);
            calibrationWheel.setDriveVisible(false);
        } else {
            setBtnApparance(btnOpRange, R.drawable.vl_tab_off, Color.BLACK);
            setBtnApparance(btnDrive, R.drawable.vl_tab_on, Color.WHITE);
            calibrationWheel.setDriveVisible(true);
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

    public void onClick(View v) {

        if (v == btnOK) {
            Hide();
            return;
        }

        int mode = viewToMode(v);
        if (mode != MODE_UNKNOWN) {
            setMode(mode);
            calCfgRequest(VL_MSG_SET_MODE, (byte)(mode & 0xFF), null);
        }

        int drive = viewToDrive(v);
        if (drive != DRIVE_UNKNOWN) {
            setDrive(drive);
            calCfgRequest(VL_MSG_SET_MODE, (byte)(drive & 0xFF), null);

            if (drive == DRIVE_YES) {
                onDriveChanged(calibrationWheel);
            }
        }

        if (v == btnOpRange) {
            displayOpRange(true);
        } else if (v == btnDrive) {
            displayOpRange(false);
        }
    }

    public void Show() {
        SuperuserAuthorizationDialog Dialog =
                new SuperuserAuthorizationDialog(detailRGB.getContext());
        Dialog.show();
        /*
        setMode(MODE_AUTO);
        setDrive(DRIVE_AUTO);
        detailRGB.getContentView().setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);
        */
    }

    public void Hide() {
        mainView.setVisibility(View.GONE);
        detailRGB.getContentView().setVisibility(View.VISIBLE);
    }

    public boolean isVisible() {
        return mainView.getVisibility() == View.VISIBLE;
    }

    private void calCfgDelayed(int msg) {
        if (delayTimer1!=null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }

        if (System.currentTimeMillis() - lastCalCfgTime >= MIN_DELAY_TIME) {
            Trace.d("VL", "MSG: "+Integer.toString(msg));
            switch (msg) {
                case VL_MSG_SET_MINIMUM:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getMinimum()));
                    break;
                case VL_MSG_SET_MAXIMUM:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getMaximum()));
                    break;
                case VL_MSG_SET_DRIVE_LEVEL:
                    calCfgRequest(msg, null,
                            new Short((short)calibrationWheel.getDriveLevel()));
                    break;
            }
        } else {

            long delayTime = 1;

            if (System.currentTimeMillis() - lastCalCfgTime < MIN_DELAY_TIME)
                delayTime = MIN_DELAY_TIME - (System.currentTimeMillis() - lastCalCfgTime) + 1;

            delayTimer1 = new Timer();

            if (delayTime < 1) {
                delayTime = 1;
            }

            delayTimer1.schedule(new DelayedTask(msg), delayTime, 1000);
        }
        
    }

    @Override
    public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
        LockUIrefresh();
        calCfgDelayed(minimum ? VL_MSG_SET_MINIMUM : VL_MSG_SET_MAXIMUM);
    }

    @Override
    public void onDriveChanged(SuplaRangeCalibrationWheel calibrationWheel) {
        LockUIrefresh();
        calCfgDelayed(VL_MSG_SET_DRIVE);
    }
}
