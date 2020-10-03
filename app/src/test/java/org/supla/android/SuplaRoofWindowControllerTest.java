package org.supla.android;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class SuplaRoofWindowControllerTest extends TestCase {

    private static final int REFCOLOR = 0xFFAABBCC;
    private SuplaRoofWindowController roofWindowController;

    @Override
    protected void setUp() throws Exception {
        roofWindowController = new SuplaRoofWindowController(null);
    }

    @Test
    public void testFrameSetterAndGetter() {
        Assert.assertEquals(0xffb49a63, roofWindowController.getFrameColor());
        roofWindowController.setFrameColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getFrameColor());
    }

    @Test
    public void testFrontSetterAndGetter() {
        Assert.assertEquals(0xFF61645c, roofWindowController.getFrontColor());
        roofWindowController.setFrontColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getFrontColor());
    }

    @Test
    public void testOpeningPercentageSetterAndGetter() {
        Assert.assertEquals(0, roofWindowController.getOpeningPercentage(), 0);
        roofWindowController.setOpeningPercentage(55.55f);
        Assert.assertEquals(55.55f, roofWindowController.getOpeningPercentage(), 0.001);
        roofWindowController.setOpeningPercentage(-1f);
        Assert.assertEquals(0, roofWindowController.getOpeningPercentage(), 0);
        roofWindowController.setOpeningPercentage(110f);
        Assert.assertEquals(100, roofWindowController.getOpeningPercentage(), 0);
    }
}