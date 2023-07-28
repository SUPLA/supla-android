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

const val HVAC_MODE_OFF = 1
const val HVAC_MODE_HEAT = 2
const val HVAC_MODE_COOL = 3
const val HVAC_MODE_AUTO = 4
const val HVAC_MODE_FAN_ONLY = 5
const val HVAC_MODE_DRY = 6

@UsedFromNativeCode
class HvacActionParameters(
  action: ActionId,
  subjectType: SubjectType,
  subjectId: Int,
  var durationSec: Long?,
  var mode: Int?,
  var setpointTemperatureMin: Double?,
  var setpointTemperatureMax: Double?,
) :
  ActionParameters(action, subjectType, subjectId)
