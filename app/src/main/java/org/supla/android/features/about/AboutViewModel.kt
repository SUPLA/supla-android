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
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.formatting.DateFormatter
import org.supla.android.tools.SuplaSchedulers
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
  private val dateFormatter: DateFormatter,
  private val encryptedPreferences: EncryptedPreferences,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<AboutViewModelState, AboutViewEvent>(AboutViewModelState(), suplaSchedulers) {

  private var versionClickCount: Int = 0

  override fun onViewCreated() {
    updateState {
      val date = dateFormatter.getFullDateString(Date(BuildConfig.BUILD_TIME)) ?: ""
      val timeString = "$date (${BuildConfig.VERSION_CODE})"
      it.copy(viewState = it.viewState.copy(buildTime = timeString))
    }
  }

  fun onVersionClick() {
    if (encryptedPreferences.devModeActive) {
      sendEvent(AboutViewEvent.NavigateToDeveloperInfoScreen)
    } else if (versionClickCount < 4) {
      versionClickCount++
    } else {
      sendEvent(AboutViewEvent.ShowDeveloperModeActivated)
      encryptedPreferences.devModeActive = true
    }
  }
}

sealed class AboutViewEvent : ViewEvent {
  data object NavigateToDeveloperInfoScreen : AboutViewEvent()
  data object ShowDeveloperModeActivated : AboutViewEvent()
}

data class AboutViewModelState(
  val viewState: AboutViewState = AboutViewState()
) : ViewState()
