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
import android.support.annotation.NonNull;

import org.supla.android.db.DbItem;

public abstract class BaseDao {

    private final DatabaseAccessProvider databaseAccessProvider;

    BaseDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
        this.databaseAccessProvider = databaseAccessProvider;
    }

    <T> T read(DatabaseCallable<T> runnable) {
        return runnable.call(databaseAccessProvider.getReadableDatabase());
    }

    <T> T write(DatabaseCallable<T> runnable) {
        return runnable.call(databaseAccessProvider.getWritableDatabase());
    }

    void write(DatabaseRunnable runnable) {
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

        return read(sqLiteDatabase -> {
            Cursor c = sqLiteDatabase.query(tableName, projection, selectionBuilder.toString(),
                    selectionArgs, null, null, null);

            if (c.getCount() > 0) {
                c.moveToFirst();
                T item = instanceProvider.provide();
                item.AssignCursorData(c);
                return item;
            }

            return null;
        });
    }

    void update(DbItem item, String tableName, Key<?> key) {
        String selection = key.asSelection();
        String[] selectionArgs = {String.valueOf(key.value)};

        write(sqLiteDatabase -> {
            sqLiteDatabase.update(tableName, item.getContentValues(), selection, selectionArgs);
        });
    }

    void insert(DbItem item, String tableName) {
        write(sqLiteDatabase -> {
            sqLiteDatabase.insert(tableName, null, item.getContentValues());
        });
    }

    <T> Key<T> key(String column, T id) {
        return new Key<>(column, id);
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

    class Key<T> {
        public final String column;
        public final T value;

        private Key(String column, T value) {
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
