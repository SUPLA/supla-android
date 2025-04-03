package org.supla.android.db;

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import org.supla.android.data.source.local.BaseDao;
import org.supla.android.db.room.SqlExecutor;

public abstract class BaseDbHelper extends SQLiteOpenHelper
    implements BaseDao.DatabaseAccessProvider, SqlExecutor {

  private final ProfileIdProvider profileIdProvider;

  BaseDbHelper(
      @Nullable Context context,
      @Nullable String name,
      @Nullable SQLiteDatabase.CursorFactory factory,
      int version,
      ProfileIdProvider profileIdProvider) {
    super(context, name, factory, version);
    this.profileIdProvider = profileIdProvider;
  }

  public Long getCachedProfileId() {
    return profileIdProvider.getCachedProfileId();
  }
}
