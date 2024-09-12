package org.supla.android.data.source.remote.electricitymeter
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

import org.supla.android.data.source.remote.channel.SuplaElectricityMeasurementType
import org.supla.android.extensions.ifTrue
import org.supla.android.features.details.detailbase.electricitymeter.EnergyData
import org.supla.android.lib.SuplaChannelElectricityMeterValue
import org.supla.android.usecases.channel.valueformatter.ListElectricityMeterValueFormatter

val SuplaChannelElectricityMeterValue.hasForwardEnergy: Boolean
  get() = measuredValues and SuplaElectricityMeasurementType.FORWARD_ACTIVE_ENERGY.rawValue > 0

val SuplaChannelElectricityMeterValue.hasReverseEnergy: Boolean
  get() = measuredValues and SuplaElectricityMeasurementType.REVERSE_ACTIVE_ENERGY.rawValue > 0

fun SuplaChannelElectricityMeterValue.getForwardEnergy(formatter: ListElectricityMeterValueFormatter): EnergyData? =
  hasForwardEnergy.ifTrue { EnergyData(formatter, summary.totalForwardActiveEnergy, pricePerUnit, currency) }

fun SuplaChannelElectricityMeterValue.getReverseEnergy(formatter: ListElectricityMeterValueFormatter): EnergyData? =
  hasReverseEnergy.ifTrue { EnergyData(energy = formatter.format(summary.totalReverseActiveEnergy)) }
