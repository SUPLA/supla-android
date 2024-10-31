package org.supla.android.core.shared
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

import android.content.Context
import org.supla.android.core.ui.StringProvider
import org.supla.core.shared.infrastructure.LocalizedString

operator fun LocalizedString.invoke(context: Context): String {
  return when (this) {
    is LocalizedString.Constant -> text
    LocalizedString.Empty -> ""
    is LocalizedString.WithId -> context.getString(id.resourceId)
    is LocalizedString.WithResourceIntStringInt -> context.getString(id, arg1, arg2(context), arg3)
    is LocalizedString.WithResource -> context.getString(id)
    is LocalizedString.WithIdIntStringInt -> context.getString(id.resourceId, arg1, arg2(context), arg3)
  }
}

fun LocalizedString.provider(): StringProvider = { context -> invoke(context) }
