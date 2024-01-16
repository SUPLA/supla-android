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

public class SuplaThermostatCalendarTest extends TestCase {
  SuplaThermostatCalendar calendar;

  @Override
  protected void setUp() throws Exception {
    calendar = new SuplaThermostatCalendar(null);
  }

  @Test
  public void testSettingFirtsDay() {
    for (short a = 1; a <= 7; a++) {
      calendar.setFirtsDay(a);
      Assert.assertEquals(a, calendar.getFirtsDay());
    }

    calendar.setFirtsDay(0);
    Assert.assertEquals(7, calendar.getFirtsDay());
    calendar.setFirtsDay(8);
    Assert.assertEquals(7, calendar.getFirtsDay());
  }

  @Test
  public void testGettingDayOffset() {

    {
      int d[] = {1, 2, 3, 4, 5, 6, 7};

      for (short a = 1; a <= 7; a++) {
        Assert.assertEquals(d[a - 1], calendar.dayWithOffset(a));
      }
    }

    calendar.setFirtsDay(2);

    {
      int d[] = {2, 3, 4, 5, 6, 7, 1};

      for (short a = 1; a <= 7; a++) {
        Assert.assertEquals(d[a - 1], calendar.dayWithOffset(a));
      }
    }

    calendar.setFirtsDay(7);

    {
      int d[] = {7, 1, 2, 3, 4, 5, 6};

      for (short a = 1; a <= 7; a++) {
        Assert.assertEquals(d[a - 1], calendar.dayWithOffset(a));
      }
    }
  }

  @Test
  public void testSettingHourProgramTo1() {

    short d, h;

    for (d = 1; d <= 7; d++) {
      for (h = 0; h < 24; h++) {
        Assert.assertEquals(false, calendar.isHourProgramIsSetTo1(d, h));
        calendar.setHourProgramTo1(d, h, true);
        Assert.assertEquals(true, calendar.isHourProgramIsSetTo1(d, h));
      }
    }
  }
}
