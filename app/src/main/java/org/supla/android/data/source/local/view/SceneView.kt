package org.supla.android.data.source.local.view
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

import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.SceneEntity

object SceneView {
  const val NAME = "scene_view"
  const val COLUMN_LOCATION_NAME = "location_name"
  const val COLUMN_LOCATION_SORT_ORDER = "location_sort_order"
  const val COLUMN_LOCATION_VISIBLE = "location_visible"

  const val SQL = """
    CREATE VIEW $NAME
    AS
      SELECT
        S.${SceneEntity.COLUMN_ID}, S.${SceneEntity.COLUMN_REMOTE_ID},
        S.${SceneEntity.COLUMN_LOCATION_ID}, S.${SceneEntity.COLUMN_ALT_ICON},
        S.${SceneEntity.COLUMN_USER_ICON}, S.${SceneEntity.COLUMN_CAPTION},
        S.${SceneEntity.COLUMN_STARTED_AT}, S.${SceneEntity.COLUMN_ESTIMATED_END_DATE},
        S.${SceneEntity.COLUMN_INITIATOR_ID}, S.${SceneEntity.COLUMN_INITIATOR_NAME},
        S.${SceneEntity.COLUMN_SORT_ORDER}, S.${SceneEntity.COLUMN_PROFILE_ID},
        S.${SceneEntity.COLUMN_VISIBLE},
        L.${LocationEntity.COLUMN_CAPTION} AS $COLUMN_LOCATION_NAME,
        L.${LocationEntity.COLUMN_SORT_ORDER} AS $COLUMN_LOCATION_SORT_ORDER,
        L.${LocationEntity.COLUMN_VISIBLE} AS $COLUMN_LOCATION_VISIBLE
      FROM ${SceneEntity.TABLE_NAME} S
      JOIN ${LocationEntity.TABLE_NAME} L
        ON S.${SceneEntity.COLUMN_LOCATION_ID} = L.${LocationEntity.COLUMN_REMOTE_ID}
          AND S.${SceneEntity.COLUMN_PROFILE_ID} = L.${LocationEntity.COLUMN_PROFILE_ID}
  """

  val ALL_COLUMNS = arrayOf(
    SceneEntity.COLUMN_ID,
    SceneEntity.COLUMN_REMOTE_ID,
    SceneEntity.COLUMN_LOCATION_ID,
    SceneEntity.COLUMN_ALT_ICON,
    SceneEntity.COLUMN_USER_ICON,
    SceneEntity.COLUMN_CAPTION,
    SceneEntity.COLUMN_STARTED_AT,
    SceneEntity.COLUMN_ESTIMATED_END_DATE,
    SceneEntity.COLUMN_INITIATOR_ID,
    SceneEntity.COLUMN_INITIATOR_NAME,
    SceneEntity.COLUMN_SORT_ORDER,
    SceneEntity.COLUMN_PROFILE_ID,
    SceneEntity.COLUMN_VISIBLE,
    COLUMN_LOCATION_NAME,
    COLUMN_LOCATION_SORT_ORDER,
    COLUMN_LOCATION_VISIBLE
  )
}
