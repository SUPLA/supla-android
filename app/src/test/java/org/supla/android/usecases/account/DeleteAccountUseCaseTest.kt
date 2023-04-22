package org.supla.android.usecases.account

import android.content.Context
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever
import org.supla.android.Preferences
import org.supla.android.core.SuplaAppApi
import org.supla.android.core.SuplaAppProvider
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.db.AuthProfileItem
import org.supla.android.profile.ProfileIdHolder
import org.supla.android.profile.ProfileManager

@RunWith(MockitoJUnitRunner::class)
class DeleteAccountUseCaseTest {
  @Mock
  private lateinit var context: Context

  @Mock
  private lateinit var profileManager: ProfileManager

  @Mock
  private lateinit var suplaClientProvider: SuplaClientProvider

  @Mock
  private lateinit var suplaAppProvider: SuplaAppProvider

  @Mock
  private lateinit var preferences: Preferences

  @Mock
  private lateinit var profileIdHolder: ProfileIdHolder

  @InjectMocks
  private lateinit var useCase: DeleteAccountUseCase

  @Test
  fun `should delete inactive profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, false)

    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))
    whenever(profileManager.delete(profileId)).thenReturn(Completable.complete())

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).read(profileId)
    verify(profileManager).delete(profileId)
    verifyNoMoreInteractions(profileManager)
    verifyZeroInteractions(suplaClientProvider, preferences, profileIdHolder, context)
  }

  @Test
  fun `should delete last active profile`() {
    // given
    val profileId = 132L
    val profile = profileMock(profileId, true)

    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))
    whenever(profileManager.delete(profileId)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(emptyList()))

    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.cancel() } answers { }
    every { suplaClient.join() } answers { }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).read(profileId)
    verify(profileManager).delete(profileId)
    verify(profileManager).getAllProfiles()
    verify(preferences).isAnyAccountRegistered = false
    verify(profileIdHolder).profileId = null
    verify(suplaClientProvider).provide()
    verifyNoMoreInteractions(
      profileManager,
      suplaClientProvider,
      suplaAppProvider,
      preferences,
      profileIdHolder,
    )
    verifyZeroInteractions(context, suplaAppProvider)

    io.mockk.verify {
      suplaClient.cancel()
      suplaClient.join()
    }
    confirmVerified(suplaClient)
  }

  @Test
  fun `should delete active profile and activate other one`() {
    // given
    val profileId = 132L
    val profileIdToActivate = 234L
    val profile = profileMock(profileId, true)

    whenever(profileManager.read(profileId)).thenReturn(Maybe.just(profile))
    whenever(profileManager.delete(profileId)).thenReturn(Completable.complete())
    whenever(profileManager.getAllProfiles()).thenReturn(Observable.just(listOf(profileMock(profileIdToActivate, false))))
    whenever(profileManager.activateProfile(profileIdToActivate, true)).thenReturn(Completable.complete())

    val suplaClient = mockk<SuplaClientApi>()
    every { suplaClient.cancel() } answers { }
    every { suplaClient.join() } answers { }
    whenever(suplaClientProvider.provide()).thenReturn(suplaClient)

    val suplaApp = mockk<SuplaAppApi>()
    every { suplaApp.SuplaClientInitIfNeed(any()) } returns null
    whenever(suplaAppProvider.provide()).thenReturn(suplaApp)

    // when
    val testObserver = useCase(profileId).test()

    // then
    testObserver.assertComplete()

    verify(profileManager).read(profileId)
    verify(profileManager).delete(profileId)
    verify(profileManager).getAllProfiles()
    verify(profileManager).activateProfile(profileIdToActivate, true)
    verify(suplaClientProvider).provide()
    verify(suplaAppProvider).provide()
    verifyNoMoreInteractions(
      profileManager,
      suplaClientProvider,
      suplaAppProvider,
      preferences,
      profileIdHolder
    )
    verifyZeroInteractions(context, profileIdHolder)

    io.mockk.verify {
      suplaClient.cancel()
      suplaClient.join()
      suplaApp.SuplaClientInitIfNeed(context)
    }
    confirmVerified(suplaClient, suplaApp)
  }

  fun profileMock(profileId: Long, isActive: Boolean): AuthProfileItem {
    val profile = mockk<AuthProfileItem>()
    every { profile.id } returns profileId
    every { profile.isActive } returns isActive

    return profile
  }
}