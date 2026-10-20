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

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.core.storage.EncryptedPreferences

class CloudApiTokenProviderTest {

  private val encryptedPreferences: EncryptedPreferences = mockk()
  private val dateProvider: DateProvider = mockk()

  private lateinit var tokenProvider: CloudApiTokenProvider

  @Before
  fun setUp() {
    tokenProvider = CloudApiTokenProvider(encryptedPreferences, dateProvider)
  }

  @Test
  fun `should load valid token from encrypted preferences once`() {
    // given
    val expiresAt = 2_000L
    every { dateProvider.currentTimestamp() } returns 1_000L
    every { encryptedPreferences.getCloudApiToken() } returns ("stored-token" to expiresAt)

    // when
    val firstToken = tokenProvider.provide()
    val secondToken = tokenProvider.provide()

    // then
    assertEquals("stored-token", firstToken)
    assertEquals("stored-token", secondToken)
    verify(exactly = 1) { encryptedPreferences.getCloudApiToken() }
  }

  @Test
  fun `should cache missing token from encrypted preferences`() {
    // given
    every { encryptedPreferences.getCloudApiToken() } returns null

    // when
    val firstToken = tokenProvider.provide()
    val secondToken = tokenProvider.provide()

    // then
    assertNull(firstToken)
    assertNull(secondToken)
    verify(exactly = 1) { encryptedPreferences.getCloudApiToken() }
  }

  @Test
  fun `should remove expired token from encrypted preferences`() {
    // given
    every { dateProvider.currentTimestamp() } returns 1_000L
    every { encryptedPreferences.getCloudApiToken() } returns ("expired-token" to 1_000L)
    every { encryptedPreferences.removeCloudApiToken() } just Runs

    // when
    val token = tokenProvider.provide()

    // then
    assertNull(token)
    verify(exactly = 1) { encryptedPreferences.getCloudApiToken() }
    verify(exactly = 1) { encryptedPreferences.removeCloudApiToken() }
  }

  @Test
  fun `should persist and cache updated token`() {
    // given
    val now = 1_000L
    every { dateProvider.currentTimestamp() } returns now
    every { encryptedPreferences.setCloudApiToken("new-token", 6_000L) } just Runs

    // when
    tokenProvider.update("new-token", expiresInSeconds = 5)
    val token = tokenProvider.provide()

    // then
    assertEquals("new-token", token)
    verify(exactly = 1) { encryptedPreferences.setCloudApiToken("new-token", 6_000L) }
    verify(exactly = 0) { encryptedPreferences.getCloudApiToken() }
  }

  @Test
  fun `should clear cached and persisted token`() {
    // given
    every { encryptedPreferences.removeCloudApiToken() } just Runs

    // when
    tokenProvider.clean()
    val token = tokenProvider.provide()

    // then
    assertNull(token)
    verify(exactly = 1) { encryptedPreferences.removeCloudApiToken() }
    verify(exactly = 0) { encryptedPreferences.getCloudApiToken() }
  }
}
