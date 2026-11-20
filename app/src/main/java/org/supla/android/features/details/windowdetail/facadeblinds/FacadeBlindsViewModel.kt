package org.supla.android.features.details.windowdetail.facadeblinds
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

import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.ApplicationPreferences
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.toShadingBlindMarkers
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.di.GSON_FOR_REPO
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.ValueDestination
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.BaseBlindsViewModel
import org.supla.android.features.details.windowdetail.base.ui.BaseBlindsViewModelState
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.tools.SuplaThreading
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ObserveChannelWithChildrenUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.CalculateGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ObserveChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import org.supla.core.shared.data.model.shadingsystem.SuplaShadingSystemFlag
import org.supla.core.shared.extensions.forTrue
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class FacadeBlindsViewModel @Inject constructor(
  private val calculateGroupTiltingDetailsUseCase: CalculateGroupTiltingDetailsUseCase,
  @param:Named(GSON_FOR_REPO) private val gson: Gson,
  observeChannelGroupByRemoteIdUseCase: ObserveChannelGroupByRemoteIdUseCase,
  executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  observeChannelWithChildrenUseCase: ObserveChannelWithChildrenUseCase,
  callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  channelConfigEventsManager: ChannelConfigEventsManager,
  executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: ProfileRepository,
  channelRepository: ChannelRepository,
  preferences: ApplicationPreferences,
  authorizeUseCase: AuthorizeUseCase,
  dateProvider: DateProvider,
  loginUseCase: LoginUseCase,
  threading: SuplaThreading
) : BaseBlindsViewModel<FacadeBlindsViewModelState>(
  observeChannelGroupByRemoteIdUseCase,
  executeShadingSystemActionUseCase,
  getGroupOnlineSummaryUseCase,
  channelConfigEventsManager,
  suplaClientProvider,
  channelRepository,
  callSuplaClientOperationUseCase,
  observeChannelWithChildrenUseCase,
  executeSimpleActionUseCase,
  profileRepository,
  preferences,
  authorizeUseCase,
  dateProvider,
  loginUseCase,
  FacadeBlindsViewModelState(),
  threading
) {

  override fun updatePosition(state: FacadeBlindsViewModelState, position: Float) =
    state.copy(windowState = state.windowState.copy(position = WindowGroupedValue.Similar(position), markers = emptyList()))

  override fun updateWindowState(
    state: FacadeBlindsViewModelState,
    position: WindowGroupedValue,
    tilt: Float?,
    markers: List<ShadingBlindMarker>
  ): FacadeBlindsViewModelState =
    state.copy(
      windowState = state.windowState.copy(
        position = position,
        windowTilt = tilt?.let { WindowGroupedValue.Similar(it) },
        sliderTilt = tilt?.let { WindowGroupedValue.Similar(it) },
        markers = markers
      )
    )

  override fun stateCopy(
    state: FacadeBlindsViewModelState,
    remoteId: Int?,
    moveStartTime: Long?,
    manualMoving: Boolean,
    showCalibrationDialog: Boolean,
    authorizationDialogState: AuthorizationDialogState?,
    viewStateUpdater: (WindowViewState) -> WindowViewState
  ): FacadeBlindsViewModelState =
    state.copy(
      remoteId = remoteId,
      moveStartTime = moveStartTime,
      manualMoving = manualMoving,
      showCalibrationDialog = showCalibrationDialog,
      authorizationDialogState = authorizationDialogState,
      viewState = viewStateUpdater(state.viewState)
    )

  override fun handleChannel(channel: ChannelDataEntity) {
    updateState { state ->
      if (state.manualMoving) {
        return@updateState state // Skip position updating when moving by finger
      }

      val value = channel.channelValueEntity.asFacadeBlindValue()
      val position = if (value.hasValidPosition()) value.position else 0
      val tilt = if (value.hasValidTilt() && value.flags.contains(SuplaShadingSystemFlag.TILT_IS_SET)) value.tilt else null
      val config = (channel.configEntity?.toSuplaConfig(gson) as? SuplaChannelFacadeBlindConfig)

      updateChannel(state, channel, value) {
        it.copy(
          windowState = it.windowState.copy(
            position = WindowGroupedValue.Similar(if (value.status.online) position.toFloat() else 25f),
            windowTilt = tilt?.let { t -> WindowGroupedValue.Similar(if (value.status.online) t.toFloat() else 50f) },
            sliderTilt = tilt?.let { t -> WindowGroupedValue.Similar(if (value.status.online) t.toFloat() else 50f) },
            tilt0Angle = config?.tilt0Angle?.toFloat(),
            tilt100Angle = config?.tilt100Angle?.toFloat(),
            positionTextFormat = positionTextFormat
          ),
          tiltControlType = config?.type,
          lastPosition = position
        )
      }
    }
  }

  override fun handleGroup(group: GroupData) {
    updateState { state ->
      if (state.manualMoving) {
        return@updateState state // Skip position updating when moving by finger
      }

      val positions = group.groupDataEntity.channelGroupEntity.getFacadeBlindPositions()
      val overallPosition = getGroupValues(positions, state.windowState.markers.isNotEmpty()) { it.position.toFloat() }
      val slatTiltForWindow = getGroupValues(
        values = positions,
        hadMarkers = state.windowState.markers.isNotEmpty(),
        threshold = GroupThreshold.Slat,
        extractor = { it.tilt.toFloat() }
      )
      val slatTiltForSlider = getGroupValues(
        values = positions,
        hadMarkers = state.windowState.markers.isNotEmpty(),
        threshold = GroupThreshold.None,
        extractor = { it.tilt.toFloat() }
      )
      val markers = group.channels.toShadingBlindMarkers(gson)
      val tiltingDetails = calculateGroupTiltingDetailsUseCase(group.channels.map { it.configEntity?.toSuplaConfig(gson) })

      updateGroup(state, group.groupDataEntity, group.onlineSummary) {
        it.copy(
          remoteId = group.groupDataEntity.remoteId,
          windowState = it.windowState.copy(
            position = if (group.groupDataEntity.status.online) overallPosition else WindowGroupedValue.Similar(25f),
            windowTilt = if (group.groupDataEntity.status.online) slatTiltForWindow else WindowGroupedValue.Similar(50f),
            sliderTilt = if (group.groupDataEntity.status.online) slatTiltForSlider else WindowGroupedValue.Similar(50f),
            tilt0Angle = tiltingDetails.tilt0Angle?.toFloat(),
            tilt100Angle = tiltingDetails.tilt100Angle?.toFloat(),
            markers = if (group.groupDataEntity.status.online) markers else emptyList(),
            positionTextFormat = positionTextFormat
          ),
          tiltControlType = tiltingDetails.tiltControlType,
          viewState = it.viewState.copy(
            positionUnknown = overallPosition is WindowGroupedValue.Invalid,
          ),
          lastPosition = group.groupDataEntity.status.online.forTrue { overallPosition.value(ValueDestination.WINDOW)?.toInt() }
        )
      }
    }
  }

  override fun canShowMoveTime(state: FacadeBlindsViewModelState) =
    state.viewState.positionUnknown || state.windowState.windowTilt == null

  override fun updateTiltingDetails(
    tilt0Angle: Float,
    tilt100Angle: Float,
    tiltControlType: SuplaTiltControlType,
    tiltingTime: Int,
    openingTime: Int,
    closingTime: Int
  ) {
    updateState {
      it.copy(
        windowState = it.windowState.copy(
          tilt0Angle = tilt0Angle,
          tilt100Angle = tilt100Angle
        ),
        tiltControlType = tiltControlType,
        tiltingTime = tiltingTime,
        openingTime = openingTime,
        closingTime = closingTime
      )
    }
  }
}

private fun ChannelGroupEntity.getFacadeBlindPositions(): List<ShadowingBlindGroupValue> =
  groupTotalValues.filterIsInstance<ShadowingBlindGroupValue>()

data class FacadeBlindsViewModelState(
  override val tiltControlType: SuplaTiltControlType? = null,

  override val tiltingTime: Int? = null,
  override val openingTime: Int? = null,
  override val closingTime: Int? = null,
  override val lastPosition: Int? = null,

  override val remoteId: Int? = null,
  override val windowState: FacadeBlindWindowState = FacadeBlindWindowState(WindowGroupedValue.Similar(0f)),
  override val viewState: WindowViewState = WindowViewState(),
  override val moveStartTime: Long? = null,
  override val manualMoving: Boolean = false,
  override val showCalibrationDialog: Boolean = false,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : BaseBlindsViewModelState()
