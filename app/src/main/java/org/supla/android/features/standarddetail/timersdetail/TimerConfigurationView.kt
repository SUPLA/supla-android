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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.PluralsRes
import androidx.constraintlayout.widget.ConstraintLayout
import org.supla.android.R
import org.supla.android.databinding.ViewTimerConfigurationBinding
import org.supla.android.extensions.visibleIf

private const val TIME_FORMAT = "%02d:%02d:%02d"

class TimerConfigurationView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

  private val binding: ViewTimerConfigurationBinding =
    ViewTimerConfigurationBinding.inflate(LayoutInflater.from(context), this)

  var onStartClickListener: (timeInSeconds: Int, action: TimerTargetAction) -> Unit = { _, _ -> }
  var onEditCancelClickListener: () -> Unit = { }

  var editMode = false
    set(value) {
      field = value
      updateView()
    }
  var deviceStateOn = false

  private val timeString: String
    get() = String.format(
      TIME_FORMAT,
      binding.detailsTimerHour.value,
      binding.detailsTimerMinute.value,
      binding.detailsTimerSecond.value
    )

  var timeInSeconds: Int
    get() = binding.detailsTimerSecond.value
      .plus(binding.detailsTimerMinute.value.times(60))
      .plus(binding.detailsTimerHour.value.times(3600))
    set(time) {
      binding.detailsTimerSecond.value = (time % 60)
      binding.detailsTimerMinute.value = ((time / 60) % 60)
      binding.detailsTimerHour.value = (time / 3600)
      updateInfoText()
    }

  init {
    setupView()
  }

  private fun setupView() {
    binding.detailsTimerHour.maxValue = 23
    binding.detailsTimerHour.displayedValues = displayValues(R.plurals.hour_pattern, 24)
    binding.detailsTimerHour.setOnValueChangedListener { _, _, _ -> updateInfoText() }
    binding.detailsTimerMinute.maxValue = 59
    binding.detailsTimerMinute.displayedValues = displayValues(R.plurals.minute_pattern, 60)
    binding.detailsTimerMinute.setOnValueChangedListener { _, _, _ -> updateInfoText() }
    binding.detailsTimerSecond.maxValue = 59
    binding.detailsTimerSecond.displayedValues = displayValues(R.plurals.second_pattern, 60)
    binding.detailsTimerSecond.setOnValueChangedListener { _, _, _ -> updateInfoText() }
    binding.detailsTimerActionSwitch.items = listOf(
      context.getString(R.string.details_timer_turn_on_for),
      context.getString(R.string.details_timer_turn_off_for)
    )
    binding.detailsTimerActionSwitch.selectedItemListener = { position ->
      TimerTargetAction.from(position)?.let { updateInfoText(it) }
    }
    binding.detailsTimerStartButton.setOnClickListener { _ ->
      TimerTargetAction.from(binding.detailsTimerActionSwitch.activeItem)?.let {
        onStartClickListener(timeInSeconds, it)
      }
    }
    binding.detailsTimerEditCancelButton.setOnClickListener { onEditCancelClickListener() }
    updateInfoText(TimerTargetAction.TURN_ON)

    updateView()
  }

  private fun updateView() {
    updateHeaderText()
    updatePrimaryButtonText()

    binding.detailsTimerDivider.visibleIf(editMode.not())
    binding.detailsTimerInfoText.visibleIf(editMode.not())
    binding.detailsTimerActionSwitch.visibleIf(editMode.not())
    binding.detailsTimerEditCancelButton.visibleIf(editMode)
  }

  private fun displayValues(@PluralsRes pattern: Int, count: Int): Array<String> {
    val result = Array(count) { "" }

    for (i in 0 until count) {
      result[i] = context.resources.getQuantityString(pattern, i, i)
    }

    return result
  }

  private fun updateInfoText() {
    TimerTargetAction.from(binding.detailsTimerActionSwitch.activeItem)?.let {
      updateInfoText(it)
    }
  }

  private fun updateInfoText(action: TimerTargetAction) {
    when (action) {
      TimerTargetAction.TURN_OFF -> {
        binding.detailsTimerInfoText.text = context.getString(
          R.string.details_timer_info,
          context.resources.getQuantityString(R.plurals.details_timer_info_off, 1),
          timeString,
          context.resources.getQuantityString(R.plurals.details_timer_info_on, 2)
        )
      }
      TimerTargetAction.TURN_ON -> {
        binding.detailsTimerInfoText.text = context.getString(
          R.string.details_timer_info,
          context.resources.getQuantityString(R.plurals.details_timer_info_on, 1),
          timeString,
          context.resources.getQuantityString(R.plurals.details_timer_info_off, 2)
        )
      }
    }
  }

  private fun updateHeaderText() {
    when {
      editMode.not() -> binding.detailsTimerHeaderText.setText(R.string.details_timer_text)
      deviceStateOn -> binding.detailsTimerHeaderText.text = context.getString(
        R.string.details_timer_edit_header,
        context.resources.getQuantityString(R.plurals.details_timer_info_on, 1)
      )
      else -> binding.detailsTimerHeaderText.text = context.getString(
        R.string.details_timer_edit_header,
        context.resources.getQuantityString(R.plurals.details_timer_info_off, 1)
      )
    }
  }

  private fun updatePrimaryButtonText() {
    binding.detailsTimerStartButton.setText(if (editMode) R.string.details_timer_save else R.string.details_timer_start)
  }
}

enum class TimerTargetAction(val id: Int) {
  TURN_ON(0), TURN_OFF(1);

  companion object {
    fun from(id: Int): TimerTargetAction? {
      for (action in TimerTargetAction.values()) {
        if (action.id == id) {
          return action
        }
      }

      return null
    }
  }
}
