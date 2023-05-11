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

@SuppressWarnings("unused")
public class SuplaClientMsg {

  public static final int onDataChanged = 1;
  public static final int onConnecting = 2;
  public static final int onRegistered = 3;
  public static final int onRegistering = 4;
  public static final int onRegisterError = 5;
  public static final int onDisconnected = 6;
  public static final int onConnected = 7;
  public static final int onVersionError = 8;
  public static final int onEvent = 9;
  public static final int onConnError = 10;
  public static final int onRegistrationEnabled = 11;
  public static final int onOAuthTokenRequestResult = 12;
  public static final int onCalCfgResult = 13;
  public static final int onSuperuserAuthorizationResult = 14;
  public static final int onChannelState = 15;
  public static final int onChannelBasicCfg = 16;
  public static final int onChannelFunctionSetResult = 17;
  public static final int onChannelCaptionSetResult = 18;
  public static final int onClientsReconnectResult = 19;
  public static final int onSetRegistrationEnabledResult = 20;
  public static final int onZWaveResetAndClearResult = 21;
  public static final int onZWaveAddNodeResult = 22;
  public static final int onZWaveRemoveNodeResult = 23;
  public static final int onZWaveGetNodeListResult = 24;
  public static final int onZWaveGetAssignedNodeIdResult = 25;
  public static final int onZWaveAssignNodeIdResult = 26;
  public static final int onCalCfgProgressReport = 27;
  public static final int onZWaveWakeUpSettingsReport = 28;
  public static final int onZWaveSetWakeUpTimeResult = 29;
  public static final int onLocationCaptionSetResult = 30;
  public static final int onSceneCaptionSetResult = 31;
  public final static int onSceneChanged = 32;
  public static final int onChannelGroupCaptionSetResult = 33;
    private Object Sender;
    private int Type;
    private SuplaVersionError VersionError;
    private SuplaRegisterResult RegisterResult;
    private SuplaRegisterError RegisterError;
    private SuplaConnError ConnError;
    private SuplaEvent Event;
    private SuplaRegistrationEnabled RegistrationEnabled;
    private SuplaOAuthToken OAuthToken;
    private SuplaChannelState ChannelState;
    private SuplaChannelBasicCfg ChannelBasicCfg;
    private int ChannelId;
    private int ChannelGroupId;
    private int LocationId;
    private int SceneId;
    private boolean Success;
    private int Code;
    private int Command;
    private int Result;
    private byte[] data;
    private int Func;
    private short NodeId;
    private ZWaveNode Node;
    private String Text;
    private short Progress;
    private boolean ExtendedValue;
    private ZWaveWakeUpSettings WakeUpSettings;

  public SuplaClientMsg(Object sender, int type) {
    Type = type;
    Sender = sender;
    ChannelId = 0;
    ChannelGroupId = 0;
    SceneId = 0;
  }

  public Object getSender() {
    return Sender;
  }

  public int getType() {
    return Type;
  }

  public SuplaVersionError getVersionError() {
    return VersionError;
  }

  public void setVersionError(SuplaVersionError versionError) {
    VersionError = versionError == null ? null : new SuplaVersionError(versionError);
  }

  public SuplaRegisterResult getRegisterResult() {
    return RegisterResult;
  }

  public void setRegisterResult(SuplaRegisterResult registerResult) {
    RegisterResult = registerResult == null ? null : new SuplaRegisterResult(registerResult);
  }

  public SuplaRegisterError getRegisterError() {
    return RegisterError;
  }

  public void setRegisterError(SuplaRegisterError registerError) {
    RegisterError = registerError == null ? null : new SuplaRegisterError(registerError);
  }

  public SuplaConnError getConnError() {
    return ConnError;
  }

  public void setConnError(SuplaConnError connError) {
    ConnError = connError == null ? null : new SuplaConnError(connError);
  }

  public SuplaEvent getEvent() {
    return Event;
  }

  public void setEvent(SuplaEvent event) {
    Event = event == null ? null : new SuplaEvent(event);
  }

  public int getChannelId() {
    return ChannelId;
  }

  public void setChannelId(int channelId) {
    ChannelId = channelId;
  }

  public int getChannelGroupId() {
    return ChannelGroupId;
  }

  public void setChannelGroupId(int channelGroupId) {
    ChannelGroupId = channelGroupId;
  }

  public void setSceneId(int sceneId) {
    SceneId = sceneId;
  }

  public int getSceneId() {
    return SceneId;
  }

  public SuplaRegistrationEnabled getRegistrationEnabled() {
    return RegistrationEnabled;
  }

  public void setRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {
    RegistrationEnabled =
        registrationEnabled == null ? null : new SuplaRegistrationEnabled(registrationEnabled);
  }

  public SuplaOAuthToken getOAuthToken() {
    return OAuthToken;
  }

  public void setOAuthToken(SuplaOAuthToken token) {
    OAuthToken = token == null ? null : new SuplaOAuthToken(token);
  }

  public boolean isSuccess() {
    return Success;
  }

  public void setSuccess(boolean success) {
    Success = success;
  }

  public int getCode() {
    return Code;
  }

  public void setCode(int code) {
    Code = code;
  }

  public int getCommand() {
    return Command;
  }

  public void setCommand(int command) {
    Command = command;
  }

  public int getResult() {
    return Result;
  }

  public void setResult(int result) {
    Result = result;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public SuplaChannelState getChannelState() {
    return ChannelState;
  }

  public void setChannelState(SuplaChannelState channelState) {
    ChannelState = channelState;
  }

  public SuplaChannelBasicCfg getChannelBasicCfg() {
    return ChannelBasicCfg;
  }

  public void setChannelBasicCfg(SuplaChannelBasicCfg channelBasicCfg) {
    ChannelBasicCfg = channelBasicCfg;
  }

  public int getFunc() {
    return Func;
  }

  public void setFunc(int func) {
    Func = func;
  }

  public short getNodeId() {
    return NodeId;
  }

  public void setNodeId(short nodeId) {
    NodeId = nodeId;
  }

  public ZWaveNode getNode() {
    return Node;
  }

  public void setNode(ZWaveNode node) {
    Node = node;
  }

  public String getText() {
    return Text;
  }

  public void setText(String text) {
    Text = text;
  }

  public short getProgress() {
    return Progress;
  }

  public void setProgress(short progress) {
    Progress = progress;
  }

  public boolean isExtendedValue() {
    return ExtendedValue;
  }

  public void setExtendedValue(boolean extendedValue) {
    ExtendedValue = extendedValue;
  }

  public ZWaveWakeUpSettings getWakeUpSettings() {
    return WakeUpSettings;
  }

  public void setWakeUpSettings(ZWaveWakeUpSettings wakeUpSettings) {
    WakeUpSettings = wakeUpSettings;
  }

  public int getLocationId() {
    return LocationId;
  }

  public void setLocationId(int locationId) {
    LocationId = locationId;
  }
}
