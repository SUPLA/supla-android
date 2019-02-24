package org.supla.android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import org.supla.android.lib.SuplaClient;
import org.supla.android.lib.SuplaConst;
import org.supla.android.listview.ChannelListView;
import org.supla.android.listview.DetailLayout;

import static java.lang.String.format;


public class ChannelDetailThermostatHP extends DetailLayout implements View.OnClickListener {

    private Button btnOnOff;

    public ChannelDetailThermostatHP(Context context, ChannelListView cLV) {
        super(context, cLV);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ChannelDetailThermostatHP(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init() {
        super.init();

        btnOnOff = findViewById(R.id.hpBtnOnOff);
        btnOnOff.setOnClickListener(this);
    }

    @Override
    public View getContentView() {
        return inflateLayout(R.layout.detail_homeplus);
    }

    @Override
    public void OnChannelDataChanged() {

    }

    @Override
    public void onClick(View view) {

        SuplaClient client = SuplaApp.getApp().getSuplaClient();

        if (client == null || !isDetailVisible()) {
            return;
        }

        if (view == btnOnOff) {
            byte[] data = new byte[20];
            client.DeviceCalCfgRequest(getRemoteId(), SuplaConst.SUPLA_THERMOSTAT_CMD_TURNON, 0, data);
        }
    }
}

