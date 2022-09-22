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

open class ActionParameters {
    companion object {
        const val ACTION_OPEN = 10
        const val ACTION_CLOSE = 20
        const val ACTION_SHUT = 30
        const val ACTION_REVEAL = 40
        const val ACTION_REVEAL_PARTIALLY = 50
        const val ACTION_SHUT_PARTIALLY = 51
        const val ACTION_TURN_ON = 60
        const val ACTION_TURN_OFF = 70
        const val ACTION_SET_RGBW_PARAMETERS = 80
        const val ACTION_OPEN_CLOSE = 90
        const val ACTION_STOP = 100
        const val ACTION_TOGGLE = 110
        const val ACTION_UP_OR_STOP = 140
        const val ACTION_DOWN_OR_STOP = 150
        const val ACTION_STEP_BY_STEP = 160
        const val ACTION_EXECUTE = 3000
        const val ACTION_INTERRUPT = 3001
        const val ACTION_INTERRUPT_AND_EXECUTE = 3002

        const val SUBJECT_TYPE_UNKNOWN = 0
        const val SUBJECT_TYPE_CHANNEL = 1
        const val SUBJECT_TYPE_CHANNEL_GROUP = 2
        const val SUBJECT_TYPE_SCENE = 3
    }
    var action: Int = 0
    var subjectType: Int = SUBJECT_TYPE_UNKNOWN
    var subjectId: Int = 0
}
