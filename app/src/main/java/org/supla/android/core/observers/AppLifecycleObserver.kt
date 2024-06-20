package org.supla.android.core.observers

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.supla.android.Trace
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.extensions.TAG
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class AppLifecycleObserver @Inject constructor() : DefaultLifecycleObserver {

  @Inject
  lateinit var suplaClientProvider: SuplaClientProvider

  var clientTerminator: Job? = null

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
    if (clientTerminator?.isActive == true) {
      Trace.d(TAG, "Supla client process terminator was active - stopping")
      clientTerminator?.cancel()
    }
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    clientTerminator = GlobalScope.launch {
      Trace.d(TAG, "Starting supla client process terminator")
      delay(30.seconds)
      suplaClientProvider.provide()?.cancel()
      Trace.d(TAG, "Supla client process terminated")
    }
  }
}
