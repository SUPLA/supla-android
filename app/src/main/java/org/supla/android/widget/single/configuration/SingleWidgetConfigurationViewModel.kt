package org.supla.android.widget.single.configuration
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.Preferences
import org.supla.android.data.ValuesFormatter
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.db.ChannelBase
import org.supla.android.db.DbItem
import org.supla.android.db.Location
import org.supla.android.di.CoroutineDispatchers
import org.supla.android.extensions.isDoorLock
import org.supla.android.extensions.isGateController
import org.supla.android.extensions.isGpm
import org.supla.android.extensions.isRollerShutter
import org.supla.android.extensions.isSwitch
import org.supla.android.extensions.isThermometer
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.channel.LoadChannelConfigUseCase
import org.supla.android.widget.WidgetPreferences
import org.supla.android.widget.shared.configuration.*
import javax.inject.Inject

@HiltViewModel
class SingleWidgetConfigurationViewModel @Inject constructor(
  preferences: Preferences,
  widgetPreferences: WidgetPreferences,
  profileManager: ProfileManager,
  channelRepository: ChannelRepository,
  sceneRepository: SceneRepository,
  dispatchers: CoroutineDispatchers,
  singleCallProvider: SingleCall.Provider,
  valuesFormatter: ValuesFormatter,
  loadChannelConfigUseCase: LoadChannelConfigUseCase
) : WidgetConfigurationViewModelBase(
  preferences,
  widgetPreferences,
  profileManager,
  channelRepository,
  sceneRepository,
  dispatchers,
  singleCallProvider,
  valuesFormatter,
  loadChannelConfigUseCase
) {

  private val _actionsList = MutableLiveData<List<WidgetAction>>()
  val actionsList: LiveData<List<WidgetAction>> = _actionsList

  override fun filterItems(channelBase: DbItem): Boolean {
    return when (channelBase) {
      is Location -> true
      is ChannelBase -> channelBase.isGateController() ||
        channelBase.isDoorLock() ||
        channelBase.isSwitch() ||
        channelBase.isRollerShutter() ||
        channelBase.isThermometer() ||
        channelBase.isGpm()
      else -> false
    }
  }

  override fun valueWithUnit(): Boolean = false

  override fun changeItem(channel: DbItem?) {
    super.changeItem(channel)
    updateActions()
  }

  private fun updateActions() {
    val item = selectedItem
    when {
      item is Scene -> {
        _actionsList.postValue(
          listOf(
            WidgetAction.EXECUTE,
            WidgetAction.INTERRUPT_AND_EXECUTE,
            WidgetAction.INTERRUPT
          )
        )
      }
      item is ChannelBase && item.isSwitch() -> {
        _actionsList.postValue(
          listOf(
            WidgetAction.TURN_ON,
            WidgetAction.TURN_OFF,
            WidgetAction.TOGGLE
          )
        )
      }
      item is ChannelBase && item.isRollerShutter() -> {
        _actionsList.postValue(
          listOf(
            WidgetAction.MOVE_UP,
            WidgetAction.MOVE_DOWN
          )
        )
      }
      item is ChannelBase && item.isGateController() -> {
        _actionsList.postValue(
          listOf(
            WidgetAction.OPEN,
            WidgetAction.CLOSE,
            WidgetAction.OPEN_CLOSE
          )
        )
      }
      else -> _actionsList.postValue(listOf())
    }
  }
}
