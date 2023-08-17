package org.supla.android.features.thermostatdetail.thermostatgeneral
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

import android.annotation.SuppressLint
import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.DelayedCommandSubject
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.client.ExecuteThermostatActionUseCase
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("CheckResult")
@Singleton
class DelayedThermostatActionSubject @Inject constructor(
  private val executeThermostatActionUseCase: ExecuteThermostatActionUseCase,
  schedulers: SuplaSchedulers
) : DelayedCommandSubject<ThermostatGeneralViewModelState>(schedulers) {

  override fun execute(state: ThermostatGeneralViewModelState): Completable =
    executeThermostatActionUseCase.invoke(
      SubjectType.CHANNEL,
      state.remoteId,
      state.mode,
      state.setpointMinTemperature,
      state.setpointMaxTemperature
    )
}
