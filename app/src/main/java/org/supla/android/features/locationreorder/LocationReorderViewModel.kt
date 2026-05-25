package org.supla.android.features.locationreorder
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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import kotlinx.coroutines.withContext
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.source.LocationRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.extensions.subscribeBy
import org.supla.android.tools.SuplaSchedulers
import javax.inject.Inject

data class LocationReorderViewState(
  val locations: List<LocationEntity> = emptyList()
) : ViewState()

interface LocationReorderScope {
  fun onMove(from: Int, to: Int)
  fun onMoveFinished()
}

@HiltViewModel
class LocationReorderViewModel @Inject constructor(
  private val locationRepository: LocationRepository,
  schedulers: SuplaSchedulers
) : BaseViewModel<LocationReorderViewState, ViewEvent>(LocationReorderViewState(), schedulers), LocationReorderScope {

  override fun onViewCreated() {
    viewModelScope.launch {
      val locations = schedulers.io { locationRepository.getAllLocations() }
      updateState { it.copy(locations = locations) }
    }
  }

  override fun onMove(from: Int, to: Int) {
    if (from == to) {
      return
    }

    updateState {
      it.copy(
        locations = it.locations.toMutableList().apply {
          add(to, removeAt(from))
        }
      )
    }
  }

  override fun onMoveFinished() {
    viewModelScope.launch {
      val state = currentState()

      state.locations.forEachIndexed { index, location ->
        locationRepository.updateLocation(location.copy(sortOrder = index)).await()
      }
    }
  }
}
