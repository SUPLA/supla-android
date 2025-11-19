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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.extensions.toHsv
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.IGNORE_BRIGHTNESS
import org.supla.android.lib.actions.IGNORE_COLOR
import org.supla.android.lib.actions.RgbwActionParameters
import org.supla.android.lib.actions.SubjectType
import org.supla.android.tools.VibrationHelper
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExecuteRgbwActionUseCase @Inject constructor(
  private val suplaClientProvider: SuplaClientProvider,
  private val vibrationHelper: VibrationHelper
) {
  operator fun invoke(
    actionId: ActionId,
    type: SubjectType,
    remoteId: Int,
    color: Color? = null,
    colorBrightness: Int? = null,
    brightness: Int? = null,
    onOff: Boolean = false,
    vibrate: Boolean = true
  ): Completable = Completable.fromRunnable {
    suplaClientProvider.provide()?.run {
      val parameters = RgbwActionParameters(
        action = actionId,
        subjectType = type,
        subjectId = remoteId,
        brightness = brightness?.toShort() ?: IGNORE_BRIGHTNESS,
        colorBrightness = colorBrightness?.toShort()
          ?: color?.toHsv()?.valueAsPercentage?.toShort()
          ?: IGNORE_BRIGHTNESS,
        color = color?.toArgb()?.toLong() ?: IGNORE_COLOR,
        colorRandom = false,
        onOff
      )
      Timber.d("Executing RGBW action with parameters: $parameters")

      if (executeAction(parameters)) {
        if (vibrate) {
          vibrationHelper.vibrate()
        }
        Timber.d("RGBW action executed successfully.")
      }
    }
  }
}
