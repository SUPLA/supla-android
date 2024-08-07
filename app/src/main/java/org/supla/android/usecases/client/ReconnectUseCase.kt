package org.supla.android.usecases.client
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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import org.supla.android.core.SuplaAppProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReconnectUseCase @Inject constructor(
  @ApplicationContext private val applicationContext: Context,
  private val disconnectUseCase: DisconnectUseCase,
  private val suplaAppProvider: SuplaAppProvider,
) {

  operator fun invoke(): Completable =
    disconnectUseCase()
      .andThen(Completable.fromRunnable { suplaAppProvider.provide().SuplaClientInitIfNeed(applicationContext) })
}
