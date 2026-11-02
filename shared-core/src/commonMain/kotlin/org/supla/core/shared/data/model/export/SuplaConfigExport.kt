package org.supla.core.shared.data.model.export
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

import kotlinx.serialization.Serializable
import org.supla.core.shared.data.model.thermometer.TemperatureUnit

@Serializable
data class SuplaConfigExport(
  val version: Int = 1,
  val settings: Settings = Settings(),
  val profiles: List<Profile> = emptyList()
) {

  @Serializable
  data class Settings(
    val channelHeight: Int = 100,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val temperaturePrecision: Int = 1,
    val buttonAutohide: Boolean = true,
    val showChannelInfo: Boolean = true,
    val showBottomLabel: Boolean = true,
    val showBottomMenu: Boolean = true,
    val showOpeningPercent: Boolean = false,
    val hideUnavailableChannels: Boolean = false,
    val nightMode: NightModeSetting = NightModeSetting.UNSET,
    val batteryWarningLevel: Int = 10
  )

  @Serializable
  data class Profile(
    val name: String = "",
    val email: String = "",
    val serverForAccessId: String = "",
    val serverForEmail: String = "",
    val serverAutoDetect: Boolean = false,
    val emailAuth: Boolean = false,
    val accessId: Int = 0,
    val preferredProtocolVersion: Int = 0,
    val active: Boolean = false,
    val advancedMode: Boolean = false,
    val position: Int = 0
  )
}
