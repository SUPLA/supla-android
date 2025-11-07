package org.supla.android.usecases.details
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

import org.supla.android.data.model.general.ChannelDataBase
import org.supla.android.features.details.detailbase.base.DetailPage
import org.supla.core.shared.data.model.general.SuplaFunction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProvideGroupDetailTypeUseCase @Inject constructor() : BaseDetailTypeProviderUseCase() {

  operator fun invoke(channelDataBase: ChannelDataBase): DetailType? =
    when (channelDataBase.function) {
      SuplaFunction.THERMOSTAT_HEATPOL_HOMEPLUS ->
        ThermostatDetailType(listOf(DetailPage.THERMOSTAT_HEATPOL_GENERAL))
      else -> provide(channelDataBase.function)
    }
}
