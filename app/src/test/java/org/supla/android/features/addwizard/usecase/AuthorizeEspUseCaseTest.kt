package org.supla.android.features.addwizard.usecase
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

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.remote.esp.EspConfigurationSession
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.features.addwizard.model.EspHtmlParser
import retrofit2.HttpException
import retrofit2.Response

class AuthorizeEspUseCaseTest {

  @MockK
  private lateinit var session: EspConfigurationSession

  @MockK
  private lateinit var espHtmlParser: EspHtmlParser

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var espService: EspService

  @InjectMockKs
  private lateinit var useCase: AuthorizeEspUseCase

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should authorize successfully when login redirects to root`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.login() } returns document
    every { espHtmlParser.findInputs(document) } returns mapOf("ssid" to "supla")
    coEvery { espService.login(any()) } throws createHttpException(303, "/")

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(AuthorizeEspUseCase.Result.SUCCESS)

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.login() }
    coVerify(exactly = 1) { espService.login(capture(capturedFieldMap)) }
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "ssid" to "supla",
        "cfg_pwd" to "new-password"
      )
    )
  }

  @Test
  fun `should report wrong password when session says authorization failed`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.login() } returns document
    every { espHtmlParser.findInputs(document) } returns emptyMap()
    coEvery { espService.login(any()) } returns Unit
    every { session.lastAuthStatus } returns EspConfigurationSession.AuthStatus.Failed

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(AuthorizeEspUseCase.Result.FAILURE_WRONG_PASSWORD)

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    verify(exactly = 1) { session.lastAuthStatus }
    coVerify(exactly = 1) { espService.login() }
    coVerify(exactly = 1) { espService.login(capture(capturedFieldMap)) }
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf("cfg_pwd" to "new-password")
    )
  }

  @Test
  fun `should report temporarily locked when device returns 403`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.login() } returns document
    every { espHtmlParser.findInputs(document) } returns emptyMap()
    coEvery { espService.login(any()) } throws createHttpException(403)

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(AuthorizeEspUseCase.Result.TEMPORARILY_LOCKED)

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.login() }
    coVerify(exactly = 1) { espService.login(capture(capturedFieldMap)) }
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf("cfg_pwd" to "new-password")
    )
  }

  @Test
  fun `should report unknown failure when login returns without redirect and session is not failed`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.login() } returns document
    every { espHtmlParser.findInputs(document) } returns mapOf("custom" to "value")
    coEvery { espService.login(any()) } returns Unit
    every { session.lastAuthStatus } returns EspConfigurationSession.AuthStatus.Ok

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(AuthorizeEspUseCase.Result.FAILURE_UNKNOWN)

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    verify(exactly = 1) { session.lastAuthStatus }
    coVerify(exactly = 1) { espService.login() }
    coVerify(exactly = 1) { espService.login(capture(capturedFieldMap)) }
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "custom" to "value",
        "cfg_pwd" to "new-password"
      )
    )
  }

  @Suppress("UNCHECKED_CAST")
  private fun createHttpException(code: Int, location: String? = null): HttpException {
    val rawResponse = okhttp3.Response.Builder()
      .request(Request.Builder().url("http://localhost/").build())
      .protocol(Protocol.HTTP_1_1)
      .code(code)
      .message("Response")
      .apply {
        if (location != null) {
          header("Location", location)
        }
      }
      .build()

    val response = Response::class.java.getDeclaredConstructor(
      okhttp3.Response::class.java,
      Any::class.java,
      okhttp3.ResponseBody::class.java
    ).apply {
      isAccessible = true
    }.newInstance(rawResponse, null, "".toResponseBody(null)) as Response<Any>

    return HttpException(response)
  }
}
