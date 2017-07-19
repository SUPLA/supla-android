package org.supla.android;


/*
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

 Author: Przemyslaw Zygmunt przemek@supla.org
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaClient;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

public class ChannelDetailRS extends DetailLayout implements SuplaRollerShutter.OnTouchListener {

    private SuplaRollerShutter rs;

    public ChannelDetailRS(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailRS(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init() {

        super.init();
        rs = (SuplaRollerShutter) findViewById(R.id.rs1);
        rs.setOnTouchListener(this);

    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_rs);
    }

    @Override
    public void OnChannelDataChanged() {

        Channel channel = getChannelFromDatabase();
        float p = channel.getPercent();

        if ( p < 100 && channel.getSubValueHi() == 1 )
            p = 100;

        rs.setPercent(p);
    }

    @Override
    public void onPercentChanged(SuplaRollerShutter rs, float percent) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client == null || !isDetailVisible() )
            return;

        client.Open(getChannelId(), (int)(10+percent));

    }
}


