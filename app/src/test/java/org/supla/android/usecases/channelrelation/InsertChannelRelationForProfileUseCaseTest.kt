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
import io.reactivex.rxjava3.core.Maybe
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.capture
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.ChannelRelationRepository
import org.supla.android.data.source.local.entity.ChannelRelationEntity
import org.supla.android.db.AuthProfileItem
import org.supla.android.lib.SuplaChannelRelation
import org.supla.android.profile.ProfileManager
import org.supla.android.testhelpers.profileMock
import org.supla.android.testhelpers.relationMock
import org.supla.core.shared.data.model.channel.ChannelRelationType

@Suppress("SameParameterValue")
@RunWith(MockitoJUnitRunner::class)
class InsertChannelRelationForProfileUseCaseTest {

  @Mock
  lateinit var profileManager: ProfileManager

  @Mock
  lateinit var channelRelationRepository: ChannelRelationRepository

  @InjectMocks
  lateinit var useCase: InsertChannelRelationForProfileUseCase

  @Test
  fun `should insert relation`() {
    // given
    val profileId = 321L
    val channelId = 123
    val parentId = 234
    val relationType = ChannelRelationType.MAIN_THERMOMETER

    val relation: SuplaChannelRelation = relationMock(channelId, parentId, relationType)
    val profile: AuthProfileItem = profileMock(profileId)

    whenever(profileManager.getCurrentProfile()).thenReturn(Maybe.just(profile))
    whenever(channelRelationRepository.insertOrUpdate(any())).thenReturn(Completable.complete())

    // when
    val observer = useCase(relation).test()

    // then
    observer.assertComplete()
    val entityCaptor: ArgumentCaptor<ChannelRelationEntity> = ArgumentCaptor.forClass(ChannelRelationEntity::class.java)
    verify(channelRelationRepository).insertOrUpdate(capture(entityCaptor))

    val entity = entityCaptor.value
    assertThat(entity.channelId).isEqualTo(channelId)
    assertThat(entity.parentId).isEqualTo(parentId)
    assertThat(entity.relationType).isEqualTo(relationType)
    assertThat(entity.profileId).isEqualTo(profileId)
    assertThat(entity.deleteFlag).isFalse

    verify(profileManager).getCurrentProfile()
    verifyNoMoreInteractions(profileManager, channelRelationRepository)
  }
}
