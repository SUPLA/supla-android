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

import io.reactivex.rxjava3.core.Completable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRelationRepository

@RunWith(MockitoJUnitRunner::class)
class DeleteRemovableChannelRelationsUseCaseTest {

  @Mock
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMocks
  lateinit var useCase: DeleteRemovableChannelRelationsUseCase

  @Test
  fun `should invoke relation repository`() {
    // given
    whenever(channelRelationRepository.cleanUnused()).thenReturn(Completable.complete())

    // when
    val observer = useCase().test()

    // then
    observer.assertComplete()
  }
}
