package org.supla.core.shared.extensions
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

import org.supla.core.shared.infrastructure.LocalizedString
import org.supla.core.shared.infrastructure.LocalizedStringId

val Boolean.localizedString: LocalizedString
  get() = if (this) {
    LocalizedString.WithId(LocalizedStringId.GENERAL_YES)
  } else {
    LocalizedString.WithId(LocalizedStringId.GENERAL_NO)
  }

fun <T> Boolean.ifTrue(value: T): T? = if (this) {
  value
} else {
  null
}

fun <T> Boolean.ifTrue(valueProvider: () -> T): T? = if (this) {
  valueProvider()
} else {
  null
}

fun <T> Boolean.ifFalse(value: T): T? = if (this.not()) {
  value
} else {
  null
}

fun ifTrue(value: Boolean, callback: () -> Unit) {
  if (value) {
    callback()
  }
}
