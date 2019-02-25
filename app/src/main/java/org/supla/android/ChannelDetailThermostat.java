package org.supla.android;

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
    public View getContentView() {
        return inflateLayout(R.layout.detail_thermostat);
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void onClick(View view) {
    }
}

