package org.supla.android.usecases.db
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
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import org.supla.android.core.infrastructure.storage.FileExporter
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.db.DbHelper
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

const val DATABASE_COPY_FILE_NAME = "supla_copy.db"

@Singleton
class MakeAnonymizedDatabaseCopyUseCase @Inject constructor(
  @ApplicationContext private val context: Context
) {

  var file: File = File(context.filesDir, DATABASE_COPY_FILE_NAME)

  operator fun invoke(): Boolean {
    val dbFile = context.getDatabasePath(DbHelper.DATABASE_NAME)
    if (dbFile == null) {
      Timber.w("Database file not found")
    }
    if (file.exists()) {
      try {
        file.delete()
      } catch (ex: Exception) {
        Timber.e(ex, "Could not delete old database copy!")
        return false
      }
    }
    if (!FileExporter.copyFile(dbFile, file)) {
      Timber.e("Could not perform database copy!")
      return false
    }

    try {
      val dbHelper = CopyDbHelper(context, DATABASE_COPY_FILE_NAME, DbHelper.DATABASE_VERSION)
      val emptyBlob = ByteArray(0)
      val bindArgs = arrayOf<Any>(emptyBlob)
      dbHelper.writableDatabase.execSQL("UPDATE ${ProfileEntity.TABLE_NAME} SET ${ProfileEntity.COLUMN_GUID} = ?", bindArgs)
      dbHelper.writableDatabase.execSQL("UPDATE ${ProfileEntity.TABLE_NAME} SET ${ProfileEntity.COLUMN_AUTH_KEY} = ?", bindArgs)
      dbHelper.writableDatabase.execSQL("UPDATE ${ProfileEntity.TABLE_NAME} SET ${ProfileEntity.COLUMN_ACCESS_ID} = 0")
      dbHelper.writableDatabase.execSQL("UPDATE ${ProfileEntity.TABLE_NAME} SET ${ProfileEntity.COLUMN_ACCESS_ID_PASSWORD} = ''")
      dbHelper.writableDatabase.execSQL("UPDATE ${ProfileEntity.TABLE_NAME} SET ${ProfileEntity.COLUMN_EMAIL} = ''")
      dbHelper.close()
    } catch (ex: Exception) {
      Timber.e(ex, "Could not anonymize database!")
      return false
    }

    return true
  }
}

private class CopyDbHelper(context: Context, name: String, version: Int) :
  SQLiteOpenHelper(CopyDbContext(context), name, null, version) {
  override fun onCreate(p0: SQLiteDatabase?) {
    Timber.w("onCreate called!")
  }

  override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
    Timber.w("onUpgrade called!")
  }
}

private class CopyDbContext(private val context: Context) : ContextWrapper(context) {
  override fun getDatabasePath(name: String?): File? =
    File(context.filesDir, name ?: DATABASE_COPY_FILE_NAME)
}
