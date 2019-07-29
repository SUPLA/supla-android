package org.supla.android.charts;

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

import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.List;

public class SuplaPieDataSet extends PieDataSet {
    public SuplaPieDataSet(List<PieEntry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public PieEntry getEntryForIndex(int index) {
        PieEntry result;

        try {
            result = super.getEntryForIndex(index);
        }
        catch (IndexOutOfBoundsException exception) {result = null;};

        return result;
    }
}
