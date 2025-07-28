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
import org.jsoup.nodes.Document
import org.supla.android.Trace
import org.supla.android.data.source.RoomProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.esp.EspConfigurationSession
import org.supla.android.data.source.remote.esp.EspDeviceProtocol
import org.supla.android.data.source.remote.esp.EspPostData
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.extensions.TAG
import org.supla.android.extensions.isNotNull
import org.supla.android.extensions.locationHeader
import org.supla.android.features.addwizard.model.EspConfigResult
import org.supla.android.features.addwizard.model.EspHtmlParser
import retrofit2.HttpException
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
  private val session: EspConfigurationSession,
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
    val getResult = performRequest(GET_RETRIES) { getRequest(fieldMap) }
    return when (getResult) {
      null -> {
        Trace.w(TAG, "Could not connect to the ESP device")
        Result.ConnectionError
      }

      is GetResult.CredentialsNeeded -> {
        Trace.w(TAG, "Configuration broken, credentials needed")
        Result.CredentialsNeeded
      }

      is GetResult.SetupNeeded -> {
        Trace.w(TAG, "Configuration broken, setup needed")
        Result.SetupNeeded
      }

      is GetResult.TemporarilyLocked -> {
        Trace.w(TAG, "Device temporarily locked")
        Result.TemporarilyLocked
      }

      is GetResult.Success -> performConfigurationUpdate(profile, getResult, inputData, fieldMap)
    }
  }

  private suspend fun performConfigurationUpdate(
    profile: ProfileEntity,
    getResult: GetResult.Success,
    inputData: InputData,
    fieldMap: MutableMap<String, String>
  ): Result {
    val result = getResult.result

    val espData = EspPostData(fieldMap)
    if (!espData.isCompatible || !result.isCompatible) {
      Trace.w(TAG, "Got incompatible data")
      return Result.Incompatible
    }

    espData.ssid = inputData.ssid
    espData.password = inputData.ssidPassword
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
      val document = storeRequest(espData.fieldMap)

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
      } catch (exception: Exception) {
        Trace.e(TAG, "Could not perform request", exception)
      }
    }

    return null
  }

  private suspend fun getRequest(fieldMap: MutableMap<String, String>): GetResult? {
    delay(1500.milliseconds)
    try {
      val document = espService.read()
      fieldMap.putAll(espHtmlParser.findInputs(document))
      return GetResult.Success(espHtmlParser.prepareResult(document, fieldMap))
    } catch (exception: HttpException) {
      val code = exception.code()
      Trace.e(TAG, "Request failed (code: $code)", exception)

      if (code == 301) {
        if (exception.locationHeader?.startsWith("https://") == true) {
          Trace.i(TAG, "Recognized secured connection, changing to https")
          session.useSecureLayer = true
        }
      } else if (code == 303) {
        if (exception.locationHeader == "/setup") {
          return GetResult.SetupNeeded
        } else if (exception.locationHeader == "/login") {
          return GetResult.CredentialsNeeded
        }
      } else if (code == 403) {
        return GetResult.TemporarilyLocked
      }
      return null
    } catch (exception: Exception) {
      Trace.e(TAG, "Request failed", exception)
      return null
    }
  }

  private suspend fun rebootRequest(espData: EspPostData) {
    Trace.i(TAG, "Data saved, trying to reboot")
    espData.reboot = true
    try {
      storeRequest(espData.fieldMap)
      Trace.i(TAG, "Reboot accepted")
    } catch (exception: IOException) {
      Trace.w(TAG, "Reboot request failed", exception)
    }
  }

  private suspend fun storeRequest(fieldMap: Map<String, String>): Document {
    return try {
      espService.store(fieldMap)
    } catch (ex: HttpException) {
      if (ex.code() == 303 && ex.locationHeader == "/") {
        espService.read()
      } else {
        throw ex
      }
    }
  }

  data class InputData(
    val ssid: String,
    val ssidPassword: String,
    val devicePassword: String? = null
  )

  sealed interface Result {
    data class Success(val result: EspConfigResult) : Result
    data object ConnectionError : Result
    data object Incompatible : Result
    data object Failed : Result
    data object Timeout : Result
    data object SetupNeeded : Result
    data object CredentialsNeeded : Result
    data object TemporarilyLocked : Result
  }

  private sealed interface GetResult {
    data class Success(val result: EspConfigResult) : GetResult
    data object SetupNeeded : GetResult
    data object CredentialsNeeded : GetResult
    data object TemporarilyLocked : GetResult
  }
}

private val EspConfigResult.isCompatible: Boolean
  get() = deviceFirmwareVersion?.isNotEmpty() == true
