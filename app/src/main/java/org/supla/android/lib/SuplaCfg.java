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

import org.supla.android.tools.UsedFromNativeCode;

public class SuplaCfg {

    @UsedFromNativeCode byte[] clientGUID;
    @UsedFromNativeCode byte[] AuthKey;
    @UsedFromNativeCode String Name;
    @UsedFromNativeCode int AccessID;
    @UsedFromNativeCode String AccessIDpwd;
    @UsedFromNativeCode String Email;
    @UsedFromNativeCode String Password;
    @UsedFromNativeCode String SoftVer;
    @UsedFromNativeCode String Host;
    @UsedFromNativeCode int tcp_port;
    @UsedFromNativeCode int ssl_port;
    @UsedFromNativeCode boolean ssl_enabled;
    @UsedFromNativeCode int protocol_version = 0;

    @UsedFromNativeCode
    void setClientGUID(byte[] clientGUID) {

        int len = Math.min(clientGUID.length, SuplaConst.SUPLA_GUID_SIZE);

        if (len > 0) {
            System.arraycopy(clientGUID, 0, this.clientGUID, 0, len);
        }
    }

    @UsedFromNativeCode
    void setAuthKey(byte[] AuthKey) {

        int len = Math.min(AuthKey.length, SuplaConst.SUPLA_AUTHKEY_SIZE);

        if (len > 0) {
            System.arraycopy(AuthKey, 0, this.AuthKey, 0, len);
        }
    }

    @UsedFromNativeCode
    void setName(String Name) {
        this.Name = Name == null ? "" : Name.substring(0,
                Math.min(Name.length(), SuplaConst.SUPLA_CLIENT_NAME_MAXSIZE - 1));
    }

    @UsedFromNativeCode
    void setAccessIDpwd(String AccessIDpwd) {
        this.AccessIDpwd = AccessIDpwd == null ? "" : AccessIDpwd.substring(0,
                Math.min(AccessIDpwd.length(), SuplaConst.SUPLA_ACCESSID_PWD_MAXSIZE - 1));
    }

    @UsedFromNativeCode
    void setEmail(String Email) {
        this.Email = Email == null ? "" : Email.substring(0,
                Math.min(Email.length(), SuplaConst.SUPLA_EMAIL_MAXSIZE - 1));
    }

    @UsedFromNativeCode
    void setPassword(String Password) {
        this.Password = Password == null ? "" : Password.substring(0,
                Math.min(Password.length(), SuplaConst.SUPLA_PASSWORD_MAXSIZE - 1));
    }

    @UsedFromNativeCode
    void setSoftVer(String SoftVer) {
        this.SoftVer = SoftVer == null ? "" : SoftVer.substring(0,
                Math.min(SoftVer.length(), SuplaConst.SUPLA_SOFTVER_MAXSIZE - 1));
    }
}
