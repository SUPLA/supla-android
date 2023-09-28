package org.supla.android.usecases.channel

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.Channel
import org.supla.android.profile.ProfileManager
import org.supla.android.usecases.channelrelation.FindChannelChildrenUseCase

@RunWith(MockitoJUnitRunner::class)
class ReadChannelWithChildrenUseCaseTest {

  @Mock
  lateinit var profileManager: ProfileManager

  @Mock
  lateinit var channelRepository: ChannelRepository

  @Mock
  lateinit var findChannelChildrenUseCase: FindChannelChildrenUseCase

  @InjectMocks
  lateinit var useCase: ReadChannelWithChildrenUseCase

  @Test
  fun `should load channel with children`() {
    // given
    val profileId = 123L
    val remoteId = 234

    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns profileId
    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))

    val children = listOf(mockk<ChannelChild>())
    whenever(findChannelChildrenUseCase(profileId, remoteId)).thenReturn(Maybe.just(children))

    val channel = mockk<Channel>()
    whenever(channelRepository.getChannel(remoteId)).thenReturn(channel)

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    val result = observer.values()[0]
    assertThat(result.channel).isSameAs(channel)
    assertThat(result.children).isSameAs(children)

    verify(profileManager).getCurrentProfile()
    verify(findChannelChildrenUseCase).invoke(profileId, remoteId)
    verify(channelRepository).getChannel(remoteId)
    verifyNoMoreInteractions(profileManager, findChannelChildrenUseCase, channelRepository)
  }
}
