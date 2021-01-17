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

        Assert.assertEquals(true, val.isAnySectionTransparent());
        Assert.assertEquals(false, val.isPlannedRegenerationInProgress());
        Assert.assertEquals(true, val.isTooLongOperationWarningPresent());
        Assert.assertEquals(true, val.isSectionTransparent(0));
        Assert.assertEquals(true, val.isSectionTransparent(4));
        Assert.assertEquals(false, val.isSectionTransparent(5));

        v[0] = DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS;
        v[1] = 5;
        v[2] = 0;
        v[3] = 0;

        val = new DigiglassValue(v);

        Assert.assertEquals(false, val.isAnySectionTransparent());
        Assert.assertEquals(true, val.isPlannedRegenerationInProgress());
        Assert.assertEquals(false, val.isTooLongOperationWarningPresent());
        Assert.assertEquals(false, val.isSectionTransparent(0));

        v[0] = DigiglassValue.TOO_LONG_OPERATION_WARNING
                | DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS;

        val = new DigiglassValue(v);

        Assert.assertEquals(false, val.isAnySectionTransparent());
        Assert.assertEquals(true, val.isPlannedRegenerationInProgress());
        Assert.assertEquals(true, val.isTooLongOperationWarningPresent());
        Assert.assertEquals(false, val.regenerationAfter20hInProgress());
        Assert.assertEquals(false, val.isSectionTransparent(0));


        v[0] = DigiglassValue.TOO_LONG_OPERATION_WARNING
                | DigiglassValue.PLANNED_REGENERATION_IN_PROGRESS
                | DigiglassValue.REGENERATION_AFTER_20H_IN_PROGRESS;


        val = new DigiglassValue(v);

        Assert.assertEquals(false, val.isAnySectionTransparent());
        Assert.assertEquals(true, val.isPlannedRegenerationInProgress());
        Assert.assertEquals(true, val.isTooLongOperationWarningPresent());
        Assert.assertEquals(true, val.regenerationAfter20hInProgress());
        Assert.assertEquals(false, val.isSectionTransparent(0));


    }
}