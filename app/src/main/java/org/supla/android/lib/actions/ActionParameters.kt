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

@UsedFromNativeCode
open class ActionParameters(var action: ActionId, var subjectType: SubjectType, var subjectId: Int)

enum class SubjectType(val value: Int) {
    CHANNEL(1),
    GROUP(2),
    SCENE(3)
}

enum class ActionId(val value: Int) {
    OPEN(10),
    CLOSE(20),
    SHUT(30),
    REVEAL(40),
    REVEAL_PARTIALLY(50),
    SHUT_PARTIALLY(51),
    TURN_ON(60),
    TURN_OFF(70),
    SET_RGBW_PARAMETERS(80),
    OPEN_CLOSE(90),
    STOP(100),
    TOGGLE(110),
    UP_OR_STOP(140),
    DOWN_OR_STOP(150),
    STEP_BY_STEP(160),
    EXECUTE(3000),
    INTERRUPT(3001),
    INTERRUPT_AND_EXECUTE(3002),
}