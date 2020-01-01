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


public class SuplaCfg {

    byte[] clientGUID;
    byte[] AuthKey;
    String Name;
    int AccessID;
    String AccessIDpwd;
    String Email;
    String Password;
    String SoftVer;
    String Host;
    int tcp_port;
    int ssl_port;
    boolean ssl_enabled;
    int protocol_version = 0;


    void setClientGUID(byte[] clientGUID) {

        int len = clientGUID.length > SuplaConst.SUPLA_GUID_SIZE ? SuplaConst.SUPLA_GUID_SIZE : clientGUID.length;

        if ( len > 0 )
            System.arraycopy(clientGUID, 0, this.clientGUID, 0, len);

    }

    void setAuthKey(byte[] AuthKey) {

        int len = AuthKey.length > SuplaConst.SUPLA_AUTHKEY_SIZE ? SuplaConst.SUPLA_AUTHKEY_SIZE : AuthKey.length;

        if ( len > 0 )
            System.arraycopy(AuthKey, 0, this.AuthKey, 0, len);

    }

    void setName(String Name) {
        this.Name = Name.substring(0,SuplaConst.SUPLA_CLIENT_NAME_MAXSIZE-1);
    }

    void setAccessIDpwd(String AccessIDpwd) {
        this.AccessIDpwd = AccessIDpwd.substring(0, SuplaConst.SUPLA_ACCESSID_PWD_MAXSIZE-1);
    }

    void setEmail(String Email) {
        this.Email = Email.substring(0, SuplaConst.SUPLA_EMAIL_MAXSIZE-1);
    }

    void setPassword(String Password) {
        this.Password = Password.substring(0, SuplaConst.SUPLA_PASSWORD_MAXSIZE-1);
    }

    void setSoftVer(String SoftVer) {
        this.SoftVer = SoftVer.substring(0, SuplaConst.SUPLA_SOFTVER_MAXSIZE-1);
    }


}
