package org.supla.android.usecases.profile
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
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.infrastructure.NativeLoader
import org.supla.android.core.networking.suplaclient.SuplaClientEvent
import org.supla.android.core.networking.suplaclient.SuplaClientStateHolder
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.lib.SuplaClient
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.usecases.client.DisconnectUseCase
import org.supla.android.widget.WidgetManager

@RunWith(MockitoJUnitRunner::class)
class DeleteProfileUseCaseTest {
  @MockK
  private lateinit var context: Context

  @MockK
  private lateinit var deleteProfileRelatedDataUseCase: DeleteProfileRelatedDataUseCase

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var suplaAppProvider: SuplaAppProvider

  @MockK
  private lateinit var activateProfileUseCase: ActivateProfileUseCase

  @MockK
  private lateinit var suplaClientStateHolder: SuplaClientStateHolder

  @MockK
  private lateinit var disconnectUseCase: DisconnectUseCase

  @MockK
  private lateinit var singleCallProvider: SingleCall.Provider

  @MockK
  private lateinit var widgetManager: WidgetManager

  @InjectMockKs
  private lateinit var useCase: DeleteProfileUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should delete inactive profile`() {
    // given
    mockkObject(NativeLoader)
    every { NativeLoader.loadLibrary(any()) } just Runs

    val profileId = 132L
    val profile = profileMock(profileId, false)

    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { widgetManager.onProfileRemoved(profileId) } answers {}

    val singleCall: SingleCall = mockk {
      every { registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile) } just Runs
    }
    every { singleCallProvider.provide(profileId) } returns singleCall

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.deleteProfile(profile)
      widgetManager.onProfileRemoved(profileId)
      deleteProfileRelatedDataUseCase.invoke(profileId)
      singleCallProvider.provide(profileId)
      singleCall.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile)
    }
    confirmVerified(
      profileRepository, suplaAppProvider,
      context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, deleteProfileRelatedDataUseCase, singleCall
    )
  }

  @Test
  fun `should delete last active profile`() {
    // given
    mockkObject(NativeLoader)
    every { NativeLoader.loadLibrary(any()) } just Runs

    val profileId = 132L
    val profile = profileMock(profileId, true)

    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { profileRepository.findAllProfiles() } returns Observable.just(emptyList())
    every { disconnectUseCase.invoke() } returns Completable.complete()
    every { suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount) } answers {}
    every { widgetManager.onProfileRemoved(profileId) } answers {}

    val singleCall: SingleCall = mockk {
      every { registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile) } just Runs
    }
    every { singleCallProvider.provide(profileId) } returns singleCall

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.deleteProfile(profile)
      profileRepository.findAllProfiles()
      disconnectUseCase.invoke()
      suplaClientStateHolder.handleEvent(SuplaClientEvent.NoAccount)
      widgetManager.onProfileRemoved(profileId)
      deleteProfileRelatedDataUseCase.invoke(profileId)
      singleCallProvider.provide(profileId)
      singleCall.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile)
    }
    confirmVerified(
      profileRepository, suplaAppProvider,
      context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, deleteProfileRelatedDataUseCase, singleCall
    )
  }

  @Test
  fun `should delete active profile and activate other one`() {
    // given
    mockkObject(NativeLoader)
    every { NativeLoader.loadLibrary(any()) } just Runs

    val profileId = 133L
    val profileIdToActivate = 234L
    val profile = profileMock(profileId, true)

    every { deleteProfileRelatedDataUseCase.invoke(profileId) } returns Completable.complete()
    every { profileRepository.deleteProfile(profile) } returns Completable.complete()
    every { activateProfileUseCase.invoke(profileIdToActivate, true) } returns Completable.complete()
    every { profileRepository.findAllProfiles() } returns Observable.just(listOf(profileMock(profileIdToActivate, false)))
    every { disconnectUseCase.invoke() } returns Completable.complete()
    every { widgetManager.onProfileRemoved(profileId) } answers {}
    val singleCall: SingleCall = mockk {
      every { registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile) } just Runs
    }
    every { singleCallProvider.provide(profileId) } returns singleCall

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.SuplaClientInitIfNeed(any()) } returns null
    every { suplaAppProvider.provide() } returns suplaApp

    // when
    val testObserver = useCase(profile).test()

    // then
    testObserver.assertComplete()

    verify {
      profileRepository.deleteProfile(profile)
      profileRepository.findAllProfiles()
      activateProfileUseCase.invoke(profileIdToActivate, true)
      suplaAppProvider.provide()
      suplaApp.SuplaClientInitIfNeed(context)
      disconnectUseCase.invoke()
      widgetManager.onProfileRemoved(profileId)
      deleteProfileRelatedDataUseCase.invoke(profileId)
      singleCallProvider.provide(profileId)
      singleCall.registerPushNotificationClientToken(SuplaClient.SUPLA_APP_ID, "", profile)
    }
    confirmVerified(
      suplaApp, profileRepository, suplaAppProvider,
      context, activateProfileUseCase, suplaClientStateHolder,
      disconnectUseCase, widgetManager, deleteProfileRelatedDataUseCase, singleCall
    )
  }

  private fun profileMock(profileId: Long, isActive: Boolean): ProfileEntity = mockk {
    every { id } returns profileId
    every { this@mockk.active } returns isActive
  }
}
