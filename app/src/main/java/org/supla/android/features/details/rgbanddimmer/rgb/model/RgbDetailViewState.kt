package org.supla.android.features.details.rgbanddimmer.rgb.model
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

import androidx.compose.ui.graphics.Color
import org.supla.android.data.ValuesFormatter
import org.supla.android.extensions.HsvColor
import org.supla.android.features.details.rgbanddimmer.rgb.ui.ColorDialogState
import org.supla.android.ui.views.DeviceStateData
import org.supla.android.ui.views.buttons.SwitchButtonState
import org.supla.core.shared.data.model.lists.ChannelIssueItem

data class RgbDetailViewState(
  val deviceStateData: DeviceStateData? = null,
  val channelIssues: List<ChannelIssueItem>? = null,
  val offButtonState: SwitchButtonState? = null,
  val onButtonState: SwitchButtonState? = null,
  val value: RgbValue = RgbValue.Empty,
  val savedColors: List<SavedColor> = emptyList(),
  val offline: Boolean = false,
  val loading: Boolean = false,
  val colorDialogState: ColorDialogState? = null
)

data class SavedColor(
  val id: Long,
  val color: Color,
  val brightness: Int
)

sealed interface RgbValue {
  val hsv: HsvColor?
  val markers: List<HsvColor>
  val brightnessString: String

  data object Empty : RgbValue {
    override val hsv: HsvColor? = null
    override val markers: List<HsvColor> = emptyList()
    override val brightnessString: String = "?"
  }

  data class Single(val color: HsvColor) : RgbValue {
    override val hsv: HsvColor = color
    override val markers: List<HsvColor> = emptyList()
    override val brightnessString: String = ValuesFormatter.getPercentageString(color.value)
  }

  data class Multiple(val colors: List<HsvColor>) : RgbValue {
    override val hsv: HsvColor? = if (colors.size == 1) colors.first() else null
    override val brightnessString: String
      get() = if (colors.size == 1) {
        ValuesFormatter.getPercentageString(colors.first().value)
      } else if (colors.isEmpty()) {
        "?"
      } else {
        val min = ValuesFormatter.getPercentageString(colors.minOf { it.value })
        val max = ValuesFormatter.getPercentageString(colors.maxOf { it.value })
        "$min - $max"
      }

    override val markers: List<HsvColor>
      get() =
        if (colors.size == 1) {
          emptyList()
        } else {
          colors
        }
  }
}
