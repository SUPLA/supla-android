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
import io.reactivex.rxjava3.subjects.Subject
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.core.MainDispatcherRule
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.SceneRepository
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.actions.ActionId
import org.supla.android.lib.actions.SubjectType
import org.supla.android.main.topbar.TopBarSearchEvent
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.tools.VibrationHelper
import org.supla.android.ui.lists.ListItem
import org.supla.android.usecases.client.ExecuteSimpleActionUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import org.supla.android.usecases.location.CollapsedFlag
import org.supla.android.usecases.location.ToggleLocationUseCase
import org.supla.android.usecases.profile.CloudUrl
import org.supla.android.usecases.profile.LoadActiveProfileUrlUseCase
import org.supla.android.usecases.scene.CreateProfileScenesListUseCase
import org.supla.android.usecases.scene.ReorderScenesUseCase

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
  private lateinit var getSceneIconUseCase: GetSceneIconUseCase

  @MockK
  private lateinit var reorderScenesUseCase: ReorderScenesUseCase

  @MockK
  private lateinit var sceneRepository: SceneRepository

  @MockK
  private lateinit var updateEventsManager: UpdateEventsManager

  @MockK
  private lateinit var loadActiveProfileUrlUseCase: LoadActiveProfileUrlUseCase

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var vibrationHelper: VibrationHelper

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  override val viewModel: SceneListViewModel by lazy {
    SceneListViewModel(
      createProfileScenesListUseCase,
      executeSimpleActionUseCase,
      reorderScenesUseCase,
      toggleLocationUseCase,
      getSceneIconUseCase,
      sceneRepository,
      loadActiveProfileUrlUseCase,
      updateEventsManager,
      vibrationHelper,
      schedulers,
      dateProvider
    )
  }

  private val listsEventsSubject: Subject<Any> = PublishSubject.create()

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    every { updateEventsManager.observeScenesUpdate() } returns listsEventsSubject
    every { updateEventsManager.observeAllScenes() } returns Observable.empty()
    super.setUp()
  }

  @Test
  fun `should load scenes`() {
    // given
    val item = mockk<ListItem.SceneItem>()
    val items = listOf(item)
    every { createProfileScenesListUseCase() } returns Observable.just(items)

    // when
    viewModel.loadScenes()

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
    val firstItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val scenes = listOf(firstItem, secondItem, thirdItem)
    every { createProfileScenesListUseCase() } returns Observable.just(scenes)

    // when
    viewModel.loadScenes()
    viewModel.moveItems(0, 2)

    // then
    assertThat(viewModel.list).containsExactly(secondItem, thirdItem, firstItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should not swap when items are in different location`() {
    // given
    val firstItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val thirdItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "2"
    }
    val scenes = listOf(firstItem, secondItem, thirdItem)
    every { createProfileScenesListUseCase() } returns Observable.just(scenes)

    // when
    viewModel.loadScenes()
    viewModel.moveItems(0, 2)

    // then
    assertThat(viewModel.list).containsExactly(firstItem, secondItem, thirdItem)
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
    viewModel.loadScenes()
    viewModel.moveItems(2, 0)

    // then
    assertThat(viewModel.list).containsExactly(firstItem, secondItem, thirdItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    verify { createProfileScenesListUseCase.invoke() }
    confirmDependencies()
  }

  @Test
  fun `should toggle location collapsed and reload scenes`() {
    // given
    val locationId = 1
    every { toggleLocationUseCase(locationId, CollapsedFlag.SCENE) } returns Completable.complete()
    val item = mockk<ListItem.SceneItem>()
    val list = listOf(item)
    every { createProfileScenesListUseCase() } returns Observable.just(list)

    // when
    viewModel.onLocationClick(locationId)

    // then
    assertThat(viewModel.list).containsExactly(item)
    Assertions.assertThat(states).isEmpty()
    Assertions.assertThat(events).isEmpty()

    verify { toggleLocationUseCase(locationId, CollapsedFlag.SCENE) }
    verify { createProfileScenesListUseCase() }
    confirmDependencies()
  }

  @Test
  fun `should reload list on update`() {
    // given
    val item = mockk<ListItem.SceneItem>()
    val list = listOf(item)
    every { createProfileScenesListUseCase() } returns Observable.just(list)

    // when
    listsEventsSubject.onNext(Any())

    // then
    assertThat(viewModel.list).containsExactly(item)
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
  fun `should reorder scenes and reload list when drag stops`() {
    // given
    val remoteId = 123
    val firstItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val secondItem: ListItem.SceneItem = mockk {
      every { locationCaption } returns "1"
    }
    val scenes = listOf<ListItem>(firstItem, secondItem)
    val reorderedScenes = listOf<ListItem>(secondItem, firstItem)
    coEvery { reorderScenesUseCase(match { it.toList() == scenes }, remoteId) } returns Unit
    every { createProfileScenesListUseCase() } returnsMany listOf(Observable.just(scenes), Observable.just(reorderedScenes))

    // when
    viewModel.loadScenes()
    viewModel.onDragStopped(remoteId)

    // then
    assertThat(viewModel.list).containsExactly(secondItem, firstItem)
    assertThat(states).isEmpty()
    assertThat(events).isEmpty()

    coVerify { reorderScenesUseCase(any(), remoteId) }
    verify(exactly = 2) { createProfileScenesListUseCase() }
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
      getSceneIconUseCase,
      sceneRepository,
      loadActiveProfileUrlUseCase,
      dateProvider
    )
  }
}
