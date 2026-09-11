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

import android.os.Process
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDbQueryExecutor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppDbTransactionExecutor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeasurementsDbQueryExecutor

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MeasurementsDbTransactionExecutor

@Module
@InstallIn(SingletonComponent::class)
class DatabaseExecutorsModule {

  @Provides
  @Singleton
  @AppDbQueryExecutor
  fun provideAppDbQueryExecutor(): Executor =
    Executors.newFixedThreadPool(2) { runnable ->
      Thread(runnable, "AppDbQuery")
    }

  @Provides
  @Singleton
  @AppDbTransactionExecutor
  fun provideAppDbTransactionExecutor(): Executor =
    Executors.newSingleThreadExecutor { runnable ->
      Thread(runnable, "AppDbTransaction")
    }

  @Provides
  @Singleton
  @MeasurementsDbQueryExecutor
  fun provideMeasurementsDbQueryExecutor(): Executor =
    Executors.newFixedThreadPool(2) { runnable ->
      Thread(
        {
          Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
          runnable.run()
        },
        "MeasurementsDbQuery"
      )
    }

  @Provides
  @Singleton
  @MeasurementsDbTransactionExecutor
  fun provideMeasurementsDbTransactionExecutor(): Executor =
    Executors.newSingleThreadExecutor { runnable ->
      Thread(
        {
          Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
          runnable.run()
        },
        "MeasurementsDbTransaction"
      )
    }
}
