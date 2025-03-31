package org.supla.android.lib;

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

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.supla.android.BuildConfig;
import org.supla.android.R;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.core.networking.suplaclient.SuplaClientApi;
import org.supla.android.core.networking.suplaclient.SuplaClientDependencies;
import org.supla.android.core.networking.suplaclient.SuplaClientEvent;
import org.supla.android.core.networking.suplaclient.SuplaClientEvent.Cancel;
import org.supla.android.core.networking.suplaclient.SuplaClientEvent.Connecting;
import org.supla.android.core.networking.suplaclient.SuplaClientEvent.Finish;
import org.supla.android.core.networking.suplaclient.SuplaClientState;
import org.supla.android.core.networking.suplaclient.SuplaClientState.Reason;
import org.supla.android.core.networking.suplaclient.SuplaClientState.Reason.NoNetwork;
import org.supla.android.core.networking.suplaclient.SuplaClientState.Reason.VersionError;
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder;
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder;
import org.supla.android.core.notifications.NotificationsHelper;
import org.supla.android.core.storage.EncryptedPreferences;
import org.supla.android.data.model.general.EntityUpdateResult;
import org.supla.android.data.source.SceneRepository;
import org.supla.android.data.source.remote.ChannelConfigType;
import org.supla.android.data.source.remote.ConfigResult;
import org.supla.android.data.source.remote.FieldType;
import org.supla.android.data.source.remote.SuplaChannelConfig;
import org.supla.android.data.source.remote.SuplaDeviceConfig;
import org.supla.android.db.AuthProfileItem;
import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.db.room.app.AppDatabase;
import org.supla.android.db.room.measurements.MeasurementsDatabase;
import org.supla.android.events.ChannelConfigEventsManager;
import org.supla.android.events.DeviceConfigEventsManager;
import org.supla.android.events.UpdateEventsManager;
import org.supla.android.lib.actions.ActionId;
import org.supla.android.lib.actions.ActionParameters;
import org.supla.android.lib.actions.SubjectType;
import org.supla.android.profile.AuthInfo;
import org.supla.android.profile.ProfileIdHolder;
import org.supla.android.profile.ProfileManager;
import org.supla.android.usecases.channel.ChannelToRootRelationHolderUseCase;
import org.supla.android.usecases.channel.UpdateChannelExtendedValueUseCase;
import org.supla.android.usecases.channel.UpdateChannelUseCase;
import org.supla.android.usecases.channel.UpdateChannelValueUseCase;
import org.supla.android.usecases.channel.UpdateExtendedValueResult;
import org.supla.android.usecases.channelconfig.InsertChannelConfigUseCase;
import org.supla.android.usecases.channelrelation.DeleteRemovableChannelRelationsUseCase;
import org.supla.android.usecases.channelrelation.InsertChannelRelationForProfileUseCase;
import org.supla.android.usecases.channelrelation.MarkChannelRelationsAsRemovableUseCase;
import org.supla.android.usecases.channelstate.UpdateChannelStateUseCase;
import org.supla.android.usecases.group.UpdateChannelGroupTotalValueUseCase;

@SuppressWarnings("unused")
public class SuplaClient extends Thread implements SuplaClientApi {

  private static final long MINIMUM_WAITING_TIME_MSEC = 2000;
  private static final int CONNECTION_TIMEOUT_MS = 5000; // 5 seconds
  public static final int SUPLA_APP_ID = 1;
  private static final String log_tag = "SuplaClientThread";
  private static final Object st_lck = new Object();
  private static SuplaRegisterError lastRegisterError = null;

  static {
    System.loadLibrary("suplaclient");
  }

  private final Object sc_lck = new Object();
  private int _client_id;
  private long _supla_client_ptr = 0;
  private long _supla_client_ptr_counter = 0;
  private boolean _canceled = false;
  private DbHelper DbH = null;
  private int regTryCounter = 0; // supla-server v1.0 for Raspberry Compatibility fix
  private long lastTokenRequest = 0;
  private boolean superUserAuthorized = false;
  private String oneTimePassword;
  private final Context _context;
  private final ConnectivityManager connectivityManager;
  private final ProfileManager profileManager;
  private final UpdateEventsManager updateEventsManager;
  private final ChannelConfigEventsManager channelConfigEventsManager;
  private final DeviceConfigEventsManager deviceConfigEventsManager;
  private final EncryptedPreferences preferences;
  private final MarkChannelRelationsAsRemovableUseCase markChannelRelationsAsRemovableUseCase;
  private final InsertChannelRelationForProfileUseCase insertChannelRelationForProfileUseCase;
  private final DeleteRemovableChannelRelationsUseCase deleteRemovableChannelRelationsUseCase;
  private final SuplaCloudConfigHolder suplaCloudConfigHolder;
  private final InsertChannelConfigUseCase insertChannelConfigUseCase;
  private final UpdateChannelUseCase updateChannelUseCase;
  private final UpdateChannelValueUseCase updateChannelValueUseCase;
  private final UpdateChannelExtendedValueUseCase updateChannelExtendedValueUseCase;
  private final UpdateChannelStateUseCase updateChannelStateUseCase;
  private final AppDatabase appDatabase;
  private final MeasurementsDatabase measurementsDatabase;
  private final ProfileIdHolder profileIdHolder;
  private final UpdateChannelGroupTotalValueUseCase updateChannelGroupTotalValueUseCase;
  private final SuplaClientStateHolder suplaClientStateHolder;
  private final ChannelToRootRelationHolderUseCase channelToRootRelationHolderUseCase;

  public SuplaClient(
      Context context, String oneTimePassword, SuplaClientDependencies dependencies) {
    super();
    this._context = context;
    this.connectivityManager =
        (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
    this.oneTimePassword = oneTimePassword;
    this.profileManager = dependencies.getProfileManager();
    this.updateEventsManager = dependencies.getUpdateEventsManager();
    this.channelConfigEventsManager = dependencies.getChannelConfigEventsManager();
    this.deviceConfigEventsManager = dependencies.getDeviceConfigEventsManager();
    this.preferences = dependencies.getEncryptedPreferences();
    this.markChannelRelationsAsRemovableUseCase =
        dependencies.getMarkChannelRelationsAsRemovableUseCase();
    this.insertChannelRelationForProfileUseCase =
        dependencies.getInsertChannelRelationForProfileUseCase();
    this.deleteRemovableChannelRelationsUseCase =
        dependencies.getDeleteRemovableChannelRelationsUseCase();
    this.suplaCloudConfigHolder = dependencies.getSuplaCloudConfigHolder();
    this.insertChannelConfigUseCase = dependencies.getInsertChannelConfigUseCase();
    this.updateChannelUseCase = dependencies.getUpdateChannelUseCase();
    this.updateChannelValueUseCase = dependencies.getUpdateChannelValueUseCase();
    this.updateChannelExtendedValueUseCase = dependencies.getUpdateChannelExtendedValueUseCase();
    this.updateChannelStateUseCase = dependencies.getUpdateChannelStateUseCase();
    this.appDatabase = dependencies.getAppDatabase();
    this.measurementsDatabase = dependencies.getMeasurementsDatabase();
    this.profileIdHolder = dependencies.getProfileIdHolder();
    this.updateChannelGroupTotalValueUseCase =
        dependencies.getUpdateChannelGroupTotalValueUseCase();
    this.channelToRootRelationHolderUseCase = dependencies.getChannelToRootRelationHolderUseCase();
    this.suplaClientStateHolder = dependencies.getSuplaClientStateHolder();
  }

  public static SuplaRegisterError getLastRegisterError() {

    SuplaRegisterError result;

    synchronized (st_lck) {
      result = lastRegisterError;
    }

    return result;
  }

  public native void cfgInit(SuplaCfg cfg);

  private native long scInit(SuplaCfg cfg);

  private native void scFree(long _supla_client);

  private native int scGetId(long _supla_client);

  private native boolean scConnect(long _supla_client, int connectionTimeoutMs);

  private native boolean scConnected(long _supla_client);

  private native boolean scRegistered(long _supla_client);

  private native void scDisconnect(long _supla_client);

  private native boolean scIterate(long _supla_client, int wait_usec);

  private native boolean scOpen(long _supla_client, int ID, int Group, int Open);

  private native boolean scTimerArm(long _supla_client, int ChannelID, int On, int DurationMS);

  private native boolean scSetRGBW(
      long _supla_client,
      int ID,
      int Group,
      int Color,
      int ColorBrightness,
      int Brightness,
      int TurnOnOff);

  private native boolean scGetRegistrationEnabled(long _supla_client);

  private native int scGetProtoVersion(long _supla_client);

  private native int scGetMaxProtoVersion(long _supla_client);

  private native boolean scOAuthTokenRequest(long _supla_client);

  private native boolean scDeviceCalCfgRequest(
      long _supla_client, int ID, int Group, int Command, int DataType, byte[] Data);

  private native boolean scDeviceCalCfgCancelAllCommands(long _supla_client, int DeviceID);

  private native boolean scThermostatScheduleCfgRequest(
      long _supla_client, int ID, int Group, SuplaThermostatScheduleCfg cfg);

  private native boolean scSuperUserAuthorizationRequest(
      long _supla_client, String email, String password);

  private native boolean scGetSuperUserAuthorizationResult(long _supla_client);

  private native boolean scGetChannelState(long _supla_client, int ChannelID);

  private native boolean scGetChannelBasicCfg(long _supla_client, int ChannelID);

  private native boolean scSetChannelFunction(long _supla_client, int ChannelID, int Function);

  private native boolean scSetChannelCaption(long _supla_client, int ChannelID, String Caption);

  private native boolean scSetChannelGroupCaption(
      long _supla_client, int ChannelGroupID, String Caption);

  private native boolean scSetLocationCaption(long _supla_client, int LocationID, String Caption);

  private native boolean scSetSceneCaption(long _supla_client, int SceneID, String Caption);

  private native boolean scReconnectAllClients(long _supla_client);

  private native boolean scSetRegistrationEnabled(
      long _supla_client, int ioDeviceRegTimeSec, int clientRegTimeSec);

  private native boolean scReconnectDevice(long _supla_client, int DeviceID);

  private native boolean scZWaveConfigModeActive(long _supla_client, int DeviceID);

  private native boolean scZWaveResetAndClear(long _supla_client, int DeviceID);

  private native boolean scZWaveAddNode(long _supla_client, int DeviceID);

  private native boolean scZWaveRemoveNode(long _supla_client, int DeviceID);

  private native boolean scZWaveGetNodeList(long _supla_client, int DeviceID);

  private native boolean scZWaveGetAssignedNodeId(long _supla_client, int ChannelID);

  private native boolean scZWaveAssignNodeId(long _supla_client, int ChannelID, short NodeId);

  private native boolean scZWaveGetWakeUpSettings(long _supla_client, int ChannelID);

  private native boolean scZWaveSetWakeUpTime(long _supla_client, int ChannelID, int Time);

  private native boolean scSetLightsourceLifespan(
      long _supla_client, int ChannelID, boolean resetCounter, boolean setTime, int lifeSpan);

  private native boolean scSetDfgTransparency(
      long _supla_client, int ChannelID, short mask, short active_bits);

  private native boolean scExecuteAction(long _supla_client, @NotNull ActionParameters parameters);

  private native boolean scRegisterPushNotificationClientToken(
      long _supla_client, int appId, String token, String profileName);

  private native boolean scGetChannelConfig(
      long _supla_client, int channelId, @NotNull ChannelConfigType type);

  private native boolean scSetChannelConfig(long _supla_client, @NotNull SuplaChannelConfig config);

  private native boolean scGetDeviceConfig(
      long _supla_client, int deviceId, @NotNull EnumSet<FieldType> fieldTypes);

  private void sendMessage(SuplaClientMsg msg) {
    if (canceled()) {
      return;
    }
    SuplaClientMessageHandler.getGlobalInstance().sendMessage(msg);
  }

  private void init(SuplaCfg cfg) {
    synchronized (sc_lck) {
      if (_supla_client_ptr == 0) {
        _supla_client_ptr_counter = 0;
        _supla_client_ptr = scInit(cfg);
      }
    }
  }

  private long lockClientPtr() {
    long result = 0;
    synchronized (sc_lck) {
      if (_supla_client_ptr != 0) {
        _supla_client_ptr_counter++;
        result = _supla_client_ptr;
      }
    }
    return result;
  }

  private void unlockClientPtr() {
    synchronized (sc_lck) {
      if (_supla_client_ptr != 0 && _supla_client_ptr_counter > 0) {
        _supla_client_ptr_counter--;
      }
    }
  }

  private void free() {
    boolean freed = false;

    while (!freed) {

      synchronized (sc_lck) {
        if (_supla_client_ptr != 0 && _supla_client_ptr_counter == 0) {
          scFree(_supla_client_ptr);
          _supla_client_ptr = 0;
          freed = true;
        } else if (_supla_client_ptr == 0 && _supla_client_ptr_counter == 0) {
          freed = true;
        }
      }

      if (!freed) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
      }
    }
  }

  public int getClientId() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 ? scGetId(_supla_client_ptr) : 0;
    } finally {
      unlockClientPtr();
    }
  }

  private boolean connect() {
    if (hasNetworkConnection()) {
      long _supla_client_ptr = lockClientPtr();
      try {
        return _supla_client_ptr != 0 && scConnect(_supla_client_ptr, CONNECTION_TIMEOUT_MS);
      } finally {
        unlockClientPtr();
      }
    } else {
      suplaClientStateHolder.handleEvent(new SuplaClientEvent.Error(NoNetwork.INSTANCE));
    }

    return false;
  }

  private boolean hasNetworkConnection() {
    NetworkInfo activeNetworkInfo =
        connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();

    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }

  public void reconnect() {
    if (connected()) {
      disconnect();
    }
  }

  private boolean connected() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scConnected(_supla_client_ptr);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean registered() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scRegistered(_supla_client_ptr);
    } finally {
      unlockClientPtr();
    }
  }

  private void disconnect() {
    long _supla_client_ptr = lockClientPtr();
    try {
      if (_supla_client_ptr != 0) {
        scDisconnect(_supla_client_ptr);
      }
    } finally {
      unlockClientPtr();
    }
  }

  private boolean iterate() {
    return _supla_client_ptr != 0 && scIterate(_supla_client_ptr, 100000);
  }

  public boolean open(int ID, boolean Group, int Open) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scOpen(_supla_client_ptr, ID, Group ? 1 : 0, Open);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean timerArm(int ChannelID, boolean On, int DurationMS) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scTimerArm(_supla_client_ptr, ChannelID, On ? 1 : 0, DurationMS);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean open(int ChannelID, int Open) {
    return open(ChannelID, false, Open);
  }

  public boolean setRGBW(
      int ID, boolean Group, int Color, int ColorBrightness, int Brightness, boolean TurnOnOff) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scSetRGBW(
              _supla_client_ptr,
              ID,
              Group ? 1 : 0,
              Color,
              ColorBrightness,
              Brightness,
              TurnOnOff ? 1 : 0);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setRGBW(
      int ChannelID, int Color, int ColorBrightness, int Brightness, boolean TurnOnOff) {
    return setRGBW(ChannelID, false, Color, ColorBrightness, Brightness, TurnOnOff);
  }

  public void getRegistrationEnabled() {
    long _supla_client_ptr = lockClientPtr();
    try {
      if (_supla_client_ptr != 0) {
        scGetRegistrationEnabled(_supla_client_ptr);
      }
    } finally {
      unlockClientPtr();
    }
  }

  public int getProtoVersion() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 ? scGetProtoVersion(_supla_client_ptr) : 0;
    } finally {
      unlockClientPtr();
    }
  }

  public int getMaxProtoVersion() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 ? scGetMaxProtoVersion(_supla_client_ptr) : 0;
    } finally {
      unlockClientPtr();
    }
  }

  public void oAuthTokenRequest() {
    if (canceled() || !registered()) {
      return;
    }

    long now = System.currentTimeMillis();
    if (now - lastTokenRequest <= 5000) {
      Trace.d(log_tag, "Token already requested: " + (now - lastTokenRequest));
      return;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      if (_supla_client_ptr != 0 && scOAuthTokenRequest(_supla_client_ptr)) {
        lastTokenRequest = now;
      }
    } finally {
      unlockClientPtr();
    }
  }

  public boolean turnOnOff(
      Context context,
      boolean turnOn,
      int remoteId,
      boolean group,
      int channelFunc,
      boolean vibrate) {
    if ((channelFunc == SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH
        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH
        || channelFunc == SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER)) {
      if (turnOn) {
        DbHelper helper = DbHelper.getInstance(context);
        if (helper == null) {
          return false;
        }
        Channel channel = helper.getChannel(remoteId);
        if (channel == null) {
          return false;
        }
        if (!channel.getValue().hiValue() && channel.getValue().overcurrentRelayOff()) {
          AlertDialog.Builder builder = new AlertDialog.Builder(context);
          builder.setTitle(android.R.string.dialog_alert_title);
          builder.setMessage(R.string.overcurrent_question);

          builder.setPositiveButton(
              R.string.yes,
              (dialog, which) -> {
                dialog.dismiss();

                if (vibrate) {
                  SuplaApp.Vibrate(context);
                }
                open(remoteId, group, 1);
              });

          builder.setNeutralButton(R.string.no, (dialog, id) -> dialog.cancel());

          AlertDialog alert = builder.create();
          alert.show();
          return true;
        }
      }

      if (vibrate) {
        SuplaApp.Vibrate(context);
      }
      open(remoteId, group, turnOn ? 1 : 0);
      return true;
    }

    return false;
  }

  public void superUserAuthorizationRequest(String email, String password) {
    long _supla_client_ptr = lockClientPtr();
    try {
      if (_supla_client_ptr != 0) {
        scSuperUserAuthorizationRequest(_supla_client_ptr, email, password);
      }
    } finally {
      unlockClientPtr();
    }
  }

  public void getSuperUserAuthorizationResult() {
    long _supla_client_ptr = lockClientPtr();
    try {
      if (_supla_client_ptr != 0) {
        scGetSuperUserAuthorizationResult(_supla_client_ptr);
      }
    } finally {
      unlockClientPtr();
    }
  }

  public boolean isSuperUserAuthorized() {
    boolean result = false;
    synchronized (sc_lck) {
      result = superUserAuthorized;
    }
    return result;
  }

  public boolean deviceCalCfgRequest(
      int ID, boolean Group, int Command, int DataType, byte[] Data) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scDeviceCalCfgRequest(_supla_client_ptr, ID, Group ? 1 : 0, Command, DataType, Data);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean deviceCalCfgRequest(int ChannelID, int Command, int DataType, byte[] Data) {
    return deviceCalCfgRequest(ChannelID, false, Command, DataType, Data);
  }

  public boolean thermostatScheduleCfgRequest(
      int ID, boolean Group, SuplaThermostatScheduleCfg cfg) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scThermostatScheduleCfgRequest(_supla_client_ptr, ID, Group ? 1 : 0, cfg);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean deviceCalCfgCancelAllCommands(int DeviceID) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scDeviceCalCfgCancelAllCommands(_supla_client_ptr, DeviceID);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean thermostatScheduleCfgRequest(int ChannelID, SuplaThermostatScheduleCfg cfg) {
    return thermostatScheduleCfgRequest(ChannelID, false, cfg);
  }

  public boolean getChannelState(int ChannelID) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scGetChannelState(_supla_client_ptr, ChannelID);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean getChannelBasicCfg(int ChannelID) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scGetChannelBasicCfg(_supla_client_ptr, ChannelID);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setChannelFunction(int ChannelID, int Function) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scSetChannelFunction(_supla_client_ptr, ChannelID, Function);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setChannelCaption(int ChannelID, String Caption) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scSetChannelCaption(_supla_client_ptr, ChannelID, Caption);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setChannelGroupCaption(int ChannelGroupID, String Caption) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scSetChannelGroupCaption(_supla_client_ptr, ChannelGroupID, Caption);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setLocationCaption(int LocationID, String Caption) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scSetLocationCaption(_supla_client_ptr, LocationID, Caption);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setSceneCaption(int SceneID, String Caption) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scSetSceneCaption(_supla_client_ptr, SceneID, Caption);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setDfgTransparency(int ChannelID, short mask, short active_bits) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scSetDfgTransparency(_supla_client_ptr, ChannelID, mask, active_bits);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean reconnectAllClients() {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scReconnectAllClients(_supla_client_ptr);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setRegistrationEnabled(int ioDeviceRegTimeSec, int clientRegTimeSec) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scSetRegistrationEnabled(_supla_client_ptr, ioDeviceRegTimeSec, clientRegTimeSec);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean reconnectDevice(int DeviceId) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scReconnectDevice(_supla_client_ptr, DeviceId);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveConfigModeActive(Integer DeviceID) {
    if (DeviceID == null) {
      return false;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scZWaveConfigModeActive(_supla_client_ptr, DeviceID.intValue());
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveResetAndClear(Integer DeviceID) {
    if (DeviceID == null) {
      return false;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveResetAndClear(_supla_client_ptr, DeviceID.intValue());
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveAddNode(Integer DeviceID) {
    if (DeviceID == null) {
      return false;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveAddNode(_supla_client_ptr, DeviceID.intValue());
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveRemoveNode(Integer DeviceID) {
    if (DeviceID == null) {
      return false;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveRemoveNode(_supla_client_ptr, DeviceID.intValue());
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveGetNodeList(Integer DeviceID) {
    if (DeviceID == null) {
      return false;
    }

    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveGetNodeList(_supla_client_ptr, DeviceID.intValue());
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveGetAssignedNodeId(int ChannelID) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveGetAssignedNodeId(_supla_client_ptr, ChannelID);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveAssignNodeId(int ChannelID, short NodeId) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveAssignNodeId(_supla_client_ptr, ChannelID, NodeId);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveGetWakeUpSettings(int ChannelID) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveGetWakeUpSettings(_supla_client_ptr, ChannelID);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean zwaveSetWakeUpTime(int ChannelID, int Time) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scZWaveSetWakeUpTime(_supla_client_ptr, ChannelID, Time);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setLightsourceLifespan(
      int ChannelID, boolean resetCounter, boolean setTime, int lifeSpan) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scSetLightsourceLifespan(
              _supla_client_ptr, ChannelID, resetCounter, setTime, lifeSpan);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean executeAction(@NotNull ActionParameters parameters) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scExecuteAction(_supla_client_ptr, parameters);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean registerPushNotificationClientToken(int appId, String token, String profileName) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0
          && scRegisterPushNotificationClientToken(_supla_client_ptr, appId, token, profileName);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean getChannelConfig(int channelId, @NotNull ChannelConfigType type) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scGetChannelConfig(_supla_client_ptr, channelId, type);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean getDeviceConfig(int deviceId, @NotNull EnumSet<FieldType> fieldTypes) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scGetDeviceConfig(_supla_client_ptr, deviceId, fieldTypes);
    } finally {
      unlockClientPtr();
    }
  }

  public boolean setChannelConfig(@NotNull SuplaChannelConfig config) {
    long _supla_client_ptr = lockClientPtr();
    try {
      return _supla_client_ptr != 0 && scSetChannelConfig(_supla_client_ptr, config);
    } finally {
      unlockClientPtr();
    }
  }

  private void onVersionError(SuplaVersionError versionError) {
    Trace.d(
        log_tag,
        Integer.valueOf(versionError.Version).toString()
            + ","
            + Integer.valueOf(versionError.RemoteVersionMin).toString()
            + ","
            + Integer.valueOf(versionError.RemoteVersion).toString());

    regTryCounter = 0;

    AuthProfileItem profile = profileManager.getCurrentProfile().blockingGet();

    if (versionError.RemoteVersion >= 7
        && versionError.Version > versionError.RemoteVersion
        && profile.getAuthInfo().getPreferredProtocolVersion() != versionError.RemoteVersion) {

      // set prefered to lower
      profile.getAuthInfo().setPreferredProtocolVersion(versionError.RemoteVersion);
      profileManager.update(profile).blockingSubscribe();

      reconnect();
      return;
    }

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onVersionError);
    msg.setVersionError(versionError);
    sendMessage(msg);

    cancel();
    suplaClientStateHolder.handleEvent(new SuplaClientEvent.Error(VersionError.INSTANCE));
  }

  private void onConnecting() {
    Trace.d(log_tag, "Connecting");

    sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onConnecting));
    suplaClientStateHolder.handleEvent(Connecting.INSTANCE);
  }

  private void onConnError(SuplaConnError connError) {

    Trace.d(log_tag, connError.codeToString(_context));

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onConnError);
    msg.setConnError(connError);
    sendMessage(msg);

    if (connError.Code == SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND) {
      cancel();
    }
    suplaClientStateHolder.handleEvent(
        new SuplaClientEvent.Error(new SuplaClientState.Reason.ConnectionError(connError)));
  }

  private void onConnected() {
    Trace.d(log_tag, "connected");
    DbH = DbHelper.getInstance(_context);

    sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onConnected));
  }

  private void onDisconnected() {
    sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onDisconnected));
  }

  private void onRegistering() {
    Trace.d(log_tag, "Registering");

    regTryCounter++;
    sendMessage(new SuplaClientMsg(this, SuplaClientMsg.onRegistering));
  }

  private void onRegistered(SuplaRegisterResult registerResult) {

    Trace.d(log_tag, "registered");

    regTryCounter = 0;
    AuthProfileItem profile = profileManager.getCurrentProfile().blockingGet();

    int maxVersionSupportedByLibrary = getMaxProtoVersion();
    int storedVersion = profile.getAuthInfo().getPreferredProtocolVersion();
    int serverVersion = registerResult.Version;
    if (maxVersionSupportedByLibrary > 0
        && storedVersion < maxVersionSupportedByLibrary
        && serverVersion > storedVersion) {
      int newVersion = serverVersion;
      if (newVersion > maxVersionSupportedByLibrary) {
        newVersion = maxVersionSupportedByLibrary;
      }

      profile.getAuthInfo().setPreferredProtocolVersion(newVersion);
      profileManager.update(profile).blockingSubscribe();

      reconnect();
      return;
    }

    _client_id = registerResult.ClientID;

    Trace.d(log_tag, "Protocol Version=" + registerResult.Version);
    Trace.d(log_tag, "registerResult.ChannelCount=" + registerResult.ChannelCount);
    Trace.d(log_tag, "registerResult.ChannelGroupCount=" + registerResult.ChannelGroupCount);

    if (registerResult.ChannelCount == 0 && DbH.setChannelsVisible(0, 2)) {
      onDataChanged();
      updateEventsManager.emitChannelsUpdate();
    }
    if (registerResult.ChannelGroupCount == 0 && DbH.setChannelGroupsVisible(0, 2)) {
      onDataChanged();
      updateEventsManager.emitGroupsUpdate();
    }
    if (registerResult.SceneCount == 0 && DbH.getSceneRepository().setScenesVisible(0, 2)) {
      onDataChanged();
      updateEventsManager.emitScenesUpdate();
    }

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegistered);
    msg.setRegisterResult(registerResult);
    sendMessage(msg);
    suplaClientStateHolder.handleEvent(SuplaClientEvent.Connected.INSTANCE);

    NotificationManager notificationManager =
        (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
    String token = preferences.getFcmToken();
    if (NotificationsHelper.Companion.areNotificationsEnabled(notificationManager)
        && token != null) {
      registerPushNotificationClientToken(SUPLA_APP_ID, token, profile.getName());
    } else {
      registerPushNotificationClientToken(SUPLA_APP_ID, "", profile.getName());
    }
  }

  private void onRegisterError(SuplaRegisterError registerError) {
    Trace.d(log_tag, "onRegisterError: " + registerError.codeToString(_context));

    regTryCounter = 0;
    synchronized (st_lck) {
      lastRegisterError = registerError;
    }

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegisterError);
    msg.setRegisterError(registerError);
    sendMessage(msg);

    cancel();
    suplaClientStateHolder.handleEvent(
        new SuplaClientEvent.Error(new SuplaClientState.Reason.RegisterError(registerError)));
  }

  private void onRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {

    Trace.d(log_tag, "onRegistrationEnabled");

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onRegistrationEnabled);
    msg.setRegistrationEnabled(registrationEnabled);
    sendMessage(msg);
  }

  private void onMinVersionRequired(SuplaMinVersionRequired minVersionRequired) {

    Trace.d(
        log_tag,
        "SuplaMinVersionRequired - CallType: "
            + minVersionRequired.CallType
            + " MinVersion: "
            + minVersionRequired.MinVersion);
  }

  private void locationUpdate(SuplaLocation location) {
    Trace.d(log_tag, "Location " + location.Id + " " + location.Caption);

    if (DbH.updateLocation(location)) {
      Trace.d(log_tag, "Location updated");
      onDataChanged();
    }
  }

  private boolean isChannelExcluded(SuplaChannel channel) {
    // For partner applications
    return false;
  }

  private void channelUpdate(SuplaChannel channel) {

    boolean _DataChanged = false;

    Trace.d(
        log_tag,
        "Channel Function"
            + channel.Func
            + "  channel ID: "
            + channel.Id
            + " channel Location ID: "
            + channel.LocationID
            + " status: "
            + channel.getAvailabilityStatus()
            + " AltIcon: "
            + channel.AltIcon
            + " UserIcon: "
            + channel.UserIcon
            + " Flags: "
            + channel.Flags);

    // Update channel value before update the channel
    if (!isChannelExcluded(channel)) {
      if (updateChannelValueUseCase.invoke(channel).blockingGet() == EntityUpdateResult.UPDATED) {
        _DataChanged = true;
      }
      if (updateChannelUseCase.invoke(channel).blockingGet() == EntityUpdateResult.UPDATED) {
        _DataChanged = true;
      }
    }

    if (channel.EOL) {
      _DataChanged = DbH.setChannelsVisible(0, 2);
      updateEventsManager.emitChannelsUpdate();
    }

    if (_DataChanged) {
      Trace.d(log_tag, "Channel updated");
      onDataChanged(channel.Id, 0);
    }
  }

  private void onChannelGroupValueChanged() {
    List<Integer> groupIds = updateChannelGroupTotalValueUseCase.invoke().blockingGet();
    for (int groupId : groupIds) {
      onDataChanged(0, groupId);
    }
  }

  private void channelGroupUpdate(SuplaChannelGroup channel_group) {

    boolean _DataChanged = false;

    Trace.d(
        log_tag,
        "Channel Group Function "
            + channel_group.Func
            + "  group ID: "
            + channel_group.Id
            + " group Location ID: "
            + channel_group.LocationID
            + " AltIcon: "
            + channel_group.AltIcon
            + " UserIcon: "
            + channel_group.UserIcon);

    if (DbH.updateChannelGroup(channel_group)) {
      _DataChanged = true;
    }

    if (channel_group.EOL) {
      updateEventsManager.emitGroupsUpdate();
      _DataChanged = DbH.setChannelGroupsVisible(0, 2);
    }

    if (channel_group.EOL) {
      onChannelGroupValueChanged();
    }

    if (_DataChanged) {
      Trace.d(log_tag, "Channel Group updated");
      onDataChanged(0, channel_group.Id);
    }
  }

  private void channelRelationUpdate(SuplaChannelRelation channel_relation) {
    Trace.d(log_tag, "Channel Relation Update: " + channel_relation.toString());

    if (channel_relation.isSol()) {
      markChannelRelationsAsRemovableUseCase.invoke().blockingSubscribe();
    }

    insertChannelRelationForProfileUseCase.invoke(channel_relation).blockingSubscribe();

    if (channel_relation.isEol()) {
      deleteRemovableChannelRelationsUseCase.invoke().blockingSubscribe();
      channelToRootRelationHolderUseCase.reloadRelations();
      updateEventsManager.emitChannelsUpdate();
    }
  }

  private void channelGroupRelationUpdate(SuplaChannelGroupRelation channelgroup_relation) {

    boolean _DataChanged = false;

    Trace.d(
        log_tag,
        "Channel Group Relation group ID: "
            + channelgroup_relation.ChannelGroupID
            + " channel ID: "
            + channelgroup_relation.ChannelID);

    if (DbH.updateChannelGroupRelation(channelgroup_relation)) {
      _DataChanged = true;
    }

    if (channelgroup_relation.EOL && DbH.setChannelGroupRelationsVisible(0, 2)) {
      _DataChanged = true;
    }

    if (channelgroup_relation.EOL) {
      onChannelGroupValueChanged();
    }

    if (_DataChanged) {
      Trace.d(log_tag, "Channel Group Relation updated");
      onDataChanged(0, channelgroup_relation.ChannelGroupID);
    }
  }

  private void sceneUpdate(SuplaScene scene) {
    boolean dataChanged = false;
    Trace.d(
        log_tag,
        "Scene id:"
            + scene.getId()
            + " locationId: "
            + scene.getLocationId()
            + " altIcon:"
            + scene.getAltIcon()
            + " userIcon:"
            + scene.getUserIcon()
            + " caption: "
            + scene.getCaption()
            + " EOL: "
            + scene.isEol());

    SceneRepository sr = DbH.getSceneRepository();
    if (sr.updateSuplaScene(scene)) {
      SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onSceneChanged);
      msg.setSceneId(scene.getId());
      sendMessage(msg);
      updateEventsManager.emitSceneUpdate(scene.getId());
    }

    if (scene.isEol()) {
      updateEventsManager.emitScenesUpdate();
      sr.setScenesVisible(0, 2);
    }
  }

  private void sceneStateUpdate(SuplaSceneState state) {
    SceneRepository sr = DbH.getSceneRepository();
    if (sr.updateSuplaSceneState(state)) {
      Trace.d(
          log_tag,
          "Scene State sceneId:"
              + state.getSceneId()
              + " startedAt: "
              + state.getStartedAt()
              + " estimatedEndDate: "
              + state.getEstimatedEndDate()
              + " isDuringExecution: "
              + state.isDuringExecution()
              + " initiatorId: "
              + state.getInitiatorId()
              + " initiatorName: "
              + state.getInitiatorName()
              + " EOL: "
              + state.isEol());
      SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onSceneChanged);
      msg.setSceneId(state.getSceneId());
      sendMessage(msg);
      updateEventsManager.emitSceneUpdate(state.getSceneId());
    }
  }

  private void channelValueUpdate(SuplaChannelValueUpdate channelValueUpdate) {
    Trace.d(
        log_tag,
        "Channel value id:"
            + channelValueUpdate.Id
            + " value: "
            + channelValueUpdate.Value
            + " status:"
            + channelValueUpdate.AvailabilityStatus
            + " EOL:"
            + channelValueUpdate.EOL);
    if (updateChannelValueUseCase.invoke(channelValueUpdate).blockingGet()
        == EntityUpdateResult.UPDATED) {
      onDataChanged(channelValueUpdate.Id, 0);
    }

    if (channelValueUpdate.EOL) {
      onChannelGroupValueChanged();
    }
  }

  private void channelExtendedValueUpdate(
      SuplaChannelExtendedValueUpdate channelExtendedValueUpdate) {
    UpdateExtendedValueResult result =
        updateChannelExtendedValueUseCase
            .invoke(channelExtendedValueUpdate.Id, channelExtendedValueUpdate.Value)
            .blockingGet();

    if (result.getResult() == EntityUpdateResult.UPDATED) {
      onDataChanged(channelExtendedValueUpdate.Id, 0, true, result.getTimerChanged());
    }
  }

  private void onEvent(SuplaEvent event) {
    Trace.d(log_tag, "Supla Event");

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onEvent);
    event.Owner = event.SenderID == _client_id;
    msg.setEvent(event);
    sendMessage(msg);
  }

  private void onOAuthTokenRequestResult(SuplaOAuthToken token) {
    Trace.d(log_tag, "OAuthToken" + (token == null ? " is null" : ""));

    if (token != null && token.getUrl() == null) {
      AuthInfo info = profileManager.getCurrentProfile().blockingGet().getAuthInfo();
      try {
        token.setUrl(new URL(info.getServerUrlString()));
      } catch (MalformedURLException ignored) {
      }
    }

    suplaCloudConfigHolder.setToken(token);

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onOAuthTokenRequestResult);
    msg.setOAuthToken(token);
    sendMessage(msg);
  }

  private void onDeviceCalCfgResult(int ChannelId, int Command, int Result, byte[] Data) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onCalCfgResult);
    msg.setChannelId(ChannelId);
    msg.setCommand(Command);
    msg.setResult(Result);
    msg.setData(Data);
    sendMessage(msg);
  }

  private void onDeviceCalCfgProgressReport(int ChannelId, int Command, short Progress) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onCalCfgProgressReport);
    msg.setChannelId(ChannelId);
    msg.setCommand(Command);
    msg.setProgress(Progress);
    sendMessage(msg);
  }

  private void onDeviceCalCfgDebugString(String str) {
    Trace.d("CalCfgDebugString", str);
  }

  private void onChannelState(SuplaChannelState state) {
    Trace.d(log_tag, "onChannelState channelId: " + state.getChannelId());
    updateChannelStateUseCase.invoke(state).blockingSubscribe();
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onChannelState);
    msg.setChannelState(state);
    sendMessage(msg);
    updateEventsManager.emitChannelUpdate(state.getChannelId());
  }

  private void onChannelBasicCfg(SuplaChannelBasicCfg cfg) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onChannelBasicCfg);
    msg.setChannelBasicCfg(cfg);
    sendMessage(msg);
  }

  private void onChannelFunctionSetResult(int ChannelID, int Func, int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onChannelFunctionSetResult);
    msg.setCode(ResultCode);
    msg.setFunc(Func);
    msg.setChannelId(ChannelID);
    sendMessage(msg);
  }

  private void onChannelCaptionSetResult(int ChannelID, String Caption, int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onChannelCaptionSetResult);
    msg.setCode(ResultCode);
    msg.setText(Caption);
    msg.setChannelId(ChannelID);
    sendMessage(msg);
  }

  private void onChannelGroupCaptionSetResult(int ChannelGroupID, String Caption, int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onChannelGroupCaptionSetResult);
    msg.setCode(ResultCode);
    msg.setText(Caption);
    msg.setChannelGroupId(ChannelGroupID);
    sendMessage(msg);
  }

  private void onLocationCaptionSetResult(int LocationID, String Caption, int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onLocationCaptionSetResult);
    msg.setCode(ResultCode);
    msg.setText(Caption);
    msg.setLocationId(LocationID);
    sendMessage(msg);
  }

  private void onSceneCaptionSetResult(int SceneID, String Caption, int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onSceneCaptionSetResult);
    msg.setCode(ResultCode);
    msg.setText(Caption);
    msg.setSceneId(SceneID);
    sendMessage(msg);
  }

  private void onClientsReconnectResult(int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onClientsReconnectResult);
    msg.setCode(ResultCode);
    sendMessage(msg);
  }

  private void onSetRegistrationEnabledResult(int ResultCode) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onSetRegistrationEnabledResult);
    msg.setCode(ResultCode);
    sendMessage(msg);
  }

  private void onSuperUserAuthorizationResult(boolean authorized, int code) {
    synchronized (sc_lck) {
      superUserAuthorized = authorized && code == SuplaConst.SUPLA_RESULTCODE_AUTHORIZED;
    }

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onSuperuserAuthorizationResult);
    msg.setSuccess(authorized);
    msg.setCode(code);
    sendMessage(msg);
  }

  private void onDataChanged(
      int ChannelId, int GroupId, boolean extendedValue, boolean timerValue) {

    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onDataChanged);
    msg.setChannelId(ChannelId);
    msg.setChannelGroupId(GroupId);
    msg.setExtendedValue(extendedValue);
    msg.setTimerValue(timerValue);

    sendMessage(msg);

    if (ChannelId != 0) {
      updateEventsManager.emitChannelUpdate(ChannelId);
    }
    if (GroupId != 0) {
      updateEventsManager.emitGroupUpdate(GroupId);
    }
  }

  private void onDataChanged(int ChannelId, int GroupId) {
    onDataChanged(ChannelId, GroupId, false, false);
  }

  private void onDataChanged() {
    onDataChanged(0, 0, false, false);
  }

  private void onZWaveResetAndClearResult(int result) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveResetAndClearResult);
    msg.setResult(result);
    sendMessage(msg);
  }

  private void onZWaveAddNodeResult(int result, ZWaveNode node) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveAddNodeResult);
    msg.setResult(result);
    msg.setNode(node);
    sendMessage(msg);
  }

  private void onZWaveRemoveNodeResult(int result, short nodeId) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveRemoveNodeResult);
    msg.setResult(result);
    msg.setNodeId(nodeId);
    sendMessage(msg);
  }

  private void onZWaveGetNodeListResult(int result, ZWaveNode node) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveGetNodeListResult);
    msg.setResult(result);
    msg.setNode(node);
    sendMessage(msg);
  }

  private void onZWaveGetAssignedNodeIdResult(int result, short nodeId) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveGetAssignedNodeIdResult);
    msg.setResult(result);
    msg.setNodeId(nodeId);
    sendMessage(msg);
  }

  private void onZWaveAssignNodeIdResult(int result, short nodeId) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveAssignNodeIdResult);
    msg.setResult(result);
    msg.setNodeId(nodeId);
    sendMessage(msg);
  }

  private void onZWaveWakeUpSettingsReport(int result, ZWaveWakeUpSettings settings) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveWakeUpSettingsReport);
    msg.setResult(result);
    msg.setWakeUpSettings(settings);
    sendMessage(msg);
  }

  private void onZWaveSetWakeUpTimeResult(int result) {
    SuplaClientMsg msg = new SuplaClientMsg(this, SuplaClientMsg.onZWaveSetWakeUpTimeResult);
    msg.setResult(result);
    sendMessage(msg);
  }

  private void onChannelConfigUpdateOrResult(SuplaChannelConfig config, ConfigResult result) {
    insertChannelConfigUseCase.invoke(config, result).blockingSubscribe();
    channelConfigEventsManager.emitConfig(result, config);
    if (result == ConfigResult.RESULT_TRUE && config != null) {
      updateEventsManager.emitChannelUpdate(config.getRemoteId());
    }
  }

  private void onDeviceConfigUpdateOrResult(
      SuplaDeviceConfig config, ConfigResult result, boolean eol) {
    deviceConfigEventsManager.emitConfig(result, config);
  }

  private void onActionExecutionResult(
      @Nullable ActionId actionId,
      @Nullable SubjectType subjectType,
      int subjectId,
      int resultCode) {
    Trace.w(log_tag, "Action exec result " + subjectId + " resultCode " + resultCode);
  }

  public synchronized boolean canceled() {
    return _canceled;
  }

  public void cancel() {
    cancel(null);
  }

  public synchronized void cancel(@Nullable Reason reason) {
    _canceled = true;
    suplaClientStateHolder.handleEvent(new Cancel(reason));
  }

  private String autodiscoverGetHost(String email) {

    String result = "";

    Uri.Builder builder = new Uri.Builder();
    builder
        .scheme("https")
        .authority("autodiscover.supla.org")
        .appendPath("users")
        .appendPath(email);

    URL url;
    try {
      url = new URL(builder.build().toString());

      StringBuilder json = new StringBuilder();
      String line;

      HttpsURLConnection https;
      try {
        https = (HttpsURLConnection) url.openConnection();
        BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()));

        while ((line = br.readLine()) != null) {
          json.append(line);
        }

        JSONTokener tokener = new JSONTokener(json.toString());
        try {
          JSONObject jsonResult = new JSONObject(tokener);
          if (jsonResult.getString("email").equals(email)) {
            result = jsonResult.getString("server");
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }

      } catch (IOException e) {
        e.printStackTrace();
      }

    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    return result;
  }

  private void setVisible(int Visible, int WhereVisible) {

    boolean _DataChanged = false;

    if (DbH.setChannelsVisible(Visible, WhereVisible)) {
      _DataChanged = true;
    }

    if (DbH.setChannelGroupsVisible(Visible, WhereVisible)) {
      _DataChanged = true;
    }

    if (DbH.setChannelGroupRelationsVisible(Visible, WhereVisible)) {
      _DataChanged = true;
    }

    if (DbH.getSceneRepository().setScenesVisible(Visible, WhereVisible)) {
      _DataChanged = true;
    }

    if (DbH.setChannelsOffline()) {
      _DataChanged = true;
    }

    if (_DataChanged) {
      onDataChanged();
    }
  }

  public void run() {

    // Needed to trigger database migration through Room.
    appDatabase.getOpenHelper().getReadableDatabase();
    measurementsDatabase.getOpenHelper().getReadableDatabase();

    // After database is ready - set current profile id
    AuthProfileItem currentProfile = profileManager.getCurrentProfile().blockingGet();
    if (currentProfile != null) {
      profileIdHolder.setProfileId(currentProfile.getId());
    }

    DbH = DbHelper.getInstance(_context);

    while (!canceled()) {

      synchronized (st_lck) {
        lastRegisterError = null;
        superUserAuthorized = false;
      }

      long _connectingStatusLastTime = System.currentTimeMillis();

      onConnecting();
      setVisible(2, 1);
      markChannelRelationsAsRemovableUseCase.invoke().blockingSubscribe();

      try {
        {
          SuplaCfg cfg = new SuplaCfg();
          cfgInit(cfg);

          AuthProfileItem profile = profileManager.getCurrentProfile().blockingGet();
          if (profile != null) {
            AuthInfo info = profile.getAuthInfo();

            cfg.Host = info.getServerForCurrentAuthMethod();
            cfg.clientGUID = info.getDecryptedGuid(_context);
            cfg.AuthKey = info.getDecryptedAuthKey(_context);
            cfg.Name = Build.MANUFACTURER + " " + Build.MODEL;
            cfg.SoftVer = "Android" + Build.VERSION.RELEASE + "/" + BuildConfig.VERSION_NAME;

            if (isAccessIDAuthentication()) {
              cfg.AccessID = info.getAccessID();
              cfg.AccessIDpwd = info.getAccessIDpwd();

              if (regTryCounter >= 2) {
                // supla-server v1.0 for Raspberry Compatibility fix
                info.setPreferredProtocolVersion(4);
                profileManager.update(profile).blockingSubscribe();
              }

            } else {
              cfg.Email = info.getEmailAddress();
              if (!cfg.Email.isEmpty() && cfg.Host.isEmpty() && shouldAutodiscoverHost()) {
                cfg.Host = autodiscoverGetHost(cfg.Email);

                if (hasNetworkConnection() && cfg.Host.isEmpty()) {
                  onConnError(new SuplaConnError(SuplaConst.SUPLA_RESULT_HOST_NOT_FOUND));
                } else {
                  info.setServerForEmail(cfg.Host);
                  profileManager.update(profile).blockingSubscribe();
                }
              }

              cfg.setPassword(oneTimePassword);
            }

            oneTimePassword = "";
            cfg.protocol_version = info.getPreferredProtocolVersion();
            init(cfg);
          }
        }

        if (connect()) {
          //noinspection StatementWithEmptyBody
          while (!canceled() && iterate()) {}
        }

      } finally {
        free();
      }

      if (!canceled()) {
        long timeDiff = System.currentTimeMillis() - _connectingStatusLastTime;
        if (timeDiff < MINIMUM_WAITING_TIME_MSEC) {
          try {
            Thread.sleep(MINIMUM_WAITING_TIME_MSEC - timeDiff);
          } catch (InterruptedException ignored) {
          }
        }
      }
    }

    SuplaApp.getApp().OnSuplaClientFinished(this);
    suplaClientStateHolder.handleEvent(new Finish());
    Trace.d(log_tag, "SuplaClient Finished");
  }

  private boolean isAccessIDAuthentication() {
    return !profileManager.getCurrentProfile().blockingGet().getAuthInfo().getEmailAuth();
  }

  private boolean shouldAutodiscoverHost() {
    return profileManager.getCurrentProfile().blockingGet().getAuthInfo().getServerAutoDetect();
  }

  public void startScene(int sceneId) {
    ActionParameters params = new ActionParameters(ActionId.EXECUTE, SubjectType.SCENE, sceneId);
    if (!executeAction(params)) {
      Trace.w(log_tag, "Failed to start scene " + sceneId);
    }
  }

  public void stopScene(int sceneId) {
    ActionParameters params = new ActionParameters(ActionId.INTERRUPT, SubjectType.SCENE, sceneId);
    if (!executeAction(params)) {
      Trace.w(log_tag, "Failed to interrupt scene " + sceneId);
    }
  }

  public void renameScene(int sceneId, String newName) {
    if (!setSceneCaption(sceneId, newName)) {
      Trace.w(log_tag, "Failed to rename scene " + sceneId);
    }
  }
}
