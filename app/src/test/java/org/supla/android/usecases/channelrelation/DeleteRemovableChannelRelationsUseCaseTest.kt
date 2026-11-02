package org.supla.android.usecases.channelrelation
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
import io.reactivex.rxjava3.core.Completable
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.ChannelRelationRepository

class DeleteRemovableChannelRelationsUseCaseTest {

  @MockK
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMockKs
  lateinit var useCase: DeleteRemovableChannelRelationsUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should invoke relation repository`() {
    // given
    every { channelRelationRepository.cleanUnused() } returns Completable.complete()

    // when
    val observer = useCase().test()

    // then
    observer.assertComplete()
  }
}
