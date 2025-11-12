package org.supla.android.testhelpers
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

import timber.log.Timber

class StdoutTree : Timber.Tree() {
  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val level = when (priority) {
      2 -> "VERBOSE"
      3 -> "DEBUG"
      4 -> "INFO"
      5 -> "WARN"
      6 -> "ERROR"
      7 -> "ASSERT"
      else -> priority.toString()
    }
    val prefix = buildString {
      append('[').append(level).append(']')
      if (!tag.isNullOrBlank()) append('[').append(tag).append(']')
      append(' ')
    }
    println(prefix + message)
    t?.printStackTrace(System.out)
  }
}
