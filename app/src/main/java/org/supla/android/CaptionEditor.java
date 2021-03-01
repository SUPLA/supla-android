package org.supla.android;

import android.content.Context;

public class CaptionEditor {
    private Context context;

    public CaptionEditor(Context context) {
        this.context = context;
    }

    public void edit(int id) {
        SuplaApp.Vibrate(context);
    }
}
