package org.supla.android;

/*
Copyright (C) AC SOFTWARE SP. Z O.O.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.res.ResourcesCompat;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaChannelBasicCfg;
import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.lib.ZWaveNode;
import org.supla.android.lib.ZWaveWakeUpSettings;
import org.supla.core.shared.data.model.general.SuplaCallConfigCommand;

/**
 * @noinspection SequencedCollectionMethodCanBeUsed
 */
@AndroidEntryPoint
public class ZWaveConfigurationWizardActivity extends WizardActivity
    implements AdapterView.OnItemSelectedListener {

  protected final int RESET_TIMEOUT_SEC = 15;
  protected final int ADD_NODE_BUTTON_PRESS_TIMEOUT_SEC = 35;
  protected final int ADD_NODE_TIMEOUT_SEC = 30; // After button press
  protected final int REMOVE_NODE_TIMEOUT_SEC = 45;
  protected final int GET_ASSIGNED_NODE_ID_TIMEOUT_SEC = 5;
  protected final int GET_BASIC_CFG_TIMEOUT_SEC = 5;
  protected final int SET_CHANNEL_FUNCTION_TIMEOUT_SEC = 5;
  protected final int SET_CHANNEL_CAPTION_TIMEOUT_SEC = 5;
  protected final int ASSIGN_NODE_ID_TIMEOUT_SEC = 15;
  protected final int GET_NODE_LIST_TIMEOUT_SEC = 250;
  private final int PAGE_WELCOME = 1;
  private final int PAGE_ZWAVE_ERROR = 2;
  private final int PAGE_SELECT_CHANNEL = 3;
  private final int PAGE_CHANNEL_DETAILS = 4;
  private final int PAGE_BEFORE_SEARCH = 5;
  private final int PAGE_ZWAVE_DETAILS = 6;
  private final int PAGE_ZWAVE_DONE = 7;
  private final int ERROR_TYPE_TIMEOUT = 1;
  private final int ERROR_TYPE_DISCONNECTED = 2;
  private int mPreviousPage;
  private Timer mAnyCalCfgResultWatchdogTimer;
  private Timer mWatchdogTimer;
  private Timer mConfigModeNoficationTimer;
  private int mWatchdogTimeoutMsgId;
  private Spinner mChannelListSpinner;
  private Spinner mDeviceListSpinner;
  private Channel mSelectedCahnnel;
  private ArrayList<SuplaChannelBasicCfg> mChannelBasicCfgList;
  private ArrayList<Integer> mDeviceList;
  private List<Channel> mChannelList;
  private Spinner mFunctionListSpinner;
  private ArrayList<Integer> mFuncList;
  private ArrayList<Integer> mDevicesToRestart;
  private ArrayList<Channel> mChannelBasicCfgToFetch;
  private TextView mTvErrorMessage;
  private ImageView mTvErrorIcon;
  private TextView mTvDeviceName;
  private TextView mTvSoftVer;
  private TextView mTvChannelNumber;
  private TextView mTvChannelId;
  private TextView mTvDeviceId;
  private EditText mEtCaption;
  private AppCompatImageButton mBtnResetAndClearLeft;
  private Button mBtnResetAndClearRight;
  private AppCompatImageButton mBtnAddNodeLeft;
  private Button mBtnAddNodeRight;
  private AppCompatImageButton mBtnRemoveNodeLeft;
  private Button mBtnRemoveNodeRight;
  private Button mBtnGetNodeList;
  private ArrayList<ZWaveNode> mNodeList;
  private Spinner mNodeListSpinner;
  private TextView mTvChannel;
  private TextView mTvInfo;
  private int mWaitMessagePreloaderDotCount;
  private Timer mWaitMessagePreloaderTimer;
  private short mAssignedNodeId;
  private short mProgress;
  private ZWaveWakeupSettingsDialog wakeupSettingsDialog;
  private TextView mTvWakeUpInfo;
  private Button mBtnWakeUpSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mFuncList = new ArrayList<>();
    mNodeList = new ArrayList<>();
    mDeviceList = new ArrayList<>();
    mChannelBasicCfgList = new ArrayList<>();
    mChannelBasicCfgToFetch = new ArrayList<>();
    mDevicesToRestart = new ArrayList<>();

    registerMessageHandler();
    addStepPage(R.layout.zwave_welcome, PAGE_WELCOME);
    addStepPage(R.layout.zwave_error, PAGE_ZWAVE_ERROR);
    addStepPage(R.layout.zwave_select_channel, PAGE_SELECT_CHANNEL);
    addStepPage(R.layout.zwave_channel_details, PAGE_CHANNEL_DETAILS);
    addStepPage(R.layout.zwave_before_search, PAGE_BEFORE_SEARCH);
    addStepPage(R.layout.zwave_details, PAGE_ZWAVE_DETAILS);
    addStepPage(R.layout.zwave_done, PAGE_ZWAVE_DONE);

    mTvErrorMessage = findViewById(R.id.tv_error_txt);
    mTvErrorIcon = findViewById(R.id.tv_error_icon);
    mDeviceListSpinner = findViewById(R.id.zwave_device_list);
    mChannelListSpinner = findViewById(R.id.zwave_channel_list);
    mFunctionListSpinner = findViewById(R.id.zwave_func_list);
    mTvDeviceName = findViewById(R.id.tv_device_name);
    mTvSoftVer = findViewById(R.id.tv_soft_ver);
    mTvChannelNumber = findViewById(R.id.tv_channel_number);
    mTvChannelId = findViewById(R.id.tv_channel_id);
    mTvDeviceId = findViewById(R.id.tv_device_id);
    mEtCaption = findViewById(R.id.et_caption);
    mBtnResetAndClearLeft = findViewById(R.id.btnResetAndClearLeft);
    mBtnResetAndClearRight = findViewById(R.id.btnResetAndClearRight);
    mBtnAddNodeLeft = findViewById(R.id.btnAddNodeLeft);
    mBtnAddNodeRight = findViewById(R.id.btnAddNodeRight);
    mBtnRemoveNodeLeft = findViewById(R.id.btnRemoveNodeLeft);
    mBtnRemoveNodeRight = findViewById(R.id.btnRemoveNodeRight);
    mBtnGetNodeList = findViewById(R.id.btnGetNodeList);
    mNodeListSpinner = findViewById(R.id.zwave_node_list);
    mTvChannel = findViewById(R.id.tv_details_channel_text);
    mTvInfo = findViewById(R.id.tv_info);
    mTvWakeUpInfo = findViewById(R.id.tv_wake_up_txt);
    mBtnWakeUpSettings = findViewById(R.id.btnWakeUpSettings);

    mBtnResetAndClearLeft.setOnClickListener(this);
    mBtnResetAndClearRight.setOnClickListener(this);
    mBtnAddNodeLeft.setOnClickListener(this);
    mBtnAddNodeRight.setOnClickListener(this);
    mBtnRemoveNodeLeft.setOnClickListener(this);
    mBtnRemoveNodeRight.setOnClickListener(this);
    mBtnGetNodeList.setOnClickListener(this);
    mBtnWakeUpSettings.setOnClickListener(this);

    mDeviceListSpinner.setOnItemSelectedListener(this);
    mChannelListSpinner.setOnItemSelectedListener(this);
    mFunctionListSpinner.setOnItemSelectedListener(this);
  }

  private String getChannelName(Channel channel) {
    return "#" + channel.getChannelId() + " " + channel.getCaption(this);
  }

  private Channel getChannelById(int id) {
    for (Channel channel : mChannelList) {
      if (channel.getChannelId() == id) {
        return channel;
      }
    }

    return null;
  }

  private SuplaChannelBasicCfg getChannelBasicCfgWithDeviceId(int deviceId) {
    for (SuplaChannelBasicCfg cfg : mChannelBasicCfgList) {
      if (cfg.getDeviceId() == deviceId) {
        return cfg;
      }
    }
    return null;
  }

  private SuplaChannelBasicCfg getChannelBasicCfgWithChannelId(int channelId) {
    for (SuplaChannelBasicCfg cfg : mChannelBasicCfgList) {
      if (cfg.getChannelId() == channelId) {
        return cfg;
      }
    }
    return null;
  }

  private void fetchChannelBasicCfg(Integer channelId) {

    if (channelId == null && !mChannelBasicCfgToFetch.isEmpty()) {
      channelId = mChannelBasicCfgToFetch.get(0).getChannelId();
      mChannelBasicCfgToFetch.remove(0);
    }

    if (channelId == null) {
      return;
    }

    wathdogActivate(GET_BASIC_CFG_TIMEOUT_SEC, R.string.zwave_error_get_basic_cfg_timeout, false);
    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.getChannelBasicCfg(channelId);
    }
  }

  private void loadChannelList() {
    setBtnNextEnabled(false);

    mDeviceList.clear();
    mChannelList = getDbHelper().getZWaveBridgeChannels();

    for (Channel channel : mChannelList) {
      boolean exists = false;

      for (Channel fc : mChannelBasicCfgToFetch) {
        if (fc.getDeviceID() == channel.getDeviceID()) {
          exists = true;
          break;
        }
      }
      if (!exists) {
        mChannelBasicCfgToFetch.add(channel);
      }
    }

    fetchChannelBasicCfg(null);
  }

  private void loadDeviceListSpinner() {
    ArrayList<String> spinnerList = new ArrayList<>();

    for (Channel channel : mChannelList) {
      if (!mDeviceList.contains(channel.getDeviceID())) {
        mDeviceList.add(channel.getDeviceID());
      }
    }

    int position = 0;

    for (Integer deviceId : mDeviceList) {
      if (mSelectedCahnnel != null && mSelectedCahnnel.getDeviceID() == deviceId) {
        position = spinnerList.size();
      }
      SuplaChannelBasicCfg cfg = getChannelBasicCfgWithDeviceId(deviceId);
      spinnerList.add("#" + deviceId + (cfg == null ? "" : (" " + cfg.getDeviceName())));
    }

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);

    mDeviceListSpinner.setAdapter(adapter);
    mDeviceListSpinner.setSelection(position, false);
  }

  private Integer getDeviceListSpinnerSelectedId() {
    return mDeviceList.get(mDeviceListSpinner.getSelectedItemPosition());
  }

  private Channel getChannelListSpinnerSelectedChannel() {
    Integer deviceId = getDeviceListSpinnerSelectedId();
    if (deviceId == null) {
      return null;
    }

    int position = mChannelListSpinner.getSelectedItemPosition();
    int n = 0;

    for (Channel channel : mChannelList) {
      if (channel.getDeviceID() != deviceId) {
        continue;
      }

      if (n == position) {
        return channel;
      }

      n++;
    }

    return null;
  }

  private void loadChannelListSpinner() {
    Integer deviceId = getDeviceListSpinnerSelectedId();
    if (deviceId == null) {
      return;
    }

    ArrayList<String> spinnerList = new ArrayList<>();
    int position = 0;

    for (Channel channel : mChannelList) {
      if (channel.getDeviceID() != deviceId) {
        continue;
      }
      if (mSelectedCahnnel != null && mSelectedCahnnel.getChannelId() == channel.getChannelId()) {
        position = spinnerList.size();
      }
      spinnerList.add(getChannelName(channel));
    }

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);

    mChannelListSpinner.setAdapter(adapter);
    mChannelListSpinner.setSelection(position, false);
  }

  private void loadNodeListSpinner(Short selectNodeId) {
    ArrayList<String> spinnerList = new ArrayList<>();
    spinnerList.add("");

    int position = 0;
    int n = 0;
    String used = getResources().getString(R.string.zwave_used);

    for (ZWaveNode node : mNodeList) {
      n++;
      String title = "#" + node.getNodeId() + " " + node.getName();
      if (node.getChannelId() != null && node.getChannelId() != mSelectedCahnnel.getChannelId()) {
        title += " (" + used + " #" + node.getChannelId().toString() + ")";
      }
      spinnerList.add(title);
      if ((selectNodeId != null && node.getNodeId() == selectNodeId)
          || (selectNodeId == null && node.getNodeId() == mAssignedNodeId)) {
        position = n;
      }
    }

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);

    mNodeListSpinner.setAdapter(adapter);
    mNodeListSpinner.setSelection(position, false);
  }

  private void loadNodeListSpinner() {
    loadNodeListSpinner(null);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mDevicesToRestart.clear();
    mChannelBasicCfgToFetch.clear();
    mChannelBasicCfgList.clear();
    configModeNotificationTimerActivate();
    showPage(PAGE_WELCOME);
  }

  private void anyCalCfgResultWatchdogDeactivate() {
    if (mAnyCalCfgResultWatchdogTimer != null) {
      mAnyCalCfgResultWatchdogTimer.cancel();
      mAnyCalCfgResultWatchdogTimer = null;
    }
  }

  private void configModeNotificationTimerDeactivate() {
    if (mConfigModeNoficationTimer != null) {
      mConfigModeNoficationTimer.cancel();
      mConfigModeNoficationTimer = null;
    }
  }

  private void configModeNotificationTimerActivate() {
    configModeNotificationTimerDeactivate();

    mConfigModeNoficationTimer = new Timer();
    mConfigModeNoficationTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            runOnUiThread(
                () -> {
                  if (isFinishing()) {
                    configModeNotificationTimerDeactivate();
                    return;
                  }

                  if (getVisiblePageId() != PAGE_SELECT_CHANNEL && getDevivceId() > 0) {
                    SuplaClient client = SuplaApp.getApp().getSuplaClient();
                    if (client != null) {
                      client.zwaveConfigModeActive(getDevivceId());
                    }
                  }
                });
          }
        },
        0,
        5000);
  }

  private void wathdogDeactivate() {
    anyCalCfgResultWatchdogDeactivate();

    mWatchdogTimeoutMsgId = -1;
    if (mWatchdogTimer != null) {
      mWatchdogTimer.cancel();
      mWatchdogTimer = null;
    }
  }

  private boolean isWathdogActive() {
    return mWatchdogTimer != null;
  }

  private void wathdogActivate(final int timeoutSec, final int msgResId, boolean calCfg) {

    wathdogDeactivate();

    if (timeoutSec < 5) {
      throw new IllegalArgumentException("Watchdog - The minimum timeout value is 5 seconds");
    }

    setBtnNextEnabled(false);

    if (calCfg) {
      mAnyCalCfgResultWatchdogTimer = new Timer();
      mAnyCalCfgResultWatchdogTimer.schedule(
          new TimerTask() {
            @Override
            public void run() {
              runOnUiThread(
                  () -> showError(R.string.zwave_bridge_offline, ERROR_TYPE_DISCONNECTED));
            }
          },
          timeoutSec > 10 ? 10000 : (timeoutSec - 1) * 1000,
          1000);
    }

    mWatchdogTimeoutMsgId = msgResId;
    mWatchdogTimer = new Timer();
    mWatchdogTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            runOnUiThread(() -> showError(msgResId, ERROR_TYPE_TIMEOUT));
          }
        },
        timeoutSec * 1000L,
        1000);
  }

  protected int getNextButtonTextForThePage(int pageId) {
    return switch (pageId) {
      case PAGE_ZWAVE_ERROR -> R.string.exit;
      case PAGE_ZWAVE_DONE -> R.string.ok;
      default -> R.string.next;
    };
  }

  private void updateSelectedChannel() {
    if (mSelectedCahnnel != null) {
      mSelectedCahnnel = getDbHelper().getChannel(mSelectedCahnnel.getChannelId());
    }
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
      case PAGE_CHANNEL_DETAILS:
        updateChannelDetailsPage(null);
        break;
      case PAGE_SELECT_CHANNEL:
        loadChannelList();
        break;
      case PAGE_ZWAVE_DETAILS:
        updateSelectedChannel();
        mTvChannel.setText(getChannelName(mSelectedCahnnel));
        mNodeListSpinner.setAdapter(null);
        hideInfoMessage();
        zwaveGetNodeList();
        break;
      case PAGE_ZWAVE_DONE:
        boolean wakeUpSettingsAvailable =
            (getSelectedNodeFlags() & SuplaConst.ZWAVE_NODE_FLAG_WAKEUP_TIME_SETTABLE) > 0;
        mTvWakeUpInfo.setVisibility(wakeUpSettingsAvailable ? View.VISIBLE : View.INVISIBLE);
        mBtnWakeUpSettings.setVisibility(mTvWakeUpInfo.getVisibility());

        break;
    }
  }

  private int getDevivceId() {
    return mSelectedCahnnel == null ? 0 : mSelectedCahnnel.getDeviceID();
  }

  private int getChannelId() {
    return mSelectedCahnnel == null ? 0 : mSelectedCahnnel.getChannelId();
  }

  private void hideInfoMessage() {
    if (mWaitMessagePreloaderTimer != null) {
      mWaitMessagePreloaderTimer.cancel();
      mWaitMessagePreloaderTimer = null;
    }
    mTvInfo.setVisibility(View.GONE);
  }

  private void showInfoMessage(String msg) {
    mTvInfo.setVisibility(View.VISIBLE);
    mTvInfo.setText(msg);
  }

  private void showWaitMessage(
      int waitMsgResId, int timeoutSec, int timoutMsgResId, final boolean progress) {
    hideInfoMessage();

    final Resources res = getResources();
    final String waitMessage = res.getString(waitMsgResId);

    showInfoMessage(waitMessage);

    mWaitMessagePreloaderTimer = new Timer();
    mWaitMessagePreloaderTimer.schedule(
        new TimerTask() {
          @Override
          public void run() {
            runOnUiThread(
                () -> {
                  StringBuilder msg = new StringBuilder(waitMessage);

                  final int max = 8;

                  if (progress) {
                    msg.append(" ");
                    msg.append(mProgress);
                    msg.append("% ");
                  }

                  for (int a = 0; a < max; a++) {
                    msg.append(".");
                  }

                  mTvInfo.setText(msg.toString(), TextView.BufferType.SPANNABLE);
                  Spannable s = (Spannable) mTvInfo.getText();
                  s.setSpan(
                      new ForegroundColorSpan(
                          ResourcesCompat.getColor(res, R.color.zwave_info_label_bg, null)),
                      msg.length() - (max - mWaitMessagePreloaderDotCount),
                      msg.length(),
                      Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                  mWaitMessagePreloaderDotCount++;
                  if (mWaitMessagePreloaderDotCount > max) {
                    mWaitMessagePreloaderDotCount = 0;
                  }
                });
          }
        },
        0,
        200);

    wathdogActivate(timeoutSec, timoutMsgResId, true);
  }

  private void showWaitMessage(int waitMsgResId, int timeoutSec, int timoutMsgResId) {
    showWaitMessage(waitMsgResId, timeoutSec, timoutMsgResId, false);
  }

  private void cancelAllCommands() {
    if (getDevivceId() == 0) {
      return;
    }

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.deviceCalCfgCancelAllCommands(getDevivceId());
    }
  }

  private void zwaveResetAndClear() {
    if (getDevivceId() == 0) {
      return;
    }

    showWaitMessage(
        R.string.zwave_waiting_for_reset,
        RESET_TIMEOUT_SEC,
        R.string.zwave_waiting_for_reset_timeout);

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.zwaveResetAndClear(getDevivceId());
    }
  }

  private void zwaveAddNode() {

    if (getDevivceId() == 0) {
      return;
    }

    showWaitMessage(
        R.string.zwave_waiting_for_button_press,
        ADD_NODE_BUTTON_PRESS_TIMEOUT_SEC,
        R.string.zwave_button_press_timeout);

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.zwaveAddNode(getDevivceId());
    }
  }

  private void zwaveRemoveNode() {
    if (getDevivceId() == 0) {
      return;
    }

    showWaitMessage(
        R.string.zwave_waiting_for_button_press,
        REMOVE_NODE_TIMEOUT_SEC,
        R.string.zwave_remove_device_timeout);

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.zwaveRemoveNode(getDevivceId());
    }
  }

  private void zwaveGetNodeList() {

    if (getChannelId() == 0) {
      return;
    }

    mProgress = 0;

    if (mNodeList.isEmpty()) {
      showWaitMessage(
          R.string.zwave_node_searching,
          GET_ASSIGNED_NODE_ID_TIMEOUT_SEC,
          R.string.zwave_error_get_assigned_node_id_timeout,
          true);
    }

    SuplaClient client = SuplaApp.getApp().getSuplaClient();
    if (client != null) {
      client.zwaveGetAssignedNodeId(getChannelId());
    }
  }

  private void showResetConfirmDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.zwave_reset_confirm_question);

    builder.setPositiveButton(R.string.yes, (dialog, id) -> zwaveResetAndClear());
    builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void assignNodeId() {
    hideInfoMessage();
    wathdogActivate(ASSIGN_NODE_ID_TIMEOUT_SEC, R.string.zwave_error_assign_node_id_timeout, true);
    setBtnNextPreloaderVisible(true);
    assignNodeIdIfChanged();
  }

  private void showNodeAssignConfirmDialog() {

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(R.string.zwave_node_assign_confirm_question);

    builder.setPositiveButton(R.string.yes, (dialog, id) -> assignNodeId());
    builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

    AlertDialog alert = builder.create();
    alert.show();
  }

  @Override
  public void onClick(View v) {
    super.onClick(v);

    if (isWathdogActive()) {
      return;
    }

    if (v == mBtnResetAndClearLeft || v == mBtnResetAndClearRight) {
      showResetConfirmDialog();
    } else if (v == mBtnAddNodeLeft || v == mBtnAddNodeRight) {
      zwaveAddNode();
    } else if (v == mBtnRemoveNodeLeft || v == mBtnRemoveNodeRight) {
      zwaveRemoveNode();
    } else if (v == mBtnGetNodeList) {
      mNodeList.clear();
      zwaveGetNodeList();
    } else if (v == mBtnWakeUpSettings) {
      wakeupSettingsDialog = new ZWaveWakeupSettingsDialog(this);
      wakeupSettingsDialog.show(getChannelId());
    }
  }

  @Override
  protected void onBtnNextClick() {

    if (isBtnNextPreloaderVisible()) {
      return;
    }

    switch (getVisiblePageId()) {
      case PAGE_WELCOME:
        gotoChannelSelectionPage();
        break;
      case PAGE_ZWAVE_ERROR:
      case PAGE_ZWAVE_DONE:
        showMain();
        break;
      case PAGE_SELECT_CHANNEL:
        showPage(PAGE_CHANNEL_DETAILS);
        break;
      case PAGE_CHANNEL_DETAILS:
        applyChannelCaptionChange();
        break;
      case PAGE_BEFORE_SEARCH:
        showPage(PAGE_ZWAVE_DETAILS);
        break;
      case PAGE_ZWAVE_DETAILS:
        ZWaveNode node = getSelectedNode();
        if (node == null
            || node.getChannelId() == null
            || node.getChannelId() == 0
            || node.getChannelId() == mSelectedCahnnel.getChannelId()) {
          assignNodeId();
        } else {
          showNodeAssignConfirmDialog();
        }
        break;
    }
  }

  private void gotoChannelSelectionPage() {
    if (!mChannelBasicCfgToFetch.isEmpty()) {
      setBtnNextPreloaderVisible(true);
    } else {
      showPage(PAGE_SELECT_CHANNEL);
    }
  }

  @SuppressLint("SetTextI18n")
  private void updateChannelDetailsPage(SuplaChannelBasicCfg basicCfg) {
    if (mSelectedCahnnel == null) {
      return;
    }

    mEtCaption.setText("");
    mEtCaption.setEnabled(false);
    mFunctionListSpinner.setAdapter(null);

    if (basicCfg == null) {
      fetchChannelBasicCfg(mSelectedCahnnel.getChannelId());
      return;
    }

    updateSelectedChannel();

    if (!mDevicesToRestart.contains(getDevivceId())) {
      mDevicesToRestart.add(getDevivceId());
    }

    mTvDeviceName.setText(basicCfg.getDeviceName());
    mTvSoftVer.setText(basicCfg.getDeviceSoftwareVersion());
    mTvChannelNumber.setText(Integer.toString(basicCfg.getNumber()));
    mTvChannelId.setText(Integer.toString(basicCfg.getChannelId()));
    mTvDeviceId.setText(Integer.toString(basicCfg.getDeviceId()));
    mEtCaption.setText(basicCfg.getCaption());
    mEtCaption.setEnabled(true);

    String functionName = SuplaConst.getFunctionName(0, this);
    ArrayList<String> spinnerList = new ArrayList<>();
    int position = 0;

    mFuncList.clear();
    mFuncList.add(0);
    spinnerList.add(functionName);

    for (int a = 0; a < 32; a++) {
      int func = SuplaConst.functionBit2functionNumber(basicCfg.getFuncList() & 1 << a);
      if (func > 0) {
        functionName = SuplaConst.getFunctionName(func, this);
        mFuncList.add(func);
        spinnerList.add(functionName);
        if (func == basicCfg.getFunc()) {
          position = spinnerList.size() - 1;
        }
      }
    }

    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerList);
    mFunctionListSpinner.setAdapter(adapter);
    mFunctionListSpinner.setSelection(position, false);
  }

  private void applyChannelFunctionSelection() {
    int position = mFunctionListSpinner.getSelectedItemPosition();
    SuplaChannelBasicCfg cfg = getChannelBasicCfgWithChannelId(mSelectedCahnnel.getChannelId());

    if (cfg == null) {
      return;
    }

    int func = cfg.getFunc();

    setBtnNextEnabled(false);
    setBtnNextPreloaderVisible(true);

    if (mFuncList.get(position).equals(func)) {
      onChannelFunctionSetResult(getChannelId(), func, SuplaConst.SUPLA_RESULTCODE_TRUE);
    } else {
      wathdogActivate(
          SET_CHANNEL_FUNCTION_TIMEOUT_SEC, R.string.zwave_error_set_function_timeout, false);
      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.setChannelFunction(getChannelId(), mFuncList.get(position));
      }
    }
  }

  private void applyChannelCaptionChange() {
    setBtnNextEnabled(false);
    setBtnNextPreloaderVisible(true);
    SuplaChannelBasicCfg cfg = getChannelBasicCfgWithChannelId(mSelectedCahnnel.getChannelId());

    if (cfg == null) {
      return;
    }

    if (mEtCaption.getText().toString().equals(cfg.getCaption())) {
      applyChannelFunctionSelection();
    } else {
      wathdogActivate(
          SET_CHANNEL_CAPTION_TIMEOUT_SEC, R.string.zwave_error_set_caption_timeout, false);
      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.setChannelCaption(getChannelId(), mEtCaption.getText().toString());
      }
    }
  }

  private ZWaveNode getSelectedNode() {
    if (mNodeListSpinner.getSelectedItemPosition() > 0
        && mNodeListSpinner.getSelectedItemPosition() <= mNodeList.size()) {
      return mNodeList.get(mNodeListSpinner.getSelectedItemPosition() - 1);
    }

    return null;
  }

  private short getSelectedNodeId() {
    ZWaveNode node = getSelectedNode();
    return node == null ? 0 : node.getNodeId();
  }

  private int getSelectedNodeFlags() {
    ZWaveNode node = getSelectedNode();
    return node == null ? 0 : node.getFlags();
  }

  private void assignNodeIdIfChanged() {
    short selectedNodeId = getSelectedNodeId();

    if (selectedNodeId != mAssignedNodeId) {
      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.zwaveAssignNodeId(getChannelId(), selectedNodeId);
      }
    } else {
      onZWaveAssignNodeIdResult(SuplaConst.SUPLA_CALCFG_RESULT_TRUE, mAssignedNodeId);
    }
  }

  @Override
  protected void onCalCfgProgressReport(int channelId, int command, short progress) {
    if (mSelectedCahnnel != null
        && command == SuplaCallConfigCommand.ZWAVE_GET_NODE_LIST.getValue()
        && getVisiblePageId() == PAGE_ZWAVE_DETAILS) {
      mProgress = progress;
    }
  }

  @Override
  protected void onChannelBasicCfg(SuplaChannelBasicCfg basicCfg) {
    super.onChannelBasicCfg(basicCfg);

    if (isFinishing()) {
      return;
    }

    for (SuplaChannelBasicCfg cfg : mChannelBasicCfgList) {
      if (cfg.getChannelId() == basicCfg.getChannelId()) {
        mChannelBasicCfgList.remove(cfg);
        break;
      }
    }

    mChannelBasicCfgList.add(basicCfg);

    if (!mChannelBasicCfgToFetch.isEmpty()) {
      fetchChannelBasicCfg(null);
    } else {
      wathdogDeactivate();
      setBtnNextEnabled(true);
      if (getVisiblePageId() == PAGE_CHANNEL_DETAILS) {
        updateChannelDetailsPage(basicCfg);
      } else if (getVisiblePageId() == PAGE_SELECT_CHANNEL && mDeviceList.isEmpty()) {
        loadDeviceListSpinner();
      }
    }
  }

  private void showMain() {
    cancelAllCommands();
    wathdogDeactivate();
    configModeNotificationTimerDeactivate();

    if (!mDevicesToRestart.isEmpty()) {
      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        while (!mDevicesToRestart.isEmpty()) {
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

    mTvErrorIcon.setBackground(ResourcesCompat.getDrawable(getResources(), iconResId, null));

    showPage(PAGE_ZWAVE_ERROR);
  }

  private void showError(String message) {
    showError(message, R.drawable.add_wizard_error);
  }

  private void showError(int msgResId, int errorType) {
    switch (errorType) {
      case ERROR_TYPE_DISCONNECTED:
        showError(getResources().getString(msgResId), R.drawable.bridge_disconnected);
        break;
      case ERROR_TYPE_TIMEOUT:
        showError(getResources().getString(msgResId), R.drawable.zwave_timeout);
        break;
      default:
        showError(getResources().getString(msgResId));
        break;
    }
  }

  private boolean timeoutResultNotDisplayed(int result) {
    if (result == SuplaConst.SUPLA_CALCFG_RESULT_TIMEOUT && mWatchdogTimeoutMsgId > -1) {
      showError(mWatchdogTimeoutMsgId, ERROR_TYPE_TIMEOUT);
      return false;
    }
    return true;
  }

  private void showUnexpectedResponseError(int result) {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

    String methodName = stackTrace.length > 3 ? stackTrace[3].getMethodName() : "???";

    showError(
        getResources()
            .getString(
                R.string.zwave_error_unexpected_response, methodName, Integer.toString(result)));
  }

  @Override
  protected void onChannelFunctionSetResult(int channelId, int func, int code) {
    super.onChannelFunctionSetResult(channelId, func, code);

    wathdogDeactivate();

    if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {

      Integer msgErrResId =
          switch (code) {
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_BELONG_TO_GROUP ->
                R.string.belongs_to_group_error;
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_HAS_SCHEDULE ->
                R.string.has_schedule_error;
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_SCENE ->
                R.string.associeted_with_scene_error;
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_ACTION_TRIGGER ->
                R.string.associeted_with_at_error;
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_VBT ->
                R.string.associeted_with_vbt_error;
            case SuplaConst.SUPLA_RESULTCODE_DENY_CHANNEL_IS_ASSOCIETED_WITH_PUSH ->
                R.string.associeted_with_push_error;
            default -> null;
          };

      if (msgErrResId == null) {
        showError(
            getResources()
                .getString(R.string.zwave_error_function_change_error, Integer.toString(code)));
      } else {
        showError(getResources().getString(msgErrResId));
      }

      return;
    }

    if (func == 0) {
      showMain();
    } else {
      fetchChannelBasicCfg(channelId);
      if (mNodeList.isEmpty()) {
        showPage(PAGE_BEFORE_SEARCH);
      } else {
        showPage(PAGE_ZWAVE_DETAILS);
      }
    }
  }

  @Override
  protected void onChannelCaptionSetResult(int channelId, String Caption, int code) {
    super.onChannelCaptionSetResult(channelId, Caption, code);

    wathdogDeactivate();

    if (code != SuplaConst.SUPLA_RESULTCODE_TRUE) {
      showError(
          getResources()
              .getString(R.string.zwave_error_caption_change_error, Integer.toString(code)));
      return;
    }

    applyChannelFunctionSelection();
  }

  @Override
  protected void onCalCfgResult(int channelId, int command, int result, byte[] data) {
    super.onCalCfgResult(channelId, command, result, data);
    if (command != 5000) {
      Trace.d(
          "onCalCfgResult",
          channelId + "," + command + "," + result + "," + (data == null ? 0 : data.length));
    }

    if (mSelectedCahnnel != null) {
      Channel channel = getChannelById(channelId);
      if (channel != null && channel.getDeviceID() == mSelectedCahnnel.getDeviceID()) {
        anyCalCfgResultWatchdogDeactivate();
      }
    }
  }

  @Override
  protected void onZWaveGetAssignedNodeIdResult(int result, short nodeId) {
    super.onZWaveGetAssignedNodeIdResult(result, nodeId);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      if (timeoutResultNotDisplayed(result)) {
        showUnexpectedResponseError(result);
      }
      return;
    }

    wathdogDeactivate();

    mAssignedNodeId = nodeId;

    if (mNodeList.isEmpty()) {
      wathdogActivate(GET_NODE_LIST_TIMEOUT_SEC, R.string.zwave_error_get_node_list_timeout, true);

      SuplaClient client = SuplaApp.getApp().getSuplaClient();
      if (client != null) {
        client.zwaveGetNodeList(getDevivceId());
      }
    } else {
      loadNodeListSpinner();
    }
  }

  @Override
  protected void onZWaveAssignNodeIdResult(int result, short nodeId) {
    super.onZWaveAssignNodeIdResult(result, nodeId);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      if (timeoutResultNotDisplayed(result)) {
        showUnexpectedResponseError(result);
      }
      return;
    }

    wathdogDeactivate();
    setBtnNextPreloaderVisible(false);

    if (mSelectedCahnnel != null) {

      for (ZWaveNode n : mNodeList) {
        if (n.getChannelId() != null && n.getChannelId() == mSelectedCahnnel.getChannelId()) {
          n.setChannelId(null);
        }
      }

      int _nodeId = nodeId == 0 ? mAssignedNodeId : nodeId;
      if (_nodeId > 0) {
        for (ZWaveNode n : mNodeList) {
          if (n.getNodeId() == _nodeId) {
            n.setChannelId(nodeId > 0 ? mSelectedCahnnel.getChannelId() : null);
            break;
          }
        }
      }
    }

    mAssignedNodeId = nodeId;
    showPage(PAGE_ZWAVE_DONE);
  }

  @Override
  protected void onZWaveWakeUpSettingsReport(int result, ZWaveWakeUpSettings settings) {
    if (wakeupSettingsDialog != null && wakeupSettingsDialog.isVisible()) {
      wakeupSettingsDialog.onWakeUpSettingsReport(result, settings);
    }
  }

  @Override
  protected void onZwaveSetWakeUpTimeResult(int result) {
    if (wakeupSettingsDialog != null && wakeupSettingsDialog.isVisible()) {
      wakeupSettingsDialog.onZwaveSetWakeUpTimeResult(result);
    }
  }

  private boolean nodeIdNotExists(short nodeId) {
    for (ZWaveNode n : mNodeList) {
      if (n.getNodeId() == nodeId) {
        return false;
      }
    }

    return true;
  }

  private boolean nodeNotExists(ZWaveNode node) {
    return nodeIdNotExists(node.getNodeId());
  }

  @Override
  protected void onZWaveGetNodeListResult(int result, ZWaveNode node) {
    super.onZWaveGetNodeListResult(result, node);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      if (timeoutResultNotDisplayed(result)) {
        showUnexpectedResponseError(result);
      }
      return;
    }

    if (node != null && nodeNotExists(node)) {
      mNodeList.add(node);
    }

    if (node == null) {
      if (mAssignedNodeId > 0 && nodeIdNotExists(mAssignedNodeId)) {
        node =
            new ZWaveNode(
                mAssignedNodeId,
                (short) 0,
                (short) 0,
                mSelectedCahnnel == null ? 0 : mSelectedCahnnel.getChannelId(),
                getResources().getString(R.string.zwave_offline));
        mNodeList.add(node);
      }

      wathdogDeactivate();
      hideInfoMessage();
      loadNodeListSpinner();
      setBtnNextEnabled(true);

      if (getVisiblePageId() == PAGE_BEFORE_SEARCH) {
        showPage(PAGE_ZWAVE_DETAILS);
      }
    }
  }

  protected boolean slaveModeError(int result) {
    if (result == SuplaConst.SUPLA_CALCFG_RESULT_NOT_SUPPORTED_IN_SLAVE_MODE) {
      showError(getResources().getString(R.string.zwave_error_slave_mode));
      return true;
    }
    return false;
  }

  @Override
  protected void onZWaveResetAndClearResult(int result) {
    super.onZWaveResetAndClearResult(result);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (slaveModeError(result)) {
      return;
    }

    if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      if (timeoutResultNotDisplayed(result)) {
        showUnexpectedResponseError(result);
      }
      return;
    }

    zwaveGetNodeList();
    setBtnNextEnabled(true);
  }

  @Override
  protected void onZWaveAddNodeResult(int result, ZWaveNode node) {
    super.onZWaveAddNodeResult(result, node);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (slaveModeError(result)) {
      return;
    }

    setBtnNextEnabled(true);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_NODE_FOUND) {

      showWaitMessage(
          R.string.zwave_waiting_for_add,
          ADD_NODE_TIMEOUT_SEC,
          R.string.zwave_waiting_for_add_timeout);

    } else if (result == SuplaConst.SUPLA_CALCFG_RESULT_DONE) {
      wathdogDeactivate();
      hideInfoMessage();
      if (node != null && nodeNotExists(node)) {
        mNodeList.add(node);
        loadNodeListSpinner(node.getNodeId());
      }
    } else if (timeoutResultNotDisplayed(result)) {
      showUnexpectedResponseError(result);
    }
  }

  @Override
  protected void onZWaveRemoveNodeResult(int result, short nodeId) {
    super.onZWaveRemoveNodeResult(result, nodeId);

    if (result == SuplaConst.SUPLA_CALCFG_RESULT_IN_PROGRESS) {
      return;
    }

    if (slaveModeError(result)) {
      return;
    }

    if (result != SuplaConst.SUPLA_CALCFG_RESULT_TRUE) {
      if (timeoutResultNotDisplayed(result)) {
        showUnexpectedResponseError(result);
      }
      return;
    }

    setBtnNextEnabled(true);

    wathdogDeactivate();
    hideInfoMessage();

    if (nodeId > 0) {
      for (ZWaveNode node : mNodeList) {
        if (node.getNodeId() == nodeId) {
          mNodeList.remove(node);
          break;
        }
      }
    }

    loadNodeListSpinner(getSelectedNodeId());
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (parent == mDeviceListSpinner) {
      loadChannelListSpinner();
    } else if (parent == mChannelListSpinner) {
      Channel selectedChannel = getChannelListSpinnerSelectedChannel();
      if (mSelectedCahnnel == null
          || selectedChannel == null
          || selectedChannel.getDeviceID() != mSelectedCahnnel.getDeviceID()) {
        mNodeList.clear();
      }
      mSelectedCahnnel = selectedChannel;
    } else if (parent == mFunctionListSpinner && position >= 0 && position < mFuncList.size()) {
      if (mFuncList.get(position) == 0) {
        setBtnNextText(R.string.ok);
      } else {
        setBtnNextText(R.string.next);
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {}

  @Override
  public void onBackPressed() {
    super.onBackPressed();
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
        showPage(PAGE_CHANNEL_DETAILS);
        break;
      case PAGE_ZWAVE_DONE:
        showPage(PAGE_ZWAVE_DETAILS);
        break;
      default:
        showMain();
    }
  }
}
