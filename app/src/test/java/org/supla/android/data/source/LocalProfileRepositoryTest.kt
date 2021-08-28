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
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.database.sqlite.SQLiteConstraintException
import org.supla.android.BuildConfig
import org.supla.android.db.DbHelper
import org.supla.android.data.source.local.LocalProfileRepository

@RunWith(AndroidJUnit4::class)
@Config(sdk = [24])
class LocalProfileRepositoryTest: TestCase() {

    lateinit var repository: ProfileRepository

    @Before
    override public fun setUp() { 
        // Shamelessly copied from https://github.com/robolectric/robolectric/issues/1890
        val inst = DbHelper::class.java.getDeclaredField("instance")
        inst.setAccessible(true)
        inst.set(null, null)

        val dbHelper = DbHelper.getInstance(ApplicationProvider.getApplicationContext())
        dbHelper.getWritableDatabase()
        repository = LocalProfileRepository(dbHelper)
    }

    @Test
    fun testCreateSingleProfile() {
        val id = repository.createNamedProfile("p1")
        assertEquals(1, id)
        val profile = repository.allProfiles.get(0)
        assertEquals(1, profile.id)
        assertEquals("p1", profile.name)
    }

    @Test
    fun testUniqueProfileName() {
        val id = repository.createNamedProfile("p1")
        assertEquals(1, id)
        assertThrows(SQLiteConstraintException::class.java) {
            repository.createNamedProfile("p1")
        }
    }

    @Test
    fun testAddManyProfiles() {
        val id1 = repository.createNamedProfile("p1")
        val id2 = repository.createNamedProfile("p2")
        assertEquals(1, id1)
        assertEquals(2, id2)

        assertEquals(2, repository.allProfiles.size)
        assertEquals(id1, repository.allProfiles.get(0).id)
        assertEquals(id2, repository.allProfiles.get(1).id)
        assertEquals("p1", repository.allProfiles.get(0).name)
        assertEquals("p2", repository.allProfiles.get(1).name)
    }

    @Test
    fun profileCanBeRenamed() {
        val id = repository.createNamedProfile("default")
        val profile = repository.getProfile(id)!!
        assertEquals("default", profile.name)
        profile.name = "alt"
        repository.updateProfile(profile)
        val p2 = repository.getProfile(id)!!
        assertEquals("alt", p2.name)
    }

    @Test
    fun profileCanBeDeleted() {
        val id1 = repository.createNamedProfile("p1")
        val id2 = repository.createNamedProfile("p2")
        assertEquals(1, id1)
        assertEquals(2, id2)

        assertEquals(2, repository.allProfiles.size)
        repository.deleteProfile(id1)
        assertEquals(1, repository.allProfiles.size)
        
        assertEquals(id2, repository.allProfiles.get(0).id)
        assertEquals("p2", repository.allProfiles.get(0).name)
    }

    @Test
    fun profileNamesCannotCollide() {
        val id1 = repository.createNamedProfile("p1")
        val id2 = repository.createNamedProfile("p2")
        assertEquals(1, id1)
        assertEquals(2, id2)
        val p1 = repository.getProfile(id1)!!
        assertEquals("p1", p1.name)
        assertEquals("p2", repository.getProfile(id2)!!.name)
        p1.name = "p2"
        assertThrows(SQLiteConstraintException::class.java) {
            repository.updateProfile(p1)
        }
    }

    @Test
    fun profileEmailAuthSettingsArePersisted() {
        val id = repository.createNamedProfile("default")
        val profile = repository.getProfile(id)!!
        assertEquals("default", profile.name)
        profile.emailAddr = "hi@there.com"
        repository.updateProfile(profile)
        val p2 = repository.getProfile(id)!!
        assertEquals("hi@there.com", p2.emailAddr)
    }

    @Test
    fun profileAccessIDSettingsArePersisted() {
        val id = repository.createNamedProfile("default")
        val profile = repository.getProfile(id)!!
        assertEquals("default", profile.name)
        profile.serverAddr = "a.host.somewhere.there"
        profile.accessID = 314159
        profile.accessIDpwd = "abrakadabra"
        repository.updateProfile(profile)
        val p2 = repository.getProfile(id)!!
        assertEquals("a.host.somewhere.there", p2.serverAddr)
        assertEquals(314159, p2.accessID)
        assertEquals("abrakadabra", p2.accessIDpwd)
    }
}
