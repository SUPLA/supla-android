package org.supla.android.widget.extended.value
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

import org.supla.android.Trace
import org.supla.android.data.source.local.entity.complex.WidgetConfigurationDataEntity
import org.supla.android.data.source.local.entity.custom.Phase
import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.TAG
import org.supla.android.lib.actions.SubjectType
import org.supla.android.lib.singlecall.ElectricityMeterValue
import org.supla.android.lib.singlecall.ResultException
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.widget.extended.WidgetValue
import org.supla.core.shared.data.model.general.SuplaFunction
import org.supla.core.shared.data.model.suplaclient.SuplaResultCode
import org.supla.core.shared.extensions.ifTrue
import org.supla.core.shared.usecase.channel.valueformatter.formatters.ElectricityMeterValueFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtendedValueWidgetProvider @Inject constructor(
  singleCallProvider: SingleCall.Provider
) {

  private val providers: List<WidgetValueProvider> = listOf(
    ElectricityMeterWidgetValueProvider(singleCallProvider)
  )

  fun provide(configuration: WidgetConfigurationDataEntity): WidgetValue =
    providers
      .firstOrNull { it.handle(configuration) }
      ?.provide(configuration) ?: WidgetValue.Unknown
}

interface WidgetValueProvider {
  fun handle(configuration: WidgetConfigurationDataEntity): Boolean
  fun provide(configuration: WidgetConfigurationDataEntity): WidgetValue
}

class ElectricityMeterWidgetValueProvider(
  private val singleCallProvider: SingleCall.Provider
) : WidgetValueProvider {
  val formatter = ElectricityMeterValueFormatter()

  override fun handle(configuration: WidgetConfigurationDataEntity) =
    configuration.widgetConfiguration.subjectType == SubjectType.CHANNEL &&
      configuration.function == SuplaFunction.ELECTRICITY_METER

  override fun provide(configuration: WidgetConfigurationDataEntity): WidgetValue {
    val singleCall = singleCallProvider.provide(configuration.widgetConfiguration.profileId)
    val channelValue =
      try {
        singleCall.getChannelValue(configuration.widgetConfiguration.subjectId)
      } catch (ex: ResultException) {
        Trace.e(TAG, "Could not load channel value", ex)
        return if (ex.resultCode == SuplaResultCode.CHANNEL_IS_OFFLINE) {
          WidgetValue.Offline
        } else {
          WidgetValue.Unknown
        }
      }

    if (channelValue is ElectricityMeterValue) {
      val hasReverseEnergy = channelValue.measuredValues
        .contains(SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY)
      val forwardEnergy = channelValue.phases.sumOf { it.forwardActiveEnergy }
      val reversedEnergy = hasReverseEnergy.ifTrue { channelValue.phases.sumOf { it.reverseActiveEnergy } }

      val phases = Phase.entries
        .filter { configuration.channelEntity?.flags?.and(it.disabledFlag.rawValue) == 0L }
      val phaseValues = (phases.size > 1).ifTrue { phases.associateWithValue(channelValue, hasReverseEnergy) } ?: emptyMap()

      return WidgetValue.ElectricityMeter(
        totalEnergy = WidgetValue.ElectricityMeter.Energy(
          forwarded = formatter.format(forwardEnergy),
          reversed = reversedEnergy?.let { formatter.format(it) }
        ),
        phases = phaseValues
      )
    }

    return WidgetValue.Unknown
  }

  private fun List<Phase>.associateWithValue(
    value: ElectricityMeterValue,
    hasReverseEnergy: Boolean
  ): Map<Phase, WidgetValue.ElectricityMeter.Energy> =
    associateWith { phase ->
      val phaseValues = value.phases[phase.phaseIndex]
      WidgetValue.ElectricityMeter.Energy(
        forwarded = formatter.format(phaseValues.forwardActiveEnergy),
        reversed = hasReverseEnergy.ifTrue { formatter.format(phaseValues.reverseActiveEnergy) }
      )
    }
}

private val Phase.phaseIndex: Int
  get() = value - 1
