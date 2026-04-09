package org.supla.android.usecases.icon.producers

import org.supla.android.R
import org.supla.android.usecases.icon.IconData
import org.supla.android.usecases.icon.IconResourceProducer
import org.supla.core.shared.data.model.general.SuplaFunction

object ThermostatHvacHeatCoolIconResourceProducer : IconResourceProducer {
  override fun accepts(function: SuplaFunction): Boolean =
    function == SuplaFunction.HVAC_THERMOSTAT_HEAT_COOL

  override fun produce(data: IconData): Int = R.drawable.fnc_thermostat_heat_cool
}
