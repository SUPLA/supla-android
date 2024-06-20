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

import org.supla.android.data.source.local.entity.ChannelEntity
import org.supla.android.data.source.local.entity.ChannelExtendedValueEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.data.source.local.entity.UserIconEntity

object ChannelView {
  const val NAME = "channel_v1"

  const val COLUMN_CHANNEL_ID = ChannelEntity.COLUMN_ID
  const val COLUMN_CHANNEL_DEVICE_ID = ChannelEntity.COLUMN_DEVICE_ID
  const val COLUMN_CHANNEL_REMOTE_ID = ChannelEntity.COLUMN_CHANNEL_REMOTE_ID
  const val COLUMN_CHANNEL_CAPTION = ChannelEntity.COLUMN_CAPTION
  const val COLUMN_VALUE_ID = ChannelValueEntity.COLUMN_ID
  const val COLUMN_EXTENDED_VALUE_ID = ChannelExtendedValueEntity.COLUMN_ID
  const val COLUMN_VALUE_ONLINE = ChannelValueEntity.COLUMN_ONLINE
  const val COLUMN_VALUE_SUB_VALUE = ChannelValueEntity.COLUMN_SUB_VALUE
  const val COLUMN_VALUE_SUB_VALUE_TYPE = ChannelValueEntity.COLUMN_SUB_VALUE_TYPE
  const val COLUMN_VALUE_VALUE = ChannelValueEntity.COLUMN_VALUE
  const val COLUMN_EXTENDED_VALUE_VALUE = ChannelExtendedValueEntity.COLUMN_VALUE
  const val COLUMN_EXTENDED_VALUE_TIMER_START_TIME = ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME
  const val COLUMN_CHANNEL_TYPE = ChannelEntity.COLUMN_TYPE
  const val COLUMN_CHANNEL_FUNCTION = ChannelEntity.COLUMN_FUNCTION
  const val COLUMN_CHANNEL_VISIBLE = ChannelEntity.COLUMN_VISIBLE
  const val COLUMN_CHANNEL_LOCATION_ID = ChannelEntity.COLUMN_LOCATION_ID
  const val COLUMN_CHANNEL_ALT_ICON = ChannelEntity.COLUMN_ALT_ICON
  const val COLUMN_CHANNEL_USER_ICON = ChannelEntity.COLUMN_USER_ICON
  const val COLUMN_CHANNEL_MANUFACTURER_ID = ChannelEntity.COLUMN_MANUFACTURER_ID
  const val COLUMN_CHANNEL_PRODUCT_ID = ChannelEntity.COLUMN_PRODUCT_ID
  const val COLUMN_CHANNEL_FLAGS = ChannelEntity.COLUMN_FLAGS
  const val COLUMN_CHANNEL_PROTOCOL_VERSION = ChannelEntity.COLUMN_PROTOCOL_VERSION
  const val COLUMN_CHANNEL_POSITION = ChannelEntity.COLUMN_POSITION
  const val COLUMN_USER_ICON_IMAGE_1 = UserIconEntity.COLUMN_IMAGE_1
  const val COLUMN_USER_ICON_IMAGE_2 = UserIconEntity.COLUMN_IMAGE_2
  const val COLUMN_USER_ICON_IMAGE_3 = UserIconEntity.COLUMN_IMAGE_3
  const val COLUMN_USER_ICON_IMAGE_4 = UserIconEntity.COLUMN_IMAGE_4
  const val COLUMN_CHANNEL_PROFILE_ID = ChannelEntity.COLUMN_PROFILE_ID

  const val SQL = """
    CREATE VIEW $NAME
      AS
        SELECT
          C.${ChannelEntity.COLUMN_ID} $COLUMN_CHANNEL_ID,
          C.${ChannelEntity.COLUMN_DEVICE_ID} $COLUMN_CHANNEL_DEVICE_ID,
          C.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} $COLUMN_CHANNEL_REMOTE_ID,
          C.${ChannelEntity.COLUMN_CAPTION} $COLUMN_CHANNEL_CAPTION,
          C.${ChannelEntity.COLUMN_PROFILE_ID} $COLUMN_CHANNEL_PROFILE_ID,
          CV.${ChannelValueEntity.COLUMN_ID} $COLUMN_VALUE_ID,
          CEV.${ChannelExtendedValueEntity.COLUMN_ID} $COLUMN_EXTENDED_VALUE_ID,
          CV.${ChannelValueEntity.COLUMN_ONLINE} $COLUMN_VALUE_ONLINE,
          CV.${ChannelValueEntity.COLUMN_SUB_VALUE_TYPE} $COLUMN_VALUE_SUB_VALUE_TYPE,
          CV.${ChannelValueEntity.COLUMN_SUB_VALUE} $COLUMN_VALUE_SUB_VALUE,
          CV.${ChannelValueEntity.COLUMN_VALUE} $COLUMN_VALUE_VALUE,
          CEV.${ChannelExtendedValueEntity.COLUMN_VALUE} $COLUMN_EXTENDED_VALUE_VALUE,
          CEV.${ChannelExtendedValueEntity.COLUMN_TIMER_START_TIME} $COLUMN_EXTENDED_VALUE_TIMER_START_TIME,
          C.${ChannelEntity.COLUMN_TYPE} $COLUMN_CHANNEL_TYPE,
          C.${ChannelEntity.COLUMN_FUNCTION} $COLUMN_CHANNEL_FUNCTION,
          C.${ChannelEntity.COLUMN_VISIBLE} $COLUMN_CHANNEL_VISIBLE,
          C.${ChannelEntity.COLUMN_LOCATION_ID} $COLUMN_CHANNEL_LOCATION_ID,
          C.${ChannelEntity.COLUMN_ALT_ICON} $COLUMN_CHANNEL_ALT_ICON,
          C.${ChannelEntity.COLUMN_USER_ICON} $COLUMN_CHANNEL_USER_ICON,
          C.${ChannelEntity.COLUMN_MANUFACTURER_ID} $COLUMN_CHANNEL_MANUFACTURER_ID,
          C.${ChannelEntity.COLUMN_PRODUCT_ID} $COLUMN_CHANNEL_PRODUCT_ID,
          C.${ChannelEntity.COLUMN_FLAGS} $COLUMN_CHANNEL_FLAGS,
          C.${ChannelEntity.COLUMN_PROTOCOL_VERSION} $COLUMN_CHANNEL_PROTOCOL_VERSION,
          C.${ChannelEntity.COLUMN_POSITION} $COLUMN_CHANNEL_POSITION,
          I.${UserIconEntity.COLUMN_IMAGE_1} $COLUMN_USER_ICON_IMAGE_1,
          I.${UserIconEntity.COLUMN_IMAGE_2} $COLUMN_USER_ICON_IMAGE_2,
          I.${UserIconEntity.COLUMN_IMAGE_3} $COLUMN_USER_ICON_IMAGE_3,
          I.${UserIconEntity.COLUMN_IMAGE_4} $COLUMN_USER_ICON_IMAGE_4
        FROM ${ChannelEntity.TABLE_NAME} C
        JOIN ${ChannelValueEntity.TABLE_NAME} CV
          ON ( 
            C.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = CV.${ChannelValueEntity.COLUMN_CHANNEL_REMOTE_ID}
            AND C.${ChannelEntity.COLUMN_PROFILE_ID} = CV.${ChannelValueEntity.COLUMN_PROFILE_ID}
          )
        LEFT JOIN ${ChannelExtendedValueEntity.TABLE_NAME} CEV
          ON (
            C.${ChannelEntity.COLUMN_CHANNEL_REMOTE_ID} = CEV.${ChannelExtendedValueEntity.COLUMN_CHANNEL_ID}
            AND C.${ChannelEntity.COLUMN_PROFILE_ID} = CEV.${ChannelExtendedValueEntity.COLUMN_PROFILE_ID}
          )
        LEFT JOIN ${UserIconEntity.TABLE_NAME} I
        ON (
          C.${ChannelEntity.COLUMN_USER_ICON} = I.${UserIconEntity.COLUMN_REMOTE_ID}
            AND C.${ChannelEntity.COLUMN_PROFILE_ID} = I.${UserIconEntity.COLUMN_PROFILE_ID}
        )
  """

  val ALL_COLUMNS = arrayOf(
    COLUMN_CHANNEL_ID,
    COLUMN_CHANNEL_DEVICE_ID,
    COLUMN_CHANNEL_REMOTE_ID,
    COLUMN_CHANNEL_CAPTION,
    COLUMN_VALUE_ID,
    COLUMN_EXTENDED_VALUE_ID,
    COLUMN_VALUE_ONLINE,
    COLUMN_VALUE_SUB_VALUE,
    COLUMN_VALUE_SUB_VALUE_TYPE,
    COLUMN_VALUE_VALUE,
    COLUMN_EXTENDED_VALUE_VALUE,
    COLUMN_EXTENDED_VALUE_TIMER_START_TIME,
    COLUMN_CHANNEL_TYPE,
    COLUMN_CHANNEL_FUNCTION,
    COLUMN_CHANNEL_VISIBLE,
    COLUMN_CHANNEL_LOCATION_ID,
    COLUMN_CHANNEL_ALT_ICON,
    COLUMN_CHANNEL_USER_ICON,
    COLUMN_CHANNEL_MANUFACTURER_ID,
    COLUMN_CHANNEL_PRODUCT_ID,
    COLUMN_CHANNEL_FLAGS,
    COLUMN_CHANNEL_PROTOCOL_VERSION,
    COLUMN_CHANNEL_POSITION,
    COLUMN_USER_ICON_IMAGE_1,
    COLUMN_USER_ICON_IMAGE_2,
    COLUMN_USER_ICON_IMAGE_3,
    COLUMN_USER_ICON_IMAGE_4,
    COLUMN_CHANNEL_PROFILE_ID,
  )
}
