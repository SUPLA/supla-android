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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CfgViewModel @Inject constructor(
  private val configurationProvider: AppConfigurationProvider
) : ViewModel() {

  val cfgData: CfgData = configurationProvider.getConfiguration()
  private val _isDirty = MutableLiveData(false)

  /**
  indicates that configuration is changed and should be saved.
   */
  val isDirty: LiveData<Boolean> = _isDirty

  val saveEnabled = MutableLiveData(true)

  fun setTemperatureUnit(unit: TemperatureUnit) {
    if (cfgData.temperatureUnit.value != unit) {
      cfgData.temperatureUnit.value = unit
      setConfigDirty()
    }
  }

  fun setButtonAutohide(autohideEnabled: Boolean) {
    if (cfgData.buttonAutohide.value != autohideEnabled) {
      cfgData.buttonAutohide.value = autohideEnabled
      setConfigDirty()
    }
  }

  fun setChannelHeight(height: ChannelHeight) {
    if (cfgData.channelHeight.value != height) {
      cfgData.channelHeight.value = height
      setConfigDirty()
    }
  }

  fun setShowChannelInfo(show: Boolean) {
    if (cfgData.showChannelInfo.value != show) {
      cfgData.showChannelInfo.value = show
      setConfigDirty()
    }
  }

  fun setShowOpeningPercent(show: Boolean) {
    if (cfgData.showOpeningPercent.value != show) {
      cfgData.showOpeningPercent.value = show
      setConfigDirty()
    }
  }

  fun saveConfig() {
    if (isDirty.value == true) {
      configurationProvider.storeConfiguration(cfgData)
    }
  }

  fun onSaveConfig() {
    saveEnabled.value = false
    saveConfig()
  }

  /**
  sets config dirty flag, to indicate that configuration has
  been updated.
   */
  private fun setConfigDirty() {
    _isDirty.value = true
  }
}
