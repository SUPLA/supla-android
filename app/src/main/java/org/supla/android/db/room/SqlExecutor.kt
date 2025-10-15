package org.supla.android.db.room
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

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.db.DbHelper
import timber.log.Timber

interface SqlExecutor {

  fun getDatabaseNameForLog() = DbHelper.DATABASE_NAME

  fun execSQL(db: SupportSQLiteDatabase, sql: String) {
    Timber.d("sql-statements/%s: %s", getDatabaseNameForLog(), sql)
    db.execSQL(sql)
  }

  fun query(db: SupportSQLiteDatabase, sql: String): Cursor {
    Timber.d("sql-statements/%s: %s", getDatabaseNameForLog(), sql)
    return db.query(sql)
  }

  fun execSQL(db: SQLiteDatabase, sql: String) {
    Timber.d("sql-statements/%s: %s", getDatabaseNameForLog(), sql)
    db.execSQL(sql)
  }

  fun execSQL(db: SupportSQLiteDatabase, queries: Array<String>) {
    queries.forEach { execSQL(db, it) }
  }
}
