package org.supla.android.profile

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

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.supla.android.db.AuthProfileItem
import org.supla.android.SuplaApp

/**
ProfileMigrator is a utility class which only task is
to generate a profile (default) profile entry derived
from legacy settings (i.e. authentication settings 
stored in application preferences) or for usage in
new app installations.
*/
class ProfileMigrator(private val ctx: Context) {

    companion object {
        private const val pref_serveraddr = "pref_serveraddr"
        private const val pref_accessid = "pref_accessid"
        private const val pref_accessidpwd = "pref_accessidpwd"
        private const val pref_email = "pref_email"
        private const val pref_advanced = "pref_advanced"
        private const val pref_proto_ver = "pref_proto_ver"

    }

    private val prefs: SharedPreferences

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
    }
    
    /**
     @returns a profile item object populated with
     values derived from application preferences.
     */
    fun makeProfileUsingPreferences(): AuthProfileItem {

        val serverAddr = prefs.getString(pref_serveraddr, "") ?: ""
        val accessID = prefs.getInt(pref_accessid, 0)
        val accessIDpwd = prefs.getString(pref_accessidpwd, "") ?: ""
        val email = prefs.getString(pref_email, "") ?: ""
        val isAdvanced = prefs.getBoolean(pref_advanced, false)

        val client = SuplaApp.getApp().getSuplaClient()
        val protoVer = prefs.getInt(pref_proto_ver, if(client != null) 
                                    client.getMaxProtoVersion() else 0)        
        
        val ai = AuthInfo(emailAuth = !isAdvanced,
                          emailAddress = email,
                          serverAutoDetect = !isAdvanced,
                          serverForEmail = if(isAdvanced) "" else serverAddr,
                          serverForAccessID = if(isAdvanced) serverAddr else "",
                          accessID = accessID,
                          accessIDpwd = accessIDpwd,
                          preferredProtocolVersion = protoVer)

        return AuthProfileItem(authInfo = ai,
                               advancedAuthSetup = isAdvanced,
                               isActive = true)
    }
}
