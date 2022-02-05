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

import java.text.DecimalFormat;

public class SuplaFormatterTest extends TestCase  {
    private SuplaFormatter formatter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        formatter = SuplaFormatter.sharedFormatter();
    }

    public void testDoubleToStringConversionWithUnit() {
        String p = new DecimalFormat().format(1.2).equals("1,2") ? "," : ".";
        
        assertEquals("321"+p+"00", formatter.doubleToStringWithUnit(
                321.000000001,
                null,
                5));

        assertEquals("321"+p+"0", formatter.doubleToStringWithUnit(
                321.000000001,
                null,
                1));

        assertEquals("321", formatter.doubleToStringWithUnit(
                321.000000001,
                null,
                0));

        assertEquals("321"+p+"000000001", formatter.doubleToStringWithUnit(
                321.000000001,
                null,
                15));

        assertEquals("321"+p+"000000001", formatter.doubleToStringWithUnit(
                321.000000001,
                null,
                9));

        assertEquals("321"+p+"000000001", formatter.doubleToStringWithUnit(
                321.00000000101,
                null,
                9));

        assertEquals("0"+p+"00", formatter.doubleToStringWithUnit(0,
                null,
                10));

        assertEquals("321"+p+"00 kWh", formatter.doubleToStringWithUnit(
                321,
                "kWh",
                10));
    }
}
