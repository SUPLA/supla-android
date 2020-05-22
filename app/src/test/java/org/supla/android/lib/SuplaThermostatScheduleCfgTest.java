package org.supla.android.lib;

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

import org.junit.Assert;
import org.junit.Test;

public class SuplaThermostatScheduleCfgTest {

    @Test
    public void testTemperatureSettingForOneDay() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        for (short a = 0; a < 23; a++) {
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, a, (byte) a);
        }

        Assert.assertEquals(1, cfg.getGroupCount());
        cfg.clear();
        Assert.assertEquals(0, cfg.getGroupCount());
    }

    @Test
    public void testTemperatureSettingForAllDaysWithIdenticalTemperatures() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        for (short a = 0; a < 23; a++) {
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.TUESDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.FRIDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.SATURDAY, a, (byte) a);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.SUNDAY, a, (byte) a);

        }

        Assert.assertEquals(1, cfg.getGroupCount());
        cfg.clear();
        Assert.assertEquals(0, cfg.getGroupCount());
    }

    @Test
    public void testSettingTemperatureAndProgram() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 0, (byte) 0);
        cfg.setProgram(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 0, (byte) 0);

        Assert.assertEquals(1, cfg.getGroupCount());

        cfg.clear();

        Assert.assertEquals(0, cfg.getGroupCount());

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 0, (byte) 0);
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.TUESDAY, (byte) 0, (byte) 0);

        Assert.assertEquals(1, cfg.getGroupCount());

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 0, (byte) 0);
        cfg.setProgram(SuplaThermostatScheduleCfg.DayOfWeek.TUESDAY, (byte) 0, (byte) 0);

        Assert.assertEquals(2, cfg.getGroupCount());
    }

    @Test
    public void testTemperatureSettingForAllDaysWithDifferentTemperatures() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        for (short a = 0; a < 23; a++) {
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, a, (byte) 1);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.TUESDAY, a, (byte) 2);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, a, (byte) 3);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY, a, (byte) 4);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.FRIDAY, a, (byte) 5);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.SATURDAY, a, (byte) 6);
            cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.SUNDAY, a, (byte) 7);
        }

        Assert.assertEquals(7, cfg.getGroupCount());
        cfg.clear();
        Assert.assertEquals(0, cfg.getGroupCount());
    }

    @Test
    public void testGettingGroupWeekDays() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, (byte) 1, (byte) 3);
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY, (byte) 2, (byte) 4);

        Assert.assertEquals(2, cfg.getGroupCount());
        Assert.assertEquals(0, cfg.getGroupWeekDays(-1));
        Assert.assertEquals(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY,
                cfg.getGroupWeekDays(0));
        Assert.assertEquals(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY,
                cfg.getGroupWeekDays(1));

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY, (byte) 1, (byte) 3);

        Assert.assertEquals(2, cfg.getGroupCount());
        Assert.assertEquals(SuplaThermostatScheduleCfg.DayOfWeek.THURSDAY,
                cfg.getGroupWeekDays(1));
        Assert.assertEquals(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY,
                cfg.getGroupWeekDays(0));
    }

    @Test
    public void testGettingGroupHourValueType() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        Assert.assertEquals(SuplaThermostatScheduleCfg.HourValueType.TEMPERATURE,
                cfg.getGroupHourValueType(0));

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, (byte) 1, (byte) 3);

        Assert.assertEquals(SuplaThermostatScheduleCfg.HourValueType.TEMPERATURE,
                cfg.getGroupHourValueType(0));

        cfg.setProgram(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, (byte) 1, (byte) 3);

        Assert.assertEquals(SuplaThermostatScheduleCfg.HourValueType.PROGRAM,
                cfg.getGroupHourValueType(0));
    }

    @Test
    public void testGettingGroupHourValue() {

        byte[] hourValue = new byte[24];
        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();

        Assert.assertArrayEquals(hourValue, cfg.getGroupHourValue(0));

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, (byte) 17, (byte) 30);

        hourValue[17] = 30;
        Assert.assertArrayEquals(hourValue, cfg.getGroupHourValue(0));

    }

    @Test
    public void testGroupCount() {

        SuplaThermostatScheduleCfg cfg = new SuplaThermostatScheduleCfg();
        Assert.assertEquals(0, cfg.getGroupCount());

        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 17, (byte) 30);
        Assert.assertEquals(1, cfg.getGroupCount());
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 18, (byte) 30);
        Assert.assertEquals(1, cfg.getGroupCount());
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.WEDNESDAY, (byte) 18, (byte) 30);
        Assert.assertEquals(2, cfg.getGroupCount());
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 18, (byte) 40);
        Assert.assertEquals(2, cfg.getGroupCount());
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 17, (byte) 0);
        Assert.assertEquals(2, cfg.getGroupCount());
        cfg.setTemperature(SuplaThermostatScheduleCfg.DayOfWeek.MONDAY, (byte) 18, (byte) 30);
        Assert.assertEquals(1, cfg.getGroupCount());
    }
}