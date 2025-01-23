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
import io.reactivex.rxjava3.core.Observable
import org.supla.android.core.networking.suplaclient.SuplaClientProvider
import org.supla.android.core.networking.suplacloud.OkHttpClientProvider
import org.supla.android.core.networking.suplacloud.SuplaCloudConfigHolder
import org.supla.android.data.source.remote.rest.channel.ElectricityMeasurement
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeasurement
import org.supla.android.data.source.remote.rest.channel.GeneralPurposeMeter
import org.supla.android.data.source.remote.rest.channel.HistoryMeasurement
import org.supla.android.data.source.remote.rest.channel.HistoryMeasurementType
import org.supla.android.data.source.remote.rest.channel.HumidityMeasurement
import org.supla.android.data.source.remote.rest.channel.ImpulseCounterMeasurement
import org.supla.android.data.source.remote.rest.channel.TemperatureAndHumidityMeasurement
import org.supla.android.data.source.remote.rest.channel.TemperatureMeasurement
import org.supla.android.di.GSON_FOR_API
import org.supla.core.shared.data.model.rest.ImpulseCounterPhotoDto
import org.supla.core.shared.data.model.rest.channel.DefaultChannelDto
import org.supla.core.shared.data.model.rest.channel.ElectricityChannelDto
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private const val API_VERSION = "3"

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
  fun getHumidityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null
  ): Observable<List<HumidityMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialHumidityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<HumidityMeasurement>>

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

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getGpmMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null
  ): Observable<List<GeneralPurposeMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialGpmMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<GeneralPurposeMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getGpmCounterMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null
  ): Observable<List<GeneralPurposeMeter>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialGpmCounterMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<GeneralPurposeMeter>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getElectricityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null,
    @Query("beforeTimestamp") beforeTimestamp: Long? = null
  ): Observable<List<ElectricityMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialElectricityMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<ElectricityMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getImpulseCounterMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null,
    @Query("beforeTimestamp") beforeTimestamp: Long? = null
  ): Observable<List<ImpulseCounterMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialImpulseCounterMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0
  ): Call<List<ImpulseCounterMeasurement>>

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getHistoryMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("logsType") logsType: String,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
    @Query("afterTimestamp") afterTimestamp: Long? = null,
    @Query("beforeTimestamp") beforeTimestamp: Long? = null
  ): Observable<List<HistoryMeasurement>>

  fun getHistoryMeasurements(
    remoteId: Int,
    afterTimestamp: Long?,
    measurementType: HistoryMeasurementType
  ): Observable<List<HistoryMeasurement>> =
    getHistoryMeasurements(remoteId, afterTimestamp = afterTimestamp, logsType = measurementType.type)

  @GET("/api/$API_VERSION/channels/{remoteId}/measurement-logs")
  fun getInitialHistoryMeasurements(
    @Path("remoteId") remoteId: Int,
    @Query("order") order: String = "ASC",
    @Query("limit") limit: Int = 2,
    @Query("offset") offset: Int = 0,
    @Query("logsType") logsType: String = "currentHistory"
  ): Call<List<HistoryMeasurement>>

  fun getInitialHistoryMeasurements(
    remoteId: Int,
    measurementType: HistoryMeasurementType
  ): Call<List<HistoryMeasurement>> =
    getInitialHistoryMeasurements(remoteId, limit = 2, offset = 0, logsType = measurementType.type)

  @GET("/api/$API_VERSION/integrations/ocr/{remoteId}/latest")
  fun getLatestImpulseCounterPhotoOld(
    @Path("remoteId") remoteId: Int
  ): Observable<ImpulseCounterPhotoDto>

  @GET("/api/$API_VERSION/integrations/ocr/{remoteId}/images")
  fun getImpulseCounterPhotos(
    @Path("remoteId") remoteId: Int
  ): Observable<List<ImpulseCounterPhotoDto>>

  @GET("/api/$API_VERSION/integrations/ocr/{remoteId}/images/latest")
  fun getLatestImpulseCounterPhoto(
    @Path("remoteId") remoteId: Int
  ): Observable<ImpulseCounterPhotoDto>

  @GET("/api/$API_VERSION/channels/{remoteId}/")
  fun getChannel(
    @Path("remoteId") remoteId: Int
  ): Observable<DefaultChannelDto>

  @GET("/api/$API_VERSION/channels/{remoteId}/")
  fun getElectricityMeterChannel(
    @Path("remoteId") remoteId: Int
  ): Observable<ElectricityChannelDto>

  @Singleton
  class Provider @Inject constructor(
    private val configHolder: SuplaCloudConfigHolder,
    private val okHttpClientProvider: OkHttpClientProvider,
    private val suplaClientProvider: SuplaClientProvider,
    @Named(GSON_FOR_API) private val gson: Gson
  ) {

    private var retrofitInstance: Retrofit? = null
    private var urlHash = 0

    @WorkerThread
    fun provide(): SuplaCloudService {
      val url = requireUrl()
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
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(url)
            .client(okHttpClientProvider.provide())
            .build().also { retrofitInstance = it }
        }
      }
    }

    @WorkerThread
    private fun requireUrl(): String {
      suplaClientProvider.provide()?.oAuthTokenRequest()

      for (i in 0..50) {
        configHolder.url?.let {
          return it
        }
        Thread.sleep(100)
      }

      throw IllegalStateException("Server token missing!")
    }
  }
}
