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

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.notifications.NotificationsHelper
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.core.storage.EncryptedPreferences
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
  private val applicationPreferences: ApplicationPreferences,
  private val encryptedPreferences: EncryptedPreferences,
  private val notificationsHelper: NotificationsHelper,
  @ApplicationContext private val context: Context,
  suplaSchedulers: SuplaSchedulers
) : BaseViewModel<DeveloperInfoViewModelState, DeveloperInfoViewEvent>(DeveloperInfoViewModelState(), suplaSchedulers),
  DeveloperInfoScope {

  override fun onViewCreated() {
    super.onViewCreated()

    updateState {
      it.copy(
        state = it.state.copy(
          developerOptions = encryptedPreferences.devModeActive,
          rotationEnabled = applicationPreferences.rotationEnabled
        )
      )
    }

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

  override fun setDeveloperOptionEnabled(enabled: Boolean) {
    encryptedPreferences.devModeActive = enabled
    updateState { it.copy(state = it.state.copy(developerOptions = enabled)) }
  }

  override fun setRotationEnabled(enabled: Boolean) {
    applicationPreferences.rotationEnabled = enabled
    updateState { it.copy(state = it.state.copy(rotationEnabled = enabled)) }
    sendEvent(DeveloperInfoViewEvent.UpdateOrientationLock)
  }

  override fun sendTestNotification() {
    notificationsHelper.showNotification(context, "Test notification title", "Test notification message", "Test profile")
  }
}

sealed class DeveloperInfoViewEvent : ViewEvent {
  data object UpdateOrientationLock : DeveloperInfoViewEvent()
}

data class DeveloperInfoViewModelState(
  val state: DeveloperInfoViewState = DeveloperInfoViewState()
) : ViewState()
