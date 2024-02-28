package org.supla.android.db.room.app
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

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.supla.android.data.source.local.view.ChannelGroupValueView
import org.supla.android.data.source.local.view.ChannelView
import org.supla.android.data.source.local.view.SceneView
import org.supla.android.db.room.SqlExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDatabaseCallback @Inject constructor() : RoomDatabase.Callback(), SqlExecutor {

  override fun onCreate(db: SupportSQLiteDatabase) {
    execSQL(db, SceneView.SQL)
    execSQL(db, ChannelGroupValueView.SQL)
    execSQL(db, ChannelView.SQL)
  }
}
