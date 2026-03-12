package org.supla.android.features.details.rgbanddimmer.dimmercct
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.ColorListRepository
import org.supla.android.data.source.local.entity.ColorEntityType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.subscribeBy
import org.supla.android.features.details.rgbanddimmer.common.DelayedRgbwwActionSubject
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.dimmer.BaseDimmerDetailViewModel
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerDetailViewEvent
import org.supla.android.features.details.rgbanddimmer.common.dimmer.DimmerValue
import org.supla.android.images.ImageId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteRgbwActionUseCase
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import timber.log.Timber
import javax.inject.Inject

private const val BRIGHTNESSES_LIMIT = 10

@HiltViewModel
class DimmerCctDetailViewModel @Inject constructor(
  private val delayedRgbwwActionSubject: DelayedRgbwwActionSubject,
  private val colorListRepository: ColorListRepository,
  private val dateProvider: DateProvider,
  observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  executeRgbwActionUseCase: ExecuteRgbwActionUseCase,
  getChannelStateUseCase: GetChannelStateUseCase,
  getChannelIconUseCase: GetChannelIconUseCase,
  loadingTimeoutManager: LoadingTimeoutManager,
  userStateHolder: UserStateHolder,
  schedulers: SuplaSchedulers
) : BaseDimmerDetailViewModel(
  observeChannelWithChildrenUseCase,
  readGroupWithChannelsUseCase,
  delayedRgbwwActionSubject,
  executeRgbwActionUseCase,
  getChannelStateUseCase,
  loadingTimeoutManager,
  getChannelIconUseCase,
  colorListRepository,
  userStateHolder,
  dateProvider,
  schedulers
),
  DimmerCctDetailScope {

  override fun onSaveCurrentColor() {
    updateState { it.copy(lastInteractionTime = null) }

    val state = currentState()
    if (state.viewState.offline) {
      return
    }

    val remoteId = state.remoteId ?: return
    val profileId = state.profileId ?: return
    val type = state.type ?: return
    val brightness = state.viewState.value.brightness ?: return
    val cct = state.viewState.value.cct ?: 100
    val colorsCount = state.viewState.savedColors.size

    if (colorsCount > BRIGHTNESSES_LIMIT) {
      sendEvent(DimmerDetailViewEvent.ShowLimitReached)
      return
    }

    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          colorListRepository.save(
            remoteId = remoteId,
            isGroup = type.isGroup(),
            color = cct,
            brightness = brightness,
            profileId = profileId,
            type = ColorEntityType.DIMMER
          )
        } catch (ex: Exception) {
          Timber.e(ex, "Brightness save failed")
        }
      }
    }
  }

  override fun onSavedColorSelected(color: SavedColor) {
    updateState {
      if (it.viewState.offline) {
        return@updateState it
      }
      it.copy(
        lastInteractionTime = null,
        viewState = it.viewState.copy(
          value = DimmerValue.Single(color.brightness, color.color)
        )
      )
    }

    sendDimmerValues(color.brightness, onOff = false)
  }

  override fun getButtonIcon(stateValue: ChannelState.Value): ImageId =
    when (stateValue) {
      ChannelState.Value.ON -> ImageId(R.drawable.fnc_dimmer_cct_on)
      else -> ImageId(R.drawable.fnc_dimmer_cct_off)
    }

  override fun onCctSelectionStarted() {
    updateState {
      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = true
      )
    }
  }

  override fun onCctSelecting(cct: Int) {
    updateState {
      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
        viewState = it.viewState.copy(
          value = DimmerValue.Single(
            brightness = it.viewState.value.brightness ?: 100,
            cct = cct
          )
        )
      )
    }

    currentState().delayableState?.let { delayedRgbwwActionSubject.emit(it) }
  }

  override fun onCctSelected() {
    updateState {
      it.copy(
        loadingState = it.loadingState.changingLoading(true, dateProvider),
        changing = false
      )
    }

    currentState().delayableState?.let {
      delayedRgbwwActionSubject.sendImmediately(it)
        .attachSilent()
        .subscribeBy(onError = defaultErrorHandler("onBrightnessSelected()"))
        .disposeBySelf()
    }
  }
}
