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

import org.supla.android.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigurationProvider @Inject constructor(private val preferences: Preferences) {

  private var configuration = loadConfig()

  fun getConfiguration(): CfgData = configuration

  fun storeConfiguration(cfg: CfgData) {
    preferences.temperatureUnit = cfg.temperatureUnit.value
    preferences.isButtonAutohide = cfg.buttonAutohide.value ?: true
    preferences.channelHeight = cfg.channelHeight.value?.percent ?: 100
    preferences.isShowChannelInfo = cfg.showChannelInfo.value ?: true
    preferences.isShowOpeningPercent = cfg.showOpeningPercent.value ?: false

    configuration = cfg
  }

  private fun loadConfig(): CfgData {
    val channelHeight = ChannelHeight.values()
      .firstOrNull { it.percent == preferences.channelHeight } ?: ChannelHeight.HEIGHT_100
    return CfgData(
      preferences.temperatureUnit,
      preferences.isButtonAutohide,
      channelHeight,
      preferences.isShowChannelInfo,
      preferences.isShowOpeningPercent
    )
  }

}
