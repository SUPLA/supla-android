package org.supla.android.core.debugtools
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


import android.util.Log
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import java.util.Date

object DelayAnalyzer {

  private var startTimestamp: Long? = null

  fun start() {
    Log.d(TAG, "Delay analyzer started")
    startTimestamp = Date().time
  }

  fun controlPoint(description: String) {
    val (startTime) = guardLet(startTimestamp) {
      Log.e(TAG, "Delay analyzer not started!")
      return
    }

    val date = Date()
    Log.d(TAG, "Control point `${description}` with time ${date.time.minus(startTime)} ms")
  }
}