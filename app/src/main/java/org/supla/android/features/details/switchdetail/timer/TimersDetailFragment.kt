package org.supla.android.features.details.switchdetail.timer
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

import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.Trace
import org.supla.android.core.storage.RuntimeStateHolder
import org.supla.android.core.ui.BaseFragment
import org.supla.android.databinding.FragmentTimersDetailBinding
import org.supla.android.extensions.TAG
import org.supla.android.extensions.getChannelIconUseCase
import org.supla.android.extensions.visibleIf
import org.supla.android.images.ImageCache
import org.supla.android.lib.SuplaClientMsg
import java.util.*
import javax.inject.Inject

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class TimersDetailFragment : BaseFragment<TimersDetailViewState, TimersDetailViewEvent>(R.layout.fragment_timers_detail) {

  @Inject
  internal lateinit var stateHolder: RuntimeStateHolder

  override val viewModel: TimersDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentTimersDetailBinding::bind)

  private val remoteId: Int by lazy { requireArguments().getInt(ARG_REMOTE_ID) }
  private var timer: CountDownTimer? = null
  private var leftTimeInSecs: Int = 0

  // Not always timer.cancel() stops timer, this flag is to handle this problem
  private var timerActive = false

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.detailsTimerConfiguration.onStartClickListener = { timeInSeconds, action ->
      stateHolder.setLastTimerValue(remoteId, timeInSeconds)
      viewModel.startTimer(remoteId, action == TimerTargetAction.TURN_ON, timeInSeconds)
    }
    binding.detailsTimerConfiguration.onTimeChangedListener = { stateHolder.setLastTimerValue(remoteId, it) }
    binding.detailsTimerConfiguration.onEditCancelClickListener = { viewModel.cancelEditMode() }
    binding.detailsTimerConfiguration.onActionChangeListener = { viewModel.updateAction(it) }
    binding.detailsTimerConfiguration.timeInSeconds = stateHolder.getLastTimerValue(remoteId)
    binding.detailsTimerStopButton.setOnClickListener { viewModel.stopTimer(remoteId) }
    binding.detailsTimerCancelButton.setOnClickListener { viewModel.cancelTimer(remoteId) }
    binding.detailsTimerEditTime.setOnClickListener {
      binding.detailsTimerConfiguration.timeInSeconds = leftTimeInSecs
      viewModel.startEditMode()
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.loadData(remoteId)
  }

  override fun onPause() {
    timer?.cancel()
    super.onPause()
  }

  override fun handleEvents(event: TimersDetailViewEvent) {
    when (event) {
      TimersDetailViewEvent.ShowInvalidTimeToast -> Toast.makeText(context, R.string.details_timer_wrong_time, Toast.LENGTH_LONG).show()
    }
  }

  override fun handleViewState(state: TimersDetailViewState) {
    state.channel?.let {
      binding.detailsTimerConfiguration.deviceStateOn = it.value.hiValue()
      binding.detailsTimerConfiguration.isEnabled = it.onLine
      binding.detailsTimerState.deviceStateIcon.setImageBitmap(
        ImageCache.getBitmap(
          requireContext(),
          context?.getChannelIconUseCase?.invoke(it)
        )
      )
      binding.detailsTimerState.deviceStateValue.text = when {
        it.onLine.not() -> getString(R.string.offline)
        it.value.hiValue() -> getString(R.string.details_timer_device_on)
        else -> getString(R.string.details_timer_device_off)
      }
    }

    val showTimer = state.timerData != null
    binding.detailsTimerState.deviceStateLabel.text = if (showTimer) {
      val formatString = getString(R.string.hour_string_format)
      getString(R.string.details_timer_state_label_for_timer, DateFormat.format(formatString, state.timerData!!.endTime))
    } else {
      getString(R.string.details_timer_state_label)
    }

    state.targetAction?.let { binding.detailsTimerConfiguration.setTargetAction(it) }
    binding.detailsTimerConfiguration.visibleIf(showTimer.not() || state.editMode)
    binding.detailsTimerConfiguration.editMode = state.editMode
    binding.detailsTimerProgress.visibleIf(showTimer)

    handleTimerState(state)
  }

  private fun handleTimerState(state: TimersDetailViewState) {
    Trace.d(TAG, "Handling timer state: ${state.timerData}")
    if (state.timerData != null) {
      setupTimer(state.timerData.startTime, state.timerData.endTime)
      setTimerValues(state.timerData.startTime, state.timerData.endTime)
      binding.detailsTimerProgress.indeterminate = state.timerData.indeterminate

      val formatString = getString(R.string.hour_string_format)
      binding.detailsTimerProgressEndHour.text = getString(
        R.string.details_timer_end_hour,
        DateFormat.format(formatString, state.timerData.endTime)
      )
      binding.detailsTimerStopButton.text = getString(
        R.string.details_timer_leave_it,
        resources.getQuantityString(
          if (state.timerData.timerValue == TimerValue.ON) R.plurals.details_timer_info_on else R.plurals.details_timer_info_off,
          1
        )
      )
      binding.detailsTimerCancelButton.text = getString(
        R.string.details_timer_cancel_and,
        getString(
          if (state.timerData.timerValue == TimerValue.ON) {
            R.string.details_timer_cancel_turn_off
          } else {
            R.string.details_timer_cancel_turn_on
          }
        )
      )
    } else {
      timer?.cancel()
      timer = null
    }
  }

  private fun setTimerValues(startTime: Date, endTime: Date) {
    val data = viewModel.calculateProgressViewData(startTime, endTime)
    binding.detailsTimerProgress.progress = data.progress
    binding.detailsTimerProgressTime.text = getString(
      R.string.details_timer_format,
      data.leftTimeValues.hours,
      data.leftTimeValues.minutes,
      data.leftTimeValues.seconds
    )
  }

  private fun setupTimer(startTime: Date, endTime: Date) {
    timer?.cancel()

    val leftTime = endTime.time - Date().time
    if (leftTime > 0) {
      timerActive = true
      timer = object : CountDownTimer(leftTime, 100) {
        override fun onTick(millisUntilFinished: Long) {
          leftTimeInSecs = millisUntilFinished.div(1000).toInt()
          if (timerActive) {
            setTimerValues(startTime, endTime)
          }
        }

        override fun onFinish() {
          timer?.cancel()
          timer = null
          viewModel.loadData(remoteId)
        }
      }.start()
    }
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    super.onSuplaMessage(message)
    when (message.type) {
      SuplaClientMsg.onDataChanged -> if (message.channelId == remoteId && (message.isTimerValue || !message.isExtendedValue)) {
        Trace.i(TAG, "Detail got data changed event")
        timer?.cancel()
        timerActive = false
        viewModel.loadData(remoteId)
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(
      ARG_REMOTE_ID to remoteId
    )
  }
}
