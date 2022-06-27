package org.supla.android.lib

import org.junit.Assert
import org.junit.Test

class SuplaSceneTest {

    @Test
    fun `class initialization`() {
        val scene = SuplaScene(10,20,30,40, "My Test Scene", true);
        Assert.assertEquals(10, scene.id)
        Assert.assertEquals(20, scene.locationId)
        Assert.assertEquals(30, scene.altIcon)
        Assert.assertEquals(40, scene.userIcon)
        Assert.assertEquals("My Test Scene", scene.caption);
        Assert.assertTrue(scene.isEol);
    }
}