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
import org.supla.android.data.source.ProfileRepository
import org.supla.android.data.source.DefaultUserIconRepository
import org.supla.android.data.source.local.LocalProfileRepository
import org.supla.android.data.source.local.UserIconDao
import org.supla.android.images.ImageCacheProvider
import org.supla.android.images.ImageId

@RunWith(AndroidJUnit4::class)
@Config(sdk = [24])
class DefaultProfileManagerTest: TestCase() {

    lateinit var pM: ProfileManager

    @Before
    override public fun setUp() {
        val inst = DbHelper::class.java.getDeclaredField("instance")
        inst.setAccessible(true)
        inst.set(null, null)

        pM = DefaultProfileManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testDefaultProfileExists() {
        assertEquals(1, pM.activeProfile.id)
    }

    @Test
    fun testCanSwitchToOtherProfile() {
        assertEquals(1, pM.activeProfile.id)
        val dbh = DbHelper.getInstance(ApplicationProvider.getApplicationContext())
        val repo = LocalProfileRepository(dbh)
        val id2 = repo.createNamedProfile("p2")
        assertEquals(1, pM.activeProfile.id)
        pM.activateProfile(id2)
        assertEquals(id2, pM.activeProfile.id)
        assertEquals("p2", pM.activeProfile.name)
    }

    @Test
    fun userIconsAreProfileBound() {
        val dbh = DbHelper.getInstance(ApplicationProvider.getApplicationContext())
        val repo = LocalProfileRepository(dbh)
        val cp = MockedImageCacheProvider()
        val udao = UserIconDao(dbh)
        val id1 = repo.createNamedProfile("p1")
        assertEquals(1, id1)
        val id2 = repo.createNamedProfile("p2")
        assertEquals(2, id2)
        val uirepo1 = DefaultUserIconRepository(udao, cp, id1)
        val uirepo2 = DefaultUserIconRepository(udao, cp, id2)
        
        val iconid1 = 1
        val iconid2 = 2

        // given that each profile has a single icon added
        val img1 = byteArrayOf(0x08)
        val img2 = byteArrayOf(0x09)
        uirepo1.addUserIcons(iconid1, img1, img1, img1, img1)
        uirepo2.addUserIcons(iconid2, img2, img2, img2, img2)

        // when profile 1 is selected
        pM.activateProfile(id1)
        
        // then icon set 1 is available
        assertEquals(id1, pM.activeProfile.id)
        DefaultUserIconRepository(udao, cp, pM.activeProfile.id)
            .loadUserIconsIntoCache()
        assertEquals(0x08, cp.img[0])

        // when profile 2 is selected
        pM.activateProfile(id2)
        
        // then icon set 2 is available
        assertEquals(id2, pM.activeProfile.id)
        DefaultUserIconRepository(udao, cp, pM.activeProfile.id)
            .loadUserIconsIntoCache()
        assertEquals(0x09, cp.img[0])

    }
}

class MockedImageCacheProvider: ImageCacheProvider() {

    var img = byteArrayOf()

    override public fun addImage(imgid: ImageId, image: ByteArray) {
        img = image
    }
}
