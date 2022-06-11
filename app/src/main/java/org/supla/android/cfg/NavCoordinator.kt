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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NavCoordinator: ViewModel() {

    var wantsBack = false

    private val _navAction: MutableLiveData<NavigationFlow?> = MutableLiveData(null)
    val navAction: LiveData<NavigationFlow?> = _navAction

    fun navigate(dir: NavigationFlow) {
        _navAction.value = dir
    }

    fun returnFromAuth(didChangeSettings: Boolean) {
        if(wantsBack) {
            wantsBack = false
            _navAction.value = NavigationFlow.BACK
        } else {
            _navAction.value = if(didChangeSettings) NavigationFlow.STATUS else NavigationFlow.MAIN
        }
    }
}
enum class NavigationFlow { CREATE_ACCOUNT, STATUS, MAIN, 
                            BASIC_MODE_ALERT,
                            OPEN_PROFILES, LOCATION_REORDERING, BACK }

