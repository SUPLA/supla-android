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
import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.widget.TextView;

import org.supla.android.lib.Preferences;

public class StartActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        TextView v = (TextView)findViewById(R.id.splash_name);
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/Quicksand-Regular.ttf");
        v.setTypeface(type);

        v = (TextView)findViewById(R.id.splash_slogan);
        type = Typeface.createFromAsset(getAssets(),"fonts/OpenSans-Bold.ttf");
        v.setTypeface(type);

        SuplaApp.getApp().SuplaClientInitIfNeed(this);

        Display display = getWindowManager().getDefaultDisplay();
        Trace.d("ScreenSize", Integer.toString(display.getWidth())+"x"+Integer.toString(display.getHeight()));
        Trace.d("Density", Double.toString(getResources().getDisplayMetrics().density));
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Preferences prefs = new Preferences(this);
        final StartActivity startActivity = this;

                (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {


                if (prefs.configIsSet()) {
                    NavigationActivity.showMain(startActivity);
                } else {
                    NavigationActivity.showCfg(startActivity);
                }

            }
        }, 100);


    }

}
