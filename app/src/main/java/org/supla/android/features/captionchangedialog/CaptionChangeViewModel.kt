package org.supla.android.features.captionchangedialog
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
import org.supla.android.R
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.captionchange.CaptionChangeUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.core.shared.infrastructure.LocalizedString
import javax.inject.Inject

@HiltViewModel
class CaptionChangeViewModel @Inject constructor(
  private val captionChangeUseCase: CaptionChangeUseCase,
  private val vibrationHelper: VibrationHelper,
  roomProfileRepository: RoomProfileRepository,
  suplaClientProvider: SuplaClientProvider,
  authorizeUseCase: AuthorizeUseCase,
  loginUseCase: LoginUseCase,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<CaptionChangeViewModelState, CaptionChangeViewEvent>(
  suplaClientProvider,
  roomProfileRepository,
  loginUseCase,
  authorizeUseCase,
  CaptionChangeViewModelState(),
  schedulers
),
  CaptionChangeDialogScope {

  fun showDialog(caption: String, remoteId: Int, profileId: Long) {
    vibrationHelper.vibrate()
    updateState {
      it.copy(
        viewState = CaptionChangeDialogState(
          remoteId = remoteId,
          profileId = profileId,
          subjectType = SubjectType.CHANNEL,
          caption = caption
        )
      )
    }
    showAuthorizationDialog(reason = CaptionChange)
  }

  override fun updateAuthorizationDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized(reason: AuthorizationReason) {
    closeAuthorizationDialog()
    if (reason == CaptionChange) {
      updateState { it.copy(viewState = it.viewState?.copy(authorized = true)) }
    }
  }

  override fun onCaptionChangeDismiss() {
    updateState { it.copy(viewState = null) }
    closeAuthorizationDialog()
  }

  override fun onStateChange(state: CaptionChangeDialogState) {
    updateState { it.copy(viewState = state) }
  }

  override fun onCaptionChangeConfirmed() {
    updateState { it.copy(viewState = it.viewState?.copy(loading = true)) }
    currentState().viewState?.let { state ->
      captionChangeUseCase(state.caption, state.subjectType.asCaptionChangeType, state.remoteId, state.profileId)
        .attachSilent()
        .subscribeBy(
          onComplete = { updateState { it.copy(viewState = null) } },
          onError = {
            updateState {
              it.copy(
                viewState = it.viewState?.copy(
                  loading = false,
                  error = LocalizedString.WithResource(R.string.caption_change_failed)
                )
              )
            }
          }
        )
    }
  }
}

data object CaptionChange : AuthorizationReason

sealed class CaptionChangeViewEvent : ViewEvent

data class CaptionChangeViewModelState(
  val viewState: CaptionChangeDialogState? = null,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState()

private val SubjectType.asCaptionChangeType: CaptionChangeUseCase.Type
  get() = when (this) {
    SubjectType.CHANNEL -> CaptionChangeUseCase.Type.CHANNEL
    SubjectType.GROUP -> CaptionChangeUseCase.Type.GROUP
    SubjectType.SCENE -> CaptionChangeUseCase.Type.SCENE
  }
