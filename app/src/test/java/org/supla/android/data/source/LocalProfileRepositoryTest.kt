package org.supla.android.data.source

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

import junit.framework.TestCase
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.supla.android.BuildConfig
import org.supla.android.db.DbHelper
import org.supla.android.data.source.local.LocalProfileRepository

@RunWith(AndroidJUnit4::class)
@Config(sdk = [24])
class LocalProfileRepositoryTest: TestCase() {

    lateinit var repository: ProfileRepository

    @Before
    override public fun setUp() {
        val dbHelper = DbHelper.getInstance(ApplicationProvider.getApplicationContext())
        repository = LocalProfileRepository(dbHelper)
    }

    override public fun tearDown() {

    }
    
    @Test
    fun testCreateSingleProfile() {
        val id = repository.createNamedProfile("kanalia")
        assertEquals(0, id)
    }

}
