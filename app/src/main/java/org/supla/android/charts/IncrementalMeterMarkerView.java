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

import android.content.Context;
import com.github.mikephil.charting.data.Entry;

public class IncrementalMeterMarkerView extends SuplaMarkerView {

    public IncrementalMeterMarkerView(IncrementalMeterChartHelper helper, Context context,
                                      int layoutResource) {
        super(helper, context, layoutResource);
    }

    @Override
    protected String getValue1(Entry e) {

        if (helper instanceof IncrementalMeterChartHelper) {
            IncrementalMeterChartHelper helper = (IncrementalMeterChartHelper)this.helper;
            if (helper.getCurrency() != null) {
                return String.format("%.2f "+getString(helper.getCurrency()),
                        helper.getPricePerUnit()*e.getY());
            }
        }

        return "";
    }
}
