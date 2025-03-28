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

import android.graphics.Color;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.List;

public class SuplaBarDataSet extends BarDataSet {

  boolean colorDependsOnTheValue;

  public SuplaBarDataSet(List<BarEntry> yVals, String label) {
    super(yVals, label);
  }

  @Override
  public BarEntry getEntryForIndex(int index) {
    BarEntry result;

    try {
      result = super.getEntryForIndex(index);
    } catch (IndexOutOfBoundsException exception) {
      result = null;
    }

    return result;
  }

  @Override
  public int getColor(int index) {
    if (colorDependsOnTheValue) {
      BarEntry e = getEntryForIndex(index);

      if (e != null) {
        return e.getY() >= 0 ? mColors.get(0 % mColors.size()) : mColors.get(1 % mColors.size());
      }

      return Color.GRAY;
    }

    return super.getColor(index);
  }
}
