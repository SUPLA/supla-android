package org.supla.android.features.standarddetail.timersdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.ViewEvent
import org.supla.android.core.ui.ViewState
import org.supla.android.db.Channel
import org.supla.android.lib.SuplaTimerState
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.channel.ReadChannelByRemoteIdUseCase
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimersDetailViewModel @Inject constructor(
  private val readChannelByRemoteIdUseCase: ReadChannelByRemoteIdUseCase,
  private val suplaClientProvider: SuplaClientProvider,
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

  private fun handleChannel(channel: Channel) {
    val timerState = channel.extendedValue?.extendedValue?.TimerStateValue
    val timerValues = createTimerValues(timerState)
    updateState { state ->
      state.copy(
        isTimerOn = timerState != null && timerValues != null,
        timerValues = timerValues,
        leftTimeMillis = getLeftTime(timerState)
      )
    }
  }

  private fun createTimerValues(timerState: SuplaTimerState?): TimerValues? {
    val leftTime = getLeftTime(timerState) ?: return null
    return TimerValues.of(leftTime / 1000)
  }

  private fun getLeftTime(timerState: SuplaTimerState?): Long? {
    val endsAt = timerState?.countdownEndsAt ?: return null
    val now = Date()
    if (now.before(endsAt)) {
      return (endsAt.time - now.time)
    }

    return null
  }

  fun startTimer(remoteId: Int, turnOn: Boolean, duration: Int) {
    Completable.fromRunnable {
      suplaClientProvider.provide()?.run {
        timerArm(remoteId, turnOn, duration)
      }
    }
      .attach()
      .subscribeBy()
      .disposeBySelf()
  }
}

sealed class TimersDetailViewEvent : ViewEvent {
}

data class TimersDetailViewState(
  val isTimerOn: Boolean = false,
  val leftTimeMillis: Long? = null,
  val timerValues: TimerValues? = null,
) : ViewState()

data class TimerValues(val hours: Int, val minutes: Int, val seconds: Int) {
  companion object {
    fun of(time: Long) = TimerValues(
      seconds = (time % 60).toInt(),
      minutes = ((time / 60) % 60).toInt(),
      hours = (time / 3600).toInt()
    )
  }
}