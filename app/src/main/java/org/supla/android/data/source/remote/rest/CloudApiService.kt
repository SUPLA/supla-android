package org.supla.android.data.source.remote.rest
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

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.supla.android.BuildConfig
import org.supla.android.core.networking.suplacloud.CloudApiTokenProvider
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.data.source.remote.rest.schedule.ScheduleDto
import org.supla.android.di.GSON_FOR_API
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface CloudApiService {
  @POST("/api/webapp-auth")
  @Headers("Content-Type: application/json;charset=UTF-8")
  suspend fun authenticate(@Body request: CloudApiAuthRequest): CloudApiAuthResponse

  @GET("/api/v3/schedules?include=subject,closestExecutions")
  suspend fun getSchedules(): List<ScheduleDto>

  @GET("/api/v3/schedules/{id}")
  suspend fun getSchedule(@Path("id") remoteId: Int): ScheduleDto

  @PUT("/api/v3/schedules/{id}")
  suspend fun updateSchedule(@Path("id") remoteId: Int, @Body schedule: ScheduleDto)

  @Singleton
  class Provider @Inject constructor(
    private val configHolder: SuplaCloudConfigHolder,
    private val tokenProvider: CloudApiTokenProvider,
    @param:Named(GSON_FOR_API) private val gson: Gson
  ) {
    private var retrofitInstance: Retrofit? = null
    private var urlHash = 0

    @WorkerThread
    fun provide(): CloudApiService {
      val url = requireNotNull(configHolder.url) { "Cloud server URL missing!" }
      return provideRetrofit(url).create(CloudApiService::class.java)
    }

    @Synchronized
    private fun provideRetrofit(url: String): Retrofit {
      return retrofitInstance?.takeIf { url.hashCode() == urlHash }
        ?: Retrofit.Builder()
          .addConverterFactory(GsonConverterFactory.create(gson))
          .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
          .baseUrl(url)
          .client(
            OkHttpClient.Builder()
              .addInterceptor(
                Interceptor { chain ->
                  val request = chain.request().newBuilder().apply {
                    if (!chain.request().url.encodedPath.endsWith("/webapp-auth")) {
                      tokenProvider.provide()?.let { header("Authorization", "Bearer $it") }
                    }
                  }.build()
                  chain.proceed(request)
                }
              )
              .addInterceptor(
                HttpLoggingInterceptor().also {
                  it.redactHeader("Authorization")
                  if (BuildConfig.DEBUG) {
                    it.level = HttpLoggingInterceptor.Level.BODY
                  }
                }
              )
              .build()
          )
          .build()
          .also {
            urlHash = url.hashCode()
            retrofitInstance = it
          }
    }
  }
}

data class CloudApiAuthRequest(
  val username: String,
  val password: String
)

data class CloudApiAuthResponse(
  @SerializedName("access_token") val accessToken: String,
  @SerializedName("expires_in") val expiresIn: Long
)
