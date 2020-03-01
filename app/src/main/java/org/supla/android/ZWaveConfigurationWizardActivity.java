package org.supla.android;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.supla.android.db.Channel;
import org.supla.android.db.DbHelper;
import org.supla.android.lib.SuplaChannelBasicCfg;
import org.supla.android.lib.SuplaChannelState;
import org.supla.android.lib.SuplaClient;

import java.util.ArrayList;

public class ZWaveConfigurationWizardActivity extends WizardActivity implements AdapterView.OnItemSelectedListener {

    private Spinner mSpinnerChannels;
    private ArrayList<Channel>mChannels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // mSpinnerChannels.setOnItemSelectedListener(this);
        RegisterMessageHandler();
    }

    private void loadChannelList() {

        DbHelper dbHelper = new DbHelper(this);
        mChannels = dbHelper.getZWaveBridgeOnlineChannels();
        ArrayList<String>spinnerList = new ArrayList<>();

        for(Channel channel: mChannels) {
            spinnerList.add("#"+channel.getChannelId()
                    + " " + channel.getCaption());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        mSpinnerChannels.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadChannelList();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onBtnNextClick() {
        
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position >= 0 && mChannels != null && position < mChannels.size()) {
            SuplaClient client = SuplaApp.getApp().getSuplaClient();
            if (client!=null){
                client.getChannelBasicCfg(mChannels.get(position).getChannelId());
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void OnChannelBasicCfg(SuplaChannelBasicCfg basicCfg) {
        if (isFinishing()) {
            return;
        }
        Trace.d("OnChannelBasicCfg", basicCfg.getDeviceName());
    };
}
