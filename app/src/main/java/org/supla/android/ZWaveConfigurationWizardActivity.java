package org.supla.android;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaChannelBasicCfg;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ZWaveConfigurationWizardActivity extends WizardActivity implements AdapterView.OnItemSelectedListener {

    private final int PAGE_ZWAVE_CRITICAL_ERROR = 1;
    private final int PAGE_SELECT_CHANNEL = 2;
    private final int PAGE_CHANNEL_DETAILS = 3;
    private final int PAGE_ZWAVE_DETAILS = 4;

    private Timer mWatchdogTimer;
    private Spinner mChannelListSpinner;
    private Channel mSelectedCahnnel;
    private SuplaChannelBasicCfg mChannelBasicCfg;
    private ArrayList<Channel> mChannelList;
    private Spinner mFunctionListSpinner;
    private ArrayList<Integer> mFuncList;
    private TextView mTvCriticalErrorMessage;
    private TextView mTvDeviceName;
    private TextView mTvSoftVer;
    private TextView mTvChannelNumber;
    private TextView mTvChannelId;
    private TextView mTvCaption;
    private Button mBtnResetAndClear;
    private Button mBtnAddNode;
    private Button mBtnRemoveNode;
    private Button mBtnGetNodeList;
    private boolean deviceReconnectNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFuncList = new ArrayList<>();

        Typeface typeface = SuplaApp.getApp().getTypefaceQuicksandRegular();

        RegisterMessageHandler();
        addStepPage(R.layout.zwave_critical_error, PAGE_ZWAVE_CRITICAL_ERROR);
        addStepPage(R.layout.zwave_select_channel, PAGE_SELECT_CHANNEL);
        addStepPage(R.layout.zwave_channel_details, PAGE_CHANNEL_DETAILS);
        addStepPage(R.layout.zwave_details, PAGE_ZWAVE_DETAILS);

        TextView label = findViewById(R.id.zwave_select_channel_txt);
        label.setTypeface(typeface);

        mTvCriticalErrorMessage = findViewById(R.id.tv_error_txt);
        mChannelListSpinner = findViewById(R.id.zwave_channel_list);
        mFunctionListSpinner = findViewById(R.id.zwave_func_list);
        mTvDeviceName = findViewById(R.id.tv_device_name);
        mTvSoftVer = findViewById(R.id.tv_soft_ver);
        mTvChannelNumber = findViewById(R.id.tv_channel_number);
        mTvChannelId = findViewById(R.id.tv_channel_id);
        mTvCaption = findViewById(R.id.tv_caption);
        mBtnResetAndClear = findViewById(R.id.btnResetAndClear);
        mBtnAddNode = findViewById(R.id.btnAddNode);
        mBtnRemoveNode = findViewById(R.id.btnRemoveNode);
        mBtnGetNodeList = findViewById(R.id.btnGetNodeList);

        mBtnResetAndClear.setOnClickListener(this);
        mBtnAddNode.setOnClickListener(this);
        mBtnRemoveNode.setOnClickListener(this);
        mBtnGetNodeList.setOnClickListener(this);

        mFunctionListSpinner.setOnItemSelectedListener(this);
    }

    private void loadChannelList() {

        DbHelper dbHelper = new DbHelper(this);
        mChannelList = dbHelper.getZWaveBridgeOnlineChannels();
        ArrayList<String>spinnerList = new ArrayList<>();

        for(Channel channel: mChannelList) {
            spinnerList.add("#"+channel.getChannelId()
                    + " " + channel.getCaption());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mChannelListSpinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceReconnectNeeded = false;
        showPage(PAGE_SELECT_CHANNEL);
    }

    private void wathdogDeactivate() {
        if (mWatchdogTimer != null) {
            mWatchdogTimer.cancel();
            mWatchdogTimer = null;
        }
    }

    private void wathdogActivate(final int timeoutMS, final int msgResId) {

        wathdogDeactivate();

        if (timeoutMS == 0) {
            return;
        }

        mWatchdogTimer = new Timer();
        mWatchdogTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        mWatchdogTimer.cancel();
                        mWatchdogTimer = null;
                        showCriticalError(getResources().getString(msgResId));
                    }
                });
            }
        }, timeoutMS, 1000);
    }

    @Override
    protected void showPage(int pageId) {
        super.showPage(pageId);

        wathdogDeactivate();

        setBtnNextPreloaderVisible(false);
        setBtnNextEnabled(true);
        setBtnNextText(R.string.next);

        switch (pageId) {
            case PAGE_SELECT_CHANNEL:
                loadChannelList();
                break;
            case PAGE_ZWAVE_CRITICAL_ERROR:
            case PAGE_ZWAVE_DETAILS:
                setBtnNextText(R.string.ok);
                break;
        }
    }

    private int getDevivceId() {
        return mChannelBasicCfg == null ? 0 : mChannelBasicCfg.getDeviceId();
    }

    private void zwaveResetAndClear() {
        if (getDevivceId() == 0) {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveResetAndClear(getDevivceId());
        }
    }

    private void zwaveAddNode() {
        if (getDevivceId() == 0) {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveAddNode(getDevivceId());
        }
    }

    private void zwaveRemoveNode() {
        if (getDevivceId() == 0) {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveRemoveNode(getDevivceId());
        }
    }

    private void zwaveGetNodeList() {
        if (getDevivceId() == 0) {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveGetNodeList(getDevivceId());
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == mBtnResetAndClear) {
            zwaveResetAndClear();
        } else if (v == mBtnAddNode) {
            zwaveAddNode();
        } else if (v == mBtnRemoveNode) {
            zwaveRemoveNode();
        } else if (v ==mBtnGetNodeList) {
            zwaveGetNodeList();
        }
    }

    @Override
    protected void onBtnNextClick() {
        switch (getVisiblePageId()) {
            case PAGE_ZWAVE_CRITICAL_ERROR:
                showMain();
                break;
            case PAGE_SELECT_CHANNEL:
                {
                int position = mChannelListSpinner.getSelectedItemPosition();
                if (position  >= 0 && position  < mChannelList.size()) {
                    wathdogActivate(5000, R.string.zwave_error_get_basic_cfg_timeout);
                    setBtnNextEnabled(false);
                    setBtnNextPreloaderVisible(true);
                    SuplaClient client = SuplaApp.getApp().getSuplaClient();
                    if (client!=null){
                        mSelectedCahnnel = mChannelList.get(position);
                        client.getChannelBasicCfg(mSelectedCahnnel.getChannelId());
                    }
                }
                }
                break;
            case PAGE_CHANNEL_DETAILS:
                int position = mFunctionListSpinner.getSelectedItemPosition();
                Integer func = Integer.valueOf(mChannelBasicCfg.getFunc());

                setBtnNextEnabled(false);
                setBtnNextPreloaderVisible(true);

                if (mFuncList.get(position).equals(func)) {
                    OnChannelFunctionSetResult(mChannelBasicCfg.getChannelId(),
                            mChannelBasicCfg.getFunc(),
                            SuplaConst.SUPLA_RESULTCODE_TRUE);
                } else {
                    wathdogActivate(5000, R.string.zwave_error_set_function_timeout);
                    SuplaClient client = SuplaApp.getApp().getSuplaClient();
                    if (client!=null){
                        deviceReconnectNeeded = true;
                        client.setChannelFunction(mChannelBasicCfg.getChannelId(),
                                mFuncList.get(position).intValue());
                    }
                }
                break;
        }
    }

    @Override
    protected void OnChannelBasicCfg(SuplaChannelBasicCfg basicCfg) {

        setBtnNextPreloaderVisible(false);

        if (isFinishing()) {
            return;
        }

        mChannelBasicCfg  = basicCfg;
        mTvDeviceName.setText(basicCfg.getDeviceName());
        mTvSoftVer.setText(basicCfg.getDeviceSoftwareVersion());
        mTvChannelNumber.setText(Integer.toString(basicCfg.getNumber()));
        mTvChannelId.setText(Integer.toString(basicCfg.getChannelId()));
        mTvCaption.setText(basicCfg.getCaption());

        String functionName = SuplaConst.getFunctionName(0, this);
        ArrayList<String>spinnerList = new ArrayList<>();
        int position = 0;

        mFuncList.clear();
        mFuncList.add(Integer.valueOf(0));
        spinnerList .add(functionName);

        for(int a=0;a<32;a++) {

            int func = SuplaConst.functionBit2functionNumber(basicCfg.getFuncList() & 1<<a);
            if (func > 0) {
                functionName = SuplaConst.getFunctionName(func, this);
                mFuncList.add(Integer.valueOf(func));
                spinnerList.add(functionName);
                if (func == basicCfg.getFunc()) {
                    position = spinnerList.size() - 1;
                }
            }
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        mFunctionListSpinner.setAdapter(adapter);
        mFunctionListSpinner.setSelection(position, false);

        showPage(PAGE_CHANNEL_DETAILS);
    };

    private void showMain() {
        wathdogDeactivate();

        if (deviceReconnectNeeded) {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.reconnectDevice(mChannelBasicCfg.getDeviceId());
            }
        }

        setBtnNextPreloaderVisible(false);
        showMain(this);
    }

    private void showCriticalError(String message) {
        wathdogDeactivate();
        mTvCriticalErrorMessage.setText(message);
        showPage(PAGE_ZWAVE_CRITICAL_ERROR);
    }

    @Override
    protected void OnChannelFunctionSetResult(int channelId, int func, int code) {
        super.OnChannelFunctionSetResult(channelId, func, code);

        wathdogDeactivate();

        if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {
            showCriticalError(getResources().getString(R.string.zwave_error_function_change_error,
                Integer.toString(code)));
            return;
        }

        if (func == 0) {
            showMain();
        } else {
            showPage(PAGE_ZWAVE_DETAILS);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mFunctionListSpinner
                && position >= 0 && position < mFuncList.size()) {
            if (mFuncList.get(position).intValue() == 0) {
                setBtnNextText(R.string.ok);
            } else {
                setBtnNextText(R.string.next);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
