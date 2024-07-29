package org.supla.android.features.details.electricitymeterdetail.settings
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
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class ElectricityMeterSettingsViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<ElectricityMeterSettingsViewModelState, ElectricityMeterSettingsViewEvent>(
  ElectricityMeterSettingsViewModelState(),
  schedulers
)

sealed class ElectricityMeterSettingsViewEvent : ViewEvent

data class ElectricityMeterSettingsViewModelState(
  val any: Boolean = false
) : ViewState()
