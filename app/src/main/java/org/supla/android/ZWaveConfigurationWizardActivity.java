package org.supla.android;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
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
import org.supla.android.lib.ZWaveNode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ZWaveConfigurationWizardActivity extends WizardActivity implements AdapterView.OnItemSelectedListener {

    private final int PAGE_ZWAVE_ERROR = 1;
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
    private TextView mTvErrorMessage;
    private TextView mTvDeviceName;
    private TextView mTvSoftVer;
    private TextView mTvChannelNumber;
    private TextView mTvChannelId;
    private TextView mTvDeviceId;
    private TextView mTvCaption;
    private Button mBtnResetAndClear;
    private Button mBtnAddNode;
    private Button mBtnRemoveNode;
    private Button mBtnGetNodeList;
    private ArrayList<ZWaveNode> mNodeList;
    private Spinner mNodeListSpinner;
    private boolean deviceReconnectNeeded;
    private TextView mTvChannel;
    private TextView mTvInfo;
    private int mWaitMessagePreloaderDotCount;
    private Timer mWaitMessagePreloaderTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFuncList = new ArrayList<>();
        mNodeList = new ArrayList<>();

        Typeface typeface = SuplaApp.getApp().getTypefaceQuicksandRegular();

        RegisterMessageHandler();
        addStepPage(R.layout.zwave_error, PAGE_ZWAVE_ERROR);
        addStepPage(R.layout.zwave_select_channel, PAGE_SELECT_CHANNEL);
        addStepPage(R.layout.zwave_channel_details, PAGE_CHANNEL_DETAILS);
        addStepPage(R.layout.zwave_details, PAGE_ZWAVE_DETAILS);

        TextView label = findViewById(R.id.zwave_select_channel_txt);
        label.setTypeface(typeface);

        mTvErrorMessage = findViewById(R.id.tv_error_txt);
        mChannelListSpinner = findViewById(R.id.zwave_channel_list);
        mFunctionListSpinner = findViewById(R.id.zwave_func_list);
        mTvDeviceName = findViewById(R.id.tv_device_name);
        mTvSoftVer = findViewById(R.id.tv_soft_ver);
        mTvChannelNumber = findViewById(R.id.tv_channel_number);
        mTvChannelId = findViewById(R.id.tv_channel_id);
        mTvDeviceId = findViewById(R.id.tv_device_id);
        mTvCaption = findViewById(R.id.tv_caption);
        mBtnResetAndClear = findViewById(R.id.btnResetAndClear);
        mBtnAddNode = findViewById(R.id.btnAddNode);
        mBtnRemoveNode = findViewById(R.id.btnRemoveNode);
        mBtnGetNodeList = findViewById(R.id.btnGetNodeList);
        mNodeListSpinner = findViewById(R.id.zwave_node_list);
        mTvChannel = findViewById(R.id.tv_channel);
        mTvInfo = findViewById(R.id.tv_info);

        mBtnResetAndClear.setOnClickListener(this);
        mBtnAddNode.setOnClickListener(this);
        mBtnRemoveNode.setOnClickListener(this);
        mBtnGetNodeList.setOnClickListener(this);

        mFunctionListSpinner.setOnItemSelectedListener(this);
    }

    private String getChannelName(Channel channel) {
        return "#"+channel.getDeviceID()+":"+channel.getChannelId()
                + " " + channel.getNotEmptyCaption(this);
    }

    private void loadChannelListSpinner() {

        DbHelper dbHelper = new DbHelper(this);
        mChannelList = dbHelper.getZWaveBridgeOnlineChannels();
        ArrayList<String>spinnerList = new ArrayList<>();

        for(Channel channel: mChannelList) {
            spinnerList.add(getChannelName(channel));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mChannelListSpinner.setAdapter(adapter);
    }

    private void loadNodeListSpinner() {
        ArrayList<String>spinnerList = new ArrayList<>();
        spinnerList.add("");

        for(ZWaveNode node: mNodeList) {
            spinnerList.add("#"+node.getNodeId() + " " + node.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mNodeListSpinner.setAdapter(adapter);
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

    private boolean isWathdogActive() {
        return mWatchdogTimer != null;
    }

    private void wathdogActivate(final int timeoutSec, final int msgResId) {

        wathdogDeactivate();

        if (timeoutSec == 0) {
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
                        showError(getResources().getString(msgResId));
                    }
                });
            }
        }, timeoutSec*1000, 1000);
    }

    @Override
    protected void showPage(int pageId) {
        super.showPage(pageId);

        wathdogDeactivate();
        cancelAllCommands();

        setBtnNextPreloaderVisible(false);
        setBtnNextEnabled(true);
        setBtnNextText(R.string.next);

        switch (pageId) {
            case PAGE_SELECT_CHANNEL:
                loadChannelListSpinner();
                break;
            case PAGE_ZWAVE_ERROR:
                setBtnNextText(R.string.ok);
                break;
            case PAGE_ZWAVE_DETAILS:
                mTvChannel.setText(getChannelName(mSelectedCahnnel));
                setBtnNextText(R.string.ok);
                hideInfoMessage();
                break;
        }
    }

    private int getDevivceId() {
        return mChannelBasicCfg == null ? 0 : mChannelBasicCfg.getDeviceId();
    }

    private int getChannelId() {
        return mChannelBasicCfg == null ? 0 : mChannelBasicCfg.getChannelId();
    }

    private void hideInfoMessage() {
        if (mWaitMessagePreloaderTimer != null) {
            mWaitMessagePreloaderTimer.cancel();
            mWaitMessagePreloaderTimer = null;
        }
        mTvInfo.setVisibility(View.GONE);
    }

    private void showInfoMessage(String msg) {
        Resources res = getResources();
        mTvInfo.setBackgroundColor(res.getColor(R.color.zwave_info_bg));
        mTvInfo.setTextColor(res.getColor(R.color.zwave_info_text));
        mTvInfo.setVisibility(View.VISIBLE);
        mTvInfo.setText(msg);
    }

    private void showWaitMessage(int waitMsgResId, int timeoutSec, int timoutMsgResId) {
        hideInfoMessage();

        final Resources res = getResources();
        final String waitMessage = res.getString(waitMsgResId);

        showInfoMessage(waitMessage);

        mWaitMessagePreloaderTimer = new Timer();
        mWaitMessagePreloaderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String msg = waitMessage;

                        final int max = 8;

                        for (int a = 0; a < max; a++) {
                            msg+=".";
                        }

                        mTvInfo.setText(msg, TextView.BufferType.SPANNABLE);
                        Spannable s = (Spannable)mTvInfo.getText();
                        s.setSpan(new ForegroundColorSpan(res.getColor(R.color.zwave_info_bg)),
                                msg.length()-(max-mWaitMessagePreloaderDotCount),
                                msg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        mWaitMessagePreloaderDotCount++;
                        if (mWaitMessagePreloaderDotCount > max) {
                            mWaitMessagePreloaderDotCount = 0;
                        }
                    }
                });
            }
        }, 0, 200);

        wathdogActivate(timeoutSec, timoutMsgResId);
    }

    private void cancelAllCommands() {
        if (getDevivceId() == 0) {
            return;
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.deviceCalCfgCancelAllCommands(getDevivceId());
        }
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

        showWaitMessage(R.string.zwave_waiting_for_button_press,
                95, R.string.zwave_button_press_timeout);

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

        if (getChannelId() == 0) {
            return;
        }

        showWaitMessage(R.string.zwave_downloading_node_list,
                5, R.string.zwave_error_get_assigned_node_id_timeout);

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveGetAssignedNodeId(getChannelId());
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (isWathdogActive()) {
            return;
        }

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

    private void gotoChannelDetailsPage() {
        int position = mChannelListSpinner.getSelectedItemPosition();
        if (position  >= 0 && position  < mChannelList.size()) {
            wathdogActivate(5, R.string.zwave_error_get_basic_cfg_timeout);
            setBtnNextEnabled(false);
            setBtnNextPreloaderVisible(true);
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                mSelectedCahnnel = mChannelList.get(position);
                client.getChannelBasicCfg(mSelectedCahnnel.getChannelId());
            }
        }
    }

    private void gotoZWaveDetailsPage() {
        int position = mFunctionListSpinner.getSelectedItemPosition();
        Integer func = mChannelBasicCfg.getFunc();

        setBtnNextEnabled(false);
        setBtnNextPreloaderVisible(true);

        if (mFuncList.get(position).equals(func)) {
            OnChannelFunctionSetResult(getChannelId(),
                    mChannelBasicCfg.getFunc(),
                    SuplaConst.SUPLA_RESULTCODE_TRUE);
        } else {
            wathdogActivate(5, R.string.zwave_error_set_function_timeout);
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                deviceReconnectNeeded = true;
                client.setChannelFunction(getChannelId(),
                        mFuncList.get(position));
            }
        }
    }

    @Override
    protected void onBtnNextClick() {
        switch (getVisiblePageId()) {
            case PAGE_ZWAVE_ERROR:
                showMain();
                break;
            case PAGE_SELECT_CHANNEL:
                gotoChannelDetailsPage();
                break;
            case PAGE_CHANNEL_DETAILS:
                gotoZWaveDetailsPage();
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
        mTvDeviceId.setText(Integer.toString(basicCfg.getDeviceId()));
        mTvCaption.setText(SuplaConst.getNotEmptyCaption(
                basicCfg.getCaption(), basicCfg.getFunc(), this));

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
        cancelAllCommands();
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

    private void showError(String message) {
        wathdogDeactivate();

        mTvErrorMessage.setText(message);
        hideInfoMessage();

        mTvInfo.setText(message);
        mTvInfo.setBackgroundColor(getResources().getColor(R.color.zwave_info_error_bg));
        mTvInfo.setTextColor(getResources().getColor(R.color.zwave_info_error_text));
        mTvInfo.setVisibility(View.VISIBLE);

        if (getVisiblePageId() != PAGE_ZWAVE_DETAILS) {
            showPage(PAGE_ZWAVE_ERROR);
        }

    }

    @Override
    protected void OnChannelFunctionSetResult(int channelId, int func, int code) {
        super.OnChannelFunctionSetResult(channelId, func, code);

        wathdogDeactivate();

        if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {
            showError(getResources().getString(R.string.zwave_error_function_change_error,
                Integer.toString(code)));
            return;
        }

        if (func == 0) {
            showMain();
        } else {
            zwaveGetNodeList();
        }
    }

    @Override
    protected void OnZWaveGetAssignedNodeIdResult(int result, Short nodeId) {
        super.OnZWaveGetAssignedNodeIdResult(result, nodeId);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
            return;
        }

        wathdogDeactivate();

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            mNodeList.clear();
            wathdogActivate(160, R.string.zwave_error_get_node_list_timeout);

            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.zwaveGetNodeList(getDevivceId());
            }
        } else {
            String methodName = new Object() {}
                    .getClass()
                    .getEnclosingMethod()
                    .getName();

            showError(getResources().getString(R.string.zwave_error_unexpected_response,
                    methodName,
                    Integer.toString(result)));
        }

    }

    private boolean nodeExists(ZWaveNode node) {
        for (ZWaveNode n : mNodeList) {
            if (n.getNodeId() == node.getNodeId()) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void OnZWaveGetNodeListResult(int result, ZWaveNode node) {
        super.OnZWaveGetNodeListResult(result, node);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS
                || result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            return;
        }

        if (node != null && !nodeExists(node)) {
            mNodeList.add(node);
        }

        if (node == null || node.isEOL()) {
            wathdogDeactivate();
            hideInfoMessage();
            loadNodeListSpinner();

            if (getVisiblePageId() == PAGE_CHANNEL_DETAILS) {
                showPage(PAGE_ZWAVE_DETAILS);
            }
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
