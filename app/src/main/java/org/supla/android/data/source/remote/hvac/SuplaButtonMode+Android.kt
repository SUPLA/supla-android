package org.supla.android.data.source.remote.hvac
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

import org.supla.android.R
import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.localizedString
import org.supla.core.shared.usecase.channel.valueformatter.NO_VALUE_TEXT

val SuplaButtonMode.label: LocalizedString
  get() = when (this) {
    SuplaButtonMode.NOT_SET -> localizedString(R.string.schedule_program_default)
    SuplaButtonMode.LOCKED, SuplaButtonMode.LOCKED_TEMPORARY_UNLOCK_ALLOWED -> localizedString(R.string.general_locked)
    else -> LocalizedString.Constant(NO_VALUE_TEXT)
  }
