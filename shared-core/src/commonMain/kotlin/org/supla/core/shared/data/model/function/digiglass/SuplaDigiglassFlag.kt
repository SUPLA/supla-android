package org.supla.core.shared.data.model.function.digiglass
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

enum class SuplaDigiglassFlag(val value: Int) {
  TOO_LONG_OPERATION(1),
  PLANNED_REGENERATION_IN_PROGRESS(1 shl 1),
  REGENERATION_AFTER_20H_IN_PROGRESS(1 shl 2);

  companion object {
    fun from(value: Int): List<SuplaDigiglassFlag> {
      val result = mutableListOf<SuplaDigiglassFlag>()
      for (flag in SuplaDigiglassFlag.entries) {
        if (flag.value and value > 0) {
          result.add(flag)
        }
      }

      return result
    }
  }
}
