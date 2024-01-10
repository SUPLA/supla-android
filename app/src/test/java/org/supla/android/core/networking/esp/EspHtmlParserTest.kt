package org.supla.android.core.networking.esp
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

import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner
import org.supla.android.extensions.guardLet
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class EspHtmlParserTest {

  private val testFile: File?
    get() = javaClass.classLoader?.getResource("arduino.html")?.path?.let { File(it) }

  @InjectMocks
  private lateinit var parser: EspHtmlParser

  @Test
  fun shouldLoadInputs() {
    // given
    val (file) = guardLet(testFile) { throw IllegalStateException("Test file not found!") }
    val document = Jsoup.parse(file, "UTF-8")

    // when
    val inputs = parser.findInputs(document)

    // then
    assertThat(inputs.keys.size).isEqualTo(37)
    // no input without names
    assertThat(inputs.keys.filter { it.isEmpty() }).isEmpty()
    // no checkboxes without checked attribute
    assertThat(inputs.keys.filter { it == "set_time_toggle" }).isEmpty()
    // checkboxes with checked attribute
    assertThat(inputs.keys.filter { it == "0_t_chng_keeps" }).isNotEmpty
  }

  @Test
  fun shouldLoadConfig() {
    // given
    val (file) = guardLet(testFile) { throw IllegalStateException("Test file not found!") }
    val document = Jsoup.parse(file, "UTF-8")
    val inputs = parser.findInputs(document)

    // then
    val result = parser.prepareResult(document, inputs)

    // then
    assertThat(result.resultCode).isEqualTo(RESULT_FAILED)
    assertThat(result.deviceName).isEqualTo("Basic thermostat")
    assertThat(result.deviceLastState).isEqualTo("Config mode (145), Registered and ready (3)")
    assertThat(result.deviceFirmwareVersion).isEqualTo("SDK 23.12.02-dev")
    assertThat(result.deviceGUID).isEqualTo("9F22CD27D5D799FFCE7AA50BCFD539E8")
    assertThat(result.deviceMAC).isEqualTo("58:BF:25:31:9F:04")
    assertThat(result.needsCloudConfig).isFalse()
  }

  @Test
  fun shouldLoadConfig_withNeedsCloudConfig() {
    // given
    val (file) = guardLet(testFile) { throw IllegalStateException("Test file not found!") }
    val document = Jsoup.parse(file, "UTF-8")
    val inputs = parser.findInputs(document)
    (inputs as MutableMap<String, String>)["no_visible_channels"] = "1"

    // when
    val result = parser.prepareResult(document, inputs)

    // then
    assertThat(result.resultCode).isEqualTo(RESULT_FAILED)
    assertThat(result.deviceName).isEqualTo("Basic thermostat")
    assertThat(result.deviceLastState).isEqualTo("Config mode (145), Registered and ready (3)")
    assertThat(result.deviceFirmwareVersion).isEqualTo("SDK 23.12.02-dev")
    assertThat(result.deviceGUID).isEqualTo("9F22CD27D5D799FFCE7AA50BCFD539E8")
    assertThat(result.deviceMAC).isEqualTo("58:BF:25:31:9F:04")
    assertThat(result.needsCloudConfig).isTrue()
  }
}
