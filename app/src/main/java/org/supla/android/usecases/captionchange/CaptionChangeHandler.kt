package org.supla.android.usecases.captionchange
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

import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.R
import org.supla.android.core.ui.ViewEvent
import org.supla.android.lib.actions.SubjectType
import org.supla.android.ui.dialogs.CaptionChangeDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.core.shared.infrastructure.LocalizedString

interface CaptionChangeHandler<S : AuthorizationModelState, E : ViewEvent> {

  val captionChangeDialogState: CaptionChangeDialogState?
  val captionChangeUseCase: CaptionChangeUseCase

  fun updateCaptionChangeDialogState(updater: (CaptionChangeDialogState?) -> CaptionChangeDialogState?)

  fun showAuthorizationDialog()

  fun closeAuthorizationDialog()

  fun onCaptionChange()

  fun changeChannelCaption(caption: String, remoteId: Int, profileId: Long) {
    updateCaptionChangeDialogState {
      CaptionChangeDialogState(
        remoteId = remoteId,
        profileId = profileId,
        subjectType = SubjectType.CHANNEL,
        caption = caption
      )
    }
    showAuthorizationDialog()
  }

  fun closeCaptionChangeDialog() {
    updateCaptionChangeDialogState { null }
  }

  fun updateCaptionChangeDialogState(state: CaptionChangeDialogState) {
    updateCaptionChangeDialogState { state }
  }

  context(BaseAuthorizationViewModel<S, E>)
  fun onChannelCaptionChange() {
    captionChangeDialogState?.let { state ->
      updateCaptionChangeDialogState { it?.copy(loading = true) }
      captionChangeUseCase(state.caption, CaptionChangeUseCase.Type.CHANNEL, state.remoteId, state.profileId)
        .attachSilent()
        .subscribeBy(
          onComplete = { closeCaptionChangeDialog() },
          onError = {
            updateCaptionChangeDialogState {
              it?.copy(loading = false, error = LocalizedString.WithResource(R.string.caption_change_failed))
            }
          }
        )
        .disposeBySelf()
    }
  }

  fun onCaptionChangeNotAuthorized() {
    closeCaptionChangeDialog()
    closeAuthorizationDialog()
  }
}
