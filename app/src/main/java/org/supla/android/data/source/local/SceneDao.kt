package org.supla.android.data.source.local

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
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.supla.android.Trace
import org.supla.android.db.Scene
import org.supla.android.db.SuplaContract
import org.supla.android.extensions.TAG
import java.util.ArrayList

class SceneDao(dap: DatabaseAccessProvider) : BaseDao(dap) {

  fun getSceneByRemoteId(remoteId: Int): Scene? {
    return getItem(
      { Scene() },
      SuplaContract.SceneEntry.ALL_COLUMNS,
      SuplaContract.SceneEntry.TABLE_NAME,
      key(SuplaContract.SceneEntry.COLUMN_NAME_SCENEID, remoteId),
      key(SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID, cachedProfileId)
    )
  }

  fun getSceneUserIconIdsToDownload(): List<Int> {
    val sql = ("SELECT S."
      + SuplaContract.SceneEntry.COLUMN_NAME_USERICON + " "
      + SuplaContract.SceneEntry.COLUMN_NAME_USERICON + " FROM "
      + SuplaContract.SceneEntry.TABLE_NAME + " AS S LEFT JOIN "
      + SuplaContract.UserIconsEntry.TABLE_NAME + " AS U ON (S."
      + SuplaContract.SceneEntry.COLUMN_NAME_USERICON + " = U."
      + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID + " AND S."
      + SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID + " = U."
      + SuplaContract.UserIconsEntry.COLUMN_NAME_PROFILEID + ") WHERE "
      + SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE + " > 0 AND "
      + SuplaContract.SceneEntry.COLUMN_NAME_USERICON + " > 0 AND U."
      + SuplaContract.UserIconsEntry.COLUMN_NAME_REMOTEID + " IS NULL AND (S."
      + SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID + " = " + cachedProfileId + ")")

    val ids = ArrayList<Int>()
    read { sqLiteDatabase: SQLiteDatabase -> sqLiteDatabase.rawQuery(sql, null) }.
    use{ cursor ->
      if (cursor.moveToFirst()) {
        do {
          val id =
            cursor.getInt(cursor.getColumnIndex(SuplaContract.ChannelEntry.COLUMN_NAME_USERICON))
          if (!ids.contains(id)) {
            ids.add(id)
          }
        } while (cursor.moveToNext())
      }
    }
    return ids
  }

  fun sceneCursor(profileId: Long = cachedProfileId): Cursor {
    return read {
      val selection = "${SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID} = ? AND " +
        "${SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE} > 0"
      val selectionArgs = arrayOf(profileId.toString())
      val order = SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_SORT_ORDER + ", " +
        SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_NAME +
        " COLLATE LOCALIZED, " +
        SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER + ", " +
        SuplaContract.SceneEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED, " +
        SuplaContract.SceneEntry.COLUMN_NAME_SCENEID

      it.query(
        SuplaContract.SceneViewEntry.VIEW_NAME,
        SuplaContract.SceneViewEntry.ALL_COLUMNS,
        selection,
        selectionArgs,
        null /* groupBy */,
        null /* having */,
        order,
        null /* limit */
      )
    }
  }

  fun updateScene(scene: Scene): Boolean {
    return try {
      update(
        scene, SuplaContract.SceneEntry.TABLE_NAME,
        key(SuplaContract.SceneEntry._ID, scene.id)
      )
      true
    } catch (e: Exception) {
      Trace.w(TAG, "updateScene", e)
      false
    }
  }

  fun insertScene(scene: Scene): Boolean {
    return try {
      insert(
        scene.copy(profileId = cachedProfileId),
        SuplaContract.SceneEntry.TABLE_NAME
      )
      true
    } catch (e: Exception) {
      Trace.w(TAG, "insertScene", e)
      false
    }
  }

  fun setScenesVisible(visible: Int, whereVisible: Int) =
    setVisible(
      SuplaContract.SceneEntry.TABLE_NAME,
      visible,
      key(SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE, whereVisible)
    )

  private fun setVisible(table: String, visible: Int, key: Key<Int>): Boolean {
    val selection = key.asSelection() + " AND " +
      SuplaContract.ChannelEntry.COLUMN_NAME_PROFILEID + " = ?"
    val selectionArgs = arrayOf(key.value.toString(), cachedProfileId.toString())
    val values = ContentValues()
    values.put(key.column, visible)

    return write<Int> { return@write it.update(table, values, selection, selectionArgs) } > 0
  }
}
