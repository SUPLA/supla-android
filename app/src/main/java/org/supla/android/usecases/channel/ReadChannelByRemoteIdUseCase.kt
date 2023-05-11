package org.supla.android.usecases.channel

import io.reactivex.rxjava3.core.Maybe
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.Channel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadChannelByRemoteIdUseCase @Inject constructor(private val channelRepository: ChannelRepository) {
  operator fun invoke(remoteId: Int): Maybe<Channel> = Maybe.fromCallable {
    channelRepository.getChannel(remoteId)
  }
}