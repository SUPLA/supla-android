package org.supla.android.widget.onoff.configuration
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

import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.ChannelBase
import org.supla.android.db.DbItem
import org.supla.android.db.Location
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.extensions.isRollerShutter
import org.supla.android.extensions.isSwitch
import org.supla.android.extensions.isThermometer
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileManager
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.configuration.WidgetConfigurationViewModelBase
import javax.inject.Inject

@HiltViewModel
class OnOffWidgetConfigurationViewModel @Inject constructor(
  preferences: Preferences,
  widgetPreferences: WidgetPreferences,
  profileManager: ProfileManager,
  channelRepository: ChannelRepository,
  sceneRepository: SceneRepository,
  dispatchers: CoroutineDispatchers,
  singleCallProvider: SingleCall.Provider,
  valuesFormatter: ValuesFormatter
) : WidgetConfigurationViewModelBase(
  preferences,
  widgetPreferences,
  profileManager,
  channelRepository,
  sceneRepository,
  dispatchers,
  singleCallProvider,
  valuesFormatter
) {
  override fun filterItems(channelBase: DbItem): Boolean {
    return when (channelBase) {
      is Location -> true
      is ChannelBase -> channelBase.isSwitch() || channelBase.isRollerShutter() ||
        channelBase.isThermometer()
      else -> false
    }
  }

  override fun temperatureWithUnit(): Boolean = true
}
