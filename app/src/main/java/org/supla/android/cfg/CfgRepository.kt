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
import org.supla.android.profile.*

interface CfgRepository {
    fun getCfg(): CfgData
    fun storeCfg(cfg: CfgData)
}

class PrefsCfgRepositoryImpl(ctx: Context): CfgRepository {
    private val prefs: Preferences
    private val helper: DbHelper
    private val context: Context

    init {
        prefs = Preferences(ctx)
	      helper = DbHelper.getInstance(ctx)
        context = ctx
    }

    override fun getCfg(): CfgData {
        return CfgData(prefs.temperatureUnit,
                       prefs.isButtonAutohide,
                       ChannelHeight.values().firstOrNull { it.percent == prefs.channelHeight } ?: ChannelHeight.HEIGHT_100,
                       prefs.isShowChannelInfo)
    }


    override fun storeCfg(cfg: CfgData) {
        prefs.temperatureUnit = cfg.temperatureUnit.value
        prefs.isButtonAutohide = cfg.buttonAutohide.value ?: true
        prefs.channelHeight = cfg.channelHeight.value?.percent ?: 100
        prefs.isShowChannelInfo = cfg.showChannelInfo.value ?: true
    }

}
