package org.supla.android.core.notifications

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

import android.app.NotificationManager
import android.content.Context
import androidx.work.ExistingWorkPolicy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.WorkManagerProxy
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.features.updatetoken.UpdateTokenWorker
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class NotificationsHelperTest {

  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var encryptedPreferences: EncryptedPreferences

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var notificationManager: NotificationManager

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var workManagerProxy: WorkManagerProxy

  @Mock
  private lateinit var dateProvider: DateProvider

  @InjectMocks
  private lateinit var notificationsHelper: NotificationsHelper

  @Before
  fun setUp() {
    val date = Date()
    whenever(dateProvider.currentTimestamp()).thenReturn(date.time)
    whenever(encryptedPreferences.fcmTokenLastUpdate).thenReturn(date)
  }

  @Test
  fun `should update token when token changed`() {
    // given
    val token = "token"
    whenever(encryptedPreferences.notificationsLastEnabled).thenReturn(true)
    whenever(encryptedPreferences.fcmToken).thenReturn("something")

    // when
    notificationsHelper.updateToken(token)

    // then
    verify(workManagerProxy).enqueueUniqueWork(eq(UpdateTokenWorker.WORK_ID), eq(ExistingWorkPolicy.KEEP), any())
    verify(encryptedPreferences).notificationsLastEnabled
    verify(encryptedPreferences).fcmToken
    verify(encryptedPreferences).notificationsLastEnabled = true
    verify(encryptedPreferences).fcmToken = token
    verifyNoMoreInteractions(workManagerProxy, encryptedPreferences)
  }

  @Test
  fun `should update token when notification enabled changed`() {
    // given
    val token = "token"
    whenever(encryptedPreferences.notificationsLastEnabled).thenReturn(false)
    whenever(encryptedPreferences.fcmToken).thenReturn("token")

    // when
    notificationsHelper.updateToken(token)

    // then
    verify(workManagerProxy).enqueueUniqueWork(eq(UpdateTokenWorker.WORK_ID), eq(ExistingWorkPolicy.KEEP), any())
    verify(encryptedPreferences).notificationsLastEnabled
    verify(encryptedPreferences).fcmTokenLastUpdate
    verify(encryptedPreferences).fcmToken
    verify(encryptedPreferences).notificationsLastEnabled = true
    verify(encryptedPreferences).fcmToken = token
    verifyNoMoreInteractions(workManagerProxy, encryptedPreferences)
  }

  @Test
  fun `should update token when time limit exceeded`() {
    // given
    val token = "token"
    whenever(encryptedPreferences.notificationsLastEnabled).thenReturn(true)
    whenever(encryptedPreferences.fcmToken).thenReturn("token")
    val date = Date()
    whenever(dateProvider.currentTimestamp()).thenReturn(date.time + UpdateTokenWorker.UPDATE_PAUSE_IN_DAYS.times(ONE_DAY_MILLIS))
    whenever(encryptedPreferences.fcmTokenLastUpdate).thenReturn(date)

    // when
    notificationsHelper.updateToken(token)

    // then
    verify(workManagerProxy).enqueueUniqueWork(eq(UpdateTokenWorker.WORK_ID), eq(ExistingWorkPolicy.KEEP), any())
    verify(encryptedPreferences).notificationsLastEnabled
    verify(encryptedPreferences).fcmTokenLastUpdate
    verify(encryptedPreferences).fcmToken
    verify(encryptedPreferences).notificationsLastEnabled = true
    verify(encryptedPreferences).fcmToken = token
    verifyNoMoreInteractions(workManagerProxy, encryptedPreferences)
  }

  @Test
  fun `should not update token when all conditions not matched`() {
    // given
    val token = "token"
    whenever(encryptedPreferences.notificationsLastEnabled).thenReturn(true)
    whenever(encryptedPreferences.fcmToken).thenReturn("token")

    // when
    notificationsHelper.updateToken(token)

    // then
    verify(workManagerProxy, never()).enqueueUniqueWork(eq(UpdateTokenWorker.WORK_ID), eq(ExistingWorkPolicy.KEEP), any())
    verify(encryptedPreferences).notificationsLastEnabled
    verify(encryptedPreferences).fcmTokenLastUpdate
    verify(encryptedPreferences).fcmToken
    verifyNoMoreInteractions(workManagerProxy, encryptedPreferences)
  }
}

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000
