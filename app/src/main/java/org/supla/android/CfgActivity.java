package org.supla.android;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.supla.android.lib.Preferences;

public class CfgActivity extends ActionBarActivity  {

    private EditText edServerAddr;
    private EditText edAccessID;
    private EditText edAccessIDpwd;

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

        edServerAddr.setOnFocusChangeListener(fcl);
        edAccessID.setOnFocusChangeListener(fcl);
        edAccessIDpwd.setOnFocusChangeListener(fcl);

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public void onBackPressed() {

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

        }


        super.onBackPressed();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Preferences prefs = new Preferences(this);

        edServerAddr.setText(prefs.getServerAddress(), EditText.BufferType.EDITABLE);
        edAccessID.setText(prefs.getAccessID() == 0 ? "" : Integer.toString(prefs.getAccessID()) );
        edAccessIDpwd.setText(prefs.getAccessIDpwd(), EditText.BufferType.EDITABLE);
    }
}
