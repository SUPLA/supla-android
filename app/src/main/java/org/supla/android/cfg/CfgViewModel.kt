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

import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.fragment.findNavController
import org.supla.android.R
import org.supla.android.profile.ProfileManager

class CfgViewModel(private val repository: CfgRepository,
                   private val profileManager: ProfileManager,
                   private val navCoordinator: NavCoordinator): ViewModel() {

    val cfgData: CfgData = repository.getCfg()
    private val _isDirty = MutableLiveData<Boolean>(false)
    /**
     indicates that configuration is changed and should be saved.
     */
    val isDirty: LiveData<Boolean> = _isDirty



    private val _didSaveConfig = MutableLiveData<Boolean>(false)
    val didSaveConfig: LiveData<Boolean> get() = _didSaveConfig
    val saveEnabled = MutableLiveData<Boolean>(true)


    val nextAction = MutableLiveData<NavigationFlow?>()


    fun setTemperatureUnit(unit: TemperatureUnit) {
        if(cfgData.temperatureUnit.value != unit) {
            cfgData.temperatureUnit.value = unit
            setConfigDirty()
        }
    }

    fun setButtonAutohide(autohideEnabled: Boolean) {
        if(cfgData.buttonAutohide.value != autohideEnabled) {
            cfgData.buttonAutohide.value = autohideEnabled
            setConfigDirty()
        }
    }

    fun setChannelHeight(height: ChannelHeight) {
        if(cfgData.channelHeight.value != height) {
            cfgData.channelHeight.value = height
            setConfigDirty()
        }
    }

    fun setShowChannelInfo(show: Boolean) {
        if(cfgData.showChannelInfo.value != show) {
            cfgData.showChannelInfo.value = show
            setConfigDirty()
        }
    }

    fun setShowOpeningPercent(show: Boolean) {
        if(cfgData.showOpeningPercent.value != show) {
            cfgData.showOpeningPercent.value = show
            setConfigDirty()
        }   
    }


    fun openProfiles() {
//        nextAction.value = NavigationFlow.OPEN_PROFILES
    }

    fun openLocationReordering() {
        navCoordinator.navigate(NavigationFlow.LOCATION_REORDERING)
  //      findNavController().navigate(R.id.cfgLocationReordering)
    }

    fun saveConfig() {
        if(isDirty.value == true) {
            repository.storeCfg(cfgData)
            _didSaveConfig.value = true
        }

    }

    fun onSaveConfig() {
        saveEnabled.value = false
        saveConfig()
//        nextAction.value = NavigationFlow.MAIN
    }

    /**
     sets config dirty flag, to indicate that configuration has
     been updated.
     */
    private fun setConfigDirty() {
        _isDirty.value = true
    }

}
