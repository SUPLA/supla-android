package org.supla.android.lib.singlecall

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

@UsedFromNativeCode

/*
The variable temperature is zero if the sensor does not exist or the value is less than -273,
which is considered a sensor error.
The humidity variable is zero if the sensor does not exist or the value is below 0 or above 100
which means considered a sensor error.
 */

class TemperatureAndHumidity(val temperature: Double?,
                             val humidity: Double?) : ChannelValue()