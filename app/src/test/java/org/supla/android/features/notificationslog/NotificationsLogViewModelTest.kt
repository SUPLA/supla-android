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

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.core.BaseViewModelTest
import org.supla.android.data.source.local.entity.NotificationEntity
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.notifications.DeleteAllNotificationsUseCase
import org.supla.android.usecases.notifications.DeleteNotificationUseCase
import org.supla.android.usecases.notifications.LoadAllNotificationsUseCase

@RunWith(MockitoJUnitRunner::class)
class NotificationsLogViewModelTest : BaseViewModelTest<NotificationsLogViewState, NotificationsLogViewEvent, NotificationsLogViewModel>() {

  @Mock
  private lateinit var loadAllNotificationsUseCase: LoadAllNotificationsUseCase

  @Mock
  private lateinit var deleteNotificationUseCase: DeleteNotificationUseCase

  @Mock
  private lateinit var deleteAllNotificationsUseCase: DeleteAllNotificationsUseCase

  @Mock
  override lateinit var schedulers: SuplaSchedulers

  @InjectMocks
  override lateinit var viewModel: NotificationsLogViewModel

  @Before
  override fun setUp() {
    super.setUp()
  }

  @Test
  fun `should show deletion popup`() {
    // when
    viewModel.askDeleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true)
    )
    verifyZeroInteractions(loadAllNotificationsUseCase, deleteNotificationUseCase, deleteAllNotificationsUseCase)
  }

  @Test
  fun `should close deletion popup`() {
    // given
    viewModel.askDeleteAll()

    // when
    viewModel.cancelDeleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true),
      NotificationsLogViewState()
    )
    verifyZeroInteractions(loadAllNotificationsUseCase, deleteNotificationUseCase, deleteAllNotificationsUseCase)
  }

  @Test
  fun `should load all notifications from database`() {
    // given
    val entityId = 123L
    val entity = mockk<NotificationEntity> { every { id } returns entityId }
    val list = listOf(entity)
    whenever(loadAllNotificationsUseCase.invoke()).thenReturn(Observable.just(list))

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
    whenever(deleteAllNotificationsUseCase.invoke()).thenReturn(Completable.complete())

    // when
    viewModel.deleteAll()

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(showDeletionDialog = true),
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

    whenever(loadAllNotificationsUseCase.invoke()).thenReturn(Observable.just(listOf(entity)))
    whenever(deleteNotificationUseCase.invoke(notificationId)).thenReturn(Completable.complete())

    // when
    viewModel.onViewCreated()
    viewModel.delete(entity)

    // then
    assertThat(states).containsExactly(
      NotificationsLogViewState(items = listOf(NotificationItem(entity, false))),
      NotificationsLogViewState(items = listOf(NotificationItem(entity, true)))
    )
    assertThat(events).containsExactly(NotificationsLogViewEvent.ShowDeleteNotification(notificationId))

    verify(loadAllNotificationsUseCase).invoke()
    verify(deleteNotificationUseCase).invoke(notificationId)
    verifyZeroInteractions(loadAllNotificationsUseCase, deleteAllNotificationsUseCase)
  }
}
