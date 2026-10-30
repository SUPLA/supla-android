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
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.data.source.remote.hvac.SuplaButtonMode
import org.supla.android.lib.actions.AtActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuteActionTriggerActionUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider,
  private val vibrationHelper: VibrationHelper
) {

  operator fun invoke(
    type: SubjectType,
    remoteId: Int,
    buttonMode: SuplaButtonMode,
    durationSec: Long? = null
  ): Completable =
    Completable.fromRunnable {
      suplaClientProvider.provide()?.run {
        val parameters = AtActionParameters(
          subjectType = type,
          subjectId = remoteId,
          mode = buttonMode,
          durationSec = durationSec
        )
        Timber.d("Executing action trigger action with parameters: $parameters")

        val result = executeAction(parameters)
        if (result) {
          vibrationHelper.vibrate()
        }
        Timber.i("Action trigger action ended up with '$result'")
      }
    }
}
