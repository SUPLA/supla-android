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

import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelGroupRelationEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity

object ChannelGroupValueView {
  const val NAME = "channelgroupvalue_v1"

  const val COLUMN_ID = ChannelValueEntity.COLUMN_ID
  const val COLUMN_GROUP_REMOTE_ID = ChannelGroupEntity.COLUMN_REMOTE_ID
  const val COLUMN_GROUP_FUNCTION = ChannelGroupEntity.COLUMN_FUNCTION
  const val COLUMN_CHANNEL_REMOTE_ID = ChannelGroupRelationEntity.COLUMN_CHANNEL_ID
  const val COLUMN_CHANNEL_ONLINE = ChannelValueEntity.COLUMN_ONLINE
  const val COLUMN_CHANNEL_SUB_VALUE = ChannelValueEntity.COLUMN_SUB_VALUE
  const val COLUMN_CHANNEL_SUB_VALUE_TYPE = ChannelValueEntity.COLUMN_SUB_VALUE_TYPE
  const val COLUMN_CHANNEL_VALUE = ChannelValueEntity.COLUMN_VALUE
  const val COLUMN_CHANNEL_PROFILE_ID = ChannelValueEntity.COLUMN_PROFILE_ID

  const val SQL = """
    CREATE VIEW $NAME
      AS
        SELECT 
          V.${ChannelValueEntity.COLUMN_ID} $COLUMN_ID, 
          V.${ChannelValueEntity.COLUMN_PROFILE_ID} $COLUMN_CHANNEL_PROFILE_ID,
          G.${ChannelGroupEntity.COLUMN_REMOTE_ID} $COLUMN_GROUP_REMOTE_ID,
          G.${ChannelGroupEntity.COLUMN_FUNCTION} $COLUMN_GROUP_FUNCTION,
          R.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID} $COLUMN_CHANNEL_REMOTE_ID,
          V.${ChannelValueEntity.COLUMN_ONLINE} $COLUMN_CHANNEL_ONLINE,
          V.${ChannelValueEntity.COLUMN_SUB_VALUE} $COLUMN_CHANNEL_SUB_VALUE,
          V.${ChannelValueEntity.COLUMN_SUB_VALUE_TYPE} $COLUMN_CHANNEL_SUB_VALUE_TYPE,
          V.${ChannelValueEntity.COLUMN_VALUE} $COLUMN_CHANNEL_VALUE
        FROM ${ChannelGroupRelationEntity.TABLE_NAME} R
        JOIN ${ChannelGroupEntity.TABLE_NAME} G
          ON G.${ChannelGroupEntity.COLUMN_REMOTE_ID} = R.${ChannelGroupRelationEntity.COLUMN_GROUP_ID}
        JOIN ${ChannelValueEntity.TABLE_NAME} V
          ON V.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID} = R.${ChannelGroupRelationEntity.COLUMN_CHANNEL_ID}
        WHERE R.${ChannelGroupRelationEntity.COLUMN_VISIBLE} > 0
          AND G.${ChannelGroupEntity.COLUMN_VISIBLE} > 0
  """
}
