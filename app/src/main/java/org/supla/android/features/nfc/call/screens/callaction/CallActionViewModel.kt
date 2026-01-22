package org.supla.android.features.nfc.call.screens.callaction
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

import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.nfc.tagUuid
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ModelViewState
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.SuplaSchedulers
import timber.log.Timber
import javax.inject.Inject

private const val STEP_MIN_TIME_MS = 200
private const val SUCCESS_DELAY_MS: Long = 2000

@HiltViewModel
class CallActionViewModel @Inject constructor(
  private val singleCallProvider: SingleCall.Provider,
  private val nfcTagRepository: NfcTagRepository,
  private val dateProvider: DateProvider,
  private val schedulers: SuplaSchedulers
) : BaseViewModel<CallActionViewModelState, CallActionViewEvent>(CallActionViewModelState(), schedulers), CallActionScreenScope {
  override fun close() {
    sendEvent(CallActionViewEvent.Close)
  }

  fun onLaunch(url: String?) {
    if (url == null) {
      Timber.d("Url not found!")
      setErrorState(TagProcessingStep.FailureType.ILLEGAL_INTENT)
      return
    }
    val tagId = resolveUrlToId(url)
    if (tagId == null) {
      Timber.d("Tag id not found!")
      setErrorState(TagProcessingStep.FailureType.UNKNOWN_URL)
      return
    }

    viewModelScope.launch {
      var currentTime = dateProvider.currentTimestamp()
      val tag = schedulers.io { nfcTagRepository.findByUuid(tagId) }

      if (tag == null) {
        setErrorState(TagProcessingStep.FailureType.TAG_NOT_FOUND)
        return@launch
      }

      val configuration = tag.configuration
      if (configuration == null) {
        setErrorState(TagProcessingStep.FailureType.TAG_NOT_CONFIGURED)
        return@launch
      }

      delayIfNeeded(currentTime)
      currentTime = dateProvider.currentTimestamp()
      setState(TagProcessingStep.ExecutingAction)

      val singleCall = singleCallProvider.provide(configuration.profileId)

      val success =
        schedulers.io {
          try {
            singleCall.executeAction(configuration.actionParameters)
            true
          } catch (ex: Exception) {
            Timber.d(ex, "Could not execute action")
            false
          }
        }

      delayIfNeeded(currentTime)

      if (success) {
        setState(TagProcessingStep.Success)
        delay(SUCCESS_DELAY_MS)
        sendEvent(CallActionViewEvent.Close)
      } else {
        setErrorState(TagProcessingStep.FailureType.ACTION_FAILED)
      }
    }
  }

  private fun resolveUrlToId(url: String): String? =
    try {
      url.toUri().tagUuid
    } catch (ex: Exception) {
      Timber.d(ex, "Could not parse url $url")
      null
    }

  private fun setErrorState(type: TagProcessingStep.FailureType) =
    setState(TagProcessingStep.Failure(type))

  private fun setState(step: TagProcessingStep) =
    updateState {
      it.copy(
        screenState = it.screenState.push(step)
      )
    }

  private suspend fun delayIfNeeded(currentTime: Long) {
    val stepTime = dateProvider.currentTimestamp() - currentTime
    if (stepTime < STEP_MIN_TIME_MS) {
      delay(STEP_MIN_TIME_MS - stepTime)
    }
  }
}

private val NfcTagEntity.Configuration.actionParameters: ActionParameters
  get() = ActionParameters(actionId, subjectType, subjectId)

sealed class CallActionViewEvent : ViewEvent {
  data object Close : CallActionViewEvent()
}

data class CallActionViewModelState(
  override val screenState: CallActionScreenState = CallActionScreenState()
) : ModelViewState<CallActionScreenState>()
