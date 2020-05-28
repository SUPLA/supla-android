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

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

public class SuplaRangeCalibrationWheelTest extends TestCase {

    private static final int REFCOLOR = 0xAABBCC;
    private SuplaRangeCalibrationWheel calibrationWheel;

    @Override
    protected void setUp() throws Exception {
        calibrationWheel = new SuplaRangeCalibrationWheel(null);
    }


    @Test
    public void testWheelColorSetterAndGetter() {
        Assert.assertEquals(0xB0ABAB, calibrationWheel.getWheelColor());
        calibrationWheel.setWheelColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getWheelColor());
    }

    @Test
    public void testBorderColorSetterAndGetter() {
        Assert.assertEquals(0x575757, calibrationWheel.getBorderColor());
        calibrationWheel.setBorderColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getBorderColor());
    }

    @Test
    public void testBtnColorSetterAndGetter() {
        Assert.assertEquals(0x575757, calibrationWheel.getBtnColor());
        calibrationWheel.setBtnColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getBtnColor());
    }

    @Test
    public void testRangeColorSetterAndGetter() {
        Assert.assertEquals(0xFFE617, calibrationWheel.getRangeColor());
        calibrationWheel.setRangeColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getRangeColor());
    }

    @Test
    public void testInsideBtnColorSetterAndGetter() {
        Assert.assertEquals(0xFFFFFF, calibrationWheel.getInsideBtnColor());
        calibrationWheel.setInsideBtnColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getInsideBtnColor());
    }

    @Test
    public void testBoostLineColorSetterAndGetter() {
        Assert.assertEquals(0x12A61F, calibrationWheel.getBoostLineColor());
        calibrationWheel.setBoostLineColor(REFCOLOR);
        Assert.assertEquals(REFCOLOR, calibrationWheel.getBoostLineColor());
    }

    @Test
    public void testBorderLineWidthSetterAndGetter() {
        Assert.assertEquals(1.5f, calibrationWheel.getBorderLineWidth(), 0.001);
        calibrationWheel.setBorderLineWidth(0);
        Assert.assertTrue(calibrationWheel.getBorderLineWidth() > 0);
        calibrationWheel.setBorderLineWidth(11.55f);
        Assert.assertEquals(11.55f, calibrationWheel.getBorderLineWidth(), 0.0001);
    }

    @Test
    public void testMaximumValueSetterAndGetter() {
        Assert.assertEquals(1000, calibrationWheel.getMaximumValue(), 0);
        calibrationWheel.setMaximumValue(0);
        Assert.assertEquals(calibrationWheel.getMinimumRange(),
                calibrationWheel.getMaximumValue(), 0);
        Assert.assertEquals(0, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(calibrationWheel.getMinimumRange(),
                calibrationWheel.getMaximum(), 0);
        Assert.assertEquals(calibrationWheel.getMinimumRange(),
                calibrationWheel.getRightEdge(), 0);

        calibrationWheel.setMaximumValue(1500);
        calibrationWheel.setRightEdge(1500);
        calibrationWheel.setMaximum(1500);
        calibrationWheel.setMinimum(1000);

        Assert.assertEquals(1500, calibrationWheel.getMaximumValue(), 0);

        calibrationWheel.setMaximumValue(500);

        Assert.assertEquals(500, calibrationWheel.getMaximumValue(), 0);
        Assert.assertEquals(500, calibrationWheel.getMaximum(), 0);
        Assert.assertEquals(500, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(calibrationWheel.getMaximum() - calibrationWheel.getMinimumRange(),
                calibrationWheel.getMinimum(), 0);
    }

    @Test
    public void testMinimumRangeSetterAndGetter() {
        Assert.assertEquals(calibrationWheel.getMinimumRange(), 100, 0);

        calibrationWheel.setMaximumValue(1000);
        calibrationWheel.setMinimumRange(0);
        calibrationWheel.setLeftEdge(500);
        calibrationWheel.setRightEdge(600);

        Assert.assertEquals(0, calibrationWheel.getMinimumRange(), 0);
        Assert.assertEquals(500, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(500, calibrationWheel.getLeftEdge(), 0);

        Assert.assertEquals(500, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(600, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(500, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(600, calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMinimumRange(200);

        Assert.assertEquals(200, calibrationWheel.getMinimumRange(), 0);
        Assert.assertEquals(450, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(650, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(450, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(650, calibrationWheel.getMaximum(), 0);

        Assert.assertTrue(calibrationWheel.getRightEdge() - calibrationWheel.getLeftEdge()
                >= calibrationWheel.getMinimumRange());

        Assert.assertTrue(calibrationWheel.getRightEdge() - calibrationWheel.getLeftEdge()
                <= calibrationWheel.getMaximumValue());

        Assert.assertTrue(calibrationWheel.getMaximum() - calibrationWheel.getMinimum()
                >= calibrationWheel.getMinimumRange());

        Assert.assertTrue(calibrationWheel.getMaximum() - calibrationWheel.getMinimum()
                <= calibrationWheel.getMaximumValue());

        calibrationWheel.setMinimumRange(calibrationWheel.getMaximumValue() + 1);

        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getMinimumRange(), 0);
        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(0,
                calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMinimumRange(0);
        calibrationWheel.setLeftEdge(100);
        calibrationWheel.setRightEdge(900);
        calibrationWheel.setMaximum(500);
        calibrationWheel.setMinimum(500);

        Assert.assertEquals(100, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(900, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(500, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(500, calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMinimumRange(200);

        Assert.assertEquals(400, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(600, calibrationWheel.getMaximum(), 0);
        Assert.assertEquals(100, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(900, calibrationWheel.getRightEdge(), 0);

        Assert.assertTrue(calibrationWheel.getMaximum() - calibrationWheel.getMinimum()
                >= calibrationWheel.getMinimumRange());
        Assert.assertTrue(calibrationWheel.getMaximum() - calibrationWheel.getMinimum()
                <= calibrationWheel.getMaximumValue());
    }

    @Test
    public void testNumberOfTurnsSetterAndGetter() {
        Assert.assertEquals(5, calibrationWheel.getNumerOfTurns(), 0);
        calibrationWheel.setNumerOfTurns(0);
        Assert.assertEquals(1, calibrationWheel.getNumerOfTurns(), 0);
        calibrationWheel.setNumerOfTurns(10);
        Assert.assertEquals(10, calibrationWheel.getNumerOfTurns(), 0);
    }

    public void testMinimumSetterAndGetter() {
        Assert.assertEquals(calibrationWheel.getLeftEdge(), calibrationWheel.getMinimum(), 0);
        calibrationWheel.setMinimum(300);
        calibrationWheel.setMaximum(600);

        Assert.assertEquals(300, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(600, calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMinimum(1000);

        Assert.assertEquals(
                calibrationWheel.getMaximum() - calibrationWheel.getMinimumRange(),
                calibrationWheel.getMinimum(), 0);

        calibrationWheel.setLeftEdge(200);
        calibrationWheel.setMinimum(-10);

        Assert.assertEquals(calibrationWheel.getLeftEdge(), calibrationWheel.getMinimum(), 0);

        calibrationWheel.setLeftEdge(0);
        calibrationWheel.setMinimum(-10);

        Assert.assertEquals(0, calibrationWheel.getMinimum(), 0);
    }

    public void testMaximumSetterAndGetter() {
        Assert.assertEquals(calibrationWheel.getRightEdge(), calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMinimum(300);
        calibrationWheel.setMaximum(600);
        calibrationWheel.setRightEdge(700);
        calibrationWheel.setMaximum(calibrationWheel.getRightEdge() + 10);

        Assert.assertEquals(calibrationWheel.getRightEdge(), calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMaximum(0);

        Assert.assertEquals(
                calibrationWheel.getMinimum() + calibrationWheel.getMinimumRange(),
                calibrationWheel.getMaximum(), 0);

        calibrationWheel.setMaximum(800);

        Assert.assertEquals(calibrationWheel.getRightEdge(), calibrationWheel.getMaximum(), 0);
    }

    public void testLeftEdgeSetterAndGetter() {
        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);

        calibrationWheel.setLeftEdge(100);
        calibrationWheel.setRightEdge(300);
        calibrationWheel.setMinimum(100);
        calibrationWheel.setMaximum(300);
        calibrationWheel.setMinimumRange(200);

        Assert.assertEquals(100, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(300, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(100, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(300, calibrationWheel.getMaximum(), 0);
        Assert.assertEquals(200, calibrationWheel.getMinimumRange(), 0);

        calibrationWheel.setLeftEdge(200);

        Assert.assertEquals(200, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(400, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(200, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(400, calibrationWheel.getMaximum(), 0);

        calibrationWheel.setLeftEdge(calibrationWheel.getMaximumValue() + 10);

        Assert.assertEquals(
                calibrationWheel.getMaximumValue() - calibrationWheel.getMinimumRange(),
                calibrationWheel.getLeftEdge(), 0);

        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(calibrationWheel.getLeftEdge(), calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(calibrationWheel.getRightEdge(), calibrationWheel.getMaximum(), 0);

        calibrationWheel.setLeftEdge(-10);

        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);
    }

    public void testRightEdgeSetterAndGetter() {
        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getRightEdge(), 0);

        calibrationWheel.setLeftEdge(100);
        calibrationWheel.setRightEdge(300);
        calibrationWheel.setMinimum(100);
        calibrationWheel.setMaximum(300);
        calibrationWheel.setMinimumRange(200);

        Assert.assertEquals(100, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(300, calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(100, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(300, calibrationWheel.getMaximum(), 0);
        Assert.assertEquals(200, calibrationWheel.getMinimumRange(), 0);

        calibrationWheel.setRightEdge(calibrationWheel.getMaximumValue() + 10);

        Assert.assertEquals(calibrationWheel.getMaximumValue(),
                calibrationWheel.getRightEdge(), 0);

        Assert.assertEquals(100, calibrationWheel.getLeftEdge(), 0);

        calibrationWheel.setRightEdge(-10);

        Assert.assertEquals(calibrationWheel.getMinimumRange(),
                calibrationWheel.getRightEdge(), 0);
        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(calibrationWheel.getLeftEdge(),
                calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(calibrationWheel.getRightEdge(),
                calibrationWheel.getMaximum(), 0);
    }

    public void testBoostLevelSetterAndGetter() {
        Assert.assertEquals(0, calibrationWheel.getLeftEdge(), 0);
        Assert.assertEquals(0, calibrationWheel.getMinimum(), 0);
        Assert.assertEquals(0, calibrationWheel.getBoostLevel(), 0);

        calibrationWheel.setLeftEdge(400);

        Assert.assertEquals(400, calibrationWheel.getBoostLevel(), 0);

        calibrationWheel.setLeftEdge(400);

        Assert.assertEquals(400, calibrationWheel.getBoostLevel(), 0);

        calibrationWheel.setRightEdge(0);

        Assert.assertEquals(100, calibrationWheel.getBoostLevel(), 0);

        calibrationWheel.setBoostLevel(-1);

        Assert.assertEquals(0, calibrationWheel.getBoostLevel(), 0);

        calibrationWheel.setBoostLevel(200);

        Assert.assertEquals(100, calibrationWheel.getBoostLevel(), 0);
    }

    public void testBoostHiddenSetterAndGetter() {
        Assert.assertFalse(calibrationWheel.isBoostVisible());
        calibrationWheel.setBoostVisible(true);
        Assert.assertTrue(calibrationWheel.isBoostVisible());
    }

    public void testBoostLineHeightSetterAndGetter() {
        Assert.assertEquals(1.8, calibrationWheel.getBoostLineHeightFactor(), 0.01);
        calibrationWheel.setBoostLineHeightFactor(0);
        Assert.assertEquals(1.8, calibrationWheel.getBoostLineHeightFactor(), 0.01);
        calibrationWheel.setBoostLineHeightFactor(1.1);
        Assert.assertEquals(1.1, calibrationWheel.getBoostLineHeightFactor(), 0.01);
        calibrationWheel.setBoostLineHeightFactor(2.1);
        Assert.assertEquals(1.1, calibrationWheel.getBoostLineHeightFactor(), 0.01);
    }
}