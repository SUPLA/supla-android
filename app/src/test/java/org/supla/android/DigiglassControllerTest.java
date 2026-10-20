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
import org.junit.Assert;
import org.junit.Test;

public class DigiglassControllerTest extends TestCase
    implements DigiglassController.OnSectionClickListener {

  private DigiglassController controller;
  private static final int REFCOLOR = 0xFFAABBCC;

  @Override
  protected void setUp() throws Exception {
    controller = new DigiglassController(null);
  }

  @Test
  public void testBarColorSetterAndGetter() {
    Assert.assertEquals(Color.WHITE, controller.getBarColor());
    controller.setBarColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getBarColor());
  }

  @Test
  public void testLineColorSetterAndGetter() {
    Assert.assertEquals(Color.BLACK, controller.getLineColor());
    controller.setLineColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getLineColor());
  }

  @Test
  public void testDotColorSetterAndGetter() {
    Assert.assertEquals(Color.BLACK, controller.getDotColor());
    controller.setDotColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getDotColor());
  }

  @Test
  public void testGlassColorSetterAndGetter() {
    Assert.assertEquals(0xffbdd8f2, controller.getGlassColor());
    controller.setGlassColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getGlassColor());
  }

  @Test
  public void testBtnBackgroundColorSetterAndGetter() {
    Assert.assertEquals(Color.WHITE, controller.getBtnBackgroundColor());
    controller.setBtnBackgroundColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getBtnBackgroundColor());
  }

  @Test
  public void testBtnDotColorSetterAndGetter() {
    Assert.assertEquals(Color.BLACK, controller.getBtnDotColor());
    controller.setBtnDotColor(REFCOLOR);
    Assert.assertEquals(REFCOLOR, controller.getBtnDotColor());
  }

  @Test
  public void testClickListenerSetterAndGetter() {
    Assert.assertNull(controller.getOnSectionClickListener());
    controller.setOnSectionClickListener(this);
    Assert.assertEquals(this, controller.getOnSectionClickListener());
    controller.setOnSectionClickListener(null);
    Assert.assertNull(controller.getOnSectionClickListener());
  }

  @Test
  public void testHorizontalSetterAndGetter() {
    Assert.assertFalse(controller.isVertical());
    controller.setVertical(true);
    Assert.assertTrue(controller.isVertical());
    controller.setVertical(false);
    Assert.assertFalse(controller.isVertical());
  }

  @Test
  public void testLineWidthSetterAndGetter() {
    Assert.assertEquals(2, controller.getLineWidth(), 0);
    controller.setLineWidth(1);
    Assert.assertEquals(1, controller.getLineWidth(), 0);
    controller.setLineWidth(0);
    Assert.assertEquals(0.1, controller.getLineWidth(), 0.1);
    controller.setLineWidth(21);
    Assert.assertEquals(20, controller.getLineWidth(), 0);
  }

  @Test
  public void testSections() {
    Assert.assertEquals(7, controller.getSectionCount());
    controller.setSectionCount(8);
    Assert.assertEquals(7, controller.getSectionCount());
    controller.setSectionCount(0);
    Assert.assertEquals(1, controller.getSectionCount());
    controller.setSectionCount(5);
    Assert.assertEquals(5, controller.getSectionCount());

    controller.setTransparentSections(1);
    Assert.assertNotEquals(0, controller.getTransparentSections());
    controller.setAllOpaque();
    Assert.assertEquals(0, controller.getTransparentSections());
    controller.setAllTransparent();
    Assert.assertEquals(31, controller.getTransparentSections());
    controller.setSectionCount(7);
    Assert.assertEquals(31, controller.getTransparentSections());
    controller.setAllTransparent();
    Assert.assertEquals(127, controller.getTransparentSections());
    controller.setSectionCount(1);
    Assert.assertEquals(1, controller.getTransparentSections());
    controller.setSectionCount(2);

    Assert.assertTrue(controller.isSectionTransparent(0));
    Assert.assertFalse(controller.isSectionTransparent(2));

    controller.setSectionCount(6);
    controller.setTransparentSections(101);
    Assert.assertEquals(37, controller.getTransparentSections());
  }

  @Override
  public void onGlassSectionClick(
      DigiglassController controller, int section, boolean transparent) {}
}
