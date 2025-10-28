package org.supla.android.features.notificationslog
/*
 Copyright (C) AC SOFTWARE SP. Z O.O.

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
syays GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.notifications.DeleteNotificationUseCase
import org.supla.android.usecases.notifications.DeleteNotificationsUseCase
import org.supla.android.usecases.notifications.LoadAllNotificationsUseCase

class NotificationsLogViewModelTest : BaseViewModelTest<NotificationsLogViewState, NotificationsLogViewEvent, NotificationsLogViewModel>(
  MockSchedulers.MOCKK
) {

  @MockK
  private lateinit var loadAllNotificationsUseCase: LoadAllNotificationsUseCase

  @MockK
  private lateinit var deleteNotificationUseCase: DeleteNotificationUseCase

  @MockK
  private lateinit var deleteNotificationsUseCase: DeleteNotificationsUseCase

  @MockK
  override lateinit var schedulers: SuplaSchedulers

  @InjectMockKs
  override lateinit var viewModel: NotificationsLogViewModel

  @Before
  override fun setUp() {
    MockKAnnotations.init(this)
    super.setUp()
  }

  @Test
  fun `should show deletion popup`() {
    // when
    viewModel.askDeleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.ALL)
    )
    confirmVerified(loadAllNotificationsUseCase, deleteNotificationUseCase, deleteNotificationsUseCase)
  }

  @Test
  fun `should close deletion popup`() {
    // given
    viewModel.askDeleteAll()

    // when
    viewModel.cancelDeleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.ALL),
      NotificationsLogViewState()
    )
    confirmVerified(loadAllNotificationsUseCase, deleteNotificationUseCase, deleteNotificationsUseCase)
  }

  @Test
  fun `should load all notifications from database`() {
    // given
    val entityId = 123L
    val entity = mockk<NotificationEntity> { every { id } returns entityId }
    val list = listOf(entity)
    every { loadAllNotificationsUseCase.invoke() } returns Observable.just(list)

    // when
    viewModel.onViewCreated()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(items = listOf(NotificationItem(entity, false)))
    )
  }

  @Test
  fun `should delete all notifications`() {
    // given
    viewModel.askDeleteAll()
    every { deleteNotificationsUseCase.invoke(DeleteNotificationsUseCase.Action.ALL) } returns Completable.complete()

    // when
    viewModel.deleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.ALL),
      NotificationsLogViewState()
    )
  }

  @Test
  fun `should delete notifications older than month`() {
    // given
    viewModel.askDeleteOlderThanMonth()
    every { deleteNotificationsUseCase.invoke(DeleteNotificationsUseCase.Action.OLDER_THAN_MONTH) } returns Completable.complete()

    // when
    viewModel.deleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true, deleteAction = DeleteNotificationsUseCase.Action.OLDER_THAN_MONTH),
      NotificationsLogViewState()
    )
  }

  @Test
  fun `should delete single notification`() {
    // given
    val notificationId = 123L
    val entity: NotificationEntity = mockk {
      every { id } returns notificationId
    }

    every { loadAllNotificationsUseCase.invoke() } returns Observable.just(listOf(entity))
    every { deleteNotificationUseCase.invoke(notificationId) } returns Completable.complete()

    // when
    viewModel.onViewCreated()
    viewModel.delete(entity)

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(items = listOf(NotificationItem(entity, false))),
      NotificationsLogViewState(items = listOf(NotificationItem(entity, true)))
    )
    assertThat(events).containsExactly(NotificationsLogViewEvent.ShowDeleteNotification(notificationId))

    verify {
      loadAllNotificationsUseCase.invoke()
      deleteNotificationUseCase.invoke(notificationId)
    }
    confirmVerified(loadAllNotificationsUseCase, deleteNotificationsUseCase)
  }
}
