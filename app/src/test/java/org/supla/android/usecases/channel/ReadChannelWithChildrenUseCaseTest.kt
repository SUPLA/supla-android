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

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.ChannelRepository
import org.supla.android.data.source.local.entity.complex.ChannelChildEntity
import org.supla.android.data.source.local.entity.complex.ChannelDataEntity

class ReadChannelWithChildrenUseCaseTest {

  @MockK
  lateinit var channelRepository: ChannelRepository

  @MockK
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMockKs
  lateinit var useCase: ReadChannelWithChildrenUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should load channel with children`() {
    // given
    val remoteId = 234

    val entity = mockk<ChannelDataEntity>()
    every { channelRepository.findChannelDataEntity(remoteId) } returns Observable.just(entity)

    val child = mockk<ChannelChildEntity>()
    every { channelRelationRepository.findChildrenForParent(remoteId) } returns Maybe.just(listOf(child))

    // when
    val observer = useCase.invoke(remoteId).test()

    // then
    observer.assertComplete()
    val result = observer.values()[0]

    assertThat(result.channel).isSameAs(entity)
    assertThat(result.children).containsExactly(child)

    verify { channelRepository.findChannelDataEntity(remoteId) }
    verify { channelRelationRepository.findChildrenForParent(remoteId) }
    confirmVerified(channelRelationRepository, channelRepository)
  }
}
