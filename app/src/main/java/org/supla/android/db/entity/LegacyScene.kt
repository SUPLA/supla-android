package org.supla.android.db.entity

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
import org.supla.android.db.DbItem
import org.supla.android.db.SuplaContract
import java.text.DateFormat
import java.util.*

@Deprecated("Valid only with old database versions (<27)")
data class LegacyScene(
  var profileId: Long = 0,
  var sceneId: Int = 0,
  var locationId: Int = 0,
  var altIcon: Int = 0,
  var userIcon: Int = 0,
  var caption: String = "",
  var sortOrder: Int = 0,
  var startedAt: Date? = null,
  var estimatedEndDate: Date? = null,
  var initiatorId: Int? = null,
  var initiatorName: String? = null,
  var visible: Int = 0
) : DbItem() {

  fun getScene(): Scene {
    return Scene(
      profileId,
      sceneId,
      locationId,
      altIcon,
      userIcon,
      caption,
      sortOrder,
      startedAt,
      estimatedEndDate,
      initiatorId
    ).apply {
      id = this@LegacyScene.id
    }
  }

  override fun AssignCursorData(cur: Cursor) {
    id = cur.getLong(
      cur.getColumnIndexOrThrow(SuplaContract.SceneEntry._ID)
    )
    sceneId = cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_SCENEID))
    locationId =
      cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_LOCATIONID))
    altIcon = cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_ALTICON))
    userIcon = cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_USERICON))
    caption = cur.getString(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_CAPTION))

    var idx = cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_STARTED_AT)
    if (!cur.isNull(idx)) {
      startedAt = dateFromString(cur.getString(idx))
    }
    idx = cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_EST_END_DATE)
    if (!cur.isNull(idx)) {
      estimatedEndDate = dateFromString(cur.getString(idx))
    }
    idx = cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_ID)
    if (!cur.isNull(idx)) {
      initiatorId = cur.getInt(idx)
    }
    idx = cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_INITIATOR_NAME)
    if (!cur.isNull(idx)) {
      initiatorName = cur.getString(idx)
    }
    sortOrder =
      cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_SORT_ORDER))
    profileId =
      cur.getLong(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_PROFILEID))
    visible = cur.getInt(cur.getColumnIndexOrThrow(SuplaContract.SceneEntry.COLUMN_NAME_VISIBLE))
  }

  override fun getContentValues(): ContentValues {
    throw IllegalStateException()
  }

  private fun dateFromString(str: String): Date {
    val fmt = DateFormat.getDateTimeInstance()
    return fmt.parse(str)!!
  }
}
