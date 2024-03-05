package org.supla.android.features.details.blindsdetail.general
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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.networking.suplaclient.SuplaClientMessageHandlerWrapper
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.ViewEvent
import org.supla.android.data.model.general.ChannelIssueItem
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import org.supla.android.data.source.local.entity.complex.ChannelGroupDataEntity
import org.supla.android.data.source.local.entity.custom.GroupOnlineSummary
import org.supla.android.data.source.remote.rollershutter.SuplaRollerShutterFlag
import org.supla.android.data.source.runtime.ItemType
import org.supla.android.extensions.guardLet
import org.supla.android.extensions.ifLet
import org.supla.android.features.details.blindsdetail.ui.BlindRollerState
import org.supla.android.lib.actions.ActionId
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.ui.dialogs.AuthorizationDialogState
import org.supla.android.ui.dialogs.authorize.AuthorizationModelState
import org.supla.android.ui.dialogs.authorize.BaseAuthorizationViewModel
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.CallConfigCommandUseCase
import org.supla.android.usecases.client.ExecuteBlindsActionUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.SuplaConfigCommand
import org.supla.android.usecases.group.GetGroupOnlineSummaryUseCase
import org.supla.android.usecases.group.ReadChannelGroupByRemoteIdUseCase
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class BlindGeneralViewModel @Inject constructor(
  private val executeBlindsActionUseCase: ExecuteBlindsActionUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val callConfigCommandUseCase: CallConfigCommandUseCase,
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val readChannelGroupByRemoteIdUseCase: ReadChannelGroupByRemoteIdUseCase,
  private val getGroupOnlineSummaryUseCase: GetGroupOnlineSummaryUseCase,
  private val preferences: Preferences,
  private val dateProvider: DateProvider,
  suplaClientProvider: SuplaClientProvider,
  profileRepository: RoomProfileRepository,
  suplaClientMessageHandlerWrapper: SuplaClientMessageHandlerWrapper,
  schedulers: SuplaSchedulers
) : BaseAuthorizationViewModel<BlindsGeneralModelState, BlindsGeneralViewEvent>(
  suplaClientProvider,
  profileRepository,
  suplaClientMessageHandlerWrapper,
  BlindsGeneralModelState(),
  schedulers
) {

  fun loadData(remoteId: Int, itemType: ItemType) {
    when (itemType) {
      ItemType.CHANNEL -> loadChannelData(remoteId)
      ItemType.GROUP -> loadGroupData(remoteId)
    }
  }

  fun handleAction(action: BlindsAction, remoteId: Int, itemType: ItemType) {
    when (action) {
      is BlindsAction.Open -> {
        updateState { it.copy(moveStartTime = null, viewState = it.viewState.copy(touchTime = null)) }
        executeSimpleActionUseCase.invoke(ActionId.REVEAL, itemType.toSubjectType(), remoteId).runIt()
      }

      is BlindsAction.Close -> {
        updateState { it.copy(moveStartTime = null, viewState = it.viewState.copy(touchTime = null)) }
        executeSimpleActionUseCase.invoke(ActionId.SHUT, itemType.toSubjectType(), remoteId).runIt()
      }

      is BlindsAction.MoveUp -> {
        updateState { it.updateMoveStartTime(dateProvider) }
        executeSimpleActionUseCase.invoke(ActionId.REVEAL, itemType.toSubjectType(), remoteId).runIt()
      }

      is BlindsAction.MoveDown -> {
        updateState { it.updateMoveStartTime(dateProvider) }
        executeSimpleActionUseCase.invoke(ActionId.SHUT, itemType.toSubjectType(), remoteId).runIt()
      }

      is BlindsAction.Stop -> {
        updateState { it.calculateMoveTime(dateProvider) }
        executeSimpleActionUseCase.invoke(ActionId.STOP, itemType.toSubjectType(), remoteId).runIt()
      }

      BlindsAction.Calibrate -> updateState { it.copy(showCalibrationDialog = true) }
      is BlindsAction.OpenAt -> {
        updateState {
          if (it.viewState.calibrating) {
            // When calibration open/close time is not known so it's not possible to open window at expected position
            it
          } else {
            executeBlindsActionUseCase.invoke(ActionId.SHUT_PARTIALLY, itemType.toSubjectType(), remoteId, action.position).runIt()
            it.copy(
              moveStartTime = null,
              viewState = it.viewState.copy(touchTime = null)
            )
          }
        }
      }

      is BlindsAction.MoveTo -> updateState {
        if (it.viewState.calibrating) {
          // When calibration open/close time is not known so it's not possible to open window at expected position
          it
        } else {
          it.copy(
            rollerState = it.rollerState.copy(position = action.position),
            viewState = it.viewState.copy(touchTime = null, positionUnknown = false)
          )
        }
      }
    }
  }

  fun startCalibration() {
    updateState { it.copy(showCalibrationDialog = false) }
    showAuthorizationDialog()
  }

  fun cancelCalibration() {
    updateState { it.copy(showCalibrationDialog = false) }
  }

  override fun updateDialogState(updater: (AuthorizationDialogState?) -> AuthorizationDialogState?) {
    updateState { it.copy(authorizationDialogState = updater(it.authorizationDialogState)) }
  }

  override fun onAuthorized() {
    hideAuthorizationDialog()

    val (remoteId) = guardLet(currentState().remoteId) { return }
    callConfigCommandUseCase(remoteId, ItemType.CHANNEL, SuplaConfigCommand.RECALIBRATE)
      .attachSilent()
      .subscribeBy(
        onError = defaultErrorHandler("onAuthorized")
      )
      .disposeBySelf()
  }

  private fun loadGroupData(remoteId: Int) {
    readChannelGroupByRemoteIdUseCase(remoteId)
      .flatMap { groupData -> getGroupOnlineSummaryUseCase.invoke(groupData.id!!).map { GroupData(groupData, it) } }
      .attach()
      .subscribeBy(
        onSuccess = { handleGroup(it) },
        onError = defaultErrorHandler("loadGroupData")
      )
      .disposeBySelf()
  }

  private fun loadChannelData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = { handleChannel(it) },
        onError = defaultErrorHandler("loadChannelData")
      )
      .disposeBySelf()
  }

  private fun handleChannel(channel: ChannelDataEntity) {
    updateState {
      val value = channel.channelValueEntity.asRollerShutterValue()
      val position = if (value.hasValidPosition()) value.position else 0
      val showOpening = preferences.isShowOpeningPercent

      it.copy(
        remoteId = channel.remoteId,
        rollerState = BlindRollerState(
          position = if (value.online) position.toFloat() else 25f,
          bottomPosition = value.bottomPosition.toFloat()
        ),
        viewState = it.viewState.copy(
          issues = createIssues(value.flags),
          enabled = value.online,
          showClosingPercentage = showOpening.not(),
          positionUnknown = value.hasValidPosition().not(),
          calibrating = value.flags.contains(SuplaRollerShutterFlag.CALIBRATION_IN_PROGRESS),
          calibrationPossible = true,
          positionText = String.format("%d%%", if (showOpening) 100 - position else position)
        ),
      )
    }
  }

  private fun handleGroup(group: GroupData) {
    updateState {
      val positions = group.groupDataEntity.channelGroupEntity.getRollerShutterPositions()
      val overallPosition = getGroupPercentage(positions)
      val showOpening = preferences.isShowOpeningPercent

      it.copy(
        remoteId = group.groupDataEntity.remoteId,
        rollerState = BlindRollerState(
          position = overallPosition.position,
          markers = if (overallPosition is GroupPercentage.Different) positions else emptyList(),
        ),
        viewState = it.viewState.copy(
          enabled = group.groupDataEntity.isOnline(),
          showClosingPercentage = showOpening.not(),
          positionUnknown = overallPosition is GroupPercentage.Invalid,
          calibrating = false,
          calibrationPossible = false,
          isGroup = true,
          onlineStatusString = "${group.onlineSummary.onlineCount}/${group.onlineSummary.count}",
          positionText = overallPosition.toString(showOpening)
        ),
      )
    }
  }

  private fun createIssues(flags: List<SuplaRollerShutterFlag>) =
    flags.filter { it.isIssueFlag() }
      .map { ChannelIssueItem(it.getIssueIconType()!!, it.getIssueMessage()!!) }

  private fun Completable.runIt() {
    this.attachSilent()
      .subscribe()
      .disposeBySelf()
  }

  private fun getGroupPercentage(positions: List<Float>): GroupPercentage {
    var percentage: Float? = null
    var minPercentage: Float? = null
    var maxPercentage: Float? = null

    positions.forEach {
      if (minPercentage == null || minPercentage!! > it) {
        minPercentage = it
      }
      if (maxPercentage == null || maxPercentage!! < it) {
        maxPercentage = it
      }

      if (percentage == null) {
        percentage = it
      }
    }

    return if (percentage == null || percentage == -1f) {
      GroupPercentage.Invalid
    } else {
      ifLet(minPercentage, maxPercentage) { (min, max) ->
        if (abs(min - max) > 5) {
          return GroupPercentage.Different(min, max)
        }
      }

      GroupPercentage.Similar(percentage!!)
    }
  }

  private sealed class GroupPercentage(val position: Float) {

    open fun toString(showOpening: Boolean): String = String.format("%.0f%%", if (showOpening) 100f - position else position)

    class Different(private val min: Float, private val max: Float) : GroupPercentage(0f) {
      override fun toString(showOpening: Boolean): String = String.format(
        "%.0f%% - %.0f%%",
        if (showOpening) 100f - min else min,
        if (showOpening) 100f - max else max
      )
    }

    object Invalid : GroupPercentage(0f)
    class Similar(position: Float) : GroupPercentage(position)
  }

  private data class GroupData(
    val groupDataEntity: ChannelGroupDataEntity,
    val onlineSummary: GroupOnlineSummary
  )
}

sealed class BlindsGeneralViewEvent : ViewEvent

data class BlindsGeneralModelState(
  val remoteId: Int? = null,
  val rollerState: BlindRollerState = BlindRollerState(0f, 100f),
  val viewState: BlindsGeneralViewState = BlindsGeneralViewState(),

  val moveStartTime: Long? = null,

  val showCalibrationDialog: Boolean = false,

  // Authorization
  override val authorizationDialogState: AuthorizationDialogState? = null
) : AuthorizationModelState() {

  fun updateMoveStartTime(dateProvider: DateProvider): BlindsGeneralModelState {
    if (viewState.positionUnknown) {
      return copy(
        moveStartTime = dateProvider.currentTimestamp(),
        viewState = viewState.copy(touchTime = null),
      )
    }

    return this
  }

  fun calculateMoveTime(dateProvider: DateProvider): BlindsGeneralModelState {
    if (viewState.positionUnknown) {
      val (startTime) = guardLet(moveStartTime) { return this }
      return copy(
        moveStartTime = null,
        viewState = viewState.copy(touchTime = dateProvider.currentTimestamp().minus(startTime).div(1000f))
      )
    }

    return this
  }
}

private fun ChannelGroupEntity.getRollerShutterPositions(): List<Float> {
  totalValue?.split("|")?.let { items ->
    return mutableListOf<Float>().apply {
      items.forEach {
        val values = it.split(":")
        if (values.count() == 2) {
          val (position, sensor) = guardLet(values[0].toIntOrNull(), values[1].toIntOrNull()) { return@forEach }

          if (position < 100 && sensor == 1) {
            add(100f)
          } else {
            add(position.toFloat())
          }
        }
      }
    }
  }

  return emptyList()
}
