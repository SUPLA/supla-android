package org.supla.android.usecases.location

import io.reactivex.rxjava3.core.Completable
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleLocationUseCase @Inject constructor(
  private val channelRepository: ChannelRepository
) {

  operator fun invoke(location: Location, flag: CollapsedFlag): Completable = Completable.fromRunnable {
    if (location.isCollapsed(flag)) {
      location.collapsed = (location.collapsed and flag.value.inv())
    } else {
      location.collapsed = (location.collapsed or flag.value)
    }

    channelRepository.updateLocation(location)
  }
}

fun Location.isCollapsed(flag: CollapsedFlag): Boolean = (collapsed and flag.value > 0)

enum class CollapsedFlag(val value: Int) {
  CHANNEL(0x1),
  GROUP(0x2),
  SCENE(0x8)
}