package org.supla.android.usecases.channel
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
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.RoomChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity

@RunWith(MockitoJUnitRunner::class)
class ReadChannelWithChildrenUseCaseTest {

  @Mock
  lateinit var channelRepository: RoomChannelRepository

  @Mock
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMocks
  lateinit var useCase: ReadChannelWithChildrenUseCase

  @Test
  fun `should load channel with children`() {
    // given
    val remoteId = 234

    val entity = mockk<ChannelDataEntity>()
    whenever(channelRepository.findChannelDataEntity(remoteId)).thenReturn(Maybe.just(entity))

    val child = mockk<ChannelChildEntity>()
    whenever(channelRelationRepository.findChildrenForParent(remoteId)).thenReturn(Maybe.just(listOf(child)))

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    val result = observer.values()[0]

    assertThat(result.channel).isSameAs(entity)
    assertThat(result.children).containsExactly(child)

    verify(channelRepository).findChannelDataEntity(remoteId)
    verify(channelRelationRepository).findChildrenForParent(remoteId)
    verifyNoMoreInteractions(channelRelationRepository, channelRepository)
  }
}
