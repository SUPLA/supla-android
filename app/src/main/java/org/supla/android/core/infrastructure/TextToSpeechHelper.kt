package org.supla.android.core.infrastructure
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
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import org.supla.core.shared.extensions.guardLet
import timber.log.Timber
import java.util.Locale

class TextToSpeechHelper(
  private val context: Context,
  private val dateProvider: DateProvider
) {

  private var textToSpeech: TextToSpeech? = null
  private var textToSpeechAllowed: Boolean = false

  private val speeches = mutableListOf<String>()
  private val audioManager = context.getSystemService(AudioManager::class.java)

  private val progressListener = object : DefaultUtteranceProgressListener() {
    override fun onDone(speechId: String?) {
      speeches.remove(speechId)
      if (speeches.isEmpty()) {
        audioManager.abandonAudioFocus(null)
      }
    }
  }

  fun onCreate() {
    synchronized(this) {
      textToSpeech?.shutdown()
      textToSpeechAllowed = false
      textToSpeech = TextToSpeech(context) { status ->
        Timber.i("Text to speech status: $status")
        if (status == TextToSpeech.SUCCESS) {
          textToSpeechAllowed = true
        }
      }.also {
        it.setOnUtteranceProgressListener(progressListener)
        it.language = Locale.getDefault()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
          it.setAudioAttributes(audioAttributes)
        }
      }
    }
  }

  fun onDestroy() {
    synchronized(this) {
      textToSpeech?.shutdown()
      textToSpeech = null
      textToSpeechAllowed = false
    }
  }

  fun speak(message: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
    if (!textToSpeechAllowed) {
      Timber.w("Text to speech not initialized yet!")
      return
    }

    synchronized(this) {
      val (speaker) = guardLet(textToSpeech) {
        Timber.w("Text to speech not initialized")
        return
      }

      if (requestAudioFocus() != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        Timber.w("Could not get audio focus")
        return
      }

      val speechId = "id_${dateProvider.currentTimestamp()}"
      speeches.add(speechId)
      speaker.speak(message, queueMode, null, speechId)
    }
  }

  private fun requestAudioFocus(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(audioAttributes)
        .build()
      audioManager.requestAudioFocus(focusRequest)
    } else {
      @Suppress("DEPRECATION")
      audioManager.requestAudioFocus(null, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
    }
  }

  private val audioAttributes: AudioAttributes
    get() = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
      .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
      .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
      .build()

  companion object {
    private val TAG = TextToSpeechHelper::class.java.simpleName
  }
}

open class DefaultUtteranceProgressListener : UtteranceProgressListener() {
  override fun onStart(speechId: String?) {
  }

  override fun onDone(speechId: String?) {
  }

  @Deprecated("Deprecated in Java")
  override fun onError(speechId: String?) {
  }
}
