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

import android.util.Base64
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.supla.android.data.source.local.entity.ChannelValueEntity.Companion.COLUMN_CHANNEL_REMOTE_ID
import org.supla.android.data.source.local.entity.ChannelValueEntity.Companion.COLUMN_PROFILE_ID
import org.supla.android.data.source.local.entity.ChannelValueEntity.Companion.TABLE_NAME
import org.supla.android.data.source.remote.channel.SuplaChannelAvailabilityStatus
import org.supla.android.lib.SuplaChannelValue
import org.supla.android.lib.SuplaConst.SUPLA_CHANNELVALUE_SIZE
import org.supla.core.shared.data.model.function.container.ContainerValue
import org.supla.core.shared.data.model.function.digiglass.DigiglassValue
import org.supla.core.shared.data.model.function.facadeblind.FacadeBlindValue
import org.supla.core.shared.data.model.function.relay.RelayValue
import org.supla.core.shared.data.model.function.rollershutter.RollerShutterValue
import org.supla.core.shared.data.model.function.thermostat.HeatpolThermostatValue
import org.supla.core.shared.data.model.function.thermostat.ThermostatValue
import org.supla.core.shared.data.model.valve.ValveValue

@Entity(
  tableName = TABLE_NAME,
  indices = [
    Index(
      value = [COLUMN_CHANNEL_REMOTE_ID],
      name = "${TABLE_NAME}_${COLUMN_CHANNEL_REMOTE_ID}_index"
    ),
    Index(
      value = [COLUMN_PROFILE_ID],
      name = "${TABLE_NAME}_${COLUMN_PROFILE_ID}_index"
    )
  ]
)
data class ChannelValueEntity(
  @ColumnInfo(name = COLUMN_ID) @PrimaryKey val id: Long?,
  @ColumnInfo(name = COLUMN_CHANNEL_REMOTE_ID) val channelRemoteId: Int,
  @ColumnInfo(name = COLUMN_ONLINE) val status: SuplaChannelAvailabilityStatus,
  @ColumnInfo(name = COLUMN_SUB_VALUE) val subValue: String?,
  @ColumnInfo(name = COLUMN_SUB_VALUE_TYPE) val subValueType: Short,
  @ColumnInfo(name = COLUMN_VALUE) val value: String?,
  @ColumnInfo(name = COLUMN_PROFILE_ID) val profileId: Long,
) {

  fun asThermostatValue() = ThermostatValue.from(status, getValueAsByteArray())

  fun asBrightness() = asShortValue(0)?.let { if (it > 100) 0 else it }?.toInt() ?: 0

  fun asBrightnessColor() = asShortValue(1)?.let { if (it > 100) 0 else it }?.toInt() ?: 0

  fun asColor(): Int = getValueAsByteArray().let {
    if (it.size < 5) {
      return@let 0
    }

    return@let (it[2].toInt() and 0x00000FF) or
      ((it[3].toInt() shl 8) and 0x0000FF00) or
      ((it[4].toInt() shl 16) and 0x00FF0000)
  }

  fun asDigiglassValue() = DigiglassValue.from(status, getValueAsByteArray())

  fun asRollerShutterValue() = RollerShutterValue.from(status, getValueAsByteArray())

  fun asFacadeBlindValue() = FacadeBlindValue.from(status, getValueAsByteArray())

  fun asHeatpolThermostatValue() = HeatpolThermostatValue.from(status, getValueAsByteArray())

  fun asRelayValue() = RelayValue.from(status, getValueAsByteArray())

  fun asValveValue() = ValveValue.from(status, getValueAsByteArray())

  fun asContainerValue() = ContainerValue.from(status, getValueAsByteArray())

  fun getSubValueHi(): Int =
    getSubValueAsByteArray().let {
      var result: Byte = 0

      if (it.isNotEmpty() && it[0].toInt() == 1) {
        result = 0x1
      }

      if (it.size > 1 && it[1].toInt() == 1) {
        result = (result.toInt() or 0x2).toByte()
      }

      return@let result
    }.toInt()

  fun getValueHi(): Boolean {
    return getValueAsByteArray().let {
      return@let it.isNotEmpty() && it[0].toInt() == 1
    }
  }

  fun isClosed(): Boolean {
    return getValueAsByteArray().let {
      it.isNotEmpty() && it[0].toInt() == 1
    }
  }

  fun getValueAsByteArray(): ByteArray = toByteArray(value)

  fun getSubValueAsByteArray(): ByteArray = toByteArray(subValue)

  fun differsFrom(suplaChannelValue: SuplaChannelValue, status: SuplaChannelAvailabilityStatus): Boolean {
    val suplaValue = Companion.toString(suplaChannelValue.Value)
    val suplaSubValue = Companion.toString(suplaChannelValue.SubValue)

    return value != suplaValue ||
      subValue != suplaSubValue ||
      subValueType != suplaChannelValue.SubValueType ||
      this.status != status
  }

  fun updatedBy(suplaChannelValue: SuplaChannelValue, status: SuplaChannelAvailabilityStatus): ChannelValueEntity =
    ChannelValueEntity(
      id = id,
      channelRemoteId = channelRemoteId,
      status = status,
      subValue = if (status.online) Companion.toString(suplaChannelValue.SubValue) else subValue,
      subValueType = if (status.online) suplaChannelValue.SubValueType else subValueType,
      value = if (status.online) Companion.toString(suplaChannelValue.Value) else value,
      profileId = profileId
    )

  private fun asShortValue(bytePosition: Int): Short? {
    getValueAsByteArray().let {
      if (it.size > bytePosition) {
        return it[bytePosition].toShort()
      }
    }

    return null
  }

  companion object {
    const val TABLE_NAME = "channel_value"
    const val COLUMN_ID = "_channel_value_id"
    const val COLUMN_CHANNEL_REMOTE_ID = "channelid"
    const val COLUMN_ONLINE = "online"
    const val COLUMN_SUB_VALUE = "subvalue"
    const val COLUMN_SUB_VALUE_TYPE = "subvaluetype"
    const val COLUMN_VALUE = "value"
    const val COLUMN_PROFILE_ID = "profileid"

    val SQL = arrayOf(
      """
        CREATE TABLE $TABLE_NAME
        (
          $COLUMN_ID INTEGER PRIMARY KEY,
          $COLUMN_CHANNEL_REMOTE_ID INTEGER NOT NULL,
          $COLUMN_ONLINE INTEGER NOT NULL,
          $COLUMN_SUB_VALUE_TYPE INTEGER NOT NULL,
          $COLUMN_SUB_VALUE TEXT,
          $COLUMN_VALUE TEXT,
          $COLUMN_PROFILE_ID INTEGER NOT NULL
        )
      """.trimIndent(),
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_CHANNEL_REMOTE_ID}_index ON $TABLE_NAME ($COLUMN_CHANNEL_REMOTE_ID)",
      "CREATE INDEX ${TABLE_NAME}_${COLUMN_PROFILE_ID}_index ON $TABLE_NAME ($COLUMN_PROFILE_ID)"
    )

    const val ALL_COLUMNS =
      "$COLUMN_ID, $COLUMN_CHANNEL_REMOTE_ID, $COLUMN_ONLINE, $COLUMN_SUB_VALUE_TYPE, $COLUMN_SUB_VALUE, $COLUMN_VALUE, $COLUMN_PROFILE_ID"

    fun from(
      suplaChannelValue: SuplaChannelValue,
      channelRemoteId: Int,
      status: SuplaChannelAvailabilityStatus,
      profileId: Long
    ): ChannelValueEntity {
      return ChannelValueEntity(
        id = null,
        channelRemoteId = channelRemoteId,
        status = status,
        subValue = getValue(suplaChannelValue.SubValue),
        subValueType = suplaChannelValue.SubValueType,
        value = getValue(suplaChannelValue.Value),
        profileId = profileId
      )
    }

    private fun getValue(valueArray: ByteArray?): String? =
      if (valueArray != null && (valueArray.size == SUPLA_CHANNELVALUE_SIZE || valueArray.isEmpty())) {
        toString(valueArray)
      } else {
        null
      }

    private fun toString(value: ByteArray) = Base64.encodeToString(value, Base64.DEFAULT)

    private fun toByteArray(value: String?) = Base64.decode(value, Base64.DEFAULT)
  }
}
