package org.supla.android.usecases.group
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

import io.reactivex.rxjava3.core.Single
import okhttp3.internal.and
import org.supla.android.data.source.ChannelGroupRelationRepository
import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.local.entity.ChannelGroupEntity
import org.supla.android.data.source.local.entity.ChannelValueEntity
import org.supla.android.lib.SuplaConst
import org.supla.android.usecases.group.totalvalue.FacadeBlindGroupValue
import org.supla.android.usecases.group.totalvalue.FloatValue
import org.supla.android.usecases.group.totalvalue.GeneralGroupValue
import org.supla.android.usecases.group.totalvalue.GroupTotalValue
import org.supla.android.usecases.group.totalvalue.GroupValue
import org.supla.android.usecases.group.totalvalue.IntegerValue
import org.supla.android.usecases.group.totalvalue.ProjectorScreenGroupValue
import org.supla.android.usecases.group.totalvalue.ShadingSystemGroupValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChannelGroupTotalValueUseCase @Inject constructor(
  private val channelGroupRelationRepository: ChannelGroupRelationRepository,
  private val channelGroupRepository: ChannelGroupRepository
) {

  operator fun invoke(): Single<List<Int>> =
    channelGroupRelationRepository.findAllVisibleRelations()
      .map {
        val groups = mutableListOf<ChannelGroupEntity>()

        var group: ChannelGroupEntity? = null
        val groupTotalValue = GroupTotalValue()

        for (relation in it) {
          if (group == null) {
            group = relation.channelGroupEntity
          } else if (group.remoteId != relation.channelGroupEntity.remoteId) {
            group.updateBy(groupTotalValue) { updatedGroup -> groups.add(updatedGroup) }
            group = relation.channelGroupEntity
            groupTotalValue.clear()
          }

          group.getGroupValue(relation.channelValueEntity)?.let { value ->
            groupTotalValue.add(value, relation.channelValueEntity.online)
          }
        }

        group?.updateBy(groupTotalValue) { updatedGroup -> groups.add(updatedGroup) }

        return@map groups
      }
      .flatMap { groups ->
        channelGroupRepository.update(groups)
          .andThen(Single.just(groups.map { it.remoteId }))
      }
}

// Private extensions - ChannelGroupEntity

private fun ChannelGroupEntity.updateBy(totalValue: GroupTotalValue, onChangedCallback: (ChannelGroupEntity) -> Unit) {
  val totalOnline = totalValue.online
  val totalValueString = totalValue.asString()

  if (this.online != totalOnline || this.totalValue != totalValueString) {
    onChangedCallback(copy(online = totalOnline, totalValue = totalValueString))
  }
}

private fun ChannelGroupEntity.getGroupValue(value: ChannelValueEntity): GroupValue? {
  return when (function) {
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEDOORLOCK,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATEWAYLOCK,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGATE,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEGARAGEDOOR ->
      GeneralGroupValue(IntegerValue(value.getSensorHighValue()))

    SuplaConst.SUPLA_CHANNELFNC_POWERSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_LIGHTSWITCH,
    SuplaConst.SUPLA_CHANNELFNC_STAIRCASETIMER,
    SuplaConst.SUPLA_CHANNELFNC_VALVE_OPENCLOSE ->
      GeneralGroupValue(IntegerValue(if (value.getValueHi()) 1 else 0))

    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROLLERSHUTTER,
    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEROOFWINDOW,
    SuplaConst.SUPLA_CHANNELFNC_TERRACE_AWNING ->
      ShadingSystemGroupValue(value.asRollerShutterValue().alwaysValidPosition, (value.getSubValueHi() and 0x1) == 0x1)

    SuplaConst.SUPLA_CHANNELFNC_PROJECTOR_SCREEN ->
      ProjectorScreenGroupValue(value.asRollerShutterValue().alwaysValidPosition)

    SuplaConst.SUPLA_CHANNELFNC_CONTROLLINGTHEFACADEBLIND ->
      value.asFacadeBlindValue().let { FacadeBlindGroupValue(it.alwaysValidPosition, it.alwaysValidTilt) }

    SuplaConst.SUPLA_CHANNELFNC_DIMMER ->
      GeneralGroupValue(IntegerValue(value.asBrightness().toInt()))

    SuplaConst.SUPLA_CHANNELFNC_RGBLIGHTING ->
      GeneralGroupValue(
        IntegerValue(value.asColor()),
        IntegerValue(value.asBrightnessColor().toInt())
      )

    SuplaConst.SUPLA_CHANNELFNC_DIMMERANDRGBLIGHTING ->
      GeneralGroupValue(
        IntegerValue(value.asColor()),
        IntegerValue(value.asBrightnessColor().toInt()),
        IntegerValue(value.asBrightness().toInt())
      )

    SuplaConst.SUPLA_CHANNELFNC_THERMOSTAT_HEATPOL_HOMEPLUS ->
      GeneralGroupValue(
        IntegerValue(if (value.getValueHi()) 1 else 0),
        FloatValue(value.asHeatpolThermostatValue().measuredTemperature),
        FloatValue(value.asHeatpolThermostatValue().presetTemperature)
      )

    else -> null
  }
}

// Private extensions - ChannelValueEntity

private fun ChannelValueEntity.getSensorHighValue() = if (getSubValueHi() and 0x1 == 0x1) 1 else 0
