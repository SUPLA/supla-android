package org.supla.android.usecases.cloudapi
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

import org.supla.android.core.networking.suplacloud.CloudApiTokenProvider
import org.supla.android.data.source.remote.rest.CloudApiAuthRequest
import org.supla.android.data.source.remote.rest.CloudApiService
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticateCloudApiUseCase @Inject constructor(
  private val cloudApiServiceProvider: CloudApiService.Provider,
  private val cloudApiTokenProvider: CloudApiTokenProvider
) {
  suspend operator fun invoke(username: String, password: String) {
    val response = try {
      cloudApiServiceProvider.provide().authenticate(CloudApiAuthRequest(username, password))
    } catch (exception: HttpException) {
      if (exception.code() == 401 || exception.code() == 403) {
        throw CloudApiAuthorizationException.InvalidCredentials(exception.code())
      }
      throw exception
    }

    cloudApiTokenProvider.update(response.accessToken, response.expiresIn)
  }
}

sealed class CloudApiAuthorizationException : Exception() {
  data class InvalidCredentials(val statusCode: Int) : CloudApiAuthorizationException()
}
