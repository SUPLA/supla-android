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
import org.supla.android.data.source.local.entity.Scene
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.SceneEntity.Companion.COLUMN_VISIBLE
import org.supla.android.data.source.local.entity.UserIconEntity
import org.supla.android.data.source.local.view.SceneView
import org.supla.android.extensions.TAG

class SceneDao(dap: DatabaseAccessProvider) : BaseDao(dap) {

  fun getSceneByRemoteId(remoteId: Int): Scene? {
    return getItem(
      { Scene() },
      SceneEntity.ALL_COLUMNS_ARRAY,
      SceneEntity.TABLE_NAME,
      key(SceneEntity.COLUMN_REMOTE_ID, remoteId),
      key(COLUMN_PROFILE_ID, cachedProfileId)
    )
  }

  fun getSceneUserIconIdsToDownload(): List<Int> {
    val sql = (
      "SELECT S." +
        SceneEntity.COLUMN_USER_ICON + " " +
        SceneEntity.COLUMN_USER_ICON + " FROM " +
        SceneEntity.TABLE_NAME + " AS S LEFT JOIN " +
        UserIconEntity.TABLE_NAME + " AS U ON (S." +
        SceneEntity.COLUMN_USER_ICON + " = U." +
        UserIconEntity.COLUMN_REMOTE_ID + " AND S." +
        COLUMN_PROFILE_ID + " = U." +
        UserIconEntity.COLUMN_PROFILE_ID + ") WHERE " +
        COLUMN_VISIBLE + " > 0 AND " +
        SceneEntity.COLUMN_USER_ICON + " > 0 AND U." +
        UserIconEntity.COLUMN_REMOTE_ID + " IS NULL AND (S." +
        COLUMN_PROFILE_ID + " = " + cachedProfileId + ")"
      )

    val ids = ArrayList<Int>()
    read { sqLiteDatabase: SQLiteDatabase -> sqLiteDatabase.rawQuery(sql, null) }
      .use { cursor ->
        if (cursor.moveToFirst()) {
          do {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(SceneEntity.COLUMN_USER_ICON))
            if (!ids.contains(id)) {
              ids.add(id)
            }
          } while (cursor.moveToNext())
        }
      }
    return ids
  }

  fun sceneCursor(profileId: Long? = cachedProfileId): Cursor {
    return read {
      val selection: String?
      val selectionArgs: Array<String>?
      if (profileId == null) {
        selection = null
        selectionArgs = null
      } else {
        selection = "$COLUMN_PROFILE_ID = ? AND $COLUMN_VISIBLE > 0"
        selectionArgs = arrayOf(profileId.toString())
      }
      val order = SceneView.COLUMN_LOCATION_SORT_ORDER + ", " +
        SceneView.COLUMN_LOCATION_NAME + " COLLATE LOCALIZED, " +
        SceneEntity.COLUMN_SORT_ORDER + ", " +
        SceneEntity.COLUMN_CAPTION + " COLLATE LOCALIZED, " +
        SceneEntity.COLUMN_REMOTE_ID

      it.query(
        SceneView.NAME,
        SceneView.ALL_COLUMNS,
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
        scene,
        SceneEntity.TABLE_NAME,
        key(SceneEntity.COLUMN_ID, scene.id)
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
        SceneEntity.TABLE_NAME
      )
      true
    } catch (e: Exception) {
      Trace.w(TAG, "insertScene", e)
      false
    }
  }

  fun setScenesVisible(visible: Int, whereVisible: Int): Boolean {
    val selection = "$COLUMN_VISIBLE = ? AND $COLUMN_PROFILE_ID = ?"
    val selectionArgs = arrayOf(whereVisible.toString(), cachedProfileId?.toString() ?: "")
    val values = ContentValues()
    values.put(COLUMN_VISIBLE, visible)

    return write<Int> { return@write it.update(SceneEntity.TABLE_NAME, values, selection, selectionArgs) } > 0
  }
}
