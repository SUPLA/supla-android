package org.supla.android.events
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

import org.supla.android.data.source.remote.ConfigResult
import org.supla.android.data.source.remote.SuplaChannelConfig
import org.supla.core.shared.extensions.guardLet
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelConfigEventsManager @Inject constructor() : BaseConfigEventsManager<ChannelConfigEventsManager.ConfigEvent>() {

  fun emitConfig(result: ConfigResult, config: SuplaChannelConfig?) {
    val (remoteId) = guardLet(config?.remoteId) {
      Timber.e("Got result `$result` without config `$config`")
      return
    }
    super.emitConfig(remoteId, ConfigEvent(result, config))
  }

  data class ConfigEvent(
    val result: ConfigResult,
    val config: SuplaChannelConfig?
  )
}
