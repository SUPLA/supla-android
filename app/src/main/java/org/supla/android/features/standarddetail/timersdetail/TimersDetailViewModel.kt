package org.supla.android.features.standarddetail.timersdetail
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
import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.Channel
import org.supla.android.extensions.TAG
import org.supla.android.extensions.asDate
import org.supla.android.extensions.getTimerStateValue
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.client.StartTimerUseCase
import org.supla.android.usecases.client.StartTimerUseCase.InvalidTimeException
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimersDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val executeSimpleActionUseCase: ExecuteSimpleActionUseCase,
  private val startTimerUseCase: StartTimerUseCase,
  private val dateProvider: DateProvider,
  schedulers: SuplaSchedulers
) : BaseViewModel<TimersDetailViewState, TimersDetailViewEvent>(TimersDetailViewState(), schedulers) {

  fun loadData(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .attach()
      .subscribeBy(
        onSuccess = { handleChannel(it) }
      )
      .disposeBySelf()
  }

  fun startTimer(remoteId: Int, turnOn: Boolean, durationInSecs: Int) {
    startTimerUseCase(remoteId, turnOn, durationInSecs)
      .attach()
      .subscribeBy(
        onError = {
          if (it is InvalidTimeException) {
            sendEvent(TimersDetailViewEvent.ShowInvalidTimeToast)
          }
        }
      )
      .disposeBySelf()
  }

  fun stopTimer(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .flatMapCompletable { abortCompletable(remoteId, it.value.hiValue()) }
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }

  fun cancelTimer(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .flatMapCompletable { abortCompletable(remoteId, it.value.hiValue().not()) }
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }

  fun startEditMode() {
    updateState { it.copy(editMode = true) }
  }

  fun cancelEditMode() {
    updateState { it.copy(editMode = false) }
  }

  fun updateAction(action: TimerTargetAction) {
    updateState { it.copy(targetAction = action) }
  }

  fun calculateProgressViewData(startTime: Date, endTime: Date): ProgressViewData {
    val leftTime = calculateLeftTime(endTime)
    val wholeTime = endTime.time.minus(startTime.time)

    return ProgressViewData(
      progress = 1 - leftTime.div(wholeTime.toFloat()),
      leftTimeValues = TimeValues.of(leftTime.div(1000).plus(1))
    )
  }

  private fun calculateLeftTime(endTime: Date) = endTime.time.minus(dateProvider.currentTimestamp())

  private fun abortCompletable(remoteId: Int, turnOn: Boolean): Completable {
    val actionId = if (turnOn) ActionId.TURN_ON else ActionId.TURN_OFF
    return executeSimpleActionUseCase(actionId, SubjectType.CHANNEL, remoteId)
  }

  private fun handleChannel(channel: Channel) {
    val currentTime = dateProvider.currentDate()
    val timerState = channel.getTimerStateValue()
    val isTimerOn = timerState != null && timerState.countdownEndsAt != null && timerState.countdownEndsAt.after(currentTime)
    val startDate = channel.extendedValue?.timerStartTimestamp?.asDate()
    Trace.d(TAG, "Handling channel update $timerState")

    updateState { state ->
      var editMode = state.editMode
      if (state.editMode && isTimerOn) {
        // To avoid screen blinking, edit mode is canceled when new timer values will come
        editMode = false
      }
      val targetAction = state.targetAction
        ?: if (state.editMode && state.channel?.value?.hiValue() == false) {
          TimerTargetAction.TURN_OFF
        } else {
          TimerTargetAction.TURN_ON
        }

      state.copy(
        channel = channel,
        timerData = if (isTimerOn) {
          TimerProgressData(
            endTime = timerState!!.countdownEndsAt,
            startTime = startDate ?: currentTime,
            indeterminate = startDate == null,
            timerValue = if (timerState.expectedHiValue()) TimerValue.OFF else TimerValue.ON
          )
        } else {
          null
        },
        editMode = editMode,
        targetAction = targetAction
      )
    }
  }
}

sealed class TimersDetailViewEvent : ViewEvent {
  object ShowInvalidTimeToast : TimersDetailViewEvent()
}

data class TimersDetailViewState(
  val timerData: TimerProgressData? = null,
  val channel: Channel? = null,
  val editMode: Boolean = false,
  val targetAction: TimerTargetAction? = null
) : ViewState()

data class TimerProgressData(
  val startTime: Date,
  val endTime: Date,
  val indeterminate: Boolean,
  val timerValue: TimerValue
)

data class ProgressViewData(
  val progress: Float,
  val leftTimeValues: TimeValues
)

enum class TimerValue {
  ON, OFF
}

data class TimeValues(val hours: Int, val minutes: Int, val seconds: Int) {
  companion object {
    fun of(time: Long) = TimeValues(
      seconds = (time % 60).toInt(),
      minutes = ((time / 60) % 60).toInt(),
      hours = (time / 3600).toInt()
    )
  }
}
