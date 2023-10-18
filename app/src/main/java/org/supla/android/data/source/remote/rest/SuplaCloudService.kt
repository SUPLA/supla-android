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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.networking.suplacloud.OkHttpClientProvider
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.core.networking.suplacloud.SuplaDateConverter
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.extensions.guardLet
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val API_VERSION = "2.2.0"

interface SuplaCloudService {
  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getThermometerMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null
  ): Observable<List<TemperatureMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialThermometerMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<TemperatureMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getThermometerWithHumidityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null
  ): Observable<List<TemperatureAndHumidityMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialThermometerWithHumidityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<TemperatureAndHumidityMeasurement>>

  @Singleton
  class Provider @Inject constructor(
    private val configHolder: SuplaCloudConfigHolder,
    private val okHttpClientProvider: OkHttpClientProvider
  ) {

    private var retrofitInstance: Retrofit? = null
    private var urlHash = 0

    fun provide(): SuplaCloudService {
      val (url) = guardLet(configHolder.url) { throw IllegalStateException("Server URL missing!") }
      return provideRetrofit(url).create(SuplaCloudService::class.java)
    }

    @Synchronized
    private fun provideRetrofit(url: String): Retrofit {
      return retrofitInstance.let { retrofit ->
        return@let if (url.hashCode() == urlHash && retrofit != null) {
          retrofit
        } else {
          urlHash = url.hashCode()
          Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gsonConverter()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(url)
            .client(okHttpClientProvider.provide())
            .build().also { retrofitInstance = it }
        }
      }
    }

    private fun gsonConverter(): Gson =
      GsonBuilder().registerTypeAdapter(Date::class.java, SuplaDateConverter()).create()
  }
}
