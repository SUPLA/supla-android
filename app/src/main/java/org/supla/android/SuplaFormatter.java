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

import java.text.DecimalFormat;

public class SuplaFormatter {
    String doubleToStringWithUnit(double dbl, String unit, int maxPrecision) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(50);
        df.setMinimumFractionDigits(2);
        String sdbl = df.format(dbl);

        int a;

        for(a=sdbl.length()-1;a>=0;a--) {
            char c = sdbl.charAt(a);

            if (c == ',' || c == '.') {
                int p = sdbl.length()-a-1;
                if (maxPrecision < p) {
                    sdbl = sdbl.substring(0, a+maxPrecision+(maxPrecision > 0 ? 1 : 0));
                }

                if (maxPrecision > 0) {
                    p = a;
                    a = sdbl.length()-1;

                    while(a >= p) {
                        if (sdbl.charAt(a) != '0' || a-p <= 2) {
                            sdbl = sdbl.substring(0, a+1);
                            break;
                        }
                        a--;
                    }
                }
                break;
            }
        }

        if (unit == null) {
            return sdbl;
        }

        return sdbl + " " + unit;
    }
}
