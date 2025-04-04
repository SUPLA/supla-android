package org.supla.android.automotive
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

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import dagger.hilt.android.AndroidEntryPoint
import org.supla.android.Preferences
import org.supla.android.automotive.screen.MainScreen
import org.supla.android.core.infrastructure.DateProvider
import org.supla.android.data.source.AndroidAutoItemRepository
import org.supla.android.events.UpdateEventsManager
import org.supla.android.lib.singlecall.SingleCall
import org.supla.android.tools.SuplaSchedulers
import org.supla.android.usecases.icon.GetChannelIconUseCase
import org.supla.android.usecases.icon.GetSceneIconUseCase
import javax.inject.Inject

@AndroidEntryPoint
class SuplaCarAppService : CarAppService() {

  @Inject
  lateinit var updateEventsManager: UpdateEventsManager

  @Inject
  lateinit var schedulers: SuplaSchedulers

  @Inject
  lateinit var getSceneIconUseCase: GetSceneIconUseCase

  @Inject
  lateinit var singleCallProvider: SingleCall.Provider

  @Inject
  lateinit var androidAutoItemRepository: AndroidAutoItemRepository

  @Inject
  lateinit var getChannelIconUseCase: GetChannelIconUseCase

  @Inject
  lateinit var dateProvider: DateProvider

  @Inject
  lateinit var preferences: Preferences

  override fun createHostValidator(): HostValidator {
    return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
  }

  override fun onCreateSession(): Session {
    return SuplaSession(
      androidAutoItemRepository,
      getChannelIconUseCase,
      getSceneIconUseCase,
      updateEventsManager,
      singleCallProvider,
      schedulers,
      dateProvider,
      preferences
    )
  }
}

class SuplaSession(
  private val androidAutoItemRepository: AndroidAutoItemRepository,
  private val getChannelIconUseCase: GetChannelIconUseCase,
  private val getSceneIconUseCase: GetSceneIconUseCase,
  private val updateEventsManager: UpdateEventsManager,
  private val singleCallProvider: SingleCall.Provider,
  private val schedulers: SuplaSchedulers,
  private val dateProvider: DateProvider,
  private val preferences: Preferences,
) : Session() {

  override fun onCreateScreen(intent: Intent): Screen {
    return MainScreen(
      androidAutoItemRepository = androidAutoItemRepository,
      getChannelIconUseCase = getChannelIconUseCase,
      updateEventsManager = updateEventsManager,
      getSceneIconUseCase = getSceneIconUseCase,
      singleCallProvider = singleCallProvider,
      schedulers = schedulers,
      carContext = carContext,
      dateProvider = dateProvider,
      preferences = preferences
    )
  }
}
