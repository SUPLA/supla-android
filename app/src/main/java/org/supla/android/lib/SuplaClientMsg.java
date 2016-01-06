package org.supla.android.lib;

/*
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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

public class SuplaClientMsg {

    private SuplaClient Sender;
    private int Type;
    private SuplaVersionError VersionError;
    private SuplaRegisterResult RegisterResult;
    private SuplaRegisterError RegisterError;
    private SuplaEvent Event;

    public final static int onDataChanged     = 1;
    public final static int onConnecting      = 2;
    public final static int onRegistered      = 3;
    public final static int onRegistering     = 4;
    public final static int onRegisterError   = 5;
    public final static int onDisconnected    = 6;
    public final static int onConnected       = 7;
    public final static int onVersionError    = 8;
    public final static int onEvent           = 9;


    public SuplaClientMsg(SuplaClient sender, int type) {
        Type = type;
        Sender = sender;
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

    public SuplaEvent getEvent() {
        return Event;
    }

    public void setEvent(SuplaEvent event) {
        Event = event;
    }
}
