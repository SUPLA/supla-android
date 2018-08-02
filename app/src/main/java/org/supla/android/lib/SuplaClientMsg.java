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
    private int ChannelId;
    private int ChannelGroupId;

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
        VersionError = versionError;
    }

    public SuplaRegisterResult getRegisterResult() {
        return RegisterResult;
    }

    public void setRegisterResult(SuplaRegisterResult registerResult) {
        RegisterResult = registerResult;
    }

    public SuplaRegisterError getRegisterError() {
        return RegisterError;
    }

    public void setRegisterError(SuplaRegisterError registerError) {
        RegisterError = registerError;
    }

    public SuplaConnError getConnError() {
        return ConnError;
    }

    public void setConnError(SuplaConnError connError) {
        ConnError = connError;
    }

    public SuplaEvent getEvent() {
        return Event;
    }

    public void setEvent(SuplaEvent event) {
        Event = event;
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

    void setRegistrationEnabled(SuplaRegistrationEnabled registrationEnabled) {
        RegistrationEnabled = registrationEnabled;
    }

    void setOAuthToken(SuplaOAuthToken token) {
        OAuthToken = token;
    }

    SuplaOAuthToken getOAuthToken() {
        return OAuthToken;
    }
}
