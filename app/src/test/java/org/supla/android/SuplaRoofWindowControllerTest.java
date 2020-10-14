package org.supla.android;

import android.graphics.Color;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

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
    public void testMarkerColorSetterAndGetter() {
        Assert.assertEquals(0xFFbed9f1, roofWindowController.getMarkerColor());
        roofWindowController.setMarkerColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, roofWindowController.getMarkerColor());
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

    @Test
    public void testMarkersSetterAndGetter() {
        Assert.assertNull(roofWindowController.getMarkers());

        ArrayList<Float> markers = new ArrayList<>();
        markers.add(-100f);
        markers.add(10.55f);
        markers.add(40.20f);
        markers.add(60.70f);
        markers.add(110f);

        roofWindowController.setMarkers(markers);

        Assert.assertNotNull(roofWindowController.getMarkers());
        markers = roofWindowController.getMarkers();

        Assert.assertEquals(0, markers.get(0), 0);
        Assert.assertEquals(10.55, markers.get(1), 0.001);
        Assert.assertEquals(40.20, markers.get(2), 0.001);
        Assert.assertEquals(60.70, markers.get(3), 0.001);
        Assert.assertEquals(100, markers.get(4), 0);
    }
}