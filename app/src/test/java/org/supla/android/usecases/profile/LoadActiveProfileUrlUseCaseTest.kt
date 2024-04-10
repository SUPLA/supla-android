package org.supla.android.usecases.profile

import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity

@RunWith(MockitoJUnitRunner::class)
class LoadActiveProfileUrlUseCaseTest {

  @Mock
  private lateinit var profileRepository: RoomProfileRepository

  @InjectMocks
  private lateinit var useCase: LoadActiveProfileUrlUseCase

  @Test
  fun `should get supla cloud when email auth`() {
    // given
    val url = "srv1.supla.org"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns true
      every { serverForEmail } returns url
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.SuplaCloud)

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should get private cloud when email auth`() {
    // given
    val url = "srv.example.com"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns true
      every { serverForEmail } returns url
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.PrivateCloud("https://$url"))

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should get supla cloud when access id auth`() {
    // given
    val url = "srv1.supla.org"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns false
      every { serverForAccessId } returns url
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.SuplaCloud)

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository)
  }

  @Test
  fun `should get private cloud when access id auth`() {
    // given
    val url = "srv.example.com"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns false
      every { serverForAccessId } returns url
    }
    whenever(profileRepository.findActiveProfile()).thenReturn(Single.just(profile))

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.PrivateCloud("https://$url"))

    verify(profileRepository).findActiveProfile()
    verifyNoMoreInteractions(profileRepository)
  }
}
