package org.supla.android.lib

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

interface SuplaNativeActions {

    fun open(subjectId: Int, subjectType: IdType): RequestResponse

    fun close(subjectId: Int, subjectType: IdType): RequestResponse

    fun shut(subjectId: Int, subjectType: IdType, percentage: Float): RequestResponse

    fun reveal(subjectId: Int, subjectType: IdType): RequestResponse

    fun turnOn(subjectId: Int, subjectType: IdType): RequestResponse

    fun turnOff(subjectId: Int, subjectType: IdType): RequestResponse

    fun setRgbw(
            subjectId: Int,
            subjectType: IdType,
            color: Int,
            colorBrightness: Int,
            brightness: Int,
            onOff: Boolean
    ): RequestResponse

    fun stop(subjectId: Int, subjectType: IdType): RequestResponse

    fun toggle(subjectId: Int, subjectType: IdType): RequestResponse

    fun upOrStop(subjectId: Int, subjectType: IdType): RequestResponse

    fun downOrStop(subjectId: Int, subjectType: IdType): RequestResponse

    fun stepByStep(subjectId: Int, subjectType: IdType): RequestResponse

    fun execute(subjectId: Int, subjectType: IdType): RequestResponse

    fun interrupt(subjectId: Int, subjectType: IdType): RequestResponse
}

enum class IdType(val value: Int) {
    CHANNEL(1),
    GROUP(2),
    SCENE(3)
}
