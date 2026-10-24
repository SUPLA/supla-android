package org.supla.android.tools
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

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationHelper @Inject constructor(
  @param:ApplicationContext private val context: Context
) {

  fun vibrate() = vibrate(isWidget = false)

  fun widgetVibrate() = vibrate(isWidget = true)

  private fun vibrate(isWidget: Boolean) {
    getVibrator()?.apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          vibrate(effect, getVibrationAttributes(isWidget))
        } else {
          @Suppress("DEPRECATION")
          vibrate(effect, getAudioAttributes(isWidget))
        }
      } else {
        @Suppress("DEPRECATION")
        vibrate(100) // deprecated in API 26
      }
    }
  }

  private fun getVibrator(): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
      vibratorManager.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    }
  }

  private fun getAudioAttributes(isWidget: Boolean): AudioAttributes =
    AudioAttributes.Builder()
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(
        if (isWidget) AudioAttributes.USAGE_NOTIFICATION else AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
      )
      .build()

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private fun getVibrationAttributes(isWidget: Boolean): VibrationAttributes =
    VibrationAttributes.Builder()
      .setUsage(
        if (isWidget) VibrationAttributes.USAGE_NOTIFICATION else VibrationAttributes.USAGE_TOUCH
      )
      .build()
}
