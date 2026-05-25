package org.supla.android.usecases.icon

import org.supla.android.data.source.ChannelGroupRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.SceneRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FindIconsToDownloadUseCase @Inject constructor(
  private val channelGroupRepository: ChannelGroupRepository,
  private val channelRepository: RoomChannelRepository,
  private val sceneRepository: SceneRepository
) {
  suspend operator fun invoke(profileId: Long): List<Int> {
    val result = LinkedHashSet<Int>()
    result.addAll(channelGroupRepository.findIconIdsToDownload(profileId))
    result.addAll(channelRepository.findIconIdsToDownload(profileId))
    result.addAll(sceneRepository.findIconIdsToDownload(profileId))
    return result.toList()
  }
}
