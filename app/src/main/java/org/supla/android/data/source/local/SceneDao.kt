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
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.supla.android.db.SuplaContract
import org.supla.android.db.Scene
import org.supla.android.Trace

public class SceneDao(dap: DatabaseAccessProvider): BaseDao(dap) {

  private val TAG = SceneDao::class.java.simpleName


  fun getSceneByRemoteId(remoteId: Int): Scene? {
    return getItem( { Scene() },
                    SuplaContract.SceneEntry.ALL_COLUMNS,
                    SuplaContract.SceneEntry.TABLE_NAME,
                    key(SuplaContract.SceneEntry.COLUMN_NAME_SCENEID, remoteId),
                    key(SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID,
                        getCachedProfileId()) )
  }

  fun sceneCursor(): Cursor {
    return read {
      it.query(SuplaContract.SceneViewEntry.VIEW_NAME,
               SuplaContract.SceneViewEntry.ALL_COLUMNS, 
               SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID + " = ? " /* selection */, 
               arrayOf(getCachedProfileId().toString()) /* selectionArgs */, 
               null /* groupBy */, 
               null /* having */,
               /* order by - begin */
               SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_SORT_ORDER + ", " +
               SuplaContract.SceneViewEntry.COLUMN_NAME_LOCATION_NAME + 
               " COLLATE LOCALIZED, " +
               SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER + ", " +
               SuplaContract.SceneEntry.COLUMN_NAME_CAPTION + " COLLATE LOCALIZED, " +
               SuplaContract.SceneEntry.COLUMN_NAME_SCENEID
               /* order by - end */,
               null /* limit */)
    }
  }

  fun updateScene(scene: Scene): Boolean {
    return try {
      update(scene, SuplaContract.SceneEntry.TABLE_NAME, 
             key(SuplaContract.SceneEntry._ID, scene.id))
      true
    } catch(e: Exception) {
      Trace.w(TAG, "updateScene", e)
      false
    }
  }

  fun insertScene(scene: Scene): Boolean {
    return try {
      insert(scene.copy(profileId = getCachedProfileId()), 
             SuplaContract.SceneEntry.TABLE_NAME)
      true
    } catch(e: Exception) {
      Trace.w(TAG, "insertScene", e)
      false
    }
  }
  
}
