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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends NavigationActivity {

    private Button homepage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView v = findViewById(R.id.about_project_name);
        v.setTypeface(SuplaApp.getApp().getTypefaceQuicksandRegular());

        v = findViewById(R.id.cfg_label_svr_address);
        v.setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());

        v.setText(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME);

        v = findViewById(R.id.about_text);
        v.setTypeface(SuplaApp.getApp().getTypefaceOpenSansRegular());

        homepage = findViewById(R.id.about_homepage);
        homepage.setTypeface(SuplaApp.getApp().getTypefaceOpenSansBold());
        homepage.setOnClickListener(this);
        homepage.setTransformationMethod(null);

        showMenuBar();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v == homepage) {
            openHomepage();
        }
    }

    @Override
    public void onBackPressed() {
        showMain(this);
        finish();
    }
}
