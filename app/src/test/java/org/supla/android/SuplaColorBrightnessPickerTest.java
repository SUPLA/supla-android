package org.supla.android;
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

import android.graphics.Color;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;

public class SuplaColorBrightnessPickerTest extends TestCase {

    private SuplaColorBrightnessPicker picker;

    @Before
    public void setUp() throws Exception {
        picker = new SuplaColorBrightnessPicker(null);
    }

    @After
    public void tearDown() throws Exception {
        picker = null;
    }

    public void testColorWheelVisibleSetterAndGetter() {
        Assert.assertTrue(picker.isColorWheelVisible());
        picker.setColorWheelVisible(false);
        Assert.assertFalse(picker.isColorWheelVisible());
        picker.setColorWheelVisible(true);
        Assert.assertTrue(picker.isColorWheelVisible());
    }

    public void testCircleInsteadArrowSetterAndGetter() {
        Assert.assertTrue(picker.isCircleInsteadArrow());
        picker.setCircleInsteadArrow(false);
        Assert.assertFalse(picker.isCircleInsteadArrow());
        picker.setCircleInsteadArrow(true);
        Assert.assertTrue(picker.isCircleInsteadArrow());
    }

    public void testColorfulBrightnessWheelSetterAndGetter() {
        Assert.assertTrue(picker.isColorfulBrightnessWheel());
        picker.setColorfulBrightnessWheel(false);
        Assert.assertFalse(picker.isColorfulBrightnessWheel());
        picker.setColorfulBrightnessWheel(true);
        Assert.assertTrue(picker.isColorfulBrightnessWheel());
    }

    public void testSliderVisibleSetterAndGetter() {
        Assert.assertFalse(picker.isSliderVisible());
        picker.setSliderVisible(true);
        Assert.assertTrue(picker.isSliderVisible());
        picker.setSliderVisible(false);
        Assert.assertFalse(picker.isSliderVisible());
    }

    public void testPowerButtonVisibleSetterAndGetter() {
        Assert.assertTrue(picker.isPowerButtonVisible());
        picker.setPowerButtonVisible(false);
        Assert.assertFalse(picker.isPowerButtonVisible());
        picker.setPowerButtonVisible(true);
        Assert.assertTrue(picker.isPowerButtonVisible());
    }

    public void testPowerButtonEnabledSetterAndGetter() {
        Assert.assertTrue(picker.isPowerButtonEnabled());
        picker.setPowerButtonEnabled(false);
        Assert.assertFalse(picker.isPowerButtonEnabled());
        picker.setPowerButtonEnabled(true);
        Assert.assertTrue(picker.isPowerButtonEnabled());
    }

    public void testPowerButtonOnSetterAndGetter() {
        Assert.assertFalse(picker.isPowerButtonOn());
        picker.setPowerButtonOn(true);
        Assert.assertTrue(picker.isPowerButtonOn());
        picker.setPowerButtonOn(false);
        Assert.assertFalse(picker.isPowerButtonOn());
    }

    public void testPowerButtonColorOnSetterAndGetter() {
        Assert.assertEquals(0xffffffff, picker.getPowerButtonColorOn());
        picker.setPowerButtonColorOn(Color.BLUE);
        Assert.assertEquals(Color.BLUE, picker.getPowerButtonColorOn());
    }

    public void testPowerButtonColorOffSetterAndGetter() {
        Assert.assertEquals(0xff404040, picker.getPowerButtonColorOff());
        picker.setPowerButtonColorOff(Color.BLUE);
        Assert.assertEquals(Color.BLUE, picker.getPowerButtonColorOff());
    }

    public void testColorSetterAndGetter() {
        Assert.assertEquals(0xff00ff00, picker.getColor());
        picker.setColor(Color.BLUE);
        Assert.assertEquals(Color.BLUE, picker.getColor());
    }

    public void testBrightnessSetterAndGetter() {
        Assert.assertEquals(0f, picker.getBrightnessValue(), 0);
        picker.setBrightnessValue(55.54);
        Assert.assertEquals(55.54, picker.getBrightnessValue(), 0.001);
        picker.setBrightnessValue(-1);
        Assert.assertEquals(0, picker.getBrightnessValue(), 0);
        picker.setBrightnessValue(80.88);
        Assert.assertEquals(80.88, picker.getBrightnessValue(), 0.001);
        picker.setBrightnessValue(110);
        Assert.assertEquals(100, picker.getBrightnessValue(), 0);
    }

    public void testMovingGetter() {
        Assert.assertFalse(picker.isMoving());
    }

    public void testBrightnessMarkersSetterAndGetter() {
        Assert.assertNull(picker.getBrightnessMarkers());

        ArrayList<Double> brigtness = new ArrayList<>();
        brigtness.add(0.0);
        brigtness.add(10.0);
        brigtness.add(50.0);
        brigtness.add(90.0);
        brigtness.add(100.0);
        picker.setBrightnessMarkers(brigtness);

        Assert.assertNotNull(picker.getBrightnessMarkers());
        Assert.assertEquals(5, picker.getBrightnessMarkers().size(), 0);
        picker.setBrightnessMarkers(null);
        Assert.assertNull(picker.getBrightnessMarkers());
    }

    public void testColorMarkersSetterAndGetter() {
        Assert.assertNull(picker.getColorMarkers());

        ArrayList<Double> colors = new ArrayList<>();
        colors.add(new Double(Color.RED));
        colors.add(new Double(Color.GREEN));
        colors.add(new Double(Color.BLUE));
        picker.setColorMarkers(colors);

        Assert.assertNotNull(picker.getColorMarkers());
        Assert.assertEquals(3, picker.getColorMarkers().size(), 0);
        picker.setColorMarkers(null);
        Assert.assertNull(picker.getColorMarkers());


        Double d = new Double(Color.WHITE);
        Assert.assertEquals(Color.WHITE, d, 0);
        Assert.assertEquals(Color.WHITE, d.intValue(), 0);

        d = new Double(Color.BLACK);
        Assert.assertEquals(Color.BLACK, d, 0);
        Assert.assertEquals(Color.BLACK, d.intValue(), 0);
    }
}