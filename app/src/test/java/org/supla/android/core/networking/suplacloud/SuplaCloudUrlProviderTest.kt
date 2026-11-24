package org.supla.android.core.networking.suplacloud
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

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.supla.android.core.networking.suplaclient.SuplaClientApi
import org.supla.android.core.networking.suplaclient.SuplaClientProvider

class SuplaCloudUrlProviderTest {

  private val configHolder: SuplaCloudConfigHolder = mockk()
  private val suplaClientProvider: SuplaClientProvider = mockk()
  private val suplaClient: SuplaClientApi = mockk()

  private var cloudUrl: String? = null
  private lateinit var urlProvider: SuplaCloudUrlProvider

  @Before
  fun setUp() {
    every { configHolder.url } answers { cloudUrl }
    every { suplaClientProvider.provide() } returns suplaClient
    every { suplaClient.oAuthTokenRequest() } answers {
      cloudUrl = "https://cloud.example.com/"
    }
    urlProvider = SuplaCloudUrlProvider(configHolder, suplaClientProvider)
  }

  @Test
  fun `should return URL populated by OAuth callback`() {
    // when
    val url = urlProvider.requireUrl()

    // then
    assertEquals("https://cloud.example.com/", url)
    verify(exactly = 1) { suplaClient.oAuthTokenRequest() }
  }

  @Test
  fun `should fail when OAuth callback does not populate URL`() {
    // given
    every { suplaClient.oAuthTokenRequest() } returns Unit

    // when
    val exception = assertThrows(IllegalStateException::class.java) {
      urlProvider.requireUrl()
    }

    // then
    assertEquals("Server token missing!", exception.message)
  }
}
