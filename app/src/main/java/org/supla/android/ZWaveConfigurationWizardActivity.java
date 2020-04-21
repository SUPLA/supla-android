package org.supla.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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

    private final int PAGE_WELCOME = 1;
    private final int PAGE_ZWAVE_ERROR = 2;
    private final int PAGE_SELECT_CHANNEL = 3;
    private final int PAGE_CHANNEL_DETAILS = 4;
    private final int PAGE_BEFORE_SEARCH = 5;
    private final int PAGE_ZWAVE_DETAILS = 6;

    private final int RESET_TIMEOUT_SEC = 15;
    private final int ADD_NODE_BUTTON_PRESS_TIMEOUT_SEC = 35;
    private final int ADD_NODE_TIMEOUT_SEC = 30; // After button press
    private final int REMOVE_NODE_TIMEOUT_SEC = 45;
    private final int GET_ASSIGNED_NODE_ID_TIMEOUT_SEC = 5;
    private final int GET_BASIC_CFG_TIMEOUT_SEC = 5;
    private final int SET_CHANNEL_FUNCTION_TIMEOUT_SEC = 5;
    private final int ASSIGN_NODE_ID_TIMEOUT_SEC = 15;
    private final int GET_NODE_LIST_TIMEOUT_SEC = 250;

    private int mPreviousPage;
    private Timer mAnyResultWatchdogTimer;
    private Timer mWatchdogTimer;
    private int mWatchdogTimeoutMsgId;
    private Spinner mChannelListSpinner;
    private Channel mSelectedCahnnel;
    private Integer mSelectedChannelFunc;
    private SuplaChannelBasicCfg mChannelBasicCfg;
    private ArrayList<Channel> mChannelList;
    private Spinner mFunctionListSpinner;
    private ArrayList<Integer> mFuncList;
    private ArrayList<Integer> mDevicesToRestart;
    private TextView mTvErrorMessage;
    private ImageView mTvErrorIcon;
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
    private TextView mTvChannel;
    private TextView mTvInfo;
    private int mWaitMessagePreloaderDotCount;
    private Timer mWaitMessagePreloaderTimer;
    private short mAssignedNodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFuncList = new ArrayList<>();
        mNodeList = new ArrayList<>();
        mDevicesToRestart = new ArrayList<>();

        Typeface typeface = SuplaApp.getApp().getTypefaceQuicksandRegular();

        RegisterMessageHandler();
        addStepPage(R.layout.zwave_welcome, PAGE_WELCOME);
        addStepPage(R.layout.zwave_error, PAGE_ZWAVE_ERROR);
        addStepPage(R.layout.zwave_select_channel, PAGE_SELECT_CHANNEL);
        addStepPage(R.layout.zwave_channel_details, PAGE_CHANNEL_DETAILS);
        addStepPage(R.layout.zwave_before_search, PAGE_BEFORE_SEARCH);
        addStepPage(R.layout.zwave_details, PAGE_ZWAVE_DETAILS);

        TextView label = findViewById(R.id.zwave_select_channel_txt);
        label.setTypeface(typeface);

        mTvErrorMessage = findViewById(R.id.tv_error_txt);
        mTvErrorIcon = findViewById(R.id.tv_error_icon);
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

        mChannelListSpinner.setOnItemSelectedListener(this);
        mFunctionListSpinner.setOnItemSelectedListener(this);
    }

    private String getChannelName(Channel channel, Integer func) {
        return "#"+channel.getDeviceID()+":"+channel.getChannelId()
                + " " + SuplaConst.getNotEmptyCaption(channel.getCaption(),
                func == null ? channel.getFunc() : func.intValue(), this);
    }

    private void loadChannelListSpinner() {

        DbHelper dbHelper = new DbHelper(this);
        mChannelList = dbHelper.getZWaveBridgeChannels();
        ArrayList<String>spinnerList = new ArrayList<>();

        int position = 0;

        for(Channel channel: mChannelList) {
            if (mSelectedCahnnel != null
                    && mSelectedCahnnel.getChannelId() == channel.getChannelId()) {
                position = spinnerList.size();
            }
            spinnerList.add(getChannelName(channel, null));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mChannelListSpinner.setAdapter(adapter);
        mChannelListSpinner.setSelection(position, false);
    }

    private void loadNodeListSpinner(Short selectNodeId) {
        ArrayList<String>spinnerList = new ArrayList<>();
        spinnerList.add("");

        int position = 0;
        int n = 0;

        for(ZWaveNode node: mNodeList) {
            n++;
            spinnerList.add("#"+node.getNodeId() + " " + node.getName());
            if ((selectNodeId != null && node.getNodeId() == selectNodeId)
                    || (selectNodeId == null && node.getNodeId() == mAssignedNodeId)) {
                position = n;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mNodeListSpinner.setAdapter(adapter);
        mNodeListSpinner.setSelection(position, false);
    }

    private void loadNodeListSpinner() {
        loadNodeListSpinner((short)0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDevicesToRestart.clear();
        showPage(PAGE_WELCOME);
    }

    private void anyResultWatchdogDeactivate() {
        if (mAnyResultWatchdogTimer != null) {
            mAnyResultWatchdogTimer.cancel();
            mAnyResultWatchdogTimer = null;
        }
    }

    private void wathdogDeactivate() {
        anyResultWatchdogDeactivate();

        mWatchdogTimeoutMsgId = -1;
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

        if (timeoutSec < 5) {
            throw new IllegalArgumentException("Watchdog - The minimum timeout value is 5 seconds");
        }

        setBtnNextEnabled(false);

        mAnyResultWatchdogTimer = new Timer();
        mAnyResultWatchdogTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        showError(R.string.zwave_bridge_offline);
                    }
                });
            }
        }, 4000, 1000);

        mWatchdogTimeoutMsgId = msgResId;
        mWatchdogTimer = new Timer();
        mWatchdogTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        showError(msgResId);
                    }
                });
            }
        }, timeoutSec*1000, 1000);
    }

    protected int getNextButtonTextForThePage(int pageId) {
        switch (pageId) {
            case PAGE_ZWAVE_ERROR:
                return R.string.exit;
            case PAGE_ZWAVE_DETAILS:
                return R.string.ok;
        }

        return R.string.next;
    }

    @Override
    protected void showPage(int pageId) {
        mPreviousPage = getVisiblePageId();
        super.showPage(pageId);

        wathdogDeactivate();
        cancelAllCommands();

        setBtnNextPreloaderVisible(false);
        setBtnNextEnabled(true);
        setBtnNextText(getNextButtonTextForThePage(pageId));

        switch (pageId) {
            case PAGE_SELECT_CHANNEL:
                loadChannelListSpinner();
                break;
            case PAGE_ZWAVE_DETAILS:
                mTvChannel.setText(getChannelName(mSelectedCahnnel, mSelectedChannelFunc));
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

        showWaitMessage(R.string.zwave_waiting_for_reset,
                RESET_TIMEOUT_SEC, R.string.zwave_waiting_for_reset_timeout);

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
                ADD_NODE_BUTTON_PRESS_TIMEOUT_SEC,
                R.string.zwave_button_press_timeout);

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveAddNode(getDevivceId());
        }
    }

    private void zwaveRemoveNode() {
        if (getDevivceId() == 0) {
            return;
        }

        showWaitMessage(R.string.zwave_waiting_for_button_press,
                REMOVE_NODE_TIMEOUT_SEC, R.string.zwave_remove_device_timeout);

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveRemoveNode(getDevivceId());
        }
    }

    private void zwaveGetNodeList() {

        if (getChannelId() == 0) {
            return;
        }

        showWaitMessage(R.string.zwave_node_searching,
                GET_ASSIGNED_NODE_ID_TIMEOUT_SEC,
                R.string.zwave_error_get_assigned_node_id_timeout);

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client!=null){
            client.zwaveGetAssignedNodeId(getChannelId());
        }
    }

    private void showResetConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.zwave_reset_confirm_question);

        builder.setPositiveButton(R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        zwaveResetAndClear();
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

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (isWathdogActive()) {
            return;
        }

        if (v == mBtnResetAndClear) {
            showResetConfirmDialog();
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

        if (isBtnNextPreloaderVisible()) {
            return;
        }

        switch (getVisiblePageId()) {
            case PAGE_WELCOME:
                showPage(PAGE_SELECT_CHANNEL);
                break;
            case PAGE_ZWAVE_ERROR:
                showMain();
                break;
            case PAGE_SELECT_CHANNEL:
                gotoChannelDetailsPage();
                break;
            case PAGE_CHANNEL_DETAILS:
                applyChannelFunctionSelection();
                break;
            case PAGE_BEFORE_SEARCH:
                setBtnNextPreloaderVisible(true);
                zwaveGetNodeList();
                break;
            case PAGE_ZWAVE_DETAILS:
                hideInfoMessage();
                wathdogActivate(ASSIGN_NODE_ID_TIMEOUT_SEC,
                        R.string.zwave_error_assign_node_id_timeout);
                setBtnNextPreloaderVisible(true);
                assignNodeIdIfChanged();
                break;
        }
    }

    private void gotoChannelDetailsPage() {
        if (mSelectedCahnnel != null) {
            wathdogActivate(GET_BASIC_CFG_TIMEOUT_SEC,
                    R.string.zwave_error_get_basic_cfg_timeout);
            setBtnNextEnabled(false);
            setBtnNextPreloaderVisible(true);
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.getChannelBasicCfg(mSelectedCahnnel.getChannelId());
            }
        }
    }

    private void applyChannelFunctionSelection() {
        int position = mFunctionListSpinner.getSelectedItemPosition();
        Integer func = mChannelBasicCfg.getFunc();

        setBtnNextEnabled(false);
        setBtnNextPreloaderVisible(true);

        if (mFuncList.get(position).equals(func)) {
            onChannelFunctionSetResult(getChannelId(),
                    mChannelBasicCfg.getFunc(),
                    SuplaConst.SUPLA_RESULTCODE_TRUE);
        } else {
            Trace.d("calcfg", "aaaa");
            if (!mDevicesToRestart.contains(getDevivceId())) {
                Trace.d("calcfg", "Device to restart:"+Integer.toString(getDevivceId()));
                mDevicesToRestart.add(getDevivceId());
            }
            wathdogActivate(SET_CHANNEL_FUNCTION_TIMEOUT_SEC,
                    R.string.zwave_error_set_function_timeout);
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.setChannelFunction(getChannelId(),
                        mFuncList.get(position));
            }
        }
    }

    private short getSelectedNodeId() {
        short selectedNodeId = 0;
        if (mNodeListSpinner.getSelectedItemPosition() > 0
                && mNodeListSpinner.getSelectedItemPosition() <= mNodeList.size()) {
            selectedNodeId = mNodeList.get(mNodeListSpinner.getSelectedItemPosition()-1).getNodeId();
        }

        return selectedNodeId;
    }

    private void assignNodeIdIfChanged() {
        short selectedNodeId = getSelectedNodeId();

        if (selectedNodeId != mAssignedNodeId) {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.zwaveAssignNodeId(getChannelId(), selectedNodeId);
            }
        } else {
            onZWaveAssignNodeIdResult(SuplaConst.SUPLA_CALCFG_RESULT_TRUE, mAssignedNodeId);
        }

    }

    @Override
    protected void onChannelBasicCfg(SuplaChannelBasicCfg basicCfg) {

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

        if (mDevicesToRestart.size() > 0) {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                while (mDevicesToRestart.size() > 0) {
                    Trace.d("calcfg", "Trying to restart:"+Integer.toString(mDevicesToRestart.get(0)));
                    client.reconnectDevice(mDevicesToRestart.get(0));
                    mDevicesToRestart.remove(0);
                }

            }
        }

        setBtnNextPreloaderVisible(false);
        showMain(this);
    }

    private void showError(String message, int iconResId) {
        wathdogDeactivate();

        mTvErrorMessage.setText(message);
        setBtnNextPreloaderVisible(false);
        setBtnNextText(getNextButtonTextForThePage(getVisiblePageId()));
        hideInfoMessage();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTvErrorIcon.setBackground(getResources().getDrawable(iconResId));
        } else {
            mTvErrorIcon.setBackgroundDrawable(getResources().getDrawable(iconResId));
        }

        showPage(PAGE_ZWAVE_ERROR);
    }

    private void showError(String message) {
        showError(message, R.drawable.wizard_error);
    }

    private void showError(int msgResId) {
        if (msgResId == R.string.zwave_bridge_offline) {
            showError(getResources().getString(msgResId), R.drawable.bridge_disconnected);
        } else {
            showError(getResources().getString(msgResId));
        }
    }

    private boolean showTimeoutResult(int result) {
        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TIMEOUT && mWatchdogTimeoutMsgId > -1) {
            showError(mWatchdogTimeoutMsgId);
            return true;
        }
        return false;
    }

    private void showUnexpectedResponseError(int result) {
        StackTraceElement[] stackTrace = Thread.currentThread()
                .getStackTrace();

        String methodName = stackTrace.length > 3 ? stackTrace[3].getMethodName() : "???";

        showError(getResources().getString(R.string.zwave_error_unexpected_response,
                methodName,
                Integer.toString(result)));
    }

    @Override
    protected void onChannelFunctionSetResult(int channelId, int func, int code) {
        super.onChannelFunctionSetResult(channelId, func, code);

        wathdogDeactivate();

        if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {
            showError(getResources().getString(R.string.zwave_error_function_change_error,
                Integer.toString(code)));
            return;
        }

        mSelectedChannelFunc = Integer.valueOf(func);

        if (func == 0) {
            showMain();
        } else {
            showPage(PAGE_BEFORE_SEARCH);
        }
    }

    @Override
    protected void onCalCfgResult(int channelId, int command, int result, byte[] data) {
        super.onCalCfgResult(channelId, command, result, data);
        if (command != 5000) {
            Trace.d("onCalCfgResult", Integer.toString(command)+","+Integer.toString(result)+","+Integer.toString(data==null ? 0 : data.length));
        }

        if (mSelectedCahnnel != null && mSelectedCahnnel.getChannelId() == channelId) {
            anyResultWatchdogDeactivate();
        }
    }

    @Override
    protected void onZWaveGetAssignedNodeIdResult(int result, short nodeId) {
        super.onZWaveGetAssignedNodeIdResult(result, nodeId);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
            return;
        }

        wathdogDeactivate();

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            mAssignedNodeId = nodeId;
            mNodeList.clear();
            wathdogActivate(GET_NODE_LIST_TIMEOUT_SEC,
                    R.string.zwave_error_get_node_list_timeout);

            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.zwaveGetNodeList(getDevivceId());
            }
        } else if (!showTimeoutResult(result)) {
            showUnexpectedResponseError(result);
        }

    }

    @Override
    protected void onZWaveAssignNodeIdResult(int result, short nodeId) {
        super.onZWaveAssignNodeIdResult(result, nodeId);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
            return;
        }

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            wathdogDeactivate();
            setBtnNextPreloaderVisible(false);
            mAssignedNodeId = nodeId;
            showMain();
        } else if (!showTimeoutResult(result)) {
            showUnexpectedResponseError(result);
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
    protected void onZWaveGetNodeListResult(int result, ZWaveNode node) {
        super.onZWaveGetNodeListResult(result, node);

        if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            return;
        }

        if (node != null && !nodeExists(node)) {
            mNodeList.add(node);
        }

        if (node == null) {
            wathdogDeactivate();
            hideInfoMessage();
            loadNodeListSpinner();
            setBtnNextEnabled(true);

            if (getVisiblePageId() == PAGE_BEFORE_SEARCH) {
                showPage(PAGE_ZWAVE_DETAILS);
            }
        }
    }

    @Override
    protected void onZWaveResetAndClearResult(int result) {
        super.onZWaveResetAndClearResult(result);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
            return;
        }

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            zwaveGetNodeList();
        } else if (!showTimeoutResult(result)) {
            showUnexpectedResponseError(result);
        }
    }

    @Override
    protected void onZWaveAddNodeResult(int result, ZWaveNode node) {
        super.onZWaveAddNodeResult(result, node);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_NODE_FOUND) {

            showWaitMessage(R.string.zwave_waiting_for_add,
                    ADD_NODE_TIMEOUT_SEC, R.string.zwave_waiting_for_add_timeout);

        } else if (result == SuplaConst.SUPLA_CALCFG_RESULT_DONE) {
            wathdogDeactivate();
            hideInfoMessage();
            if (node != null && !nodeExists(node)) {
                mNodeList.add(node);
                loadNodeListSpinner(node.getNodeId());
            }
        }
    };

    @Override
    protected void onZWaveRemoveNodeResult(int result, short nodeId) {
        super.onZWaveRemoveNodeResult(result, nodeId);

        if (result == SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
            wathdogDeactivate();
            hideInfoMessage();

            if (nodeId > 0) {
                for(ZWaveNode node: mNodeList) {
                    if (node.getNodeId() == nodeId) {
                        mNodeList.remove(node);
                        break;
                    }
                }
            }

            loadNodeListSpinner(getSelectedNodeId());
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mChannelListSpinner) {
            if (position  >= 0 && position  < mChannelList.size()) {
                mSelectedCahnnel = mChannelList.get(position);
            } else {
                mSelectedCahnnel = null;
            }
        } else if (parent == mFunctionListSpinner
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

    @Override
    public void onBackPressed() {
        switch (getVisiblePageId()) {
            case PAGE_ZWAVE_ERROR:
                if (mPreviousPage > 0) {
                    showPage(mPreviousPage);
                } else {
                    showMain();
                }
                break;
            case PAGE_SELECT_CHANNEL:
                showPage(PAGE_WELCOME);
                break;
            case PAGE_CHANNEL_DETAILS:
                showPage(PAGE_SELECT_CHANNEL);
                break;
            case PAGE_BEFORE_SEARCH:
            case PAGE_ZWAVE_DETAILS:
                gotoChannelDetailsPage();
                break;
            default:
                showMain();
        }
    }
}
