package org.supla.android;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class VLCalibrationTool implements View.OnClickListener, SuplaRangeCalibrationWheel.OnChangeListener {
    private ChannelDetailRGB Parent;
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

    private Timer delayTimer1 = null;

    private Button getBtn(int resid) {
        Button btn = Parent.findViewById(resid);
        btn.setOnClickListener(this);
        return btn;
    }

    public void setParent(ChannelDetailRGB parent) {
        Parent = parent;
        if (parent==null) {
            btnDmAuto = null;
            btnDm1 = null;
            btnDm2 = null;
            btnDm3 = null;

            btnDriveAuto = null;
            btnDriveYes = null;
            btnDriveNo = null;
            btnOpRange = null;
            btnDrive = null;
            calibrationWheel = null;
        } else {
            btnDmAuto = getBtn(R.id.vlCfgDmAuto);
            btnDm1 = getBtn(R.id.vlCfgDm1);
            btnDm2 = getBtn(R.id.vlCfgDm2);
            btnDm3 = getBtn(R.id.vlCfgDm3);

            btnDriveAuto = getBtn(R.id.vlCfgDriveAuto);
            btnDriveYes = getBtn(R.id.vlCfgDriveYes);
            btnDriveNo = getBtn(R.id.vlCfgDriveNo);
            btnOpRange = getBtn(R.id.vlCfgOpRange);
            btnDrive = getBtn(R.id.vlCfgDrive);
            calibrationWheel = parent.findViewById(R.id.vlCfgCalibrationWheel);
            calibrationWheel.setOnChangeListener(this);
        }

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
        uiRefreshLockTime = System.currentTimeMillis()+2000;
    }

    public void onClick(View v) {
        int mode = viewToMode(v);
        LockUIrefresh();

        if (mode != MODE_UNKNOWN) {
            setMode(mode);
            if (Parent!=null) {
                Parent.deviceCalCfgRequest(VL_MSG_SET_MODE, new Byte((byte)(mode & 0xFF)));
            }
        }

        int drive = viewToDrive(v);
        if (drive != DRIVE_UNKNOWN) {
            setDrive(drive);
            if (Parent!=null) {
                Parent.deviceCalCfgRequest(VL_MSG_SET_DRIVE, new Byte((byte)(drive & 0xFF)));
            }
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

    public void onShow() {
        setMode(MODE_AUTO);
        setDrive(DRIVE_AUTO);
    }

    private void sendDelayed(int msg) {
//        Trace.d("VL", "Send range value. Min: "+Boolean.toString(minimum)+" Min:"+Double.toString(calibrationWheel.getMinimum())+" Max:"+Double.toString(calibrationWheel.getMaximum()));

        if (delayTimer1!=null) {
            delayTimer1.cancel();
            delayTimer1 = null;
        }
        
    }

    @Override
    public void onRangeChanged(SuplaRangeCalibrationWheel calibrationWheel, boolean minimum) {
        LockUIrefresh();
        sendDelayed(minimum ? VL_MSG_SET_MINIMUM : VL_MSG_SET_MAXIMUM);
    }

    @Override
    public void onDriveChanged(SuplaRangeCalibrationWheel calibrationWheel) {
        LockUIrefresh();
        sendDelayed(VL_MSG_SET_DRIVE);
    }
}
