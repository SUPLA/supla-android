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

import android.net.Uri
import io.mockk.*
import io.mockk.Called
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.UriProxy
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity

class LoadActiveProfileUrlUseCaseTest {

  @MockK
  private lateinit var profileRepository: ProfileRepository

  @MockK
  private lateinit var uriProxy: UriProxy

  @InjectMockKs
  private lateinit var useCase: LoadActiveProfileUrlUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should get supla cloud when email auth`() {
    // given
    val url = "srv1.supla.org"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns true
      every { serverForEmail } returns url
      every { serverAutoDetect } returns true
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.DefaultCloud)

    verify { profileRepository.findActiveProfile() }
    confirmVerified(profileRepository)
    verify {
      uriProxy wasNot Called
    }
  }

  @Test
  fun `should get private cloud when email auth`() {
    // given
    val url = "srv.example.com"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns true
      every { serverForEmail } returns url
      every { serverAutoDetect } returns false
    }
    val uri: Uri = mockk()
    every { uriProxy.toUri("https://$url") } returns uri
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.ServerUri(uri))

    verify { profileRepository.findActiveProfile() }
    verify { uriProxy.toUri("https://$url") }
    confirmVerified(profileRepository, uriProxy)
  }

  @Test
  fun `should get supla cloud when no url defined`() {
    // given
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns false
      every { serverForAccessId } returns ""
      every { serverAutoDetect } returns false
    }
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.DefaultCloud)

    verify { profileRepository.findActiveProfile() }
    confirmVerified(profileRepository)
    verify {
      uriProxy wasNot Called
    }
  }

  @Test
  fun `should get private cloud when access id auth`() {
    // given
    val url = "srv.example.com"
    val profile: ProfileEntity = mockk {
      every { emailAuth } returns false
      every { serverForAccessId } returns url
      every { serverAutoDetect } returns false
    }
    val uri: Uri = mockk()
    every { uriProxy.toUri("https://$url") } returns uri
    every { profileRepository.findActiveProfile() } returns Single.just(profile)

    // when
    val observer = useCase.invoke().test()

    // then
    observer.assertComplete()
    observer.assertResult(CloudUrl.ServerUri(uri))

    verify { profileRepository.findActiveProfile() }
    verify { uriProxy.toUri("https://$url") }
    confirmVerified(profileRepository, uriProxy)
  }
}
