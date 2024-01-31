package org.supla.android.usecases.channel

import io.reactivex.rxjava3.core.Maybe
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadChannelByRemoteIdUseCase @Inject constructor(private val channelRepository: RoomChannelRepository) {
  operator fun invoke(remoteId: Int): Maybe<ChannelDataEntity> =
    channelRepository.findChannelDataEntity(remoteId)
}
