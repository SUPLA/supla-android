package org.supla.android.features.details.rgbanddimmer.rgb
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

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.ColorListRepository
import org.supla.android.data.source.local.entity.ColorEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.rgb.color
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.HsvColor
import org.supla.android.extensions.filterHexDigits
import org.supla.android.extensions.toColor
import org.supla.android.extensions.toHexString
import org.supla.android.extensions.toHsv
import org.supla.android.features.details.rgbanddimmer.common.rgbValues
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbDetailViewState
import org.supla.android.features.details.rgbanddimmer.rgb.model.RgbValue
import org.supla.android.features.details.rgbanddimmer.rgb.model.SavedColor
import org.supla.android.features.details.rgbanddimmer.rgb.ui.ColorDialogState
import org.supla.android.features.details.rgbanddimmer.rgb.ui.RgbDetailScope
import org.supla.android.images.ImageId
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.android.usecases.channel.GetChannelStateUseCase
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.ExecuteRgbwActionUseCase
import org.supla.android.usecases.group.GroupWithChannels
import org.supla.android.usecases.group.ReadGroupWithChannelsUseCase
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.core.shared.data.model.function.rgbanddimmer.RgbBaseValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val REFRESH_DELAY_MS = 3000
private const val COLORS_LIMIT = 10

@HiltViewModel
class RgbDetailViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  private val readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  private val delayedRgbActionSubject: DelayedRgbActionSubject,
  private val executeRgbwActionUseCase: ExecuteRgbwActionUseCase,
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val colorListRepository: ColorListRepository,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<RgbDetailModelState, RgbDetailViewEvent>(RgbDetailModelState(), schedulers), RgbDetailScope {

  private val updateSubject: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

  override fun onViewCreated() {
    loadingTimeoutManager.watch({ currentState().loadingState }) {
      updateState { state ->
        updateSubject.onNext(0)
        state.copy(
          loadingState = state.loadingState.changingLoading(false, dateProvider),
          viewState = state.viewState.copy(loading = false)
        )
      }
    }.disposeBySelf()
  }

  fun observeData(remoteId: Int, type: ItemType) {
    when (type) {
      ItemType.CHANNEL -> observeChannel(remoteId)
      ItemType.GROUP -> observeGroup(remoteId)
    }

    observeSavedColors(remoteId, type)
  }

  override fun onColorSelectionStarted() {
    updateState {
      if (it.viewState.offline) {
        return@updateState it
      }

      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = true
      )
    }
  }

  override fun onColorSelecting(color: HsvColor) {
    updateState {
      if (it.viewState.offline) {
        return@updateState it
      }

      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
      )
    }

    val state = currentState().let {
      it.copy(
        viewState = it.viewState.copy(
          value = RgbValue.Single(color)
        )
      )
    }
    delayedRgbActionSubject.emit(state)
  }

  override fun onColorSelected(color: HsvColor) {
    updateState {
      if (it.viewState.offline) {
        return@updateState it
      }
      val state = it.copy(
        loadingState = it.loadingState.changingLoading(true, dateProvider),
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = false,
        viewState = it.viewState.copy(
          value = RgbValue.Single(color)
        )
      )

      delayedRgbActionSubject.sendImmediately(state)
        .attachSilent()
        .subscribeBy(onError = defaultErrorHandler("onColorSelected()"))
        .disposeBySelf()

      return@updateState state
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
          value = RgbValue.Single(color.color.toHsv(color.brightness))
        )
      )
    }

    setRgbColors(color = color.color, colorBrightness = color.brightness)
  }

  override fun onSaveCurrentColor() {
    updateState { it.copy(lastInteractionTime = null) }

    val state = currentState()
    if (state.viewState.offline) {
      return
    }

    val remoteId = state.remoteId ?: return
    val profileId = state.profileId ?: return
    val type = state.type ?: return
    val hsv = state.viewState.value.hsv ?: return
    val colorsCount = state.viewState.savedColors.size

    if (colorsCount > COLORS_LIMIT) {
      sendEvent(RgbDetailViewEvent.ShowColorsLimitReached)
      return
    }

    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          colorListRepository.save(remoteId, type.isGroup(), hsv.fullBrightnessColor, hsv.valueAsPercentage, profileId)
        } catch (ex: Exception) {
          Timber.e(ex, "Color save failed")
        }
      }
    }
  }

  override fun onRemoveColor(positionOnList: Int) {
    updateState { it.copy(lastInteractionTime = null) }

    val state = currentState()
    if (state.viewState.offline) {
      return
    }
    val color = state.viewState.savedColors.getOrNull(positionOnList) ?: return
    val remoteId = state.remoteId ?: return
    val type = state.type ?: return

    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          colorListRepository.delete(color.id, remoteId, type.isGroup())
        } catch (ex: Exception) {
          Timber.e(ex, "Color delete failed")
        }
      }
    }
  }

  override fun onMoveColors(from: Int, to: Int) {
    updateState { it.copy(lastInteractionTime = null) }

    val state = currentState()
    val remoteId = state.remoteId ?: return
    val type = state.type ?: return

    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        try {
          colorListRepository.swapPositions(remoteId = remoteId, from = from, to = to, isGroup = type.isGroup())
        } catch (ex: Exception) {
          Timber.e(ex, "Color delete failed")
        }
      }
    }
  }

  override fun turnOn() {
    updateState {
      if (it.viewState.onButtonState?.pressed == false) {
        setRgbColors(colorBrightness = 100, onOff = true)

        it.copy(
          loadingState = it.loadingState.changingLoading(true, dateProvider),
          lastInteractionTime = null,
          viewState = it.viewState.copy(loading = true)
        )
      } else {
        it
      }
    }
  }

  override fun turnOff() {
    updateState {
      if (it.viewState.offButtonState?.pressed == false) {
        setRgbColors(colorBrightness = 0, onOff = true)

        it.copy(
          loadingState = it.loadingState.changingLoading(true, dateProvider),
          lastInteractionTime = null,
          viewState = it.viewState.copy(loading = true)
        )
      } else {
        it
      }
    }
  }

  override fun onColorDialogDismiss() {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          colorDialogState = null
        )
      )
    }
  }

  override fun onColorDialogConfirm() {
    updateState { state ->
      val hsv = state.viewState.colorDialogState?.color?.toColor()?.toHsv()
      state.copy(
        viewState = state.viewState.copy(
          value = hsv?.let { RgbValue.Single(it) } ?: state.viewState.value,
          colorDialogState = null
        )
      )
    }

    delayedRgbActionSubject.sendImmediately(currentState())
      .attachSilent()
      .subscribeBy(onError = defaultErrorHandler("onColorDialogConfirm()"))
      .disposeBySelf()
  }

  override fun onColorDialogInputChange(value: String) {
    updateState {
      it.copy(
        viewState = it.viewState.copy(
          colorDialogState = it.viewState.colorDialogState?.copy(
            color = value.filterHexDigits()
          )
        )
      )
    }
  }

  override fun onOpenColorDialog() {
    updateState {
      val dialogState = it.viewState.offline.not().ifTrue {
        ColorDialogState(it.viewState.value.hsv?.color?.toHexString() ?: "#00FF00")
      }

      it.copy(
        viewState = it.viewState.copy(colorDialogState = dialogState)
      )
    }
  }

  private fun setRgbColors(color: Color? = null, colorBrightness: Int? = null, onOff: Boolean = false) {
    with(currentState()) {
      if (type != null && remoteId != null) {
        executeRgbwActionUseCase(
          actionId = ActionId.SET_RGBW_PARAMETERS,
          type = type.subjectType,
          remoteId = remoteId,
          color = color ?: viewState.value.hsv?.fullBrightnessColor,
          colorBrightness = colorBrightness,
          brightness = dimmerBrightness,
          onOff = onOff
        )
          .attachSilent()
          .subscribeBy(onError = defaultErrorHandler("executeRgbAction(color: $color)"))
          .disposeBySelf()
      }
    }
  }

  private fun observeChannel(remoteId: Int) {
    Observable.combineLatest(
      Observable.merge(
        Observable.just(0),
        updateSubject.debounce(1, TimeUnit.SECONDS)
      ),
      observeChannelWithChildrenUseCase(remoteId).distinctUntilChanged()
    ) { _, channelWithChildren -> channelWithChildren }
      .attachSilent()
      .subscribeBy(
        onNext = { channelWithChildren -> handleChannel(channelWithChildren) },
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channelWithChildren: ChannelWithChildren) {
    val channel = channelWithChildren.channel
    val channelState = getChannelStateUseCase(channel)
    val value = getValue(channel)

    updateState { state ->
      if (state.changing) {
        Timber.d("update skipped because of changing")
        return@updateState state // Do not change anything, when user makes manual operations
      }
      if (state.lastInteractionTime != null && state.lastInteractionTime + REFRESH_DELAY_MS > dateProvider.currentTimestamp()) {
        Timber.d("update skipped because of last interaction time")
        updateSubject.onNext(0)
        return@updateState state // Do not change anything during 3 secs after last user interaction
      }
      Timber.d("updating state with data")

      state.copy(
        remoteId = channel.remoteId,
        type = ItemType.CHANNEL,
        profileId = channel.profileId,
        loadingState = state.loadingState.changingLoading(false, dateProvider),
        dimmerBrightness = getDimmerBrightness(channel),
        viewState = state.viewState.copy(
          offline = channel.status.offline,
          value = RgbValue.Single(value.color.toHsv(value.colorBrightness)),
          deviceStateData = DeviceStateData(
            icon = getChannelIconUseCase(channel),
            label = localizedString(R.string.details_timer_state_label),
            value = getDeviceStateValue(channel.status, channelState),
          ),
          onButtonState = SwitchButtonState(
            icon = getButtonIcon(channel, ChannelState.Value.ON),
            textRes = R.string.channel_btn_on,
            pressed = (channelState as? ChannelState.RgbAndDimmer)?.rgb == ChannelState.Value.ON
          ),
          offButtonState = SwitchButtonState(
            icon = getButtonIcon(channel, ChannelState.Value.OFF),
            textRes = R.string.channel_btn_off,
            pressed = (channelState as? ChannelState.RgbAndDimmer)?.rgb == ChannelState.Value.OFF
          ),
          loading = false
        )
      )
    }
  }

  private fun getValue(channel: ChannelDataEntity): RgbBaseValue =
    when (channel.function) {
      SuplaFunction.RGB_LIGHTING -> channel.channelValueEntity.asRgbValue()
      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> channel.channelValueEntity.asRgbwValue()
      else -> throw IllegalStateException("Unsupported function: ${channel.function}")
    }

  private fun getDimmerBrightness(channel: ChannelDataEntity): Int =
    when (channel.function) {
      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> channel.channelValueEntity.asRgbwValue().brightness
      else -> 0
    }

  private fun observeGroup(remoteId: Int) {
    Observable.combineLatest(
      Observable.merge(
        Observable.just(0),
        updateSubject.debounce(1, TimeUnit.SECONDS)
      ),
      readGroupWithChannelsUseCase(remoteId).distinctUntilChanged(),
    ) { _, group -> group }
      .attachSilent()
      .subscribeBy(
        onNext = { group -> handleGroup(group) },
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleGroup(groupWithChannels: GroupWithChannels) {
    val group = groupWithChannels.group
    val groupStateValue: ChannelState.Value? = groupWithChannels.aggregatedState(GroupWithChannels.Policy.Rgb)
    val groupState = ChannelState.Default(groupStateValue ?: ChannelState.Value.OFF)
    val rgbValues = group.channelGroupEntity.rgbValues

    updateState { state ->
      if (state.changing) {
        Timber.d("update skipped because of changing")
        return@updateState state // Do not change anything, when user makes manual operations
      }
      if (state.lastInteractionTime != null && state.lastInteractionTime + REFRESH_DELAY_MS > dateProvider.currentTimestamp()) {
        Timber.d("update skipped because of last interaction time")
        updateSubject.onNext(0)
        return@updateState state // Do not change anything during 3 secs after last user interaction
      }
      Timber.d("updating state with data")

      state.copy(
        remoteId = group.remoteId,
        profileId = group.profileId,
        type = ItemType.GROUP,
        loadingState = state.loadingState.changingLoading(false, dateProvider),
        viewState = state.viewState.copy(
          value = RgbValue.Multiple(rgbValues.toList()),
          deviceStateData = DeviceStateData(
            icon = getChannelIconUseCase(group),
            label = localizedString(R.string.details_timer_state_label),
            value = getDeviceStateValue(group.status, groupState),
          ),
          onButtonState = SwitchButtonState(
            icon = getButtonIcon(group, ChannelState.Value.ON),
            textRes = R.string.channel_btn_on,
            pressed = groupStateValue == ChannelState.Value.ON
          ),
          offButtonState = SwitchButtonState(
            icon = getButtonIcon(group, ChannelState.Value.OFF),
            textRes = R.string.channel_btn_off,
            pressed = groupStateValue == ChannelState.Value.OFF
          ),
          loading = false
        )
      )
    }
  }

  private fun getDeviceStateValue(status: SuplaChannelAvailabilityStatus, state: ChannelState): LocalizedString = when {
    status.offline -> localizedString(R.string.offline)
    state.value == ChannelState.Value.ON -> localizedString(R.string.details_timer_device_on)
    state is ChannelState.RgbAndDimmer && state.rgb == ChannelState.Value.ON ->
      localizedString(R.string.details_timer_device_on)

    else -> localizedString(R.string.details_timer_device_off)
  }

  private fun getButtonIcon(channel: ChannelDataBase, stateValue: ChannelState.Value): ImageId =
    when {
      channel.function == SuplaFunction.RGB_LIGHTING -> getChannelIconUseCase(channel, channelStateValue = stateValue)
      stateValue == ChannelState.Value.ON -> ImageId(R.drawable.fnc_rgb_on)
      else -> ImageId(R.drawable.fnc_rgb_off)
    }

  private val ColorEntity.asSavedColor: SavedColor
    get() = SavedColor(
      id = id!!,
      color = Color(color),
      brightness = brightness.toInt()
    )

  private fun observeSavedColors(remoteId: Int, type: ItemType) {
    val observable = when (type) {
      ItemType.CHANNEL -> colorListRepository.findAllChannelColors(remoteId)
      ItemType.GROUP -> colorListRepository.findAllGroupColors(remoteId)
    }

    observable
      .attachSilent()
      .subscribeBy(
        onNext = { savedColors ->
          updateState { state ->
            state.copy(
              viewState = state.viewState.copy(savedColors = savedColors.map { it.asSavedColor })
            )
          }
        },
        onError = defaultErrorHandler("observeSavedColors($remoteId, $type)")
      )
      .disposeBySelf()
  }
}

sealed interface RgbDetailViewEvent : ViewEvent {
  data object ShowColorsLimitReached : RgbDetailViewEvent
}

data class RgbDetailModelState(
  val remoteId: Int? = null,
  val type: ItemType? = null,
  val profileId: Long? = null,
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false,
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val dimmerBrightness: Int? = null,
  val viewState: RgbDetailViewState = RgbDetailViewState(),
  override val sent: Boolean = false
) : ViewState(), DelayableState {

  override fun sentState(): DelayableState = copy(sent = true)
  override fun delayableCopy(): DelayableState = copy()
}
