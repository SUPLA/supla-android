package org.supla.android.data.model.general
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

import org.supla.android.Trace
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.extensions.TAG
import org.supla.android.extensions.guardLet
import java.util.Date

data class LockScreenSettings(
  val scope: LockScreenScope,
  val pinSum: String?,
  val biometricAllowed: Boolean,
  val failsCount: Int,
  val lockTime: Long?
) {
  val pinForAppRequired: Boolean
    get() = scope == LockScreenScope.APPLICATION && pinSum != null

  fun isLocked(dateProvider: DateProvider): Boolean =
    lockTime?.let { Date(it).after(dateProvider.currentDate()) } ?: false

  fun asString(): String =
    "${scope.value}:${pinSum ?: ""}:${if (biometricAllowed) 1 else 0}:$failsCount:${lockTime ?: ""}"

  companion object {
    val DEFAULT = LockScreenSettings(LockScreenScope.NONE, null, false, 0, null)

    operator fun invoke(scope: LockScreenScope, pinSum: String?, biometricAllowed: Boolean): LockScreenSettings =
      LockScreenSettings(scope, pinSum, biometricAllowed, 0, null)

    fun from(string: String?): LockScreenSettings {
      val (array) = guardLet(string?.split(":")) {
        return DEFAULT
      }

      if (array.count() != 5) {
        return DEFAULT
      }

      try {
        val scope = LockScreenScope.from(array[0].toInt())
        val pinSum = array[1].ifEmpty { null }
        val biometricAllowed = array[2] == "1"
        val failsCount = array[3].toInt()
        val lockTime = array[4].let { if (it.isEmpty()) null else it.toLong() }

        return LockScreenSettings(scope, pinSum, biometricAllowed, failsCount, lockTime)
      } catch (ex: Exception) {
        Trace.e(TAG, "Could not parse LockScreenSettings string", ex)
        return DEFAULT
      }
    }
  }
}
