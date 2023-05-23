package org.supla.android.features.standarddetail.timersdetail

import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.Channel
import org.supla.android.extensions.TAG
import org.supla.android.extensions.asDate
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.ActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimersDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val suplaClientProvider: SuplaClientProvider,
  private val vibrationHelper: VibrationHelper,
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
    startCompletable(remoteId, turnOn, durationInSecs)
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
      .subscribeBy(
        onError = {
          if (it is InvalidTimeException) {
            sendEvent(TimersDetailViewEvent.ShowInvalidTimeToast)
          }
        }
      )
      .disposeBySelf()
  }

  fun cancelTimer(remoteId: Int) {
    readChannelByRemoteIdUseCase(remoteId)
      .flatMapCompletable { abortCompletable(remoteId, it.value.hiValue().not()) }
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

  fun startEditMode() {
    updateState { it.copy(editMode = true) }
  }

  fun cancelEditMode() {
    updateState { it.copy(editMode = false) }
  }

  fun calculateProgressViewData(startTime: Date, endTime: Date): ProgressViewData {
    val leftTime = calculateLeftTime(endTime)
    val wholeTime = endTime.time.minus(startTime.time)

    return ProgressViewData(
      progress = 1 - leftTime.div(wholeTime.toFloat()),
      leftTimeValues = TimeValues.of(leftTime.div(1000).plus(1))
    )
  }

  private fun calculateLeftTime(endTime: Date) = endTime.time.minus(Date().time)

  private fun startCompletable(remoteId: Int, turnOn: Boolean, durationInSecs: Int) = Completable.fromRunnable {
    if (durationInSecs <= 0) {
      throw InvalidTimeException()
    }
    suplaClientProvider.provide()?.run {
      vibrationHelper.vibrate()
      timerArm(remoteId, turnOn, durationInSecs.times(1000))
    }
  }

  private fun abortCompletable(remoteId: Int, turnOn: Boolean) = Completable.fromRunnable {
    suplaClientProvider.provide()?.run {
      vibrationHelper.vibrate()
      if (turnOn) {
        executeAction(ActionParameters(ActionId.TURN_ON, SubjectType.CHANNEL, remoteId))
      } else {
        executeAction(ActionParameters(ActionId.TURN_OFF, SubjectType.CHANNEL, remoteId))
      }
    }
  }

  private fun handleChannel(channel: Channel) {
    val currentTime = Date()
    val timerState = channel.extendedValue?.extendedValue?.TimerStateValue
    val isTimerOn = timerState != null && timerState.countdownEndsAt != null && timerState.countdownEndsAt.after(currentTime)
    val startDate = channel.extendedValue?.timerStartTimestamp?.asDate()
    Trace.d(TAG, "Handling channel update $timerState")

    updateState { state ->
      var editMode = state.editMode
      if (state.editMode && isTimerOn) {
        // To avoid screen blinking, edit mode is canceled when new timer values will come
        editMode = false
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
        editMode = editMode
      )
    }
  }

  private class InvalidTimeException : Exception()
}

sealed class TimersDetailViewEvent : ViewEvent {
  object ShowInvalidTimeToast : TimersDetailViewEvent()
}

data class TimersDetailViewState(
  val timerData: TimerProgressData? = null,
  val channel: Channel? = null,
  val editMode: Boolean = false,
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
