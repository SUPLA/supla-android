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
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewModelState
import org.supla.android.data.source.NfcCallRepository
import org.supla.android.data.source.NfcTagRepository
import org.supla.android.data.source.local.entity.NfcCallResult
import org.supla.android.data.source.local.entity.NfcTagEntity
import org.supla.android.data.source.local.entity.complex.NfcTagDataEntity
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.SuplaSchedulers
import org.supla.core.shared.usecase.GetCaptionUseCase
import timber.log.Timber
import javax.inject.Inject

private const val STEP_MIN_TIME_MS = 200
private const val SUCCESS_DELAY_MS: Long = 2000

@HiltViewModel
class CallActionViewModel @Inject constructor(
  private val singleCallProvider: SingleCall.Provider,
  private val getCaptionUseCase: GetCaptionUseCase,
  private val nfcCallRepository: NfcCallRepository,
  private val nfcTagRepository: NfcTagRepository,
  private val dateProvider: DateProvider,
  private val schedulers: SuplaSchedulers
) : BaseViewModel<CallActionViewModelState, CallActionViewEvent>(CallActionViewModelState(), schedulers), CallActionScreenScope {
  override fun close() {
    sendEvent(CallActionViewEvent.Close)
  }

  override fun addNewTag(uuid: String) {
    sendEvent(CallActionViewEvent.SaveNewNfcTag(uuid, currentState().readOnly))
  }

  override fun configureTag(id: Long) {
    sendEvent(CallActionViewEvent.EditMissingAction(id))
  }

  fun onLaunchWithUrl(url: String?, readOnly: Boolean) {
    updateState { it.copy(readOnly = readOnly) }

    if (url == null) {
      Timber.d("Url not found!")
      setErrorState(TagProcessingStep.FailureType.IllegalIntent)
      return
    }
    val tagId = resolveUrlToId(url)
    if (tagId == null) {
      Timber.d("Tag id not found!")
      setErrorState(TagProcessingStep.FailureType.UnknownUrl)
      return
    }

    viewModelScope.launch {
      performAction(tagId)
    }
  }

  fun onLaunchWithId(tagId: String?, readOnly: Boolean) {
    updateState { it.copy(readOnly = readOnly) }

    if (tagId == null) {
      Timber.d("Tag id not found!")
      setErrorState(TagProcessingStep.FailureType.IllegalIntent)
      return
    }

    viewModelScope.launch {
      performAction(tagId)
    }
  }

  private suspend fun performAction(tagUuid: String) {
    val currentTime = dateProvider.currentTimestamp()
    val tag = schedulers.io { nfcTagRepository.findByUuidWithDependencies(tagUuid) }
    updateState { it.copy(screenState = it.screenState.copy(tagData = tag?.tagData)) }

    if (tag == null) {
      setErrorState(TagProcessingStep.FailureType.TagNotFound(tagUuid))
      return
    }

    val configuration = tag.tagEntity.configuration
    if (configuration == null) {
      setErrorState(TagProcessingStep.FailureType.TagNotConfigured(tag.tagEntity.id))
      nfcCallRepository.insert(tag.tagEntity.id, NfcCallResult.ACTION_MISSING)
      return
    }

    val singleCall = singleCallProvider.provide(configuration.profileId)
    val result = schedulers.io { singleCall.executeAction(configuration.actionParameters) }
    nfcCallRepository.insert(tag.tagEntity.id, result.toNfcCallResult)

    delayIfNeeded(currentTime)

    when (result) {
      SingleCall.Result.Success -> {
        setState(TagProcessingStep.Success)
        delay(SUCCESS_DELAY_MS)
        sendEvent(CallActionViewEvent.Close)
      }

      SingleCall.Result.NotFound -> setErrorState(TagProcessingStep.FailureType.ChannelNotFound(tag.tagEntity.id))
      SingleCall.Result.Offline -> setErrorState(TagProcessingStep.FailureType.ChannelOffline)
      SingleCall.Result.Inactive -> setErrorState(TagProcessingStep.FailureType.SceneInactive)
      is SingleCall.Result.AccessError,
      is SingleCall.Result.CommandError,
      is SingleCall.Result.ConnectionError,
      SingleCall.Result.NoSuchProfile,
      SingleCall.Result.UnknownError -> setErrorState(TagProcessingStep.FailureType.ActionFailed)
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
    updateState { it.copy(screenState = it.screenState.copy(step = step)) }

  private suspend fun delayIfNeeded(currentTime: Long) {
    val stepTime = dateProvider.currentTimestamp() - currentTime
    if (stepTime < STEP_MIN_TIME_MS) {
      delay(STEP_MIN_TIME_MS - stepTime)
    }
  }

  private val NfcTagDataEntity.tagData: CallActionScreenState.TagData
    get() = CallActionScreenState.TagData(
      name = tagEntity.name,
      actionId = tagEntity.actionId,
      subjectName = name(getCaptionUseCase)
    )
}

private val NfcTagEntity.Configuration.actionParameters: ActionParameters
  get() = ActionParameters(actionId, subjectType, subjectId)

private val SingleCall.Result.toNfcCallResult: NfcCallResult
  get() = when (this) {
    SingleCall.Result.Success -> NfcCallResult.SUCCESS
    is SingleCall.Result.AccessError,
    is SingleCall.Result.CommandError,
    is SingleCall.Result.ConnectionError,
    SingleCall.Result.Inactive,
    SingleCall.Result.NoSuchProfile,
    SingleCall.Result.NotFound,
    SingleCall.Result.Offline,
    SingleCall.Result.UnknownError -> NfcCallResult.FAILURE
  }

sealed class CallActionViewEvent : ViewEvent {
  data object Close : CallActionViewEvent()
  data class EditMissingAction(val id: Long) : CallActionViewEvent()
  data class SaveNewNfcTag(val uuid: String, val readOnly: Boolean) : CallActionViewEvent()
}

data class CallActionViewModelState(
  val readOnly: Boolean = false,
  override val screenState: CallActionScreenState = CallActionScreenState()
) : ViewModelState<CallActionScreenState>()
