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

    private SuplaClient Sender;
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
    private boolean Success;
    private int Code;
    private int Command;
    private int Result;
    private byte[] data;
    private int Func;
    private Short NodeId;
    private ZWaveNode Node;

    public final static int onDataChanged         = 1;
    public final static int onConnecting          = 2;
    public final static int onRegistered          = 3;
    public final static int onRegistering         = 4;
    public final static int onRegisterError       = 5;
    public final static int onDisconnected        = 6;
    public final static int onConnected           = 7;
    public final static int onVersionError        = 8;
    public final static int onEvent               = 9;
    public final static int onConnError           = 10;
    public final static int onRegistrationEnabled = 11;
    public final static int onOAuthTokenRequestResult = 12;
    public final static int onCalCfgResult = 13;
    public final static int onSuperuserAuthorizationResult = 14;
    public final static int onChannelState = 15;
    public final static int onChannelBasicCfg = 16;
    public final static int onChannelFunctionSetResult = 17;
    public final static int onClientsReconnectResult = 18;
    public final static int onSetRegistrationEnabledResult = 19;
    public final static int onZWaveResetAndClearResult = 20;
    public final static int onZWaveAddNodeResult = 21;
    public final static int onZWaveRemoveNodeResult = 22;
    public final static int onZWaveGetNodeListResult = 23;
    public final static int onZWaveGetAssignedNodeIdResult = 24;

    public SuplaClientMsg(SuplaClient sender, int type) {
        Type = type;
        Sender = sender;
        ChannelId = 0;
        ChannelGroupId = 0;
    }

    public SuplaClient getSender() {
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

    public void setChannelGroupId(int channelGroupId) {
        ChannelGroupId = channelGroupId;
    }

    public int getChannelGroupId() {
        return ChannelGroupId;
    }

    public void setChannelId(int channelId) {
        ChannelId = channelId;
    }

    public SuplaRegistrationEnabled getRegistrationEnabled() {
        return RegistrationEnabled;
    }

    public void setRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {
        RegistrationEnabled = registrationEnabled
                == null ? null : new SuplaRegistrationEnabled(registrationEnabled);
    }

    public void setOAuthToken(SuplaOAuthToken token) {
        OAuthToken = token == null ? null : new SuplaOAuthToken(token);
    }

    public SuplaOAuthToken getOAuthToken() {
        return OAuthToken;
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

    public void setChannelState(SuplaChannelState channelState) {
        ChannelState = channelState;
    }

    public SuplaChannelState getChannelState() {
        return ChannelState;
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

    public Short getNodeId() {
        return NodeId;
    }

    public void setNodeId(Short nodeId) {
        NodeId = nodeId;
    }

    public ZWaveNode getNode() {
        return Node;
    }

    public void setNode(ZWaveNode node) {
        Node = node;
    }
}
