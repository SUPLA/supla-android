package org.supla.android.lib

import org.junit.Assert
import org.junit.Test
import java.util.*

class SuplaSceneStateTest {
    @Test
    fun `The scene has not started`() {
        val state = SuplaSceneState(155,0,0,
            415, "iPhone Elon", true)
        Assert.assertEquals(155, state.sceneId)
        Assert.assertNull(state.startedAt)
        Assert.assertNull(state.estimatedEndDate)
        Assert.assertFalse(state.isDuringExecution)
        Assert.assertEquals(0, state.millisecondsLeft)
        Assert.assertEquals(415, state.initiatorId)
        Assert.assertEquals("iPhone Elon", state.initiatorName)
        Assert.assertTrue(state.isEol)
    }

    @Test
    fun `The scene is during execution`() {
        val state1 = SuplaSceneState(156,100,10,
            0, "", false);
        Assert.assertEquals(156, state1.sceneId)
        Assert.assertTrue(state1.isDuringExecution)
        Assert.assertFalse(state1.isEol)

        val state2 = SuplaSceneState(156,0,10,
            0, "", false);
        Assert.assertTrue(state2.isDuringExecution)
    }

    @Test
    fun `The scene is done`() {
        val state = SuplaSceneState(156,200,0,
            0, "", false);
        Assert.assertEquals(156, state.sceneId)
        Assert.assertNotNull(state.startedAt)
        Assert.assertNotNull(state.estimatedEndDate)
        Assert.assertFalse(state.isDuringExecution)
    }
}