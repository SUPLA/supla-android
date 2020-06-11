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

import android.content.Context;
import android.view.MotionEvent;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SuplaCurtainsTest extends TestCase {

    private SuplaCurtains curtains;
    private Context context = mock(Context.class);

    @Override
    protected void setUp() {
        curtains = new SuplaCurtains(context);
    }

    public void testLineColorDefaultValue() {
        assertEquals(0x000000, curtains.getLineColor());
    }

    public void testLineColorGetterSetter() {
        curtains.setLineColor(0x123123);
        assertEquals(0x123123, curtains.getLineColor());
    }

    public void testFillColor1DefaultValue() {
        assertEquals(0x00D449, curtains.getFillColor1());
    }

    public void testFillColor1GetterSetter() {
        curtains.setFillColor1(0x123123);
        assertEquals(0x123123, curtains.getFillColor1());
    }

    public void testFillColor2DefaultValue() {
        assertEquals(0x049629, curtains.getFillColor2());
    }

    public void testFillColor2GetterSetter() {
        curtains.setFillColor2(0x123123);
        assertEquals(0x123123, curtains.getFillColor2());
    }

    public void testGetPercent() {
        assertEquals(0, curtains.getPercent(), 0);
    }

    public void testPercentGetterSetter() {
        curtains.setPercent(75f);
        assertEquals(75f, curtains.getPercent(), 0);
    }

    public void testPercentMinValue() {
        curtains.setPercent(-15);
        assertEquals(0, curtains.getPercent(), 0);
    }

    public void testPercentMaxValue() {
        curtains.setPercent(130);
        assertEquals(100, curtains.getPercent(), 0);
    }

    public void testPercentMinChangeValue() {
        curtains.setPercent(10);
        assertEquals(10, curtains.getPercent(), 0);
        curtains.setPercent(-20);
        assertEquals(0, curtains.getPercent(), 0);
    }

    public void testPercentMaxChangeValue() {
        curtains.setPercent(90);
        assertEquals(90, curtains.getPercent(), 0);
        curtains.setPercent(170);
        assertEquals(100, curtains.getPercent(), 0);
    }

    public void testOnTouchEventDownUp() {
        MotionEvent event = mock(MotionEvent.class);
        assertNotNull(event);
        assertFalse(curtains.isTouched());
        when(event.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        curtains.onTouchEvent(event);
        assertTrue(curtains.isTouched());
        when(event.getAction()).thenReturn(MotionEvent.ACTION_UP);
        curtains.onTouchEvent(event);
        assertFalse(curtains.isTouched());
    }

    public void testOnTouchEventDownCancel() {
        MotionEvent event = mock(MotionEvent.class);
        assertNotNull(event);
        assertFalse(curtains.isTouched());
        when(event.getAction()).thenReturn(MotionEvent.ACTION_DOWN);
        curtains.onTouchEvent(event);
        assertTrue(curtains.isTouched());
        when(event.getAction()).thenReturn(MotionEvent.ACTION_CANCEL);
        curtains.onTouchEvent(event);
        assertFalse(curtains.isTouched());
    }
}