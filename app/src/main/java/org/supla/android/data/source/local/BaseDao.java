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
import org.supla.android.db.DbItem;

public abstract class BaseDao {

  private final DatabaseAccessProvider databaseAccessProvider;

  BaseDao(@NonNull DatabaseAccessProvider databaseAccessProvider) {
    this.databaseAccessProvider = databaseAccessProvider;
  }

  <T> T read(DatabaseCallable<T> runnable) {
    // SQLiteOpenHelper manages DB, there is no need to close SQLiteDatase.
    return runnable.call(databaseAccessProvider.getReadableDatabase());
  }

  void write(DatabaseRunnable runnable) {
    // SQLiteOpenHelper manages DB, there is no need to close SQLiteDatase.
    runnable.run(databaseAccessProvider.getWritableDatabase());
  }

  <T extends DbItem> T getItem(
      DbItemProvider<T> instanceProvider,
      String[] projection,
      String tableName,
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

    return select(
        instanceProvider,
        tableName,
        projection,
        selectionBuilder.toString(),
        selectionArgs,
        null,
        null);
  }

  <T extends DbItem> T select(
      DbItemProvider<T> provider,
      String table,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String order,
      String limit) {
    return read(
        sqLiteDatabase -> {
          try (Cursor cursor =
              sqLiteDatabase.query(
                  table, projection, selection, selectionArgs, null, null, order, limit)) {

            if (cursor.moveToFirst()) {
              T item = provider.provide();
              item.AssignCursorData(cursor);
              return item;
            }
          }

          return null;
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
  }
}
