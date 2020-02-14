package org.supla.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.supla.android.lib.SuplaConst;

public class ZWaveConfigurationWizardActivity extends NavigationActivity {

    private Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zwave);

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == btnTest) {
        }
    }
}
