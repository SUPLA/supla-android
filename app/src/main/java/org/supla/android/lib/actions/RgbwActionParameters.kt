package org.supla.android.lib.actions

import org.supla.android.tools.UsedFromNativeCode

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

const val IGNORE_BRIGHTNESS: Short = -1
const val IGNORE_COLOR: Long = 0

@UsedFromNativeCode
data class RgbwActionParameters(
  override val action: ActionId,
  override val subjectType: SubjectType,
  override val subjectId: Int,
  var brightness: Short,
  var colorBrightness: Short,
  var color: Long,
  var colorRandom: Boolean,
  var onOff: Boolean
) :
  ActionParameters(action, subjectType, subjectId)
