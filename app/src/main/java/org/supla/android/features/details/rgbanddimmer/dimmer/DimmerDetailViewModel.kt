package org.supla.android.features.details.rgbanddimmer.dimmer
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.supla.android.R
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.DelayableState
import org.supla.android.core.storage.UserStateHolder
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.data.model.general.ChannelState
import org.supla.android.data.source.ColorListRepository
import org.supla.android.data.source.local.entity.ColorEntityType
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.custom.ChannelWithChildren
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.data.source.remote.rgb.color
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.LoadingTimeoutManager
import org.supla.android.extensions.HsvColor
import org.supla.android.extensions.subscribeBy
import org.supla.android.extensions.toGrayColor
import org.supla.android.extensions.toHsv
import org.supla.android.features.details.rgbanddimmer.common.DelayedRgbwwActionSubject
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.features.details.rgbanddimmer.common.asSavedColor
import org.supla.android.features.details.rgbanddimmer.common.delayableState
import org.supla.android.features.details.rgbanddimmer.common.dimmerValues
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
import org.supla.core.shared.data.model.function.rgbanddimmer.DimmerBaseValue
import org.supla.core.shared.data.model.function.rgbanddimmer.RgbBaseValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

private const val REFRESH_DELAY_MS = 3000
private const val BRIGHTNESSES_LIMIT = 10

@HiltViewModel
class DimmerDetailViewModel @Inject constructor(
  private val observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  private val readGroupWithChannelsUseCase: ReadGroupWithChannelsUseCase,
  private val delayedRgbwwActionSubject: DelayedRgbwwActionSubject,
  private val executeRgbwActionUseCase: ExecuteRgbwActionUseCase,
  private val getChannelStateUseCase: GetChannelStateUseCase,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val loadingTimeoutManager: LoadingTimeoutManager,
  private val colorListRepository: ColorListRepository,
  private val userStateHolder: UserStateHolder,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<DimmerDetailModelState, DimmerDetailViewEvent>(DimmerDetailModelState(), schedulers), DimmerDetailScope {

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

  override fun onBrightnessSelectionStarted() {
    updateState {
      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
        changing = true
      )
    }
  }

  override fun onBrightnessSelecting(brightness: Int) {
    updateState {
      it.copy(
        lastInteractionTime = dateProvider.currentTimestamp(),
        viewState = it.viewState.copy(
          // Setting brightness to 0 is not allowed. If the user wants turn off the dimmer
          // should click on turn off button
          value = DimmerValue.Single(max(1, brightness))
        )
      )
    }

    currentState().delayableState?.let { delayedRgbwwActionSubject.emit(it) }
  }

  override fun onBrightnessSelected() {
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

  override fun turnOn() {
    updateState {
      if (it.viewState.onButtonState?.pressed == false) {
        sendDimmerValues(100)

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
        sendDimmerValues(0)

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

  override fun toggleSelectorType() {
    updateState {
      val newValue = when (it.viewState.selectorType) {
        DimmerSelectorType.LINEAR -> DimmerSelectorType.CIRCULAR
        DimmerSelectorType.CIRCULAR -> DimmerSelectorType.LINEAR
      }
      if (it.profileId != null && it.remoteId != null) {
        userStateHolder.setDimmerSelectorType(newValue, it.profileId, it.remoteId)
      }
      it.copy(
        viewState = it.viewState.copy(
          selectorType = newValue
        )
      )
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
          value = DimmerValue.Single(color.brightness)
        )
      )
    }

    sendDimmerValues(color.brightness, onOff = false)
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
    val brightness = state.viewState.value.brightness ?: return
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
            color = brightness.toGrayColor(),
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
          colorListRepository.delete(
            colorId = color.id,
            remoteId = remoteId,
            isGroup = type.isGroup(),
            type = ColorEntityType.DIMMER
          )
        } catch (ex: Exception) {
          Timber.e(ex, "Brightness delete failed")
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
          colorListRepository.swapPositions(
            remoteId = remoteId,
            from = from,
            to = to,
            isGroup = type.isGroup(),
            type = ColorEntityType.DIMMER
          )
        } catch (ex: Exception) {
          Timber.e(ex, "Positions swap failed")
        }
      }
    }
  }

  private fun sendDimmerValues(brightness: Int? = null, onOff: Boolean = true) {
    with(currentState()) {
      if (type != null && remoteId != null) {
        executeRgbwActionUseCase(
          type = type.subjectType,
          remoteId = remoteId,
          color = rgbColor,
          brightness = brightness ?: viewState.value.brightness,
          onOff = onOff
        )
          .attachSilent()
          .subscribeBy(onError = defaultErrorHandler("sendDimmerValues()"))
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
        onNext = this::handleChannel,
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channelWithChildren: ChannelWithChildren) {
    val channel = channelWithChildren.channel
    val channelState = getChannelStateUseCase(channel)
    val value = getValue(channel)
    val rgbValue = getRgbValue(channel)

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
        profileId = channel.profileId,
        type = ItemType.CHANNEL,
        loadingState = state.loadingState.changingLoading(false, dateProvider),
        rgbColor = rgbValue?.color?.toHsv(),
        rgbBrightness = rgbValue?.colorBrightness,
        viewState = state.viewState.copy(
          offline = channel.status.offline,
          value = DimmerValue.Single(value.brightness),
          deviceStateData = DeviceStateData(
            icon = getChannelIconUseCase(channel),
            label = localizedString(R.string.details_timer_state_label),
            value = getDeviceStateValue(channel.status, channelState),
          ),
          onButtonState = SwitchButtonState(
            icon = getButtonIcon(channel, ChannelState.Value.ON),
            textRes = R.string.channel_btn_on,
            pressed = value.brightness != 0
          ),
          offButtonState = SwitchButtonState(
            icon = getButtonIcon(channel, ChannelState.Value.OFF),
            textRes = R.string.channel_btn_off,
            pressed = value.brightness == 0
          ),
          loading = false,
          selectorType = userStateHolder.getDimmerSelectorType(channel.profileId, channel.remoteId)
        )
      )
    }
  }

  private fun getValue(channel: ChannelDataEntity): DimmerBaseValue =
    when (channel.function) {
      SuplaFunction.DIMMER -> channel.channelValueEntity.asDimmerValue()
      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> channel.channelValueEntity.asRgbwValue()
      else -> throw IllegalStateException("Unsupported function: ${channel.function}")
    }

  private fun getRgbValue(channel: ChannelDataEntity): RgbBaseValue? =
    when (channel.function) {
      SuplaFunction.DIMMER_AND_RGB_LIGHTING -> channel.channelValueEntity.asRgbwValue()
      else -> null
    }

  private fun observeGroup(remoteId: Int) {
    readGroupWithChannelsUseCase(remoteId)
      .attachSilent()
      .subscribeBy(
        onNext = this::handleGroup,
        onError = defaultErrorHandler("loadChannel($remoteId)")
      )
      .disposeBySelf()
  }

  private fun handleGroup(groupWithChannels: GroupWithChannels) {
    val group = groupWithChannels.group
    val groupStateValue: ChannelState.Value? = groupWithChannels.aggregatedState(GroupWithChannels.Policy.Dimmer)
    val groupState = ChannelState.Default(groupStateValue ?: ChannelState.Value.OFF)
    val dimmerValues = group.channelGroupEntity.dimmerValues

    updateState { state ->
      state.copy(
        remoteId = group.remoteId,
        profileId = group.profileId,
        type = ItemType.GROUP,
        loadingState = state.loadingState.changingLoading(false, dateProvider),
        viewState = state.viewState.copy(
          offline = group.status.offline,
          value = DimmerValue.Multiple(dimmerValues),
          deviceStateData = DeviceStateData(
            icon = getChannelIconUseCase(group),
            label = localizedString(R.string.details_timer_state_label),
            value = getDeviceStateValue(group.status, groupState),
          ),
          onButtonState = SwitchButtonState(
            icon = getButtonIcon(group, ChannelState.Value.ON),
            textRes = R.string.channel_btn_on,
            pressed = groupState.value == ChannelState.Value.ON
          ),
          offButtonState = SwitchButtonState(
            icon = getButtonIcon(group, ChannelState.Value.OFF),
            textRes = R.string.channel_btn_off,
            pressed = groupState.value == ChannelState.Value.OFF
          ),
          loading = false,
          selectorType = userStateHolder.getDimmerSelectorType(group.profileId, group.remoteId)
        )
      )
    }
  }

  private fun getDeviceStateValue(status: SuplaChannelAvailabilityStatus, state: ChannelState): LocalizedString = when {
    status.offline -> localizedString(R.string.offline)
    state.value == ChannelState.Value.ON -> localizedString(R.string.details_timer_device_on)
    (state is ChannelState.RgbAndDimmer) && state.dimmer == ChannelState.Value.ON -> localizedString(R.string.details_timer_device_on)
    else -> localizedString(R.string.details_timer_device_off)
  }

  private fun getButtonIcon(data: ChannelDataBase, stateValue: ChannelState.Value): ImageId =
    when {
      data.function == SuplaFunction.DIMMER -> getChannelIconUseCase(data, channelStateValue = stateValue)
      stateValue == ChannelState.Value.ON -> ImageId(R.drawable.fnc_dimmer_on)
      else -> ImageId(R.drawable.fnc_dimmer_off)
    }

  private fun observeSavedColors(remoteId: Int, type: ItemType) {
    val observable = when (type) {
      ItemType.CHANNEL -> colorListRepository.findAllChannelColors(remoteId, ColorEntityType.DIMMER)
      ItemType.GROUP -> colorListRepository.findAllGroupColors(remoteId, ColorEntityType.DIMMER)
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

sealed interface DimmerDetailViewEvent : ViewEvent {
  data object ShowLimitReached : DimmerDetailViewEvent
}

data class DimmerDetailModelState(
  val remoteId: Int? = null,
  val type: ItemType? = null,
  val profileId: Long? = null,
  val lastInteractionTime: Long? = null,
  val changing: Boolean = false,
  val loadingState: LoadingTimeoutManager.LoadingState = LoadingTimeoutManager.LoadingState(),
  val rgbColor: HsvColor? = null,
  val rgbBrightness: Int? = null,
  val viewState: DimmerDetailViewState = DimmerDetailViewState(),

  override val sent: Boolean = false
) : ViewState(), DelayableState {

  override fun sentState(): DelayableState = copy(sent = true)

  override fun delayableCopy(): DelayableState = copy()
}
