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

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import okio.IOException
import org.supla.android.Trace
import org.supla.android.core.networking.esp.EspConfigResult
import org.supla.android.core.networking.esp.EspHtmlParser
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.remote.esp.EspDeviceProtocol
import org.supla.android.data.source.remote.esp.EspPostData
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.extensions.TAG
import org.supla.android.extensions.isNotNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val GET_RETRIES = 10
private const val POST_RETRIES = 3

private const val TAG_RESULT_MESSAGE = "msg"

private val TIMEOUT = 60.seconds

@Singleton
class ConfigureEspUseCase @Inject constructor(
  private val profileRepository: RoomProfileRepository,
  private val espHtmlParser: EspHtmlParser,
  private val espService: EspService
) {

  suspend operator fun invoke(inputData: InputData): Result =
    withTimeoutOrNull(TIMEOUT) {
      perform(inputData)
    } ?: Result.Timeout

  private suspend fun perform(inputData: InputData): Result {
    val profile = profileRepository.findActiveProfileKtx()

    val fieldMap = mutableMapOf<String, String>()
    val result = performRequest(GET_RETRIES) { getRequest(fieldMap) }
    if (result == null) {
      Trace.w(TAG, "Could not connect to the ESP device")
      return Result.ConnectionError
    }

    val espData = EspPostData(fieldMap)
    if (!espData.isCompatible || !result.isCompatible) {
      Trace.w(TAG, "Got incompatible data")
      return Result.Incompatible
    }

    espData.ssid = inputData.ssid
    espData.password = inputData.password
    espData.server = profile.serverForEmail
    espData.email = profile.email

    if (espData.softwareUpdate.isNotNull) {
      Trace.i(TAG, "Turning on software update")
      espData.softwareUpdate = true
    }
    if (espData.protocol.isNotNull) {
      Trace.i(TAG, "Setting supla protocol")
      espData.protocol = EspDeviceProtocol.Supla
    }

    return performRequest(POST_RETRIES) {
      val document = espService.store(espData.fieldMap)

      if (document.getElementById(TAG_RESULT_MESSAGE)?.html()?.lowercase()?.contains("data saved") == true) {
        rebootRequest(espData)
        return@performRequest Result.Success(result)
      } else {
        return@performRequest null
      }
    } ?: Result.Failed
  }

  private suspend fun <T> performRequest(retries: Int, request: suspend () -> T?): T? {
    for (i in 1..retries) {
      try {
        request()?.let {
          return it
        }
      } catch (exception: IOException) {
        Trace.e(TAG, "Could not perform request", exception)
      }
    }

    return null
  }

  private suspend fun getRequest(fieldMap: MutableMap<String, String>): EspConfigResult {
    delay(1500.milliseconds)
    val document = espService.read()
    fieldMap.putAll(espHtmlParser.findInputs(document))
    return espHtmlParser.prepareResult(document, fieldMap)
  }

  private suspend fun rebootRequest(espData: EspPostData) {
    Trace.i(TAG, "Data saved, trying to reboot")
    espData.reboot = true
    try {
      espService.store(espData.fieldMap)
      Trace.i(TAG, "Reboot accepted")
    } catch (exception: IOException) {
      Trace.w(TAG, "Reboot request failed", exception)
    }
  }

  data class InputData(
    val ssid: String,
    val password: String
  )

  sealed interface Result {
    data class Success(val result: EspConfigResult) : Result
    data object ConnectionError : Result
    data object Incompatible : Result
    data object Failed : Result
    data object Timeout : Result
  }
}

private val EspConfigResult.isCompatible: Boolean
  get() = deviceFirmwareVersion?.isNotEmpty() == true
