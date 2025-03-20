@file:OptIn(ExperimentalObjCRefinement::class)

package org.supla.core.shared.infrastructure
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

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

sealed interface LocalizedString {

  data object Empty : LocalizedString
  data class Constant(val text: String) : LocalizedString
  data class WithId(val id: LocalizedStringId) : LocalizedString
  data class WithIdIntStringInt(val id: LocalizedStringId, val arg1: Int, val arg2: LocalizedString, val arg3: Int) : LocalizedString

  @HiddenFromObjC
  data class WithResource(val id: Int) : LocalizedString

  @HiddenFromObjC
  data class WithResourceAndValue(val id: Int, val value: Any) : LocalizedString

  @HiddenFromObjC
  data class WithResourceStringInt(val id: Int, val arg1: LocalizedString, val arg2: Int) : LocalizedString

  @HiddenFromObjC
  data class WithResourceIntStringInt(val id: Int, val arg1: Int, val arg2: LocalizedString, val arg3: Int) : LocalizedString

  @HiddenFromObjC
  data class WithResourceIntInt(val id: Int, val arg1: Int, val arg2: Int) : LocalizedString

  @HiddenFromObjC
  data class WithResourceIntIntIntInt(val id: Int, val arg1: Int, val arg2: Int, val arg3: Int, val arg4: Int) : LocalizedString
}

fun localizedString(id: LocalizedStringId?): LocalizedString = id?.let { LocalizedString.WithId(it) } ?: LocalizedString.Empty

fun localizedString(id: LocalizedStringId, arg1: Int, arg2: LocalizedString, arg3: Int): LocalizedString =
  LocalizedString.WithIdIntStringInt(id, arg1, arg2, arg3)

@HiddenFromObjC
fun localizedString(id: Int?): LocalizedString = id?.let { LocalizedString.WithResource(it) } ?: LocalizedString.Empty

@HiddenFromObjC
fun localizedString(id: Int, arg1: Int, arg2: LocalizedString, arg3: Int): LocalizedString =
  LocalizedString.WithResourceIntStringInt(id, arg1, arg2, arg3)
