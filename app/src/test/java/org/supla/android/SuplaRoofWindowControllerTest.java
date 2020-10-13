package org.supla.android;

import android.graphics.Color;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class SuplaRoofWindowControllerTest extends TestCase {

    private static final int REFCOLOR = 0xFFAABBCC;
    private SuplaRoofWindowController roofWindowController;

    @Override
    protected void setUp() throws Exception {
        roofWindowController = new SuplaRoofWindowController(null);
    }

    @Test
    public void testFrameColorSetterAndGetter() {
        Assert.assertEquals(Color.WHITE, roofWindowController.getFrameColor());
        roofWindowController.setFrameColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getFrameColor());
    }

    @Test
    public void testLineColorSetterAndGetter() {
        Assert.assertEquals(Color.BLACK, roofWindowController.getLineColor());
        roofWindowController.setLineColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getLineColor());
    }

    @Test
    public void testGlassColorSetterAndGetter() {
        Assert.assertEquals(0xFFbed9f1, roofWindowController.getGlassColor());
        roofWindowController.setGlassColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getGlassColor());
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