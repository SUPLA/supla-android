package org.supla.android.lib.actions
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
import org.supla.android.tools.UsedFromNativeCode
import org.supla.android.ui.views.spinner.SpinnerItem
import org.supla.core.shared.infrastructure.LocalizedString

@UsedFromNativeCode
open class ActionParameters(var action: ActionId, var subjectType: SubjectType, var subjectId: Int)

object SubjectTypeValue {
  const val CHANNEL = 1
  const val GROUP = 2
  const val SCENE = 3
}

enum class SubjectType(val value: Int, val nameRes: Int, val widgetNameRes: Int) {
  CHANNEL(SubjectTypeValue.CHANNEL, R.string.widget_channel, R.string.widget_configure_type_channel_label),
  GROUP(SubjectTypeValue.GROUP, R.string.widget_group, R.string.widget_configure_type_group_label),
  SCENE(SubjectTypeValue.SCENE, R.string.widget_scene, R.string.widget_configure_type_scene_label);

  companion object {
    fun from(value: Int): SubjectType {
      entries.forEach {
        if (it.value == value) {
          return it
        }
      }

      throw IllegalArgumentException("Invalid value `$value`")
    }
  }
}

enum class ActionId(val value: Int, val nameRes: Int?) : SpinnerItem {
  NONE(0, null),
  OPEN(10, R.string.channel_btn_open),
  CLOSE(20, R.string.channel_btn_close),
  SHUT(30, R.string.channel_btn_shut),
  REVEAL(40, R.string.channel_btn_reveal),
  COLLAPSE(30, R.string.channel_btn_collapse),
  EXPAND(40, R.string.channel_btn_expand),
  REVEAL_PARTIALLY(50, R.string.channel_btn_reveal),
  SHUT_PARTIALLY(51, R.string.channel_btn_shut),
  TURN_ON(60, R.string.turn_on),
  TURN_OFF(70, R.string.turn_off),
  SET_RGBW_PARAMETERS(80, null),
  OPEN_CLOSE(90, R.string.channel_btn_openclose),
  STOP(100, R.string.channel_btn_stop),
  TOGGLE(110, R.string.channel_btn_toggle),
  UP_OR_STOP(140, R.string.channel_btn_reveal),
  DOWN_OR_STOP(150, R.string.channel_btn_shut),
  STEP_BY_STEP(160, null),
  UP(170, R.string.channel_btn_reveal),
  DOWN(180, R.string.channel_btn_shut),
  SET_HVAC_PARAMETERS(230, null),
  EXECUTE(3000, R.string.btn_execute),
  INTERRUPT(3001, R.string.btn_abort),
  INTERRUPT_AND_EXECUTE(3002, R.string.btn_abort_and_execute);

  override val label: LocalizedString
    get() = nameRes?.let { LocalizedString.WithResource(it) } ?: LocalizedString.Empty

  companion object {
    fun from(value: Int): ActionId {
      entries.forEach {
        if (it.value == value) {
          return it
        }
      }

      throw IllegalArgumentException("Invalid value `$value`")
    }
  }
}
