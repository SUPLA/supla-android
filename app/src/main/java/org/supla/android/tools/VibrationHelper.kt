package org.supla.android.tools

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationHelper @Inject constructor(@ApplicationContext private val context: Context) {

  fun vibrate() {
    getVibrator()?.apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrate(
          VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE),
          getVibrateAttributes()
        )
      } else {
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

  private fun getVibrateAttributes(): AudioAttributes =
    AudioAttributes.Builder()
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(AudioAttributes.USAGE_ALARM)
      .build()
}
