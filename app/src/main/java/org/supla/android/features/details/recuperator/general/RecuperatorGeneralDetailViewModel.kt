package org.supla.android.features.details.recuperator.general
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
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

@HiltViewModel
class RecuperatorGeneralDetailViewModel @Inject constructor(
  schedulers: SuplaSchedulers
) : BaseViewModel<RecuperatorGeneralDetailViewState, RecuperatorGeneralDetailViewEvent>(
  RecuperatorGeneralDetailViewState(),
  schedulers
),
  RecuperatorGeneralDetailViewScope {

  fun load() {
    updateState {
      it.copy(
        isOff = true,
        supplyOutsideTemperature = "22.7°",
        supplyInsideTemperature = "22.7°",
        exhaustOutsideTemperature = "22.7°",
        exhaustInsideTemperature = "22.7°",
        supplyPowerPercent = "0 %",
        exhaustPowerPercent = "0 %"
      )
    }
  }

  override fun onVentilationClick() {
  }

  override fun onEmptyHouseClick() {
  }

  override fun onOpenWindowClick() {
  }

  override fun onPowerClick() {
    updateState {
      if (it.mode == null) {
        it.copy(
          isOff = false,
          mode = WorkingMode.MANUAL
        )
      } else {
        it.copy(
          isOff = true,
          mode = null
        )
      }
    }
  }

  override fun onManualClick() {
    updateState {
      it.copy(
        isOff = false,
        mode = WorkingMode.MANUAL
      )
    }
  }

  override fun onProgramClick() {
    updateState {
      it.copy(
        isOff = false,
        mode = WorkingMode.PROGRAM
      )
    }
  }
}

sealed class RecuperatorGeneralDetailViewEvent : ViewEvent
