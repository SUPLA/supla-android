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
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.local.entity.ProfileEntity
import org.supla.android.data.source.remote.esp.EspConfigurationSession
import org.supla.android.data.source.remote.esp.EspDeviceProtocol
import org.supla.android.data.source.remote.esp.EspPostData
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.data.source.remote.esp.SuplaCertificateException
import org.supla.android.extensions.isNotNull
import org.supla.android.extensions.locationHeader
import org.supla.android.features.addwizard.model.EspConfigResult
import org.supla.android.features.addwizard.model.EspHtmlParser
import org.supla.core.shared.data.model.addwizard.CertificateErrorType
import retrofit2.HttpException
import timber.log.Timber
import java.security.cert.CertPathValidatorException
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLProtocolException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val GET_RETRIES = 10
private const val POST_RETRIES = 3

private const val TAG_RESULT_MESSAGE = "msg"

private val TIMEOUT = 60.seconds

@Singleton
class ConfigureEspUseCase @Inject constructor(
  private val profileRepository: ProfileRepository,
  private val session: EspConfigurationSession,
  private val espHtmlParser: EspHtmlParser,
  private val espService: EspService
) {

  suspend operator fun invoke(inputData: InputData): Result =
    withTimeoutOrNull(TIMEOUT) {
      perform(inputData)
    } ?: Result.Timeout

  private suspend fun perform(inputData: InputData): Result {
    val profile = profileRepository.findActiveProfileKtx() ?: return Result.Failed

    val fieldMap = mutableMapOf<String, String>()
    val getResult = performRequest(GET_RETRIES) { getRequest(fieldMap) }
    return when (getResult) {
      null -> {
        Timber.w("Could not connect to the ESP device")
        Result.ConnectionError
      }
      is GetResult.CertificateError -> {
        Timber.w("Certificate error - ${getResult.type.message}")
        Result.CertificateError(getResult.type)
      }
      is GetResult.CredentialsNeeded -> {
        Timber.w("Configuration broken, credentials needed")
        Result.CredentialsNeeded
      }
      is GetResult.SetupNeeded -> {
        Timber.w("Configuration broken, setup needed")
        Result.SetupNeeded
      }
      is GetResult.TemporarilyLocked -> {
        Timber.w("Device temporarily locked")
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
      Timber.w("Got incompatible data")
      return Result.Incompatible
    }

    espData.ssid = inputData.ssid
    espData.password = inputData.ssidPassword
    espData.server = profile.serverForEmail
    espData.email = profile.email

    if (espData.softwareUpdate.isNotNull) {
      Timber.i("Turning on software update")
      espData.softwareUpdate = true
    }
    if (espData.protocol.isNotNull) {
      Timber.i("Setting supla protocol")
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
        Timber.e(exception, "Could not perform request")
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
      Timber.e(exception, "Request failed (code: $code)")

      if (code == 301) {
        if (exception.locationHeader?.startsWith("https://") == true) {
          Timber.i("Recognized secured connection, changing to https")
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
      if (exception !is IOException) {
        Timber.e(exception, "Request failed")
        return null
      }
      val certificateErrorType = exception.toCertificateErrorType()
      if (certificateErrorType == null) {
        Timber.e(exception, "Request failed")
        return null
      }
      return GetResult.CertificateError(certificateErrorType)
    }
  }

  private suspend fun rebootRequest(espData: EspPostData) {
    Timber.i("Data saved, trying to reboot")
    espData.reboot = true
    try {
      storeRequest(espData.fieldMap)
      Timber.i("Reboot accepted")
    } catch (exception: IOException) {
      Timber.w(exception, "Reboot request failed")
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
    data class CertificateError(val type: CertificateErrorType) : Result
  }

  private sealed interface GetResult {
    data class Success(val result: EspConfigResult) : GetResult
    data object SetupNeeded : GetResult
    data object CredentialsNeeded : GetResult
    data object TemporarilyLocked : GetResult
    data class CertificateError(val type: CertificateErrorType) : GetResult
  }
}

private val EspConfigResult.isCompatible: Boolean
  get() = deviceFirmwareVersion?.isNotEmpty() == true

private fun Throwable.toCertificateErrorType(): CertificateErrorType? {
  findCause<SuplaCertificateException>()?.let { return it.type }
  findCause<CertificateExpiredException>()?.let { return CertificateErrorType.CertificateExpired }
  findCause<CertificateNotYetValidException>()?.let { return CertificateErrorType.CertificateNotYetValid }

  findCause<CertPathValidatorException>()?.let { exception ->
    return when (exception.reason) {
      CertPathValidatorException.BasicReason.EXPIRED -> CertificateErrorType.CertificateExpired
      CertPathValidatorException.BasicReason.NOT_YET_VALID -> CertificateErrorType.CertificateNotYetValid
      CertPathValidatorException.BasicReason.REVOKED -> CertificateErrorType.CertificateRevoked
      CertPathValidatorException.BasicReason.ALGORITHM_CONSTRAINED -> CertificateErrorType.UnsupportedSecurity
      else -> CertificateErrorType.UntrustedCertificate
    }
  }

  findCause<SSLPeerUnverifiedException>()?.let { exception ->
    val message = exception.message.orEmpty().lowercase()
    return if (message.contains("certificate pinning failure")) {
      CertificateErrorType.CertificatePinMismatch
    } else {
      CertificateErrorType.CertificateHostMismatch
    }
  }

  findCause<SSLProtocolException>()?.let { return CertificateErrorType.UnsupportedSecurity }

  findCause<SSLHandshakeException>()?.let { exception ->
    val message = exception.message.orEmpty().lowercase()
    return if (
      message.contains("protocol") ||
      message.contains("cipher") ||
      message.contains("algorithm")
    ) {
      CertificateErrorType.UnsupportedSecurity
    } else {
      CertificateErrorType.UntrustedCertificate
    }
  }

  return null
}

private inline fun <reified T : Throwable> Throwable.findCause(): T? =
  generateSequence(this as Throwable?) { it.cause }.firstOrNull { it is T } as? T
