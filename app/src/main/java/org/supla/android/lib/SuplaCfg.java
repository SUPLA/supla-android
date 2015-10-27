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


public class SuplaCfg {

    byte[] clientGUID;
    String Name;
    int AccessID;
    String AccessIDpwd;
    String SoftVer;
    String Host;
    int tcp_port;
    int ssl_port;
    boolean ssl_enabled;


    void setClientGUID(byte[] clientGUID) {

        int len = clientGUID.length > SuplaConst.SUPLA_GUID_SIZE ? SuplaConst.SUPLA_GUID_SIZE : clientGUID.length;

        if ( len > 0 )
            System.arraycopy(clientGUID, 0, this.clientGUID, 0, len);

    }

    void setName(String Name) {
        this.Name = Name.substring(0,SuplaConst.SUPLA_CLIENT_NAME_MAXSIZE-1);
    }

    void setAccessIDpwd(String AccessIDpwd) {
        this.AccessIDpwd = AccessIDpwd.substring(0, SuplaConst.SUPLA_ACCESSID_PWD_MAXSIZE-1);
    }

    void setSoftVer(String SoftVer) {
        this.SoftVer = SoftVer.substring(0, SuplaConst.SUPLA_SOFTVER_MAXSIZE-1);
    }


}
