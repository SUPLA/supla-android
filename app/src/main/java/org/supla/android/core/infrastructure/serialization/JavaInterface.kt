package org.supla.android.core.infrastructure.serialization
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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.supla.android.lib.SuplaChannelExtendedValue
import timber.log.Timber
import java.io.IOException

object JavaInterface {
  fun deserialize(value: ByteArray): SuplaChannelExtendedValue? {
    try {
      return Json.decodeFromString<SuplaChannelExtendedValue>(String(bytes = value))
    } catch (e: IOException) {
      Timber.w(e, "Could not convert to object (IOException)")
    } catch (e: ClassNotFoundException) {
      Timber.w(e, "Could not convert to object (ClassNotFoundException)")
    } catch (e: SerializationException) {
      Timber.w("Could not convert to object (SerializationException) - skipping")
    } catch (e: Exception) {
      Timber.w(e, "Could not convert to object")
    }
    return null
  }
}
