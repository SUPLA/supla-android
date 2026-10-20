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

import java.util.*

class SuplaSceneState(
  val sceneId: Int,
  millisecondsFromStart: Long,
  millisecondsLeft: Long,
  val initiatorId: Int,
  val initiatorName: String,
  val isEol: Boolean
) {
  var startedAt: Date? = null
    private set
  var estimatedEndDate: Date? = null
    private set
  val isDuringExecution: Boolean
    get() {
      if (startedAt != null && estimatedEndDate != null) {
        val now = Date()
        return (
          startedAt!!.compareTo(now) <= 0 &&
            estimatedEndDate!!.compareTo(now) > 0
          )
      }
      return false
    }
  val millisecondsLeft: Long
    get() {
      if (estimatedEndDate != null) {
        val now = Date()
        val diff = estimatedEndDate!!.time - now.time
        if (diff > 0) {
          return diff
        }
      }
      return 0
    }

  init {
    if (millisecondsFromStart > 0 || millisecondsLeft > 0) {
      startedAt = Date(System.currentTimeMillis() - millisecondsFromStart)
      estimatedEndDate = Date(System.currentTimeMillis() + millisecondsLeft)
    } else {
      startedAt = null
      estimatedEndDate = null
    }
  }
}
