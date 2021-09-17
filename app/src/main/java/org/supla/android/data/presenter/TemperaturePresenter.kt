package org.supla.android.data.presenter
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

import org.supla.android.db.ChannelBase
import org.supla.android.db.ChannelValue

interface TemperaturePresenter {

    /**
     * Returns numeric value corresponding to measurement point,
     * respecting current temperature unit settings (i.e. converting
     * the value to target scale, if necessary).
     */
    fun getTemp(value: ChannelValue, channel: ChannelBase): Double

    /**
     * Returns string representation of temperature measurement point.
     * This is expected to be a fixed-point number possibly suffixed by
     * the unit representation.
     */
    fun formattedWithUnit(value: ChannelValue, channel: ChannelBase): String

    /**
     * Given raw measured value, returns value converted to unit representable for
     * the user.
     */
    fun getConvertedValue(rawValue: Double): Double

    /**
     * Returns string representing display unit.
     */
    fun getUnitString(): String
}