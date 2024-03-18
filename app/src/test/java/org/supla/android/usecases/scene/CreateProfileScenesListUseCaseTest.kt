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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.supla.android.data.source.RoomSceneRepository
import org.supla.android.data.source.local.entity.LocationEntity
import org.supla.android.data.source.local.entity.complex.SceneDataEntity
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.location.CollapsedFlag

@RunWith(MockitoJUnitRunner::class)
class CreateProfileScenesListUseCaseTest {

  @Mock
  private lateinit var sceneRepository: RoomSceneRepository

  @InjectMocks
  private lateinit var useCase: CreateProfileScenesListUseCase

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
      mockScene(firstLocation),
      mockScene(firstLocation),
      mockScene(collapsedLocation),
      mockScene(thirdLocation)
    )

    whenever(sceneRepository.findList()).thenReturn(Single.just(scenes))

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    Assertions.assertThat((list[1] as ListItem.SceneItem).sceneData).isEqualTo(scenes[0])
    Assertions.assertThat((list[2] as ListItem.SceneItem).sceneData).isEqualTo(scenes[1])
    Assertions.assertThat((list[5] as ListItem.SceneItem).sceneData).isEqualTo(scenes[3])

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[3] as ListItem.LocationItem).location).isEqualTo(collapsedLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
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
      mockScene(firstLocation),
      mockScene(firstLocation),
      mockScene(secondLocation),
      mockScene(thirdLocation)
    )

    whenever(sceneRepository.findList()).thenReturn(Single.just(scenes))

    // when
    val testObserver = useCase().test()

    // then
    testObserver.assertComplete()
    val list = testObserver.values()[0]

    Assertions.assertThat(list).hasSize(6)
    Assertions.assertThat(list[0]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[1]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[2]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[3]).isInstanceOf(ListItem.SceneItem::class.java)
    Assertions.assertThat(list[4]).isInstanceOf(ListItem.LocationItem::class.java)
    Assertions.assertThat(list[5]).isInstanceOf(ListItem.SceneItem::class.java)

    Assertions.assertThat((list[1] as ListItem.SceneItem).sceneData).isEqualTo(scenes[0])
    Assertions.assertThat((list[2] as ListItem.SceneItem).sceneData).isEqualTo(scenes[1])
    Assertions.assertThat((list[3] as ListItem.SceneItem).sceneData).isEqualTo(scenes[2])
    Assertions.assertThat((list[5] as ListItem.SceneItem).sceneData).isEqualTo(scenes[3])

    Assertions.assertThat((list[0] as ListItem.LocationItem).location).isEqualTo(firstLocation)
    Assertions.assertThat((list[4] as ListItem.LocationItem).location).isEqualTo(thirdLocation)
  }

  private fun mockScene(locationEntity: LocationEntity): SceneDataEntity {
    val scene: SceneDataEntity = mockk()
    every { scene.locationEntity } returns locationEntity
    return scene
  }

  private fun mockLocation(locationRemoteId: Int, name: String = "Location $locationRemoteId", collapsed: Boolean = false): LocationEntity {
    val location: LocationEntity = mockk()
    every { location.remoteId } returns locationRemoteId
    every { location.caption } returns name
    every { location.isCollapsed(CollapsedFlag.SCENE) } returns collapsed
    return location
  }
}
