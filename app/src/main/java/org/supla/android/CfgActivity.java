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

 Author: Przemyslaw Zygmunt p.zygmunt@acsoftware.pl [AC SOFTWARE]
 */

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.supla.android.lib.Preferences;

public class CfgActivity extends NavigationActivity  {

    private EditText edServerAddr;
    private EditText edAccessID;
    private EditText edAccessIDpwd;
    private Button SaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cfg);

        View.OnFocusChangeListener fcl = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        };


        edServerAddr = (EditText)findViewById(R.id.edServerAddr);
        edAccessID = (EditText)findViewById(R.id.edAccessID);
        edAccessIDpwd = (EditText)findViewById(R.id.edAccessIDpwd);
        SaveBtn = (Button)findViewById(R.id.cfg_save);

        SaveBtn.setOnClickListener(this);

        edServerAddr.setOnFocusChangeListener(fcl);
        edAccessID.setOnFocusChangeListener(fcl);
        edAccessIDpwd.setOnFocusChangeListener(fcl);

        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        edServerAddr.setTypeface(type);
        edAccessID.setTypeface(type);
        edAccessIDpwd.setTypeface(type);


        type = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Bold.ttf");
        TextView v = (TextView)findViewById(R.id.cfg_label_svr_address);
        v.setTypeface(type);

        v = (TextView)findViewById(R.id.cfg_label_access_id);
        v.setTypeface(type);

        v = (TextView)findViewById(R.id.cfg_label_access_pwd);
        v.setTypeface(type);

        type = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Regular.ttf");
        v = (TextView)findViewById(R.id.cfg_label_title);
        v.setTypeface(type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            edServerAddr.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessID.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessIDpwd.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
        } else {
            edServerAddr.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessID.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessIDpwd.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
        }

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public void onClick(View v) {
        super.onClick(v);

        if ( v != SaveBtn ) return;

        SaveBtn.setEnabled(false);

        Preferences prefs = new Preferences(this);

        boolean changed = false;
        int AccessID = 0;

        if ( false == prefs.getServerAddress().equals(edServerAddr.getText().toString()) ) {
            prefs.setServerAddress(edServerAddr.getText().toString());
            changed = true;
        }

        try {

            AccessID = Integer.parseInt(edAccessID.getText().toString());

        } catch(NumberFormatException nfe) {
            AccessID = 0;
        }

        if ( AccessID != prefs.getAccessID() ) {
            prefs.setAccessID(AccessID);
            changed = true;
        }

        if ( false == prefs.getAccessIDpwd().equals(edAccessIDpwd.getText().toString()) ) {
            prefs.setAccessIDpwd(edAccessIDpwd.getText().toString());
            changed = true;
        }


        if ( changed ) {

            showStatus(this);
            SuplaApp.getApp().SuplaClientInitIfNeed(this).Reconnect();

        } else {
            showMain(this);
        }

        finish();

    }


    @Override
    protected void onResume() {
        super.onResume();

        SaveBtn.setEnabled(true);

        Preferences prefs = new Preferences(this);

        edServerAddr.setText(prefs.getServerAddress(), EditText.BufferType.EDITABLE);
        edAccessID.setText(prefs.getAccessID() == 0 ? "" : Integer.toString(prefs.getAccessID()) );
        edAccessIDpwd.setText(prefs.getAccessIDpwd(), EditText.BufferType.EDITABLE);

        String sender = getIntent().getStringExtra(INTENTSENDER);


        if ( sender != null && sender.equals(INTENTSENDER_MAIN)) {
            showMenuBar();
        } else {
            hideMenuBar();
        }
    }

    @Override
    public void onBackPressed() {
        showMain(this);
        finish();
    }
}
