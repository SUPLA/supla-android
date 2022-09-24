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

class RgbwActionParameters : ActionParameters() {
    var brightness: Short = -1 // -1 == Ignore
        set(value) {
            field = restrictBrightnessLevel(value)
        }

    var colorBrightness: Short = -1 // -1 == Ignore
        set(value) {
            field = restrictBrightnessLevel(value)
        }

    var color: Long = 0 // 0 == Ignore
        set(value) {
            if (value < 0) {
                field = 0
            } else if (value > 0xFFFFFF) {
                field = 0xFFFFFF
            } else {
                field = value
            }
        }
    
    var colorRandom: Boolean = false
    var onOff: Boolean = false

    private fun restrictBrightnessLevel(brightness: Short) : Short {
        if (brightness > 100) {
            return 100
        } else if (brightness < -1) {
            return -1
        } else {
            return brightness
        }
    }
}
