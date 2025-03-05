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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.R
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.dialogs.AuthorizationReason
import org.supla.android.ui.dialogs.CaptionChangeDialogScope
import org.supla.android.ui.dialogs.CaptionChangeDialogState
import org.supla.core.shared.infrastructure.LocalizedString

data object CaptionChange : AuthorizationReason

interface CaptionChangeHandler : CaptionChangeDialogScope {

  val vibrationHelper: VibrationHelper
  val captionChangeDialogState: CaptionChangeDialogState?
  val captionChangeUseCase: CaptionChangeUseCase

  fun subscribeSilent(completable: Completable, onComplete: () -> Unit = {}, onError: (Throwable) -> Unit = {})

  fun updateCaptionChangeDialogState(updater: (CaptionChangeDialogState?) -> CaptionChangeDialogState?)

  fun showAuthorizationDialog(reason: AuthorizationReason)

  fun closeAuthorizationDialog()

  override fun onCaptionChangeDismiss() {
    updateCaptionChangeDialogState { null }
    closeAuthorizationDialog()
  }

  override fun onStateChange(state: CaptionChangeDialogState) {
    updateCaptionChangeDialogState { state }
  }

  fun changeChannelCaption(caption: String, remoteId: Int, profileId: Long) {
    vibrationHelper.vibrate()
    updateCaptionChangeDialogState {
      CaptionChangeDialogState(
        remoteId = remoteId,
        profileId = profileId,
        subjectType = SubjectType.CHANNEL,
        caption = caption
      )
    }
    showAuthorizationDialog(reason = CaptionChange)
  }

  override fun onCaptionChangeConfirmed() {
    captionChangeDialogState?.let { state ->
      updateCaptionChangeDialogState { it?.copy(loading = true) }
      subscribeSilent(
        completable = captionChangeUseCase(state.caption, state.subjectType.asCaptionChangeType, state.remoteId, state.profileId),
        onComplete = { updateCaptionChangeDialogState { null } },
        onError = {
          updateCaptionChangeDialogState {
            it?.copy(loading = false, error = LocalizedString.WithResource(R.string.caption_change_failed))
          }
        }
      )
    }
  }
}

val SubjectType.asCaptionChangeType: CaptionChangeUseCase.Type
  get() = when (this) {
    SubjectType.CHANNEL -> CaptionChangeUseCase.Type.CHANNEL
    SubjectType.GROUP -> CaptionChangeUseCase.Type.GROUP
    SubjectType.SCENE -> CaptionChangeUseCase.Type.SCENE
  }
