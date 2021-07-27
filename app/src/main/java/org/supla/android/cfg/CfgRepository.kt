package org.supla.android.cfg
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
import org.supla.android.Preferences
import org.supla.android.db.DbHelper

interface CfgRepository {
    fun getCfg(): CfgData
    fun storeCfg(cfg: CfgData)
}

class PrefsCfgRepositoryImpl(ctx: Context): CfgRepository {
    private val prefs: Preferences
    private val helper: DbHelper

    init {
        prefs = Preferences(ctx)
	helper = DbHelper.getInstance(ctx)
    }

    override fun getCfg(): CfgData {
        return CfgData(prefs.serverAddress, prefs.accessID, prefs.accessIDpwd,
                       prefs.email, prefs.temperatureUnit)
    }


    override fun storeCfg(cfg: CfgData) {
        helper.deleteUserIcons() // TODO: I'm not sure if this is the right place for this yet.
        prefs.serverAddress = cfg.serverAddr
        prefs.accessID = cfg.accessID
        prefs.accessIDpwd = cfg.accessIDpwd
        prefs.email = cfg.email
        prefs.temperatureUnit = cfg.temperatureUnit
        prefs.setPreferedProtocolVersion()
    }

}
