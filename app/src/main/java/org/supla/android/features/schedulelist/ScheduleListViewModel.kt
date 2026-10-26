package org.supla.android.features.schedulelist
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
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.await
import org.supla.android.R
import org.supla.android.core.networking.suplacloud.CloudApiTokenProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.remote.rest.CloudApiService
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.captionchangedialog.CaptionChangeDialogState
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.cloudapi.AuthenticateCloudApiUseCase
import org.supla.android.usecases.cloudapi.CloudApiAuthorizationException
import org.supla.android.usecases.schedule.ScheduleDtoToListItemMapper
import org.supla.android.usecases.schedule.ScheduleVo
import org.supla.core.shared.infrastructure.localizedString
import retrofit2.HttpException
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ScheduleListViewModel @Inject constructor(
  private val profileRepository: ProfileRepository,
  private val cloudApiServiceProvider: CloudApiService.Provider,
  private val cloudApiTokenProvider: CloudApiTokenProvider,
  private val authenticateCloudApiUseCase: AuthenticateCloudApiUseCase,
  private val scheduleDtoToListItemMapper: ScheduleDtoToListItemMapper,
  override val threading: SuplaThreading
) : BaseViewModel<ScheduleListViewState, ScheduleListViewEvent>(
  defaultState = ScheduleListViewState(),
  threading = threading,
  titleRes = R.string.schedules_list_title
),
  ScheduleListScope {

  private var profileId: Long? = null
  private var scheduleIdForCaptionChange: Int? = null
  private val schedulesReloadSubject = PublishSubject.create<Unit>()

  init {
    observeSchedulesReloads()
  }

  override fun onEnableChanged(remoteId: Int, enabled: Boolean) {
    updateScheduleEnabled(remoteId, enabled)
  }

  private fun updateScheduleEnabled(remoteId: Int, enabled: Boolean) {
    if (cloudApiTokenProvider.provide() == null) {
      showAuthorizationIfNeeded()
      return
    }

    viewModelScope.launch {
      updateState { it.copy(loading = true, loadingError = false) }
      try {
        val cloudApiService = threading.io { cloudApiServiceProvider.provide() }
        val schedule = threading.io { cloudApiService.getSchedule(remoteId) }
        if (schedule.enabled != enabled) {
          threading.io { cloudApiService.updateSchedule(remoteId, schedule.copy(enabled = enabled)) }
        }
        schedulesReloadSubject.onNext(Unit)
      } catch (error: Exception) {
        handleScheduleLoadingError(error)
      }
    }
  }

  override fun onTitleLongClick(schedule: ScheduleVo) {
    scheduleIdForCaptionChange = schedule.remoteId
    updateState {
      it.copy(
        captionChangeDialogState = CaptionChangeDialogState(
          caption = schedule.userCaption,
          label = localizedString(R.string.schedule_name),
          authorized = true
        )
      )
    }
  }

  override fun onCaptionChangeDismiss() {
    scheduleIdForCaptionChange = null
    updateState { it.copy(captionChangeDialogState = null) }
  }

  override fun onStateChange(state: CaptionChangeDialogState) {
    updateState { it.copy(captionChangeDialogState = state) }
  }

  override fun onCaptionChangeConfirmed() {
    val remoteId = scheduleIdForCaptionChange ?: return
    val caption = currentState().captionChangeDialogState?.caption ?: return

    if (cloudApiTokenProvider.provide() == null) {
      showAuthorizationIfNeeded()
      return
    }

    viewModelScope.launch {
      updateState {
        it.copy(captionChangeDialogState = it.captionChangeDialogState?.copy(loading = true, error = null))
      }
      try {
        val cloudApiService = threading.io { cloudApiServiceProvider.provide() }
        val schedule = threading.io { cloudApiService.getSchedule(remoteId) }
        threading.io { cloudApiService.updateSchedule(remoteId, schedule.copy(caption = caption)) }
        onCaptionChangeDismiss()
        schedulesReloadSubject.onNext(Unit)
      } catch (error: Exception) {
        if (error is HttpException && error.code() == 401) {
          updateState {
            it.copy(captionChangeDialogState = it.captionChangeDialogState?.copy(loading = false))
          }
          handleScheduleLoadingError(error)
        } else {
          Timber.e(error, "Changing schedule caption failed")
          updateState {
            it.copy(
              captionChangeDialogState = it.captionChangeDialogState?.copy(
                loading = false,
                error = localizedString(R.string.caption_change_failed)
              )
            )
          }
        }
      }
    }
  }

  override fun onStart() {
    schedulesReloadSubject.onNext(Unit)
  }

  override fun onViewCreated() {
    viewModelScope.launch {
      val loadedProfile = threading.io {
        runCatching { profileRepository.findActiveProfile().await() }.getOrNull()
      }

      if (loadedProfile == null) {
        sendEvent(ScheduleListViewEvent.Close)
        return@launch
      }

      profileId = loadedProfile.id
      updateState {
        it.copy(
          authorizationUserName = loadedProfile.email,
          authorizationIsCloudAccount = loadedProfile.isCloudAccount
        )
      }
      schedulesReloadSubject.onNext(Unit)
    }
  }

  override fun onAuthorizationDismiss() {
    dismissAuthorizationDialog()
  }

  override fun onAuthorizationCancel() {
    dismissAuthorizationDialog()
  }

  override fun onTryAgain() {
    schedulesReloadSubject.onNext(Unit)
  }

  override fun onRefresh() {
    schedulesReloadSubject.onNext(Unit)
  }

  override fun onAuthorize(userName: String, password: String) {
    viewModelScope.launch {
      updateAuthorizationDialogState { it?.copy(processing = true, error = null) }
      try {
        threading.io { authenticateCloudApiUseCase(userName, password) }
        updateState { it.copy(loading = true, loadingError = false) }
        closeAuthorizationDialog()
        schedulesReloadSubject.onNext(Unit)
      } catch (_: CloudApiAuthorizationException) {
        updateAuthorizationDialogState {
          it?.copy(processing = false, error = localizedString(R.string.incorrect_email_or_password))
        }
      } catch (error: Exception) {
        Timber.e(error, "Cloud API authentication failed")
        updateAuthorizationDialogState {
          it?.copy(processing = false, error = localizedString(R.string.status_unknown_err))
        }
      }
    }
  }

  override fun onStateChange(state: AuthorizationDialogState) {
    updateAuthorizationDialogState { state }
  }

  private fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { state -> state.copy(authorizationDialogState = updater(state.authorizationDialogState)) }
  }

  private fun closeAuthorizationDialog() {
    updateAuthorizationDialogState { null }
  }

  private fun dismissAuthorizationDialog() {
    updateState {
      it.copy(
        authorizationDialogState = null,
        loading = it.items != null && it.loading
      )
    }
  }

  private suspend fun loadSchedules() {
    val profileId = profileId ?: return

    updateState { it.copy(loading = true, loadingError = false) }
    try {
      val schedules = threading.io { cloudApiServiceProvider.provide().getSchedules() }
      val items = schedules.map { scheduleDtoToListItemMapper(it, profileId) }
      updateState { it.copy(items = items, loading = false) }
    } catch (error: Exception) {
      handleScheduleLoadingError(error)
    }
  }

  private fun observeSchedulesReloads() {
    schedulesReloadSubject
      .debounce(250, TimeUnit.MILLISECONDS, threading.schedulers.computation)
      .subscribeBy(onNext = { loadSchedulesOrShowAuthorization() })
      .disposeBySelf()
  }

  private suspend fun handleScheduleLoadingError(error: Exception) {
    Timber.e(error, "Loading schedules failed")
    if (error is HttpException && error.code() == 401) {
      threading.io { cloudApiTokenProvider.clean() }
      updateState { it.copy(loading = false, loadingError = false) }
      showAuthorizationDialog()
    } else {
      updateState { it.copy(loading = false, loadingError = true) }
    }
  }

  private fun showAuthorizationDialog() {
    val state = currentState()
    val userName = state.authorizationUserName ?: return
    val isCloudAccount = state.authorizationIsCloudAccount ?: return
    showAuthorizationDialog(userName, isCloudAccount)
  }

  private fun loadSchedulesOrShowAuthorization() {
    if (cloudApiTokenProvider.provide() == null) {
      showAuthorizationIfNeeded()
    } else if (profileId != null) {
      viewModelScope.launch { loadSchedules() }
    }
  }

  private fun showAuthorizationIfNeeded() {
    if (currentState().authorizationDialogState == null) {
      showAuthorizationDialog()
    }
  }

  private fun showAuthorizationDialog(userName: String, isCloudAccount: Boolean) {
    updateState { state ->
      if (state.authorizationDialogState != null) {
        state
      } else {
        state.copy(
          authorizationDialogState = AuthorizationDialogState(
            userName = userName,
            isCloudAccount = isCloudAccount,
            userNameEnabled = false
          )
        )
      }
    }
  }
}

sealed class ScheduleListViewEvent : ViewEvent {
  data object Close : ScheduleListViewEvent()
}
