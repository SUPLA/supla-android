package org.supla.core.shared.data.model.rest
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

import kotlin.io.encoding.Base64

data class UserIconDto(
  val id: Int,
  val functionId: Int,
  val images: List<String>,
  val imagesDark: List<String>?
) {
  fun image(index: Int): ByteArray? = images.getOrNull(index)?.let { Base64.decode(it) }
  fun imageDark(index: Int): ByteArray? = imagesDark?.getOrNull(index)?.let { Base64.decode(it) }
}
