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
import org.supla.android.db.AuthProfileItem

/**
 * Additional holder class is needed because of circular dependency between
 * MultiAccountProfileManager and DbHelper
 */
data class ProfileIdHolder(var profileId: Long?)

interface ProfileManager {

  /**
   * Saves profile to database and sets the ID
   */
  fun create(profile: AuthProfileItem): Completable

  /**
   * Tries to get a profile for the given ID
   */
  fun read(id: Long): Maybe<AuthProfileItem>

  /**
   * Updates given profile in the DB
   */
  fun update(profile: AuthProfileItem): Completable

  /**
   * Deletes profile with the given ID from DB
   */
  fun delete(id: Long): Completable

  /**
   * Gets a list of all profiles stored in DB
   */
  fun getAllProfiles(): Observable<List<AuthProfileItem>>

  /**
   * Gets a profile which currently is set as active
   */
  fun getCurrentProfile(): Maybe<AuthProfileItem>
}
