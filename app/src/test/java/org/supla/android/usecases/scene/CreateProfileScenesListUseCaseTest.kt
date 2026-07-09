package org.supla.android.usecases.scene
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

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.data.source.SceneRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.SceneEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.images.ImageId
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.location.CollapsedFlag

class CreateProfileScenesListUseCaseTest {
  @MockK
  private lateinit var getSceneIconUseCase: GetSceneIconUseCase

  @MockK
  private lateinit var sceneRepository: SceneRepository

  @InjectMockKs
  private lateinit var useCase: CreateProfileScenesListUseCase

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create list of scenes with locations`() {
    // given
    val firstLocationId = 2
    val collapsedLocationId = 4
    val thirdLocationId = 8

    val firstLocation = mockLocation(firstLocationId)
    val collapsedLocation = mockLocation(collapsedLocationId, collapsed = true)
    val thirdLocation = mockLocation(thirdLocationId)

    val scenes = listOf(
      mockScene(111, firstLocation),
      mockScene(112, firstLocation),
      mockScene(113, collapsedLocation),
      mockScene(114, thirdLocation)
    )

    every { sceneRepository.findList() } returns Single.just(scenes)

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    assertThat((list[1] as ListItem.SceneItem).remoteId).isEqualTo(111)
    assertThat((list[2] as ListItem.SceneItem).remoteId).isEqualTo(112)
    assertThat((list[5] as ListItem.SceneItem).remoteId).isEqualTo(114)

    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(firstLocationId)
    assertThat((list[3] as ListItem.LocationItem).remoteId).isEqualTo(collapsedLocationId)
    assertThat((list[4] as ListItem.LocationItem).remoteId).isEqualTo(thirdLocationId)
  }

  @Test
  fun `should create list of scenes with locations - filtered`() {
    // given
    val firstLocationId = 2
    val collapsedLocationId = 4
    val thirdLocationId = 8

    val firstLocation = mockLocation(firstLocationId)
    val collapsedLocation = mockLocation(collapsedLocationId, collapsed = true)
    val thirdLocation = mockLocation(thirdLocationId)

    val scenes = listOf(
      mockScene(111, firstLocation),
      mockScene(112, firstLocation),
      mockScene(123, collapsedLocation),
      mockScene(124, thirdLocation)
    )

    every { sceneRepository.findList() } returns Single.just(scenes)

    // when
    val testObserver = useCase("caption 11").test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(3)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)

    assertThat((list[1] as ListItem.SceneItem).remoteId).isEqualTo(111)
    assertThat((list[2] as ListItem.SceneItem).remoteId).isEqualTo(112)

    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(firstLocationId)
  }

  @Test
  fun `should merge location with same name into one`() {
    // given
    val firstLocationId = 2
    val secondLocationId = 4
    val thirdLocationId = 8

    val firstLocation = mockLocation(firstLocationId, "Test")
    val secondLocation = mockLocation(secondLocationId, "Test")
    val thirdLocation = mockLocation(thirdLocationId)

    val scenes = listOf(
      mockScene(111, firstLocation),
      mockScene(112, firstLocation),
      mockScene(113, secondLocation),
      mockScene(114, thirdLocation)
    )

    every { sceneRepository.findList() } returns Single.just(scenes)

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    assertThat(list).hasSize(6)
    assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[3]).isInstanceOf(ListItem.SceneItem::class.java)
    assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    assertThat((list[1] as ListItem.SceneItem).remoteId).isEqualTo(111)
    assertThat((list[2] as ListItem.SceneItem).remoteId).isEqualTo(112)
    assertThat((list[3] as ListItem.SceneItem).remoteId).isEqualTo(113)
    assertThat((list[5] as ListItem.SceneItem).remoteId).isEqualTo(114)

    assertThat((list[0] as ListItem.LocationItem).remoteId).isEqualTo(firstLocationId)
    assertThat((list[4] as ListItem.LocationItem).remoteId).isEqualTo(thirdLocationId)
  }

  private fun mockScene(remoteId: Int, locationEntity: LocationEntity): SceneDataEntity {
    val sceneEntity: SceneEntity = mockk {
      every { profileId } returns "1"
      every { caption } returns "caption $remoteId"
      every { estimatedEndDate } returns null
    }
    val scene: SceneDataEntity = mockk()
    every { scene.remoteId } returns remoteId
    every { scene.locationEntity } returns locationEntity
    every { scene.sceneEntity } returns sceneEntity

    every { getSceneIconUseCase.invoke(sceneEntity) } returns ImageId(0)

    return scene
  }

  private fun mockLocation(locationRemoteId: Int, name: String = "Location $locationRemoteId", collapsed: Boolean = false): LocationEntity {
    val location: LocationEntity = mockk()
    every { location.profileId } returns 1L
    every { location.remoteId } returns locationRemoteId
    every { location.caption } returns name
    every { location.isCollapsed(CollapsedFlag.SCENE) } returns collapsed
    return location
  }
}
