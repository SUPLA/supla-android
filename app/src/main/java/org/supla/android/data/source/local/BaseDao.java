package org.supla.android.data.source.local;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.supla.android.db.DbItem;

import java.util.Calendar;
import java.util.Date;

public abstract class BaseDao {

    private final DatabaseAccessProvider databaseAccessProvider;

    BaseDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        this.databaseAccessProvider = databaseAccessProvider;
    }

    <T> T read(DatabaseCallable<T> runnable) {
        // SQLiteOpenHelper manages DB, there is no need to close SQLiteDatase.
        return runnable.call(databaseAccessProvider.getReadableDatabase());
    }

    <T> T write(DatabaseCallable<T> runnable) {
        // SQLiteOpenHelper manages DB, there is no need to close SQLiteDatase.
        return runnable.call(databaseAccessProvider.getWritableDatabase());
    }

    void write(DatabaseRunnable runnable) {
        // SQLiteOpenHelper manages DB, there is no need to close SQLiteDatase.
        runnable.run(databaseAccessProvider.getWritableDatabase());
    }

    <T extends DbItem> T getItem(DbItemProvider<T> instanceProvider, String[] projection, String tableName,
                                 Key<?>... selectionKeys) {

        final StringBuilder selectionBuilder = new StringBuilder();
        final String[] selectionArgs = new String[selectionKeys.length];
        for (int i = 0; i < selectionKeys.length; i++) {
            Key<?> key = selectionKeys[i];

            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ").append(key.asSelection());
            } else {
                selectionBuilder.append(key.asSelection());
            }
            selectionArgs[i] = String.valueOf(key.value);
        }

        return select(instanceProvider, tableName, projection, selectionBuilder.toString(), selectionArgs, null, null);
    }

    <T extends DbItem> T select(DbItemProvider<T> provider, String table, String[] projection, String selection, String[] selectionArgs, String order, String limit) {
        return read(sqLiteDatabase -> {
            try (Cursor cursor = sqLiteDatabase.query(table, projection, selection, selectionArgs,
                    null, null, order, limit)) {

                if (cursor.moveToFirst()) {
                    T item = provider.provide();
                    item.AssignCursorData(cursor);
                    return item;
                }
            }

            return null;
        });
    }

    void update(DbItem item, String tableName, Key<?>... keys) {
        final StringBuilder selectionBuilder = new StringBuilder();
        final String[] selectionArgs = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            Key<?> key = keys[i];

            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ").append(key.asSelection());
            } else {
                selectionBuilder.append(key.asSelection());
            }
            selectionArgs[i] = String.valueOf(key.value);
        }

        write(sqLiteDatabase -> {
            sqLiteDatabase.update(tableName, item.getContentValues(), selectionBuilder.toString(), selectionArgs);
        });
    }

    long insert(DbItem item, String tableName) {
        return write(sqLiteDatabase -> {
            return sqLiteDatabase.insertOrThrow(tableName, null, item.getContentValues());
        });
    }

    public void insert(DbItem item, String tableName, int conflictAlgorithm) {
        write(sqLiteDatabase -> {
            sqLiteDatabase.insertWithOnConflict(tableName, null, item.getContentValues(), conflictAlgorithm);
        });
    }

    void delete(String tableName, Key<?>... keys) {
        StringBuilder whereBuilder = new StringBuilder();
        String[] args = new String[keys.length];

        if (keys.length > 0) {
            whereBuilder.append(keys[0].asSelection());
            args[0] = String.valueOf(keys[0].value);
            for (int i = 1; i < keys.length; i++) {
                whereBuilder.append(" AND ").append(keys[i].asSelection());
                args[i] = String.valueOf(keys[i].value);
            }
        }

        write(sqLiteDatabase -> {
            sqLiteDatabase.delete(tableName, whereBuilder.toString(), args);
        });
    }

    int getCount(String tableName, @Nullable Key<?>... keys) {
        final StringBuilder selection = new StringBuilder().append("SELECT count(*) FROM ").append(tableName);
        if (keys != null && keys.length > 0 && keys[0] != null) {
            selection.append(" WHERE ");
            boolean and = false;
            for (Key<?> key : keys) {
                if (and) {
                    selection.append(" AND ");
                }
                selection.append(key.asWhere());
                and = true;
            }
        }

        return read(sqLiteDatabase -> {
            int count;

            Cursor c = sqLiteDatabase.rawQuery(selection.toString(), null);
            c.moveToFirst();
            count = c.getInt(0);
            c.close();

            return count;
        });
    }

    <T> Key<T> key(String column, T id) {
        return new Key<>(column, id);
    }

    public static boolean timestampStartsWithTheCurrentMonth(long timestamp) {
        if (timestamp == 0) {
            return true;
        } else {
            Calendar now = Calendar.getInstance();
            now.setTime(new Date());

            Calendar minDate = Calendar.getInstance();
            minDate.setTime(new Date(timestamp * 1000));

            return minDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && minDate.get(Calendar.MONTH) == now.get(Calendar.MONTH);
        }

    }

    public interface DatabaseAccessProvider {
        @NonNull
        SQLiteDatabase getReadableDatabase();

        @NonNull
        SQLiteDatabase getWritableDatabase();
    }

    @FunctionalInterface
    interface DatabaseRunnable {
        void run(SQLiteDatabase sqLiteDatabase);
    }

    @FunctionalInterface
    interface DatabaseCallable<T> {
        T call(SQLiteDatabase sqLiteDatabase);
    }

    @FunctionalInterface
    interface DbItemProvider<T extends DbItem> {
        T provide();
    }

    static class Key<T> {
        public final String column;
        public final T value;

        Key(String column, T value) {
            this.column = column;
            this.value = value;
        }

        String asSelection() {
            return column + " = ?";
        }

        public String asWhere() {
            return this.column + " = " + value;
        }
    }
}
