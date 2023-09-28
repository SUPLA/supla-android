package org.supla.android.usecases.client
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

import io.reactivex.rxjava3.core.Completable
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.hvac.SuplaHvacMode
import org.supla.android.extensions.TAG
import org.supla.android.extensions.toSuplaTemperature
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.HvacActionParameters
import org.supla.android.lib.actions.SubjectType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuteThermostatActionUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider
) {

  operator fun invoke(
    type: SubjectType,
    remoteId: Int,
    mode: SuplaHvacMode? = null,
    setpointTemperatureHeat: Float? = null,
    setpointTemperatureCool: Float? = null,
    durationInSec: Long? = null
  ): Completable =
    Completable.fromRunnable {
      suplaClientProvider.provide()?.run {
        val parameters = HvacActionParameters(
          action = ActionId.SET_HVAC_PARAMETERS,
          subjectType = type,
          subjectId = remoteId,
          durationSec = durationInSec,
          mode = mode,
          setpointTemperatureHeat = setpointTemperatureHeat?.toSuplaTemperature(),
          setpointTemperatureCool = setpointTemperatureCool?.toSuplaTemperature()
        )
        Trace.d(TAG, "Executing thermostat action with parameters: $parameters")

        val result = executeAction(parameters)
        Trace.i(TAG, "Thermostat action ended up with '$result'")
      }
    }
}
