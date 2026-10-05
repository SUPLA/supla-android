package org.supla.android.features.details.programinfo
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

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString

/** Presentation data shared by schedule summaries across device functions. */
data class ProgramInfo(
  val type: Type,
  val time: LocalizedString? = null,
  @param:DrawableRes val icon: Int? = null,
  @param:ColorRes val iconColor: Int? = null,
  val description: LocalizedString? = null,
  @param:DrawableRes val indicatorIcon: Int? = null,
  @param:ColorRes val indicatorIconColor: Int? = null
) {
  enum class Type(@param:StringRes val stringRes: Int) {
    CURRENT(R.string.program_info_current),
    NEXT(R.string.program_info_next)
  }
}
