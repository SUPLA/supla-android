package org.supla.android.data.source.local.entity
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

import androidx.room.ColumnInfo
import androidx.room.Entity
import org.supla.core.shared.data.model.channel.ChannelRelationType

@Entity(
  tableName = ChannelRelationEntity.TABLE_NAME,
  primaryKeys = [ChannelRelationEntity.COLUMN_CHANNEL_ID, ChannelRelationEntity.COLUMN_PARENT_ID, ChannelRelationEntity.COLUMN_PROFILE_ID]
)
data class ChannelRelationEntity(
  @ColumnInfo(name = COLUMN_CHANNEL_ID) val channelId: Int,
  @ColumnInfo(name = COLUMN_PARENT_ID) val parentId: Int,
  @ColumnInfo(name = COLUMN_CHANNEL_RELATION_TYPE) val relationType: ChannelRelationType,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
  @ColumnInfo(name = COLUMN_DELETE_FLAG) val deleteFlag: Boolean
) {
  companion object {
    const val TABLE_NAME = "channel_relation"
    const val COLUMN_CHANNEL_ID = "channel_id"
    const val COLUMN_PARENT_ID = "parent_id"
    const val COLUMN_CHANNEL_RELATION_TYPE = "channel_relation_type"
    const val COLUMN_PROFILE_ID = "profileid" // Needs to be without underscore because of other tables
    const val COLUMN_DELETE_FLAG = "delete_flag"

    const val ALL_COLUMNS = "$COLUMN_CHANNEL_ID, $COLUMN_PARENT_ID, $COLUMN_CHANNEL_RELATION_TYPE, $COLUMN_PROFILE_ID, $COLUMN_DELETE_FLAG"
  }
}
