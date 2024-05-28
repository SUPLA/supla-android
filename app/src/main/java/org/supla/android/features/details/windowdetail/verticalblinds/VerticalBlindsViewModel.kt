package org.supla.android.features.details.windowdetail.verticalblinds
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
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.data.source.remote.shadingsystem.SuplaShadingSystemFlag
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.features.details.windowdetail.base.data.ShadingBlindMarker
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.BaseBlindsViewModel
import org.supla.android.features.details.windowdetail.base.ui.BaseBlindsViewModelState
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.windowview.ShadingSystemOrientation
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.ReadGroupTiltingDetailsUseCase
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import javax.inject.Inject

@HiltViewModel
class VerticalBlindsViewModel @Inject constructor(
  channelConfigEventsManager: ChannelConfigEventsManager,
  executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  readGroupTiltingDetailsUseCase: ReadGroupTiltingDetailsUseCase,
  suplaClientProvider: SuplaClientProvider,
  executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  callSuplaClientOperationUseCase: CallSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  preferences: Preferences,
  dateProvider: DateProvider,
  profileRepository: RoomProfileRepository,
  loginUseCase: LoginUseCase,
  authorizeUseCase: AuthorizeUseCase,
  schedulers: SuplaSchedulers
) : BaseBlindsViewModel<VerticalBlindsViewModelState>(
  channelConfigEventsManager,
  executeShadingSystemActionUseCase,
  suplaClientProvider,
  readGroupTiltingDetailsUseCase,
  executeSimpleActionUseCase,
  callSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase,
  preferences,
  dateProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  VerticalBlindsViewModelState(),
  schedulers
) {

  override fun updatePosition(state: VerticalBlindsViewModelState, position: Float) =
    state.copy(windowState = state.windowState.copy(position = WindowGroupedValue.Similar(position)))

  override fun updateWindowState(
    state: VerticalBlindsViewModelState,
    position: WindowGroupedValue,
    tilt: Float?,
    markers: List<ShadingBlindMarker>
  ): VerticalBlindsViewModelState =
    state.copy(
      windowState = state.windowState.copy(
        position = position,
        slatTilt = tilt?.let { WindowGroupedValue.Similar(it) },
        markers = markers
      )
    )

  override fun stateCopy(
    state: VerticalBlindsViewModelState,
    remoteId: Int?,
    moveStartTime: Long?,
    manualMoving: Boolean,
    showCalibrationDialog: Boolean,
    authorizationDialogState: AuthorizationDialogState?,
    viewStateUpdater: (WindowViewState) -> WindowViewState
  ): VerticalBlindsViewModelState =
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

      updateChannel(state, channel, value) {
        it.copy(
          windowState = it.windowState.copy(
            position = WindowGroupedValue.Similar(if (value.online) position.toFloat() else 25f),
            slatTilt = tilt?.let { t -> WindowGroupedValue.Similar(if (value.online) t.toFloat() else 50f) },
            positionTextFormat = positionTextFormat
          ),
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

      val positions = group.groupDataEntity.channelGroupEntity.getVerticalBlindPositions()
      val overallPosition = getGroupValues(positions, state.windowState.markers.isNotEmpty()) { it.position.toFloat() }
      val overallTilt = WindowGroupedValue.Similar(positions.maxOfOrNull { it.tilt }?.toFloat() ?: 0f)
      val markers = (if (overallPosition is WindowGroupedValue.Different) positions else emptyList())
        .map { ShadingBlindMarker(it.position.toFloat(), it.tilt.toFloat()) }

      updateGroup(state, group.groupDataEntity, group.onlineSummary) {
        it.copy(
          remoteId = group.groupDataEntity.remoteId,
          windowState = it.windowState.copy(
            position = if (group.groupDataEntity.isOnline()) overallPosition else WindowGroupedValue.Similar(25f),
            slatTilt = if (group.groupDataEntity.isOnline()) overallTilt else WindowGroupedValue.Similar(50f),
            markers = if (group.groupDataEntity.isOnline()) markers else emptyList(),
            positionTextFormat = positionTextFormat
          ),
          viewState = it.viewState.copy(
            positionUnknown = overallPosition is WindowGroupedValue.Invalid,
          ),
          lastPosition = overallPosition.value.toInt()
        )
      }
    }
  }

  override fun canShowMoveTime(state: VerticalBlindsViewModelState) =
    state.viewState.positionUnknown || state.windowState.slatTilt == null

  override fun updateTiltingDetails(
    tilt0Angle: Float,
    tilt100Angle: Float,
    tiltControlType: SuplaTiltControlType,
    tiltingTime: Int?,
    openingTime: Int?,
    closingTime: Int?
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

private fun ChannelGroupEntity.getVerticalBlindPositions(): List<ShadowingBlindGroupValue> =
  groupTotalValues.mapNotNull { it as? ShadowingBlindGroupValue }

data class VerticalBlindsViewModelState(
  override val tiltControlType: SuplaTiltControlType? = null,
  override val tiltingTime: Int? = null,
  override val openingTime: Int? = null,
  override val closingTime: Int? = null,
  override val lastPosition: Int? = null,

  override val remoteId: Int? = null,
  override val windowState: VerticalBlindWindowState = VerticalBlindWindowState(WindowGroupedValue.Similar(0f)),
  override val viewState: WindowViewState = WindowViewState(orientation = ShadingSystemOrientation.HORIZONTAL),
  override val moveStartTime: Long? = null,
  override val manualMoving: Boolean = false,
  override val showCalibrationDialog: Boolean = false,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : BaseBlindsViewModelState()
