package org.supla.android.lib;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class DigiglassValueTest extends TestCase {
  @Test
  public void testNull() {
    DigiglassValue val = new DigiglassValue(null);
    Assert.assertEquals(0, val.getFlags());
    Assert.assertEquals(0, val.getMask());
    Assert.assertEquals(0, val.getSectionCount());
  }

  @Test
  public void testIncorrectLength() {
    byte[] v = new byte[1];
    v[0] = 1;
    DigiglassValue val = new DigiglassValue(v);
    Assert.assertEquals(0, val.getFlags());
    Assert.assertEquals(0, val.getMask());
    Assert.assertEquals(0, val.getSectionCount());
  }

  @Test
  public void testCorrectValue() {
    byte[] v = new byte[8];
    v[0] = DigiglassValue.TOO_LONG_OPERATION_WARNING;
    v[1] = 7;
    v[2] = 31;
    v[3] = 0;

    DigiglassValue val = new DigiglassValue(v);
    Assert.assertEquals(1, val.getFlags());
    Assert.assertEquals(31, val.getMask());
    Assert.assertEquals(7, val.getSectionCount());

    Assert.assertTrue(val.isAnySectionTransparent());
    Assert.assertFalse(val.isPlannedRegenerationInProgress());
    Assert.assertTrue(val.isTooLongOperationWarningPresent());
    Assert.assertTrue(val.isSectionTransparent(0));
    Assert.assertTrue(val.isSectionTransparent(4));
    Assert.assertFalse(val.isSectionTransparent(5));

    v[0] = DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS;
    v[1] = 5;
    v[2] = 0;
    v[3] = 0;

    val = new DigiglassValue(v);

    Assert.assertFalse(val.isAnySectionTransparent());
    Assert.assertTrue(val.isPlannedRegenerationInProgress());
    Assert.assertFalse(val.isTooLongOperationWarningPresent());
    Assert.assertFalse(val.isSectionTransparent(0));

    v[0] =
        DigiglassValue.TOO_LONG_OPERATION_WARNING | DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS;

    val = new DigiglassValue(v);

    Assert.assertFalse(val.isAnySectionTransparent());
    Assert.assertTrue(val.isPlannedRegenerationInProgress());
    Assert.assertTrue(val.isTooLongOperationWarningPresent());
    Assert.assertFalse(val.regenerationAfter20hInProgress());
    Assert.assertFalse(val.isSectionTransparent(0));

    v[0] =
        DigiglassValue.TOO_LONG_OPERATION_WARNING
            | DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS
            | DigiglassValue.REGENERATION_AFTER_20H_IN_PROGRESS;

    val = new DigiglassValue(v);

    Assert.assertFalse(val.isAnySectionTransparent());
    Assert.assertTrue(val.isPlannedRegenerationInProgress());
    Assert.assertTrue(val.isTooLongOperationWarningPresent());
    Assert.assertTrue(val.regenerationAfter20hInProgress());
    Assert.assertFalse(val.isSectionTransparent(0));
  }
}
