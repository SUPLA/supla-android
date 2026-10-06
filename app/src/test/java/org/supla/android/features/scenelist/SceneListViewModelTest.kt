package org.supla.android.features.scenelist
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

import android.net.Uri
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaThreading
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase
import org.supla.android.usecases.scene.ReorderScenesUseCase
import java.util.concurrent.TimeUnit

class SceneListViewModelTest : BaseViewModelTest<SceneListViewState, SceneListViewEvent, SceneListViewModel>(MockSchedulers.MOCKK) {
  @get:Rule
  override val mainDispatcherRule = MainDispatcherRule()

  @MockK
  private lateinit var toggleLocationUseCase: ToggleLocationUseCase

  @MockK
  private lateinit var createProfileScenesListUseCase: CreateProfileScenesListUseCase

  @MockK
  private lateinit var executeSimpleActionUseCase: ExecuteSimpleActionUseCase

  @MockK
  private lateinit var reorderScenesUseCase: ReorderScenesUseCase

  @MockK
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @MockK
  override lateinit var threading: SuplaThreading

  override val viewModel: SceneListViewModel by lazy {
    SceneListViewModel(
      createProfileScenesListUseCase,
      executeSimpleActionUseCase,
      reorderScenesUseCase,
      toggleLocationUseCase,
      loadActiveProfileUrlUseCase,
      vibrationHelper,
      threading,
      dateProvider
    )
  }

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { threading.schedulers.computation } returns testScheduler
    super.setUp()
  }

  @Test
  fun `should load scenes`() {
    // given
    val item = mockk<ListItem.SceneItem>()
    val items = listOf(item)
    every { createProfileScenesListUseCase() } returns Observable.just(items)

    // when
    triggerScenesLoad()

    // then
    assertThat(viewModel.list).containsExactly(item)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should update scenes order`() {
    // given
    val header: ListItem.LocationItem = mockk()
    val firstItem: ListItem.SceneItem = mockk()
    val secondItem: ListItem.SceneItem = mockk()
    val thirdItem: ListItem.SceneItem = mockk()
    val scenes = listOf(header, firstItem, secondItem, thirdItem)
    every { createProfileScenesListUseCase() } returns Observable.just(scenes)

    // when
    triggerScenesLoad()
    viewModel.moveItems(1, 3)

    // then
    assertThat(viewModel.list).containsExactly(header, secondItem, thirdItem, firstItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should not swap when items are in different location`() {
    // given
    val firstHeader: ListItem.LocationItem = mockk()
    val secondHeader: ListItem.LocationItem = mockk()
    val firstItem: ListItem.SceneItem = mockk()
    val secondItem: ListItem.SceneItem = mockk()
    val thirdItem: ListItem.SceneItem = mockk()
    val scenes = listOf(firstHeader, firstItem, secondItem, secondHeader, thirdItem)
    every { createProfileScenesListUseCase() } returns Observable.just(scenes)

    // when
    triggerScenesLoad()
    viewModel.moveItems(1, 4)

    // then
    assertThat(viewModel.list).containsExactly(firstHeader, firstItem, secondItem, secondHeader, thirdItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should not swap when different items`() {
    // given
    val firstItem: ListItem.LocationItem = mockk()
    val secondItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val scenes = listOf(firstItem, secondItem, thirdItem)
    every { createProfileScenesListUseCase() } returns Observable.just(scenes)

    // when
    triggerScenesLoad()
    viewModel.moveItems(2, 0)

    // then
    assertThat(viewModel.list).containsExactly(firstItem, secondItem, thirdItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should toggle location collapsed`() {
    // given
    val locationId = 1
    every { toggleLocationUseCase(locationId, CollapsedFlag.SCENE) } returns Completable.complete()
    // when
    viewModel.onLocationClick(locationId)

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify { toggleLocationUseCase(locationId, CollapsedFlag.SCENE) }
    confirmDependencies()
  }

  @Test
  fun `should update list when scene data changes`() {
    // given
    val item = mockk<ListItem.SceneItem> { every { key } returns "S1" }
    val updatedItem = mockk<ListItem.SceneItem> { every { key } returns "S2" }
    val scenesSubject = PublishSubject.create<List<ListItem>>()
    every { createProfileScenesListUseCase() } returns scenesSubject

    // when
    triggerScenesLoad()
    scenesSubject.onNext(listOf(item))
    scenesSubject.onNext(listOf(updatedItem))

    // then
    assertThat(viewModel.list).containsExactly(updatedItem)
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase() }
    confirmDependencies()
  }

  @Test
  fun `should set filter text and reload scenes`() {
    // given
    val filterText = "kitchen"
    val item = mockk<ListItem.SceneItem>()
    val list = listOf(item)
    every { createProfileScenesListUseCase(filterText) } returns Observable.just(list)

    // when
    viewModel.handle(TopBarSearchEvent.QueryChange(filterText))
    testScheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS)

    // then
    assertThat(viewModel.searchData.query).isEqualTo(filterText)
    assertThat(viewModel.list).containsExactly(item)
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase(filterText) }
    confirmDependencies()
  }

  @Test
  fun `should execute interrupt action on left button click`() {
    // given
    val remoteId = 123
    every { executeSimpleActionUseCase(ActionId.INTERRUPT, SubjectType.SCENE, remoteId) } returns Completable.complete()

    // when
    viewModel.onLeftButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { executeSimpleActionUseCase(ActionId.INTERRUPT, SubjectType.SCENE, remoteId) }
    confirmVerified(executeSimpleActionUseCase)
    confirmDependencies()
  }

  @Test
  fun `should execute scene action on right button click`() {
    // given
    val remoteId = 123
    every { executeSimpleActionUseCase(ActionId.EXECUTE, SubjectType.SCENE, remoteId) } returns Completable.complete()

    // when
    viewModel.onRightButtonClick(remoteId)

    // then
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { executeSimpleActionUseCase(ActionId.EXECUTE, SubjectType.SCENE, remoteId) }
    confirmVerified(executeSimpleActionUseCase)
    confirmDependencies()
  }

  @Test
  fun `should store scene order and apply next list emission when drag stops`() {
    // given
    val remoteId = 123
    val firstItem: ListItem.SceneItem = mockk {
      every { key } returns "S1"
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.SceneItem = mockk {
      every { key } returns "S2"
      every { locationCaption } returns "1"
    }
    val scenes = listOf<ListItem>(firstItem, secondItem)
    val reorderedScenes = listOf<ListItem>(secondItem, firstItem)
    coEvery { reorderScenesUseCase(match { it.toList() == scenes }, remoteId) } returns Unit
    val scenesSubject = PublishSubject.create<List<ListItem>>()
    every { createProfileScenesListUseCase() } returns scenesSubject

    // when
    triggerScenesLoad()
    scenesSubject.onNext(scenes)
    viewModel.onDragStopped(remoteId)
    scenesSubject.onNext(reorderedScenes)

    // then
    assertThat(viewModel.list).containsExactly(secondItem, firstItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    coVerify { reorderScenesUseCase(any(), remoteId) }
    verify { createProfileScenesListUseCase() }
    confirmVerified(createProfileScenesListUseCase, reorderScenesUseCase)
    confirmDependencies()
  }

  @Test
  fun `on add group click should open supla cloud`() {
    // given
    every { loadActiveProfileUrlUseCase() } returns Single.just(CloudUrl.DefaultCloud)

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(SceneListViewEvent.NavigateToSuplaCloud)
    verify { loadActiveProfileUrlUseCase() }
    confirmVerified(loadActiveProfileUrlUseCase)
    confirmDependencies()
  }

  @Test
  fun `on add group click should open private cloud`() {
    // given
    val url: Uri = mockk()
    every { loadActiveProfileUrlUseCase() } returns Single.just(CloudUrl.ServerUri(url))

    // when
    viewModel.onAddGroupClick()

    // then
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).containsExactly(SceneListViewEvent.NavigateToPrivateCloud(url))
    verify { loadActiveProfileUrlUseCase() }
    confirmVerified(loadActiveProfileUrlUseCase)
    confirmDependencies()
  }

  private fun confirmDependencies() {
    confirmVerified(
      toggleLocationUseCase,
      createProfileScenesListUseCase,
      reorderScenesUseCase,
      executeSimpleActionUseCase,
      loadActiveProfileUrlUseCase,
      dateProvider
    )
  }

  private fun triggerScenesLoad() {
    viewModel.handle(TopBarSearchEvent.QueryChange(viewModel.searchData.query))
    testScheduler.advanceTimeBy(250, TimeUnit.MILLISECONDS)
  }
}
