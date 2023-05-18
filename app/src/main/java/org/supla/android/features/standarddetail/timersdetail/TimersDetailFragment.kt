package org.supla.android.features.standarddetail.timersdetail

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.R
import org.supla.android.core.ui.BaseFragment
import org.supla.android.core.ui.BaseViewModel
import org.supla.android.core.ui.theme.SuplaLightColors
import org.supla.android.databinding.FragmentTimersDetailBinding
import org.supla.android.lib.SuplaClientMsg

private const val ARG_REMOTE_ID = "ARG_REMOTE_ID"

@AndroidEntryPoint
class TimersDetailFragment : BaseFragment<TimersDetailViewState, TimersDetailViewEvent>(R.layout.fragment_timers_detail) {

  private val viewModel: TimersDetailViewModel by viewModels()
  private val binding by viewBinding(FragmentTimersDetailBinding::bind)

  private val remoteId: Int by lazy { arguments!!.getInt(ARG_REMOTE_ID) }
  private var timer: CountDownTimer? = null

  private var selectedSwitchPosition: Int = 0

  override fun getViewModel(): BaseViewModel<TimersDetailViewState, TimersDetailViewEvent> = viewModel

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.root.setBackgroundColor(SuplaLightColors.Background.toArgb())

    binding.detailsTimerHour.maxValue = 23
    binding.detailsTimerMinute.maxValue = 59
    binding.detailsTimerSecond.maxValue = 59

    binding.detailsTimerButton.buttonText = "Start"
    binding.detailsTimerButton.setOnClickListener {
      val time = binding.detailsTimerSecond.value + binding.detailsTimerMinute.value * 60 + binding.detailsTimerHour.value * 3600
      viewModel.startTimer(remoteId, selectedSwitchPosition == 0, time * 1000)
    }
    binding.detailsTimerActionSwitch.items = listOf("Turn on", "Turn off")
    binding.detailsTimerActionSwitch.selectedItemListener = { selectedSwitchPosition = it }
  }

  override fun onStart() {
    super.onStart()
    viewModel.loadData(remoteId)
  }

  override fun onStop() {
    timer?.cancel()
    super.onStop()
  }

  override fun handleEvents(event: TimersDetailViewEvent) {
  }

  override fun handleViewState(state: TimersDetailViewState) {
    binding.detailsTimerButton.disabled = state.isTimerOn

    if (state.isTimerOn) {
      setupTimer(state.leftTimeMillis!!)
    } else {
      timer?.cancel()
    }

    setupTimerValues(state.timerValues)
  }

  private fun setupTimerValues(timerValues: TimerValues?) {
    binding.detailsTimerHour.isEnabled = timerValues == null
    binding.detailsTimerMinute.isEnabled = timerValues == null
    binding.detailsTimerSecond.isEnabled = timerValues == null

    timerValues?.also {
      binding.detailsTimerHour.value = it.hours
      binding.detailsTimerMinute.value = it.minutes
      binding.detailsTimerSecond.value = it.seconds
    }
  }

  private fun setupTimer(leftTime: Long) {
    timer?.cancel()

    timer = object : CountDownTimer(leftTime, 100) {
      override fun onTick(millisUntilFinished: Long) {
        setupTimerValues(TimerValues.of(millisUntilFinished / 1000))
      }

      override fun onFinish() {
        timer = null
      }
    }
    timer?.start()
  }

  override fun onSuplaMessage(message: SuplaClientMsg) {
    super.onSuplaMessage(message)
    when (message.type) {
      SuplaClientMsg.onDataChanged -> if (message.channelId == remoteId) {
        viewModel.loadData(remoteId)
      }
    }
  }

  companion object {
    fun bundle(remoteId: Int) = bundleOf(
      ARG_REMOTE_ID to remoteId,
    )
  }
}