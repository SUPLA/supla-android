package org.supla.android.profile
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


import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import org.supla.android.SuplaApp
import org.supla.android.data.source.ProfileRepository
import org.supla.android.db.AuthProfileItem
import org.supla.android.db.DbHelper
import org.supla.android.scenes.SceneEventsManager
import org.supla.android.widget.WidgetVisibilityHandler

class MultiAccountProfileManager(
  private val dbHelper: DbHelper,
  private val profileRepository: ProfileRepository,
  private val profileIdHolder: ProfileIdHolder,
  private val widgetVisibilityHandler: WidgetVisibilityHandler,
  private val sceneEventsManager: SceneEventsManager
) : ProfileManager {

  override fun create(profile: AuthProfileItem): Completable = Completable.fromRunnable {
    if (profile.id != null) {
      throw IllegalArgumentException("Entity which is planned to be created in the database shouldn't have ID!")
    }
    profile.id = profileRepository.createProfile(profile)
  }

  override fun read(id: Long): Maybe<AuthProfileItem> = Maybe.fromCallable {
    profileRepository.getProfile(id)
  }

  override fun update(profile: AuthProfileItem): Completable = Completable.fromRunnable {
    if (profile.id == null) {
      throw IllegalArgumentException("It's not possible update entity without ID!")
    }
    profileRepository.updateProfile(profile)
  }

  override fun delete(id: Long): Completable = Completable.fromRunnable {
    profileRepository.deleteProfile(id)
    widgetVisibilityHandler.onProfileRemoved(id)
  }

  override fun getAllProfiles(): Observable<List<AuthProfileItem>> = Observable.fromCallable {
    profileRepository.allProfiles
  }

  override fun getCurrentProfile(): Maybe<AuthProfileItem> = Maybe.fromCallable {
    profileRepository.allProfiles.firstOrNull { it.isActive }
  }

  override fun activateProfile(id: Long, force: Boolean): Completable = Completable.fromRunnable {
    val current = profileRepository.allProfiles.firstOrNull { it.isActive }
    if (current != null && current.id == id && !force) {
      return@fromRunnable
    }

    if (profileRepository.setProfileActive(id)) {
      profileIdHolder.profileId = id
    }
    initiateReconnect()
    dbHelper.loadUserIconsIntoCache()
    sceneEventsManager.cleanup()
  }

  private fun initiateReconnect() {
    with(SuplaApp.getApp()) {
      CancelAllRestApiClientTasks(true)
      cleanupToken()
      suplaClient?.reconnect()
    }
  }
}
