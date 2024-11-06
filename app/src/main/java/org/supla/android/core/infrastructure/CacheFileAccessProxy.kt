package org.supla.android.core.infrastructure
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
import org.supla.android.core.shared.file
import org.supla.core.shared.infrastructure.storage.CacheFileAccess
import java.io.File

class CacheFileAccessProxy(
  val applicationContext: Context
) : CacheFileAccess {
  override fun fileExists(file: CacheFileAccess.File): Boolean =
    file.file(applicationContext.cacheDir).exists()

  override fun dirExists(name: String): Boolean =
    with(File(applicationContext.cacheDir, name)) {
      isDirectory && exists()
    }

  override fun mkdir(name: String): Boolean =
    File(applicationContext.cacheDir, name).mkdir()

  override fun delete(file: CacheFileAccess.File): Boolean =
    file.file(applicationContext.cacheDir).delete()

  override fun writeBytes(file: CacheFileAccess.File, bytes: ByteArray) {
    file.file(applicationContext.cacheDir).writeBytes(bytes)
  }

  override fun readBytes(file: CacheFileAccess.File): ByteArray =
    file.file(applicationContext.cacheDir).readBytes()
}
