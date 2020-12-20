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

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.supla.android.db.DbHelper;

public class CfgActivity extends NavigationActivity {

    static final short CFG_LAYOUT_ADVANCED = 0;
    static final short CFG_LAYOUT_BASIC = 1;
    private View vBasic;
    private View vAdvanced;
    private RelativeLayout rlContent;
    private EditText edServerAddr;
    private EditText edAccessID;
    private EditText edAccessIDpwd;
    private EditText edEmail;
    private CheckBox cbAdvanced;
    private Button btnSaveBasic, btnSaveAdv, btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View.OnFocusChangeListener fcl = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        };

        setContentView(R.layout.activity_cfg);

        rlContent = findViewById(R.id.cfg_content);

        vBasic = Inflate(R.layout.activity_cfg_basic, null);
        vAdvanced = Inflate(R.layout.activity_cfg_advanced, null);

        vBasic.setVisibility(View.GONE);
        rlContent.addView(vBasic);

        vAdvanced.setVisibility(View.VISIBLE);
        rlContent.addView(vAdvanced);

        cbAdvanced = findViewById(R.id.cfg_cb_advanced);
        cbAdvanced.setOnClickListener(this);

        edServerAddr = findViewById(R.id.edServerAddr);
        edAccessID = findViewById(R.id.edAccessID);
        edAccessIDpwd = findViewById(R.id.edAccessIDpwd);
        edEmail = findViewById(R.id.cfg_email);

        edServerAddr.setOnFocusChangeListener(fcl);
        edAccessID.setOnFocusChangeListener(fcl);
        edAccessIDpwd.setOnFocusChangeListener(fcl);
        edEmail.setOnFocusChangeListener(fcl);

        Typeface type = SuplaApp.getApp().getTypefaceOpenSansRegular();
        edServerAddr.setTypeface(type);
        edAccessID.setTypeface(type);
        edAccessIDpwd.setTypeface(type);
        edEmail.setTypeface(type);

        btnCreate = findViewById(R.id.cfg_create_account);
        btnCreate.setOnClickListener(this);

        type = SuplaApp.getApp().getTypefaceOpenSansBold();
        TextView v = findViewById(R.id.cfg_label_svr_address);
        v.setTypeface(type);

        v = findViewById(R.id.cfg_label_access_id);
        v.setTypeface(type);

        v = findViewById(R.id.cfg_label_access_pwd);
        v.setTypeface(type);

        v = findViewById(R.id.cfg_label_email);
        v.setTypeface(type);


        type = SuplaApp.getApp().getTypefaceQuicksandRegular();
        v = findViewById(R.id.cfg_label_title_basic);
        v.setTypeface(type);

        v = findViewById(R.id.cfg_label_title_adv);
        v.setTypeface(type);

        btnCreate.setTypeface(type, Typeface.BOLD);
        cbAdvanced.setTypeface(type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            edServerAddr.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessID.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessIDpwd.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
            edEmail.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));
        } else {
            edServerAddr.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessID.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            edAccessIDpwd.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
            edEmail.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_edittext));
        }

        btnSaveBasic = findViewById(R.id.cfg_save_basic);
        btnSaveBasic.setOnClickListener(this);

        btnSaveAdv = findViewById(R.id.cfg_save_adv);
        btnSaveAdv.setOnClickListener(this);

        edEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                if (getCurrentFocus() == edEmail) {
                    edServerAddr.setText("");
                }

            }
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void cbAdvancedClicked() {

        if (cbAdvanced.isChecked()) {
            vBasic.setVisibility(View.GONE);
            vAdvanced.setVisibility(View.VISIBLE);
        } else {
            vBasic.setVisibility(View.VISIBLE);
            vAdvanced.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == cbAdvanced) {
            cbAdvancedClicked();
            return;
        }

        if (v == btnCreate) {
            showCreateAccount();
            return;
        }

        if (v != btnSaveBasic && v != btnSaveAdv) return;

        btnSaveBasic.setEnabled(false);
        btnSaveAdv.setEnabled(false);

        Preferences prefs = new Preferences(this);

        boolean changed = false;
        int AccessID;

        if (!prefs.getServerAddress().equals(edServerAddr.getText().toString())) {
            prefs.setServerAddress(edServerAddr.getText().toString());
            changed = true;
        }

        try {

            AccessID = Integer.parseInt(edAccessID.getText().toString());

        } catch (NumberFormatException nfe) {
            AccessID = 0;
        }

        if (AccessID != prefs.getAccessID()) {
            prefs.setAccessID(AccessID);
            changed = true;
        }

        if (!prefs.getAccessIDpwd().equals(edAccessIDpwd.getText().toString())) {
            prefs.setAccessIDpwd(edAccessIDpwd.getText().toString());
            changed = true;
        }

        if (!prefs.getEmail().equals(edEmail.getText().toString())) {
            prefs.setEmail(edEmail.getText().toString());
            changed = true;
        }

        if (prefs.isAdvancedCfg() != cbAdvanced.isChecked()) {
            prefs.setAdvancedCfg(cbAdvanced.isChecked());
            changed = true;
        }


        if (changed) {
            getDbHelper().deleteUserIcons();

            prefs.setPreferedProtocolVersion();

            showStatus(this);
            SuplaApp.getApp().SuplaClientInitIfNeed(this).reconnect();

        } else {
            showMain(this);
        }

        finish();

    }


    @Override
    protected void onResume() {
        super.onResume();

        btnSaveBasic.setEnabled(true);
        btnSaveAdv.setEnabled(true);

        Preferences prefs = new Preferences(this);

        edServerAddr.setText(prefs.getServerAddress(), EditText.BufferType.EDITABLE);
        edAccessID.setText(prefs.getAccessID() == 0 ? "" : Integer.toString(prefs.getAccessID()));
        edAccessIDpwd.setText(prefs.getAccessIDpwd(), EditText.BufferType.EDITABLE);
        edEmail.setText(prefs.getEmail(), EditText.BufferType.EDITABLE);

        cbAdvanced.setChecked(prefs.isAdvancedCfg());
        cbAdvancedClicked();

        String sender = getIntent().getStringExtra(INTENTSENDER);

        if (sender != null && sender.equals(INTENTSENDER_MAIN)) {
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
