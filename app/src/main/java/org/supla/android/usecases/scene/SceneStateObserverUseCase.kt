package org.supla.android.usecases.scene

import org.supla.android.data.source.SceneRepository
import org.supla.android.lib.SuplaClientMessageHandler
import org.supla.android.lib.SuplaClientMsg
import org.supla.android.scenes.SceneEventsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SceneStateObserverUseCase @Inject constructor(
  private val sceneEventsManager: SceneEventsManager,
  private val messageHandler: SuplaClientMessageHandler,
  private val sceneRepository: SceneRepository
) {

  private val listener = object : SuplaClientMessageHandler.OnSuplaClientMessageListener {
    override fun onSuplaClientMessageReceived(msg: SuplaClientMsg?) {
      if (msg?.type == SuplaClientMsg.onSceneStateChanged) {
        val scene = sceneRepository.getScene(msg.sceneId) ?: return
        sceneEventsManager.emitStateChange(
          scene.sceneId,
          SceneEventsManager.SceneState(scene.isExecuting(), scene.estimatedEndDate)
        )
      }
    }
  }

  fun register() {
    messageHandler.registerMessageListener(listener)
  }

  fun unregister() {
    messageHandler.unregisterMessageListener(listener)
  }
}