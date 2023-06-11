package org.supla.android.usecases.scene

import io.reactivex.rxjava3.core.Observable
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.SceneRepository
import org.supla.android.db.Location
import org.supla.android.db.entity.Scene
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.isCollapsed
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateProfileScenesListUseCase @Inject constructor(
  private val sceneRepository: SceneRepository,
  private val channelRepository: ChannelRepository
) {
  operator fun invoke(): Observable<List<ListItem>> =
    sceneRepository.getAllProfileScenes()
      .map(this::sceneToListItem)

  private fun sceneToListItem(scenes: List<Scene>): List<ListItem> {
    val result = mutableListOf<ListItem>()

    var location: Location? = null
    for (scene in scenes) {
      if (location == null || location.locationId != scene.locationId) {
        val newLocation = channelRepository.getLocation(scene.locationId)

        if (location == null || newLocation.caption != location.caption) {
          location = newLocation
          result.add(ListItem.LocationItem(location))
        }
      }

      if (location?.isCollapsed(CollapsedFlag.SCENE) == false) {
        result.add(ListItem.SceneItem(scene, location))
      }
    }

    return result
  }
}
