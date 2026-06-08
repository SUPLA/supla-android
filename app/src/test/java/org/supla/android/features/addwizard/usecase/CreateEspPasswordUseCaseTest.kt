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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.features.addwizard.model.EspHtmlParser
import org.supla.core.shared.extensions.guardLet
import retrofit2.HttpException
import java.io.File
import kotlin.collections.component1

class CreateEspPasswordUseCaseTest {

  @MockK
  private lateinit var espHtmlParser: EspHtmlParser

  @MockK
  private lateinit var dateProvider: DateProvider

  @MockK
  private lateinit var espService: EspService

  @InjectMockKs
  private lateinit var useCase: CreateEspPasswordUseCase

  private val testFile: File?
    get() = javaClass.classLoader?.getResource("device_login.html")?.path?.let { File(it) }
  private val espHtmlParserImplementation = EspHtmlParser()

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `should create password successfully when setup redirects to root`() = runTest {
    // given
    val document = mockk<Document>()
    val fieldMap = mapOf(
      "wifi_ssid" to "supla",
      "cfg_pwd" to "old",
      "confirm_cfg_pwd" to "old"
    )
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.setup() } returns document
    every { espHtmlParser.findInputs(document) } returns fieldMap
    coEvery { espService.setup(capture(capturedFieldMap)) } throws createHttpException(303, "/")

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(CreateEspPasswordUseCase.Result.SUCCESS)
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "wifi_ssid" to "supla",
        "cfg_pwd" to "new-password",
        "confirm_cfg_pwd" to "new-password"
      )
    )

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.setup() }
    coVerify(exactly = 1) {
      espService.setup(
        mapOf(
          "wifi_ssid" to "supla",
          "cfg_pwd" to "new-password",
          "confirm_cfg_pwd" to "new-password"
        )
      )
    }

    confirmVerified(dateProvider, espHtmlParser, espService)
  }

  @Test
  fun `should create password successfully using a real html output`() = runTest {
    // given
    val (file) = guardLet(testFile) { throw IllegalStateException("Test file not found!") }
    val document = Jsoup.parse(file, "UTF-8")
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.setup() } returns document
    every { espHtmlParser.findInputs(document) } returns espHtmlParserImplementation.findInputs(document)
    coEvery { espService.setup(capture(capturedFieldMap)) } throws createHttpException(303, "/")

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(CreateEspPasswordUseCase.Result.SUCCESS)
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "cfg_pwd" to "new-password",
        "confirm_cfg_pwd" to "new-password"
      )
    )

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.setup() }
    coVerify(exactly = 1) {
      espService.setup(
        mapOf(
          "cfg_pwd" to "new-password",
          "confirm_cfg_pwd" to "new-password"
        )
      )
    }

    confirmVerified(dateProvider, espHtmlParser, espService)
  }

  @Test
  fun `should report temporarily locked when device returns 403`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.setup() } returns document
    every { espHtmlParser.findInputs(document) } returns emptyMap()
    coEvery { espService.setup(capture(capturedFieldMap)) } throws createHttpException(403)

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(CreateEspPasswordUseCase.Result.TEMPORARILY_LOCKED)
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "cfg_pwd" to "new-password",
        "confirm_cfg_pwd" to "new-password"
      )
    )

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.setup() }
    coVerify(exactly = 1) {
      espService.setup(
        mapOf(
          "cfg_pwd" to "new-password",
          "confirm_cfg_pwd" to "new-password"
        )
      )
    }

    confirmVerified(dateProvider, espHtmlParser, espService)
  }

  @Test
  fun `should report failure when setup returns without redirect`() = runTest {
    // given
    val document = mockk<Document>()
    val capturedFieldMap = slot<Map<String, String>>()

    every { dateProvider.currentTimestamp() } returnsMany listOf(1_000L, 2_000L)
    coEvery { espService.setup() } returns document
    every { espHtmlParser.findInputs(document) } returns mapOf("custom" to "value")
    coEvery { espService.setup(capture(capturedFieldMap)) } returns document

    // when
    val result = useCase("new-password")

    // then
    assertThat(result).isEqualTo(CreateEspPasswordUseCase.Result.FAILURE)
    assertThat(capturedFieldMap.captured).isEqualTo(
      mapOf(
        "custom" to "value",
        "cfg_pwd" to "new-password",
        "confirm_cfg_pwd" to "new-password"
      )
    )

    verify(exactly = 2) { dateProvider.currentTimestamp() }
    verify(exactly = 1) { espHtmlParser.findInputs(document) }
    coVerify(exactly = 1) { espService.setup() }
    coVerify(exactly = 1) {
      espService.setup(
        mapOf(
          "custom" to "value",
          "cfg_pwd" to "new-password",
          "confirm_cfg_pwd" to "new-password"
        )
      )
    }

    confirmVerified(dateProvider, espHtmlParser, espService)
  }

  private fun createHttpException(code: Int, location: String? = null): HttpException {
    val rawResponse = mockk<okhttp3.Response>(relaxed = true)
    if (location != null) {
      every { rawResponse.headers } returns Headers.headersOf("Location", location)
    }

    val response = mockk<retrofit2.Response<Any>>(relaxed = true) {
      every { raw() } returns rawResponse
    }

    return mockk {
      every { code() } returns code
      every { response() } returns response
    }
  }
}
