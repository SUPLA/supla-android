package org.supla.android.features.developerinfo
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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.developerinfo.LoadDatabaseDetailsUseCase
import org.supla.android.usecases.developerinfo.TableDetail
import org.supla.android.usecases.developerinfo.TableDetailType
import javax.inject.Inject

@HiltViewModel
class DeveloperInfoViewModel @Inject constructor(
  private val loadDatabaseDetailsUseCase: LoadDatabaseDetailsUseCase,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<DeveloperInfoViewModelState, DeveloperInfoViewEvent>(DeveloperInfoViewModelState(), suplaSchedulers) {

  override fun onViewCreated() {
    super.onViewCreated()

    loadDatabaseDetailsUseCase(TableDetailType.SUPLA)
      .attach()
      .subscribeBy(
        onNext = this::handleSuplaData
      )
      .disposeBySelf()

    loadDatabaseDetailsUseCase(TableDetailType.MEASUREMENTS)
      .attach()
      .subscribeBy(
        onNext = this::handleMeasurementsData
      )
      .disposeBySelf()
  }

  private fun handleSuplaData(details: List<TableDetail>) {
    updateState {
      it.copy(state = it.state.copy(suplaTableDetails = details))
    }
  }

  private fun handleMeasurementsData(details: List<TableDetail>) {
    updateState {
      it.copy(state = it.state.copy(measurementTableDetails = details))
    }
  }
}

sealed class DeveloperInfoViewEvent : ViewEvent

data class DeveloperInfoViewModelState(
  val state: DeveloperInfoViewState = DeveloperInfoViewState()
) : ViewState()
