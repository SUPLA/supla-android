package org.supla.android;

import android.content.Context;

import org.supla.android.db.DbHelper;
import org.supla.android.db.Location;
import org.supla.android.lib.SuplaClient;

public class LocationCaptionEditor extends CaptionEditor {
    public LocationCaptionEditor(Context context) {
        super(context);
    }

    @Override
    protected int getTitle() {
        return R.string.location_name;
    }

    @Override
    protected String getCaption() {
        DbHelper dbH = DbHelper.getInstance(getContext());
        Location location = dbH.getLocation(getId());
        if (location != null && location.getCaption() != null) {
            return location.getCaption();
        }
        return "";
    }

    @Override
    protected void applyChanged(String newCaption) {
        DbHelper dbH = DbHelper.getInstance(getContext());
        Location location = dbH.getLocation(getId());
        if (location != null) {
            location.setCaption(newCaption);
            dbH.updateLocation(location);
        }

        SuplaClient client = SuplaApp.getApp().getSuplaClient();
        if (client != null) {
            client.setLocationCaption(getId(), newCaption);
        }
    }
}
