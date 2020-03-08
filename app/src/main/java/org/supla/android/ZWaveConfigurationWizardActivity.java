package org.supla.android;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaChannelBasicCfg;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;

import java.util.ArrayList;

public class ZWaveConfigurationWizardActivity extends WizardActivity implements AdapterView.OnItemSelectedListener {

    private final int PAGE_SELECT_CHANNEL = 1;
    private final int PAGE_CHANNEL_DETAILS = 2;

    private Spinner mChannelListSpinner;
    private Channel mSelectedCahnnel;
    private SuplaChannelBasicCfg mChannelBasicCfg;
    private ArrayList<Channel> mChannelList;
    private Spinner mFunctionListSpinner;
    private ArrayList<Integer> mFuncList;
    private TextView mTvDeviceName;
    private TextView mTvSoftVer;
    private TextView mTvChannelNumber;
    private TextView mTvChannelId;
    private TextView mTvCaption;
    private boolean deviceReconnectNeeded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFuncList = new ArrayList<>();

        Typeface typeface = SuplaApp.getApp().getTypefaceQuicksandRegular();

        RegisterMessageHandler();
        addStepPage(R.layout.zwave_select_channel, PAGE_SELECT_CHANNEL);
        addStepPage(R.layout.zwave_channel_details, PAGE_CHANNEL_DETAILS);

        TextView label = findViewById(R.id.zwave_select_channel_txt);
        label.setTypeface(typeface);

        mChannelListSpinner = findViewById(R.id.zwave_channel_list);
        mFunctionListSpinner = findViewById(R.id.zwave_func_list);
        mTvDeviceName = findViewById(R.id.tv_device_name);
        mTvSoftVer = findViewById(R.id.tv_soft_ver);
        mTvChannelNumber = findViewById(R.id.tv_channel_number);
        mTvChannelId = findViewById(R.id.tv_channel_id);
        mTvCaption = findViewById(R.id.tv_caption);

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

    @Override
    protected void showPage(int pageId) {
        super.showPage(pageId);

        setBtnNextEnabled(true);
        setBtnNextText(R.string.next);

        switch (pageId) {
            case PAGE_SELECT_CHANNEL:
                loadChannelList();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onBtnNextClick() {
        switch (getVisiblePageId()) {
            case PAGE_SELECT_CHANNEL:
                {
                int position = mChannelListSpinner.getSelectedItemPosition();
                if (position  >= 0 && position  < mChannelList.size()) {
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
                    position = 1 + a;
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

        if (deviceReconnectNeeded) {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.reconnectDevice(mChannelBasicCfg.getDeviceId());
            }
        }

        setBtnNextPreloaderVisible(false);
        showMain(this);
    }

    @Override
    protected void OnChannelFunctionSetResult(int channelId, int func, int code) {
        super.OnChannelFunctionSetResult(channelId, func, code);

        if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {
            return;
        }

        if (func == 0) {
            showMain();
        } else {

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
