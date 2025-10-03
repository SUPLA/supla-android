package org.supla.android.di
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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.supla.android.data.source.remote.esp.EspCommonNameValidationInterceptor
import org.supla.android.data.source.remote.esp.EspCookiesInterceptor
import org.supla.android.data.source.remote.esp.EspCustomRootCaProvider
import org.supla.android.data.source.remote.esp.EspSchemeInterceptor
import org.supla.android.data.source.remote.esp.EspService
import org.supla.android.data.source.remote.esp.JsoupConverterFactory
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Named
import javax.inject.Singleton

private const val ESP_RETROFIT = "ESP_RETROFIT"
private const val ESP_OKHTTP = "ESP_OKHTTP"

@Module
@InstallIn(SingletonComponent::class)
class EspModule {

  @Singleton
  @Provides
  @Named(ESP_OKHTTP)
  fun provideOkHttp(
    espSchemeInterceptor: EspSchemeInterceptor,
    espCustomRootCaProvider: EspCustomRootCaProvider,
    espCommonNameValidationInterceptor: EspCommonNameValidationInterceptor,
    espCookiesInterceptor: EspCookiesInterceptor
  ): OkHttpClient =
    OkHttpClient.Builder()
      .followRedirects(false)
      .addInterceptor(
        HttpLoggingInterceptor().also {
          it.level = HttpLoggingInterceptor.Level.BODY
        }
      )
      .addInterceptor(espSchemeInterceptor)
      .addInterceptor(espCommonNameValidationInterceptor)
      .addInterceptor(espCookiesInterceptor)
      .let {
        val sslFactory = espCustomRootCaProvider.sslContext?.socketFactory
        val trustManager = espCustomRootCaProvider.customTrustManager

        if (sslFactory != null && trustManager != null) {
          Timber.i("Custom CA loaded, setting in client")
          it.sslSocketFactory(sslFactory, trustManager)
        } else {
          Timber.e("Custom CA is not loaded!")
          it
        }
      }
      .hostnameVerifier { hostname, session ->
        Timber.d("Verifying host name $hostname")
        true
      }
      .build()

  @Singleton
  @Provides
  @Named(ESP_RETROFIT)
  fun provideRetrofit(@Named(ESP_OKHTTP) okHttpClient: OkHttpClient): Retrofit =
    Retrofit.Builder()
      .baseUrl("http://192.168.4.1")
      .addConverterFactory(JsoupConverterFactory.FACTORY)
      .client(okHttpClient)
      .build()

  @Singleton
  @Provides
  fun provideEspService(
    @Named(ESP_RETROFIT) retrofit: Retrofit
  ): EspService = retrofit.create(EspService::class.java)
}
