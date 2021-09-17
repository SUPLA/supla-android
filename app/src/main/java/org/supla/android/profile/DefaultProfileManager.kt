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
import org.supla.android.R
import org.supla.android.Preferences
import org.supla.android.db.DbHelper
import org.supla.android.db.AuthProfileItem
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.LocalProfileRepository

class DefaultProfileManager(private val ctx: Context): ProfileManager {

    private val repo: ProfileRepository

    init {
        repo = LocalProfileRepository(DbHelper.getInstance(ctx))
    }

    override val activeProfile: AuthProfileItem
        get() {
            var pid = Preferences(ctx).activeProfileId
            if(pid == 0L) {
                // There is no active profile in settings, so lets try
                // to get one from the repo.
                if(repo.allProfiles.isEmpty()) {
                    pid = createDefaultProfile()
                    Preferences(ctx).activeProfileId = pid
                }
                pid = repo.allProfiles.first().id
            }
            return repo.getProfile(pid)!!
        }

    override fun activateProfile(id: Long) {
        val prefs = Preferences(ctx)
        if(id != prefs.activeProfileId && repo.getProfile(id) != null) {
            prefs.activeProfileId = id
            // FIXME: invalidate connection and data sources
        }
    }

    private fun createDefaultProfile(): Long {
        val pname = ctx.getText(R.string.cfg_profile_name_default).toString()
        val pid = repo.createNamedProfile(pname)
        populateFromPrefs(pid)
        return pid
    }

    private fun populateFromPrefs(pid: Long) {
        val prof = repo.getProfile(pid)!!
        val prefs = Preferences(ctx)
        prof.emailAddr = if(prefs.email != "") prefs.email else null
        prof.serverAddr = if(prefs.serverAddress != "") prefs.serverAddress else null
        prof.accessID = if(prefs.accessID > 0) prefs.accessID else null
        prof.accessIDpwd = if(prefs.accessIDpwd != "") prefs.accessIDpwd else null
        repo.updateProfile(prof)
    }

}
