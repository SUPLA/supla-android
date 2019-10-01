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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;


public class ChannelDetailThermostat extends DetailLayout implements View.OnClickListener {

    public ChannelDetailThermostat(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailThermostat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailThermostat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailThermostat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public View inflateContentView() {
        return inflateLayout(R.layout.detail_thermostat);
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void onClick(View view) {

    }
}

