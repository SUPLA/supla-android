package org.supla.android.usecases.app
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

import android.content.Context
import android.database.sqlite.SQLiteException
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.supla.android.Preferences
import org.supla.android.core.infrastructure.BuildConfigProxy
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.infrastructure.ThreadHandler
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.core.storage.EncryptedPreferences
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.db.DbHelper
import org.supla.android.db.MeasurementsDbHelper
import org.supla.android.db.room.app.AppDatabase
import org.supla.android.db.room.measurements.MeasurementsDatabase

class InitializationUseCaseTest() {

  @MockK
  private lateinit var stateHolder: SuplaClientStateHolder

  @MockK
  private lateinit var appDatabase: AppDatabase

  @MockK
  private lateinit var measurementsDatabase: MeasurementsDatabase

  @MockK
  private lateinit var preferences: Preferences

  @MockK
  private lateinit var profileRepository: RoomProfileRepository

  @MockK
  private lateinit var encryptedPreferences: EncryptedPreferences

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var buildConfigProxy: BuildConfigProxy

  @MockK
  private lateinit var threadHandler: ThreadHandler

  @InjectMockKs
  private lateinit var useCase: InitializationUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should initialize and set locked state`() {
    // given
    val context: Context = mockk {}
    every { dateProvider.currentTimestamp() } returnsMany listOf(5, 10)
    every { appDatabase.openHelper } returns mockk { every { readableDatabase } returns mockk() }
    every { measurementsDatabase.openHelper } returns mockk { every { readableDatabase } returns mockk() }
    every { profileRepository.findActiveProfile() } returns Single.just(mockk { every { active } returns true })
    every { encryptedPreferences.lockScreenSettings } returns mockk { every { pinForAppRequired } returns true }
    every { threadHandler.sleep(any()) } answers {}
    every { stateHolder.handleEvent(SuplaClientEvent.Lock) } answers {}

    // when
    useCase.invoke(context)

    // then
    verify {
      stateHolder.handleEvent(SuplaClientEvent.Lock)
      threadHandler.sleep(995)
    }
    confirmVerified(stateHolder, threadHandler, preferences, context)
  }

  @Test
  fun `should initialize and set initialized state - database migrations fails in production`() {
    // given
    val context: Context = mockk {
      every { deleteDatabase(DbHelper.DATABASE_NAME) } returns true
      every { deleteDatabase(MeasurementsDbHelper.DATABASE_NAME) } returns true
    }
    every { dateProvider.currentTimestamp() } returnsMany listOf(5, 10)
    every { appDatabase.openHelper } returns mockk { every { readableDatabase } answers { throw SQLiteException() } }
    every { profileRepository.findActiveProfile() } returns Single.just(mockk { every { active } returns true })
    every { encryptedPreferences.lockScreenSettings } returns mockk { every { pinForAppRequired } returns false }
    every { preferences.isAnyAccountRegistered = false } answers {}
    every { threadHandler.sleep(any()) } answers {}
    every { stateHolder.handleEvent(SuplaClientEvent.Initialized) } answers {}
    every { buildConfigProxy.debug } returns false

    // when
    useCase.invoke(context)

    // then
    verify {
      stateHolder.handleEvent(SuplaClientEvent.Initialized)
      threadHandler.sleep(995)
      preferences.isAnyAccountRegistered = false
      context.deleteDatabase(DbHelper.DATABASE_NAME)
      context.deleteDatabase(MeasurementsDbHelper.DATABASE_NAME)
    }
    confirmVerified(stateHolder, threadHandler, preferences, context)
  }

  @Test
  fun `should initialize and set initialized state - database migrations fails in production, delete fails`() {
    // given
    val context: Context = mockk {
      every { deleteDatabase(DbHelper.DATABASE_NAME) } returns false
      every { deleteDatabase(MeasurementsDbHelper.DATABASE_NAME) } returns true
    }
    every { dateProvider.currentTimestamp() } returnsMany listOf(5, 1010)
    every { appDatabase.openHelper } returns mockk { every { readableDatabase } answers { throw SQLiteException() } }
    every { profileRepository.findActiveProfile() } returns Single.error(NoSuchElementException())
    every { encryptedPreferences.lockScreenSettings } returns mockk { every { pinForAppRequired } returns false }
    every { preferences.isAnyAccountRegistered = false } answers {}
    every { threadHandler.sleep(any()) } answers {}
    every { stateHolder.handleEvent(SuplaClientEvent.NoAccount) } answers {}
    every { buildConfigProxy.debug } returns false

    // when
    useCase.invoke(context)

    // then
    verify {
      stateHolder.handleEvent(SuplaClientEvent.NoAccount)
      context.deleteDatabase(DbHelper.DATABASE_NAME)
      context.deleteDatabase(MeasurementsDbHelper.DATABASE_NAME)
    }
    confirmVerified(stateHolder, threadHandler, preferences, context)
  }

  @Test
  fun `should initialize and set initialized state - database migrations fails in debug`() {
    // given
    val context: Context = mockk {}
    val exception = SQLiteException()
    every { dateProvider.currentTimestamp() } returns 5
    every { appDatabase.openHelper } returns mockk { every { readableDatabase } answers { throw exception } }
    every { buildConfigProxy.debug } returns true

    // when
    Assertions.assertThatThrownBy { useCase.invoke(context) }.isSameAs(exception)

    // then
    confirmVerified(stateHolder, threadHandler, preferences, context)
  }
}
