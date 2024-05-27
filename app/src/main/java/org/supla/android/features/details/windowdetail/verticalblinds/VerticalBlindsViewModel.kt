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
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.remote.ChannelConfigType
import org.supla.android.data.source.remote.rollershutter.SuplaChannelFacadeBlindConfig
import org.supla.android.data.source.remote.rollershutter.SuplaTiltControlType
import org.supla.android.data.source.remote.shadingsystem.SuplaShadingSystemFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.events.ChannelConfigEventsManager
import org.supla.android.extensions.guardLet
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModel
import org.supla.android.features.details.windowdetail.base.BaseWindowViewModelState
import org.supla.android.features.details.windowdetail.base.data.WindowGroupedValue
import org.supla.android.features.details.windowdetail.base.data.facadeblinds.FacadeBlindWindowState
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindMarker
import org.supla.android.features.details.windowdetail.base.data.verticalblinds.VerticalBlindWindowState
import org.supla.android.features.details.windowdetail.base.ui.ShadingSystemAction
import org.supla.android.features.details.windowdetail.base.ui.WindowViewState
import org.supla.android.features.details.windowdetail.base.ui.windowview.ShadingSystemOrientation
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.AuthorizeUseCase
import org.supla.android.usecases.client.CallSuplaClientOperationUseCase
import org.supla.android.usecases.client.ExecuteShadingSystemActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.LoginUseCase
import org.supla.android.usecases.client.VALUE_IGNORE
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import org.supla.android.usecases.group.totalvalue.ShadowingBlindGroupValue
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class VerticalBlindsViewModel @Inject constructor(
  private val channelConfigEventsManager: ChannelConfigEventsManager,
  private val executeShadingSystemActionUseCase: ExecuteShadingSystemActionUseCase,
  private val suplaClientProvider: SuplaClientProvider,
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
) : BaseWindowViewModel<VerticalBlindsViewModelState>(
  executeShadingSystemActionUseCase,
  executeSimpleActionUseCase,
  callSuplaClientOperationUseCase,
  readChannelByRemoteIdUseCase,
  readChannelGroupByRemoteIdUseCase,
  getGroupOnlineSummaryUseCase,
  preferences,
  dateProvider,
  suplaClientProvider,
  profileRepository,
  loginUseCase,
  authorizeUseCase,
  VerticalBlindsViewModelState(),
  schedulers
) {

  override fun updatePosition(state: VerticalBlindsViewModelState, position: Float) =
    state.copy(windowState = state.windowState.copy(position = WindowGroupedValue.Similar(position)))

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

  override fun handleAction(action: ShadingSystemAction, remoteId: Int, itemType: ItemType) {
    when (action) {
      is ShadingSystemAction.TiltTo ->
        updateState {
          if (it.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            val markers = when {
              it.facadeBlindType == SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED ->
                emptyList()

              it.windowState.position is WindowGroupedValue.Different ->
                it.windowState.markers.map { marker -> VerticalBlindMarker(marker.position, action.tilt) }

              else -> emptyList()
            }
            val position = when {
              it.facadeBlindType == SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED -> WindowGroupedValue.Similar(100f)
              else -> it.windowState.position
            }
            it.copy(
              windowState = it.windowState.copy(position = position, slatTilt = WindowGroupedValue.Similar(action.tilt), markers = markers),
              viewState = it.viewState.copy(touchTime = null, positionUnknown = false),
              manualMoving = true
            )
          }
        }

      is ShadingSystemAction.TiltSetTo ->
        updateState {
          if (it.viewState.calibrating) {
            // During calibration the open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            executeShadingSystemActionUseCase.invoke(
              actionId = ActionId.SHUT_PARTIALLY,
              type = itemType.toSubjectType(),
              remoteId = remoteId,
              tilt = action.tilt
            ).runIt()
            it.copy(manualMoving = false)
          }
        }

      is ShadingSystemAction.MoveAndTiltTo ->
        updateState {
          if (it.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            val tilt = when {
              it.windowState.slatTilt == null -> null
              it.facadeBlindType == SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING -> limitTilt(action.tilt, action.position, it)
              it.facadeBlindType != SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED || action.position == 100f -> action.tilt
              else -> 0f
            }
            it.copy(
              windowState = it.windowState.copy(
                slatTilt = tilt?.let { WindowGroupedValue.Similar(tilt) },
                position = WindowGroupedValue.Similar(action.position),
                markers = emptyList()
              ),
              viewState = it.viewState.copy(
                touchTime = null,
                positionUnknown = false,
              ),
              manualMoving = true
            )
          }
        }

      is ShadingSystemAction.MoveAndTiltSetTo ->
        updateState {
          if (it.viewState.calibrating) {
            // During calibration the open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            val tilt = when {
              it.windowState.slatTilt == null -> VALUE_IGNORE
              it.facadeBlindType == SuplaTiltControlType.CHANGES_POSITION_WHILE_TILTING -> limitTilt(action.tilt, action.position, it)
              it.facadeBlindType != SuplaTiltControlType.TILTS_ONLY_WHEN_FULLY_CLOSED || action.position == 100f -> action.tilt
              else -> 0f
            }
            executeShadingSystemActionUseCase.invoke(
              ActionId.SHUT_PARTIALLY,
              itemType.toSubjectType(),
              remoteId,
              percentage = action.position,
              tilt = tilt
            ).runIt()
            it.copy(manualMoving = false)
          }
        }

      else -> super.handleAction(action, remoteId, itemType)
    }
  }

  fun observeConfig(remoteId: Int, itemType: ItemType) {
    if (itemType == ItemType.CHANNEL) {
      channelConfigEventsManager.observerConfig(remoteId).filter { it.config is SuplaChannelFacadeBlindConfig }
        .attachSilent()
        .subscribeBy(
          onNext = { handleConfig(it) },
          onError = defaultErrorHandler("loadChannelData")
        )
        .disposeBySelf()
    }
  }

  fun loadConfig(remoteId: Int, itemType: ItemType) {
    if (itemType == ItemType.CHANNEL) {
      suplaClientProvider.provide()?.getChannelConfig(remoteId, ChannelConfigType.DEFAULT)
    }
  }

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
      val overallTilt = getGroupValues(positions, state.windowState.markers.isNotEmpty()) { it.tilt.toFloat() }
      val markers = (if (overallPosition is WindowGroupedValue.Different) positions else emptyList())
        .map { VerticalBlindMarker(it.position.toFloat(), it.tilt.toFloat()) }

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

  private fun handleConfig(config: ChannelConfigEventsManager.ConfigEvent) {
    val (facadeConfig) = guardLet(config.config as? SuplaChannelFacadeBlindConfig) { return }
    val (tilt0, tilt100) =
      if (facadeConfig.tilt0Angle == facadeConfig.tilt100Angle) {
        listOf(FacadeBlindWindowState.DEFAULT_TILT_0_ANGLE, FacadeBlindWindowState.DEFAULT_TILT_100_ANGLE)
      } else {
        listOf(facadeConfig.tilt0Angle.toFloat(), facadeConfig.tilt100Angle.toFloat())
      }

    updateState {
      it.copy(
        windowState = it.windowState.copy(
          tilt0Angle = tilt0,
          tilt100Angle = tilt100
        ),
        facadeBlindType = facadeConfig.type,
        tiltingTime = facadeConfig.tiltingTimeMs,
        openingTime = facadeConfig.openingTimeMs,
        closingTime = facadeConfig.closingTimeMs
      )
    }
  }

  private fun limitTilt(tilt: Float, position: Float, state: VerticalBlindsViewModelState): Float {
    val (tiltingTime, openingTime, closingTime, lastPosition) =
      guardLet(state.tiltingTime, state.openingTime, state.closingTime, state.lastPosition) { return tilt }

    val time = if (position > lastPosition) closingTime else openingTime
    val positionTime = time.times(position).div(100f)

    if (positionTime < tiltingTime) {
      return min(tilt, 100f.times(positionTime.div(tiltingTime)))
    }
    if (positionTime > time - tiltingTime) {
      return max(tilt, 100f.minus(100f.times(time.minus(positionTime).div(tiltingTime))))
    }

    return tilt
  }
}

private fun ChannelGroupEntity.getVerticalBlindPositions(): List<ShadowingBlindGroupValue> =
  groupTotalValues.mapNotNull { it as? ShadowingBlindGroupValue }

data class VerticalBlindsViewModelState(
  val facadeBlindType: SuplaTiltControlType? = null,
  val tiltingTime: Int? = null,
  val openingTime: Int? = null,
  val closingTime: Int? = null,
  val lastPosition: Int? = null,

  override val remoteId: Int? = null,
  override val windowState: VerticalBlindWindowState = VerticalBlindWindowState(WindowGroupedValue.Similar(0f)),
  override val viewState: WindowViewState = WindowViewState(orientation = ShadingSystemOrientation.HORIZONTAL),
  override val moveStartTime: Long? = null,
  override val manualMoving: Boolean = false,
  override val showCalibrationDialog: Boolean = false,
  override val authorizationDialogState: AuthorizationDialogState? = null
) : BaseWindowViewModelState()
