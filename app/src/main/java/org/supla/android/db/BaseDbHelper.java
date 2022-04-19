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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import org.supla.android.profile.ProfileManager;
import org.supla.android.SuplaApp;
import org.supla.android.Trace;
import org.supla.android.data.source.local.BaseDao;

public abstract class BaseDbHelper extends SQLiteOpenHelper implements BaseDao.DatabaseAccessProvider {

    private Context context;

    BaseDbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    protected Context getContext() {
        return context;
    }

    protected void execSQL(SQLiteDatabase db, String sql) {
        Trace.d("sql-statments/" + getDatabaseNameForLog(), sql);
        db.execSQL(sql);
    }

    protected void createIndex(SQLiteDatabase db, String tableName, String fieldName) {
        final String SQL_CREATE_INDEX = "CREATE INDEX " + tableName + "_"
                + fieldName + "_index ON " + tableName + "(" + fieldName + ")";
        execSQL(db, SQL_CREATE_INDEX);
    }

    protected void addColumn(SQLiteDatabase db, String sql) {
        try {
            execSQL(db, sql);
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column name:")) {
                throw e;
            } else {
                e.getStackTrace();
            }
        }
    }

    protected abstract String getDatabaseNameForLog();

    public Long getCurrentProfileId() {
        ProfileManager pm = SuplaApp.getApp()
            .getProfileManager(getContext());
        return pm.getCurrentProfile().getId();
    }
}
