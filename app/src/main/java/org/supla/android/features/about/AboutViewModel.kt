package org.supla.android.features.about
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
import org.supla.android.BuildConfig
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.ValuesFormatter
import org.supla.android.tools.SuplaSchedulers
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
  private val valuesFormatter: ValuesFormatter,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<AboutViewModelState, AboutViewEvent>(AboutViewModelState(), suplaSchedulers) {
  override fun onViewCreated() {
    updateState {
      val date = valuesFormatter.getFullDateString(Date(BuildConfig.BUILD_TIME)) ?: ""
      val timeString = "$date (${BuildConfig.VERSION_CODE})"
      it.copy(viewState = it.viewState.copy(buildTime = timeString))
    }
  }
}

sealed class AboutViewEvent : ViewEvent

data class AboutViewModelState(
  val viewState: AboutViewState = AboutViewState()
) : ViewState()
