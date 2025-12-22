package org.supla.android.features.details.rgbanddimmer.common.dimmer
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

import org.supla.android.R
import org.supla.android.data.ValuesFormatter
import org.supla.android.features.details.rgbanddimmer.common.SavedColor
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.core.shared.data.model.lists.ChannelIssueItem

data class DimmerDetailViewState(
  val deviceStateData: DeviceStateData? = null,
  val channelIssues: List<ChannelIssueItem>? = null,
  val offButtonState: SwitchButtonState? = null,
  val onButtonState: SwitchButtonState? = null,
  val savedColors: List<SavedColor> = emptyList(),
  val value: DimmerValue = DimmerValue.Empty,
  val selectorType: DimmerSelectorType = DimmerSelectorType.LINEAR,
  val offline: Boolean = false,
  val loading: Boolean = false
)

sealed interface DimmerValue {
  val brightness: Int?
  val cct: Int?
  val brightnessMarkers: List<Int>
  val brightnessString: String
  val cctMarkers: List<Int>

  data object Empty : DimmerValue {
    override val brightness: Int? = null
    override val cct: Int? = null
    override val brightnessMarkers: List<Int> = emptyList()
    override val brightnessString: String = "?"
    override val cctMarkers: List<Int> = emptyList()
  }

  data class Single(
    override val brightness: Int,
    override val cct: Int = 0
  ) : DimmerValue {
    override val brightnessMarkers: List<Int> = emptyList()
    override val brightnessString: String = ValuesFormatter.getPercentageString(brightness)
    override val cctMarkers: List<Int> = emptyList()
  }

  data class Multiple(val brightnesses: List<Int>, val ccts: List<Int> = emptyList()) : DimmerValue {
    override val brightness: Int? = if (brightnesses.size == 1) brightnesses.first() else null
    override val cct: Int? = if (ccts.size == 1) ccts.first() else null
    override val brightnessString: String
      get() = if (brightnesses.size == 1) {
        ValuesFormatter.getPercentageString(brightnesses.first())
      } else if (brightnesses.isEmpty()) {
        "?"
      } else {
        val min = ValuesFormatter.getPercentageString(brightnesses.min())
        val max = ValuesFormatter.getPercentageString(brightnesses.max())
        "$min - $max"
      }

    override val brightnessMarkers: List<Int>
      get() =
        if (brightnesses.size == 1) {
          emptyList()
        } else {
          brightnesses
        }

    override val cctMarkers: List<Int>
      get() =
        if (ccts.size == 1) {
          emptyList()
        } else {
          ccts
        }
  }
}

enum class DimmerSelectorType(val value: Int, val swapIconRes: Int) {
  LINEAR(0, R.drawable.ic_dimmer_circular_selector), CIRCULAR(1, R.drawable.ic_dimmer_linear_selector);

  companion object {
    fun from(value: Int): DimmerSelectorType {
      for (type in entries) {
        if (type.value == value) {
          return type
        }
      }

      return LINEAR
    }
  }
}
