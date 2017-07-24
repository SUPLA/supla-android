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
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.supla.android.db.Channel;
import org.supla.android.lib.SuplaClient;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

public class ChannelDetailRS extends DetailLayout implements SuplaRollerShutter.OnTouchListener, View.OnTouchListener {

    private SuplaRollerShutter rs;
    private TextView tvTitle;
    private TextView tvPercentCaption;
    private TextView tvPercent;
    private Button btnUp;
    private Button btnDown;
    private Button btnStop;
    private Button btnOpen;
    private Button btnClose;

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
        rs.setOnPercentTouchListener(this);

        btnUp = (Button)findViewById(R.id.rsBtnUp);
        btnDown = (Button)findViewById(R.id.rsBtnDown);
        btnStop = (Button)findViewById(R.id.rsBtnStop);
        btnOpen = (Button)findViewById(R.id.rsBtnOpen);
        btnClose = (Button)findViewById(R.id.rsBtnClose);

        btnUp.setOnTouchListener(this);
        btnDown.setOnTouchListener(this);
        btnStop.setOnTouchListener(this);
        btnOpen.setOnTouchListener(this);
        btnClose.setOnTouchListener(this);

        Typeface type = Typeface.createFromAsset(getContext().getAssets(),"fonts/OpenSans-Bold.ttf");

        tvPercentCaption = (TextView)findViewById(R.id.rsDetailPercentCaption);
        tvPercentCaption.setTypeface(type);

        tvPercent = (TextView)findViewById(R.id.rsDetailPercent);
        tvPercent.setTypeface(type);

        type = Typeface.createFromAsset(getContext().getAssets(),"fonts/Quicksand-Regular.ttf");

        tvTitle = (TextView)findViewById(R.id.rsDetailTitle);
        tvTitle.setTypeface(type);
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

        tvTitle.setText(channel.getNotEmptyCaption(getContext()));

        rs.setPercent(p);

        if ( p < 0 ) {
            tvPercent.setText(R.string.calibration);
        } else {
            tvPercent.setText(Integer.toString((int)p)+"%");
        }
    }

    public void setData(Channel channel) {

        super.setData(channel);
        OnChannelDataChanged();

    }

    @Override
    public void onPercentChanged(SuplaRollerShutter rs, float percent) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client == null || !isDetailVisible() )
            return;

        client.Open(getChannelId(), (int)(10+percent));
        OnChannelDataChanged();
    }

    @Override
    public void onPercentChangeing(SuplaRollerShutter rs, float percent) {
        tvPercent.setText(Integer.toString((int)percent)+"%");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int action = event.getAction();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            break;

            default:
                return false;
        }


        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if ( client == null )
            return false;

        if ( v == btnUp ) {

            client.Open(getChannelId(), action == MotionEvent.ACTION_DOWN ? 2 : 0);

        } else if ( v == btnDown ) {

            client.Open(getChannelId(), action == MotionEvent.ACTION_DOWN ? 1 : 0);

        } else if ( v == btnStop ) {

            if ( action == MotionEvent.ACTION_DOWN )
                client.Open(getChannelId(), 0);
            else return false;

        } else if ( v == btnOpen ) {

            if ( action == MotionEvent.ACTION_DOWN )
                client.Open(getChannelId(), 10);
            else return false;

        } else if ( v == btnClose ) {

            if ( action == MotionEvent.ACTION_DOWN )
                client.Open(getChannelId(), 110);
            else return false;
        }

        SuplaApp.Vibrate(getContext());

        return true;
    }
}


