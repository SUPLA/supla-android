package org.supla.android.usecases.location

import io.reactivex.rxjava3.core.Completable
import org.supla.android.data.source.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleLocationUseCase @Inject constructor(
  private val locationRepository: LocationRepository
) {

  operator fun invoke(remoteId: Int, flag: CollapsedFlag): Completable =
    locationRepository.findByRemoteId(remoteId)
      .flatMapCompletable { location ->
        if (location.isCollapsed(flag)) {
          locationRepository.updateLocation(
            location.copy(collapsed = (location.collapsed and flag.value.inv()))
          )
        } else {
          locationRepository.updateLocation(
            location.copy(collapsed = (location.collapsed or flag.value))
          )
        }
      }
}

enum class CollapsedFlag(val value: Int) {
  CHANNEL(0x1),
  GROUP(0x2),
  SCENE(0x8)
}
