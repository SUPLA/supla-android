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
import android.preference.PreferenceManager

import org.supla.android.Preferences

class SingleAccountProfileManager(private val context: Context): ProfileManager {

    private val kProfileAdvancedEmailAuth = "org.supla.android.profile.default.advanced_email_auth"
    private val kProfileAdvancedServerAutoDetect = "org.supla.android.profile.default.server_auto_detect"

    override fun getAuthInfo(): AuthInfo {
        val prefs = Preferences(context)
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val useEmail = sp.getBoolean(kProfileAdvancedEmailAuth, !prefs.isAdvancedCfg)
        val autoDetect = sp.getBoolean(kProfileAdvancedServerAutoDetect, 
                                       !prefs.isAdvancedCfg || 
                                       (useEmail && prefs.serverAddress.isEmpty()))
        return AuthInfo(emailAuth = useEmail, serverAutoDetect = autoDetect)
    }

    override fun storeAuthInfo(info: AuthInfo) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val ed = sp.edit()
        ed.putBoolean(kProfileAdvancedEmailAuth, info.emailAuth)
        ed.putBoolean(kProfileAdvancedServerAutoDetect, info.serverAutoDetect)
        ed.apply()
    }
}
