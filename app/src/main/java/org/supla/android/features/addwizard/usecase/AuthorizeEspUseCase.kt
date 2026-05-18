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
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.remote.esp.EspConfigurationSession
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.extensions.locationHeader
import org.supla.android.features.addwizard.model.EspHtmlParser
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

private const val FIELD_PASSWORD = "cfg_pwd"

@Singleton
class AuthorizeEspUseCase @Inject constructor(
  private var session: EspConfigurationSession,
  private val espHtmlParser: EspHtmlParser,
  private val dateProvider: DateProvider,
  private var espService: EspService,
) {
  suspend operator fun invoke(password: String): Result {
    val fieldMap = mutableMapOf<String, String>()

    return try {
      val startTime = dateProvider.currentTimestamp()

      // Get page with all inputs
      val document = espService.login()
      fieldMap.putAll(espHtmlParser.findInputs(document))
      // Update needed inputs
      fieldMap[FIELD_PASSWORD] = password

      // Delay added to show a loading indicator on view for a moment, so the user see, that something is happening
      val operationTime = dateProvider.currentTimestamp() - startTime
      if (operationTime < 500) {
        delay(500 - operationTime)
      }

      espService.login(fieldMap)

      if (session.lastAuthStatus == EspConfigurationSession.AuthStatus.Failed) {
        Timber.e("Authorize request failed - wrong password")
        Result.FAILURE_WRONG_PASSWORD
      } else {
        Timber.e("Authorize request failed - no redirect")
        Result.FAILURE_UNKNOWN
      }
    } catch (ex: HttpException) {
      if (ex.code() == 303 && ex.locationHeader == "/") {
        Result.SUCCESS
      } else if (ex.code() == 403) {
        Timber.e(ex, "Authorize request failed - temporarily locked")
        Result.TEMPORARILY_LOCKED
      } else {
        Timber.e(ex, "Authorize request failed")
        Result.FAILURE_UNKNOWN
      }
    } catch (ex: Exception) {
      Timber.e(ex, "Authorize request failed")
      Result.FAILURE_UNKNOWN
    }
  }

  enum class Result {
    SUCCESS, FAILURE_WRONG_PASSWORD, FAILURE_UNKNOWN, TEMPORARILY_LOCKED
  }
}
