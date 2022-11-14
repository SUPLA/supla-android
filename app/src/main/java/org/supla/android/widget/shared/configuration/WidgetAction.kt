package org.supla.android.widget.shared.configuration
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
import org.supla.android.lib.actions.ActionId

enum class WidgetAction(
  val actionId: Long,
  val text: Int,
  val suplaAction: ActionId
) {
  TURN_ON(1, R.string.channel_btn_on, ActionId.TURN_ON),
  TURN_OFF(2, R.string.channel_btn_off, ActionId.TURN_OFF),
  MOVE_UP(3, R.string.channel_btn_open, ActionId.REVEAL),
  MOVE_DOWN(4, R.string.channel_btn_close, ActionId.SHUT),
  TOGGLE(5, R.string.widget_action_toggle, ActionId.TOGGLE),
  EXECUTE(6, R.string.btn_execute, ActionId.EXECUTE),
  INTERRUPT(7, R.string.btn_abort, ActionId.INTERRUPT),
  INTERRUPT_AND_EXECUTE(8, R.string.btn_abort_and_execute, ActionId.INTERRUPT_AND_EXECUTE);

  companion object {
    fun fromId(actionId: Long?): WidgetAction? {
      return values().firstOrNull { it.actionId == actionId }
    }
  }
}
